package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.data.storage.PublicFolderType
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.audio.AudioPlayerManager
import com.example.data.audio.PlaybackState
import com.example.data.audio.WavAudioUtil
import com.example.data.model.ScriptCatalog
import com.example.data.model.ScriptTemplate
import com.example.data.model.VoiceCatalog
import com.example.data.model.VoiceProfile
import com.example.data.network.GeminiAudioClient
import com.example.data.network.GeminiAudioResult
import com.example.data.storage.SessionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

import com.example.data.audio.AudioDspEnhancer
import com.example.data.audio.QualitySettings

data class StudioUiState(
    val scriptText: String = ScriptCatalog.templates.first().scriptText,
    val selectedVoice: VoiceProfile = VoiceCatalog.victor,
    val systemInstruction: String = DEFAULT_SYSTEM_INSTRUCTION,
    val temperature: Float = 0.40f,
    val selectedModel: String = GeminiAudioClient.DEFAULT_MODEL,
    val isGenerating: Boolean = false,
    val generationError: String? = null,
    val activeAudioFile: File? = null,
    val audioAmplitudes: List<Float> = emptyList(),
    val isSystemInstructionExpanded: Boolean = false,
    val apiKeyOverride: String = "",
    val isApiKeyConfigured: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val qualitySettings: QualitySettings = AudioDspEnhancer.PRESET_DOCUMENTARY_MASTER,
    val activeTab: Int = 0 // 0: Studio, 1: Presets, 2: Export, 3: Guide, 4: History
)

const val DEFAULT_SYSTEM_INSTRUCTION = """### ROLE & OBJECTIVE
You are a world-class documentary voice agent and audio director. Your task is to perform high-efficiency, deeply emotional, clear, and energetic voiceovers for YouTube documentaries.

### VOCAL PROFILES
1. [MALE NARRATOR - "VICTOR"]:
   - Tone: Deep baritone, cinematic, rich, and authoritative with emotional depth.
   - Pacing: Unhurried, measured, building suspense with resonant pauses.
   - Energy: Intense, grounded, commanding, and dramatic.

2. [FEMALE NARRATOR - "ELENA"]:
   - Tone: Crystal clear, warm, expressive, high-clarity articulate treble.
   - Pacing: Dynamic, fluid, and engaging.
   - Energy: Energetic, passionate, emotionally vivid, and captivating.

### PERFORMANCE & DELIVERY DIRECTIVES
- Clarity & Efficiency: Pronounce every word with pristine articulation without sounding robotic.
- Emotional Resonance: Adapt tone dynamically to match the emotional arc (wonder, tension, tragedy, revelation).
- Audio Markers & Cadence:
  * Insert `[pause=1.2s]` for dramatic beats and cliffhangers.
  * Use `...` for lingering, reflective thoughts.
  * Use UPPERCASE inside brackets like `[EMPHASIS]` or `[ENERGETIC]` to signal shifts in mood and dynamic power.
  * Incorporate natural breath pauses before major revelations.

### OUTPUT INSTRUCTIONS
Perform the script provided by the user adhering strictly to the designated voice profile tag (e.g., [MALE NARRATOR] or [FEMALE NARRATOR]). Output directly as high-fidelity audio narration."""

class DocuVoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val audioClient = GeminiAudioClient()
    val playerManager = AudioPlayerManager(application)
    val sessionStorage = SessionStorage(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState

    init {
        checkApiKeyStatus()
    }

    private fun checkApiKeyStatus() {
        val hasBuildConfigKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
        _uiState.update { it.copy(isApiKeyConfigured = hasBuildConfigKey || it.apiKeyOverride.isNotBlank()) }
    }

    fun onScriptTextChanged(newText: String) {
        _uiState.update { it.copy(scriptText = newText, generationError = null) }
    }

    fun onSelectVoice(voice: VoiceProfile) {
        _uiState.update { it.copy(selectedVoice = voice) }
    }

    fun onSetTemperature(temp: Float) {
        _uiState.update { it.copy(temperature = (Math.round(temp * 100f) / 100f)) }
    }

    fun onSelectModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun onSystemInstructionChanged(instruction: String) {
        _uiState.update { it.copy(systemInstruction = instruction) }
    }

    fun toggleSystemInstructionExpanded() {
        _uiState.update { it.copy(isSystemInstructionExpanded = !it.isSystemInstructionExpanded) }
    }

    fun setApiKeyOverride(key: String) {
        _uiState.update {
            it.copy(
                apiKeyOverride = key.trim(),
                isApiKeyConfigured = key.isNotBlank() || (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")
            )
        }
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun setActiveTab(tab: Int) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun onQualitySettingsChanged(settings: QualitySettings) {
        _uiState.update { it.copy(qualitySettings = settings) }
    }

    fun applyQualityEnhancements() {
        val activeFile = _uiState.value.activeAudioFile ?: return
        viewModelScope.launch {
            try {
                val settings = _uiState.value.qualitySettings
                val remastered = File(getApplication<Application>().cacheDir, "remastered_${System.currentTimeMillis()}.wav")
                val processed = AudioDspEnhancer.processAudioFile(activeFile, remastered, settings)
                val amplitudes = WavAudioUtil.extractAmplitudes(processed.readBytes())

                _uiState.update {
                    it.copy(
                        activeAudioFile = processed,
                        audioAmplitudes = amplitudes
                    )
                }
                playerManager.loadAndPlay(processed.absolutePath, autoPlay = true)
                Toast.makeText(getApplication(), "Voice Remastered (${settings.presetName})", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Failed to remaster: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun insertMarkerTag(tag: String) {
        val current = _uiState.value.scriptText
        val updated = if (current.isEmpty() || current.endsWith("\n") || current.endsWith(" ")) {
            current + tag
        } else {
            "$current $tag"
        }
        _uiState.update { it.copy(scriptText = updated) }
    }

    fun loadTemplate(template: ScriptTemplate) {
        val matchedVoice = VoiceCatalog.findById(template.suggestedVoiceId)
        _uiState.update {
            it.copy(
                scriptText = template.scriptText,
                selectedVoice = matchedVoice,
                temperature = template.suggestedTemperature,
                activeTab = 0 // return to studio
            )
        }
    }

    fun generateNarration() {
        val state = _uiState.value
        if (state.scriptText.isBlank()) {
            _uiState.update { it.copy(generationError = "Please enter or select a narration script first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, generationError = null) }

            val result = audioClient.generateSpeech(
                apiKeyOverride = state.apiKeyOverride.takeIf { it.isNotBlank() },
                model = state.selectedModel,
                scriptText = state.scriptText,
                systemInstruction = state.systemInstruction,
                voiceName = state.selectedVoice.id,
                temperature = state.temperature
            )

            when (result) {
                is GeminiAudioResult.Success -> {
                    try {
                        val tempFile = File(getApplication<Application>().cacheDir, "current_narration.wav")
                        val validWav = WavAudioUtil.saveAsValidWav(result.audioBytes, tempFile)

                        // Apply Audio Quality Controller DSP Mastering (EQ + Normalization + Leveler)
                        val enhancedFile = File(getApplication<Application>().cacheDir, "mastered_narration.wav")
                        val finalWav = AudioDspEnhancer.processAudioFile(
                            inputFile = validWav,
                            outputFile = enhancedFile,
                            settings = state.qualitySettings
                        )

                        val finalBytes = finalWav.readBytes()
                        val amplitudes = WavAudioUtil.extractAmplitudes(finalBytes)

                        // Calculate rough duration based on sample rate 24000 16-bit mono
                        val durationSec = (finalBytes.size / (24000f * 2f)).coerceAtLeast(1.0f)

                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                activeAudioFile = finalWav,
                                audioAmplitudes = amplitudes,
                                generationError = null
                            )
                        }

                        // Save session
                        val title = state.scriptText
                            .lines()
                            .firstOrNull { it.isNotBlank() && !it.startsWith("[") }
                            ?.take(36)
                            ?: "Documentary Narration"

                        sessionStorage.saveSession(
                            title = title,
                            scriptText = state.scriptText,
                            voiceName = state.selectedVoice.id,
                            characterName = state.selectedVoice.characterName,
                            temperature = state.temperature,
                            modelName = state.selectedModel,
                            tempAudioFile = finalWav,
                            durationSeconds = durationSec
                        )

                        // Load into player and auto-play
                        playerManager.loadAndPlay(finalWav.absolutePath, autoPlay = true)
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                generationError = "Failed to process audio: ${e.message}"
                            )
                        }
                    }
                }
                is GeminiAudioResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generationError = result.message,
                            showApiKeyDialog = result.isApiKeyMissing
                        )
                    }
                }
            }
        }
    }

    fun playSession(session: com.example.data.model.NarrationSession) {
        val file = File(session.audioFilePath)
        if (file.exists()) {
            _uiState.update {
                it.copy(
                    activeAudioFile = file,
                    scriptText = session.scriptText,
                    selectedVoice = VoiceCatalog.findById(session.voiceName),
                    activeTab = 0
                )
            }
            playerManager.loadAndPlay(file.absolutePath, autoPlay = true)
        }
    }

    fun deleteSession(sessionId: String) {
        sessionStorage.deleteSession(sessionId)
    }

    fun setShowExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }

    fun exportCurrentAudioToPublicFolder(folderType: PublicFolderType) {
        val file = _uiState.value.activeAudioFile ?: return
        val title = _uiState.value.scriptText.lines().firstOrNull { it.isNotBlank() && !it.startsWith("[") } ?: "Narration"
        val result = sessionStorage.exportToPublicFolder(file, title, folderType)
        if (result.success) {
            Toast.makeText(getApplication(), "Saved to ${result.destinationName}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(getApplication(), "Export failed: ${result.errorMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportCurrentAudioToSafUri(uri: Uri) {
        val file = _uiState.value.activeAudioFile ?: return
        val result = sessionStorage.exportToSafUri(file, uri)
        if (result.success) {
            Toast.makeText(getApplication(), "Saved to custom location", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(getApplication(), "Export failed: ${result.errorMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportSessionToPublicFolder(session: com.example.data.model.NarrationSession, folderType: PublicFolderType) {
        val file = File(session.audioFilePath)
        if (!file.exists()) {
            Toast.makeText(getApplication(), "Audio file not found", Toast.LENGTH_SHORT).show()
            return
        }
        val result = sessionStorage.exportToPublicFolder(file, session.title, folderType)
        if (result.success) {
            Toast.makeText(getApplication(), "Saved to ${result.destinationName}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(getApplication(), "Export failed: ${result.errorMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareCurrentAudio() {
        val file = _uiState.value.activeAudioFile ?: return
        sessionStorage.shareAudio(file.absolutePath, "YouTube Documentary Voiceover")
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
