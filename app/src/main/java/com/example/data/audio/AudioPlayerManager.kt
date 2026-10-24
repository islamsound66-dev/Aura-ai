package com.example.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playbackSpeed: Float = 1.0f,
    val isLooping: Boolean = false,
    val activeFilePath: String? = null,
    val progress: Float = 0f
)

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun loadAndPlay(filePath: String, autoPlay: Boolean = true) {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            Log.e("AudioPlayerManager", "File not found or empty: $filePath")
            return
        }

        try {
            stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    val duration = mp.duration.coerceAtLeast(0)
                    _playbackState.update {
                        it.copy(
                            durationMs = duration,
                            currentPositionMs = 0,
                            progress = 0f,
                            activeFilePath = filePath
                        )
                    }
                    try {
                        setSpeed(_playbackState.value.playbackSpeed)
                    } catch (e: Exception) {
                        Log.e("AudioPlayerManager", "Could not set speed during prepare", e)
                    }
                    try {
                        mp.isLooping = _playbackState.value.isLooping
                    } catch (e: Exception) { }

                    if (autoPlay) {
                        play()
                    }
                }
                setOnCompletionListener {
                    if (!_playbackState.value.isLooping) {
                        _playbackState.update { it.copy(isPlaying = false, currentPositionMs = it.durationMs, progress = 1.0f) }
                        stopProgressTracker()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayerManager", "MediaPlayer error: what=$what, extra=$extra")
                    _playbackState.update { it.copy(isPlaying = false) }
                    stopProgressTracker()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error preparing media player", e)
        }
    }

    fun play() {
        mediaPlayer?.let { player ->
            try {
                if (_playbackState.value.progress >= 0.99f) {
                    player.seekTo(0)
                    _playbackState.update { it.copy(currentPositionMs = 0, progress = 0f) }
                }
                if (!player.isPlaying) {
                    player.start()
                    _playbackState.update { it.copy(isPlaying = true) }
                    startProgressTracker()
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Error starting playback", e)
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                    _playbackState.update { it.copy(isPlaying = false) }
                    stopProgressTracker()
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Error pausing playback", e)
            }
        }
    }

    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { player ->
            try {
                val clamped = positionMs.coerceIn(0, player.duration)
                player.seekTo(clamped)
                val prog = if (player.duration > 0) clamped.toFloat() / player.duration else 0f
                _playbackState.update {
                    it.copy(currentPositionMs = clamped, progress = prog)
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Error seeking", e)
            }
        }
    }

    fun seekToFraction(fraction: Float) {
        val duration = _playbackState.value.durationMs
        if (duration > 0) {
            val targetMs = (fraction.coerceIn(0f, 1f) * duration).toInt()
            seekTo(targetMs)
        }
    }

    fun setSpeed(speed: Float) {
        _playbackState.update { it.copy(playbackSpeed = speed) }
        mediaPlayer?.let { player ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val wasPlaying = player.isPlaying
                    val params = player.playbackParams ?: PlaybackParams()
                    params.speed = speed
                    player.playbackParams = params
                    if (!wasPlaying) {
                        player.pause()
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Error setting speed: $speed", e)
            }
        }
    }

    fun toggleLooping() {
        val newLoop = !_playbackState.value.isLooping
        _playbackState.update { it.copy(isLooping = newLoop) }
        mediaPlayer?.isLooping = newLoop
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error stopping player", e)
        }
        stopProgressTracker()
        _playbackState.update { it.copy(isPlaying = false, currentPositionMs = 0, progress = 0f) }
    }

    fun release() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                mediaPlayer?.let { player ->
                    try {
                        val current = player.currentPosition
                        val duration = player.duration
                        val prog = if (duration > 0) current.toFloat() / duration else 0f
                        _playbackState.update {
                            it.copy(currentPositionMs = current, progress = prog)
                        }
                    } catch (e: Exception) {
                        // ignore transient position errors
                    }
                }
                delay(80)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }
}
