package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.audio.PlaybackState
import com.example.data.model.VoiceProfile
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AudioPlayerCard(
    playbackState: PlaybackState,
    voice: VoiceProfile,
    amplitudes: List<Float>,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onToggleLoop: () -> Unit,
    onShare: () -> Unit,
    onExport: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_player_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Status badge & Share / Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = GoldAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AUDIO MONITOR • MASTER OUTPUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GoldAmber
                        )
                        Text(
                            text = "Voice: ${voice.characterName} (${voice.id})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onExport != null) {
                        IconButton(
                            onClick = onExport,
                            modifier = Modifier.testTag("export_audio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export to Public Folder",
                                tint = GoldAmber
                            )
                        }
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("share_audio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Audio",
                            tint = CyanAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Waveform visualizer
            AudioWaveformVisualizer(
                amplitudes = amplitudes,
                progress = playbackState.progress,
                isPlaying = playbackState.isPlaying,
                onSeek = onSeek
            )

            // Seek slider
            Slider(
                value = playbackState.progress,
                onValueChange = onSeek,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("audio_seek_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = GoldAmber,
                    activeTrackColor = GoldAmber,
                    inactiveTrackColor = StudioBorder
                )
            )

            // Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(playbackState.currentPositionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
                Text(
                    text = formatTime(playbackState.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controls & Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Loop button
                IconButton(
                    onClick = onToggleLoop,
                    modifier = Modifier.testTag("loop_button")
                ) {
                    Icon(
                        imageVector = if (playbackState.isLooping) Icons.Default.RepeatOn else Icons.Default.Repeat,
                        contentDescription = "Toggle Loop",
                        tint = if (playbackState.isLooping) GoldAmber else TextSecondary
                    )
                }

                // Skip rewind 5s
                IconButton(
                    onClick = {
                        val newPos = (playbackState.currentPositionMs - 5000).coerceAtLeast(0)
                        val frac = if (playbackState.durationMs > 0) newPos.toFloat() / playbackState.durationMs else 0f
                        onSeek(frac)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind 5s",
                        tint = TextPrimary
                    )
                }

                // Play / Pause main button
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("play_pause_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = GoldAmber,
                        contentColor = Color(0xFF1E1202)
                    )
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Skip forward 5s
                IconButton(
                    onClick = {
                        val newPos = (playbackState.currentPositionMs + 5000).coerceAtMost(playbackState.durationMs)
                        val frac = if (playbackState.durationMs > 0) newPos.toFloat() / playbackState.durationMs else 0f
                        onSeek(frac)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward 5s",
                        tint = TextPrimary
                    )
                }

                // Speed Selector Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.8f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                        val isSelected = playbackState.playbackSpeed == speed
                        Surface(
                            onClick = { onSetSpeed(speed) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GoldAmber else StudioSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) GoldAmber else StudioBorder)
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) Color(0xFF1E1202) else TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Int): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
