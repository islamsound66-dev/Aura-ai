package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.network.GeminiAudioClient
import com.example.ui.DocuVoiceViewModel
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AudioPlayerCard
import com.example.ui.components.ExportAudioDialog
import com.example.ui.components.ScriptQuickTagBar
import com.example.ui.components.VoiceQualityController
import com.example.ui.components.VoiceSelectorRow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StudioScreen(
    viewModel: DocuVoiceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Top Studio Banner & Live Status Header
        StudioHeader(
            isGenerating = uiState.isGenerating,
            isApiKeyConfigured = uiState.isApiKeyConfigured,
            onOpenApiKey = { viewModel.setShowApiKeyDialog(true) }
        )

        // Error message notification
        if (uiState.generationError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("generation_error_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0B14)),
                border = BorderStroke(1.dp, StudioCrimson)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StudioCrimson,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Generation Issue",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = StudioCrimson
                        )
                        Text(
                            text = uiState.generationError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                    if (!uiState.isApiKeyConfigured) {
                        Button(
                            onClick = { viewModel.setShowApiKeyDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCrimson),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text("Set Key", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Narrator Voice Selector
        VoiceSelectorRow(
            selectedVoice = uiState.selectedVoice,
            onVoiceSelected = { viewModel.onSelectVoice(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Director Controls Accordion (Temperature & System Instruction)
        DirectorControlPanel(
            temperature = uiState.temperature,
            onTemperatureChange = { viewModel.onSetTemperature(it) },
            selectedModel = uiState.selectedModel,
            onModelSelect = { viewModel.onSelectModel(it) },
            isSystemInstructionExpanded = uiState.isSystemInstructionExpanded,
            onToggleInstruction = { viewModel.toggleSystemInstructionExpanded() },
            systemInstruction = uiState.systemInstruction,
            onInstructionChange = { viewModel.onSystemInstructionChanged(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Voice Improvement & Quality Controller
        VoiceQualityController(
            settings = uiState.qualitySettings,
            onSettingsChange = { viewModel.onQualitySettingsChanged(it) },
            hasActiveAudio = uiState.activeAudioFile != null,
            onApplyEnhancements = { viewModel.applyQualityEnhancements() },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Director Audio Cues & Markers
        ScriptQuickTagBar(
            onInsertTag = { viewModel.insertMarkerTag(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Script Editor Area
        ScriptEditorCard(
            scriptText = uiState.scriptText,
            onScriptChange = { viewModel.onScriptTextChanged(it) },
            onClear = { viewModel.onScriptTextChanged("") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Primary Generation Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = { viewModel.generateNarration() },
                enabled = !uiState.isGenerating && uiState.scriptText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_narration_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAmber,
                    contentColor = Color(0xFF1E1202),
                    disabledContainerColor = StudioBorder,
                    disabledContentColor = TextSecondary
                )
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1E1202),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SYNTHESIZING MULTIMODAL AUDIO...",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PERFORM & GENERATE AUDIO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }
        }

        // Audio Monitor Player (shown when active audio exists)
        if (uiState.activeAudioFile != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AudioPlayerCard(
                    playbackState = playbackState,
                    voice = uiState.selectedVoice,
                    amplitudes = uiState.audioAmplitudes,
                    onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                    onSeek = { viewModel.playerManager.seekToFraction(it) },
                    onSetSpeed = { viewModel.playerManager.setSpeed(it) },
                    onToggleLoop = { viewModel.playerManager.toggleLooping() },
                    onShare = { viewModel.shareCurrentAudio() },
                    onExport = { viewModel.setShowExportDialog(true) }
                )
            }
        }
    }

    if (uiState.showExportDialog && uiState.activeAudioFile != null) {
        ExportAudioDialog(
            audioFile = uiState.activeAudioFile!!,
            title = uiState.scriptText.lines().firstOrNull { it.isNotBlank() && !it.startsWith("[") } ?: "Narration",
            onExportToPublicFolder = { viewModel.exportCurrentAudioToPublicFolder(it) },
            onExportToSafUri = { viewModel.exportCurrentAudioToSafUri(it) },
            onShare = { viewModel.shareCurrentAudio() },
            onDismiss = { viewModel.setShowExportDialog(false) }
        )
    }

    if (uiState.showApiKeyDialog) {
        ApiKeyDialog(
            initialKey = uiState.apiKeyOverride,
            onSaveKey = { viewModel.setApiKeyOverride(it) },
            onDismiss = { viewModel.setShowApiKeyDialog(false) }
        )
    }
}

@Composable
private fun StudioHeader(
    isGenerating: Boolean,
    isApiKeyConfigured: Boolean,
    onOpenApiKey: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = BorderStroke(1.dp, StudioBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.docu_studio_banner_1792837265882),
                    contentDescription = "Studio Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )

                // Top Status Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Studio ON-AIR Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isGenerating) StudioCrimson else StudioDarkBg.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, if (isGenerating) StudioCrimson else StudioBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isGenerating) Color.White else CyanAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGenerating) "RECORDING / ON-AIR" else "STUDIO READY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = if (isGenerating) Color.White else CyanAccent
                            )
                        }
                    }

                    // Key Status Chip
                    Surface(
                        onClick = onOpenApiKey,
                        shape = RoundedCornerShape(20.dp),
                        color = StudioDarkBg.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, if (isApiKeyConfigured) GoldAmber else StudioBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isApiKeyConfigured) GoldAmber else TextTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isApiKeyConfigured) "Gemini Key Active" else "Setup API Key",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isApiKeyConfigured) GoldAmber else TextSecondary
                            )
                        }
                    }
                }

                // Title Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = "DOCUVOICE STUDIO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Multimodal Audio Narration Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectorControlPanel(
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    selectedModel: String,
    onModelSelect: (String) -> Unit,
    isSystemInstructionExpanded: Boolean,
    onToggleInstruction: () -> Unit,
    systemInstruction: String,
    onInstructionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Temperature Header & Explanation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUDIO TEMPERATURE: $temperature",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GoldAmber
                    )
                    Text(
                        text = "Recommended: 0.35–0.45 for emotional stability",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldContainer
                ) {
                    Text(
                        text = when {
                            temperature < 0.35f -> "Calm / Rigid"
                            temperature <= 0.45f -> "Sweet Spot ★"
                            else -> "High Energy"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Slider(
                value = temperature,
                onValueChange = onTemperatureChange,
                valueRange = 0.20f..0.70f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = GoldAmber,
                    activeTrackColor = GoldAmber,
                    inactiveTrackColor = StudioBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("temperature_slider")
            )

            // Model Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Engine Model",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GeminiAudioClient.AVAILABLE_MODELS.forEach { model ->
                        val isSel = model == selectedModel
                        Surface(
                            onClick = { onModelSelect(model) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) CyanContainer else StudioSurfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) CyanAccent else StudioBorder)
                        ) {
                            Text(
                                text = model.replace("gemini-", "").replace("-preview", ""),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSel) CyanAccent else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Voice Director Directive
            Surface(
                onClick = onToggleInstruction,
                shape = RoundedCornerShape(8.dp),
                color = StudioSurfaceVariant,
                border = BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Master Voice Director Directives",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                    }
                    Icon(
                        imageVector = if (isSystemInstructionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isSystemInstructionExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = systemInstruction,
                        onValueChange = onInstructionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("system_instruction_editor"),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = StudioBorder,
                            cursorColor = CyanAccent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ScriptEditorCard(
    scriptText: String,
    onScriptChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val wordCount = scriptText.split("\\s+".toRegex()).count { it.isNotBlank() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = BorderStroke(1.dp, StudioBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DOCUMENTARY SCRIPT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$wordCount words",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear script",
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = scriptText,
                onValueChange = onScriptChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("script_editor_input"),
                placeholder = {
                    Text(
                        "[MALE NARRATOR - VICTOR]\n[SLOW, DEEP] Deep beneath the Pacific Ocean...\n[pause=1.5s] lies an extraordinary silence...",
                        color = TextTertiary,
                        fontSize = 13.sp
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAmber,
                    unfocusedBorderColor = StudioBorder,
                    cursorColor = GoldAmber
                )
            )
        }
    }
}
