package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DEFAULT_SYSTEM_INSTRUCTION
import com.example.ui.DocuVoiceViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GuideScreen(
    viewModel: DocuVoiceViewModel,
    modifier: Modifier = Modifier
) {
    val expandedSteps = remember {
        mutableStateMapOf(
            1 to true,
            2 to true,
            3 to false,
            4 to false,
            5 to false,
            6 to false,
            7 to true
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(horizontal = 16.dp)
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = GoldAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MASTER PROMPT IMPLEMENTATION GUIDE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GoldAmber
                    )
                    Text(
                        text = "Step-by-step master directive for Gemini multimodal audio in Google AI Studio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Step 1
        item {
            GuideStepCard(
                stepNumber = 1,
                title = "Open and Configure Google AI Studio",
                subtitle = "Model selection, Response Modality, and Temperature tuning",
                isExpanded = expandedSteps[1] == true,
                onToggle = { expandedSteps[1] = !(expandedSteps[1] ?: false) }
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "1. Go to aistudio.google.com and log in with your Google account.\n" +
                                "2. Click Create New Prompt (or open Speech / Audio Playground).\n" +
                                "3. In the right-hand Configuration panel, set:\n" +
                                "   • Model: gemini-2.5-flash (with native audio capability)\n" +
                                "   • Response Modality: AUDIO (or AUDIO + TEXT)\n" +
                                "   • Temperature: 0.35–0.45 (Optimal balance of emotional range and vocal stability)",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextPrimary
                    )
                }
            }
        }

        // Step 2
        item {
            GuideStepCard(
                stepNumber = 2,
                title = "Set System Instructions (Master Directive)",
                subtitle = "Vocal profiles for Victor & Elena and dynamic audio markers",
                isExpanded = expandedSteps[2] == true,
                onToggle = { expandedSteps[2] = !(expandedSteps[2] ?: false) }
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Copy and paste this master directive into the System Instructions field. It serves as the voice director for character voices:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF070B10),
                        border = BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = DEFAULT_SYSTEM_INSTRUCTION,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = Color(0xFFBAE6FD)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.copyToClipboard(DEFAULT_SYSTEM_INSTRUCTION, "System Instructions") },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAmber, contentColor = Color(0xFF1E1202)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Directive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Step 3
        item {
            GuideStepCard(
                stepNumber = 3,
                title = "Select the Prebuilt Voice",
                subtitle = "Assigning Zephyr (Male Baritone) & Aoede/Kore (Female Treble)",
                isExpanded = expandedSteps[3] == true,
                onToggle = { expandedSteps[3] = !(expandedSteps[3] ?: false) }
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "In Audio Settings / Voice Configuration panel:\n" +
                                "• Deep Male Narration: Select Zephyr or Fenrir for a grounded, authoritative baritone with cinematic gravitas.\n" +
                                "• Energetic Female Narration: Select Aoede or Kore for high-clarity articulate treble with passion.\n" +
                                "• Multiple Narrators: Gemini will modulate timbre dynamically according to the speaker tags in your prompt.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextPrimary
                    )
                }
            }
        }

        // Step 4
        item {
            GuideStepCard(
                stepNumber = 4,
                title = "Input Your Script (User Prompt Template)",
                subtitle = "Dual-narrator tags and explicit dramatic pacing cues",
                isExpanded = expandedSteps[4] == true,
                onToggle = { expandedSteps[4] = !(expandedSteps[4] ?: false) }
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Format scripts with alternating voice tags and audio cadence markers:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF070B10),
                        border = BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = """[MALE NARRATOR - VICTOR]
[SLOW, DEEP] Deep beneath the Pacific Ocean... [pause=1.5s] lies a silence so profound, it defies imagination. 

[BUILDING TENSION] In the spring of 1968, a classified sub-surface radar caught something... [pause=1.0s] impossible.

[FEMALE NARRATOR - ELENA]
[ENERGETIC, EXPOSING TRUTH] But what scientists discovered wasn't an anomaly! [pause=0.8s] It was an active, pulsating signature...""",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                    }
                }
            }
        }

        // Step 5
        item {
            GuideStepCard(
                stepNumber = 5,
                title = "Generate, Test & Fine-Tune",
                subtitle = "Pacing adjustments, pause durations, and temperature modulation",
                isExpanded = expandedSteps[5] == true,
                onToggle = { expandedSteps[5] = !(expandedSteps[5] ?: false) }
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "• Speech Too Fast? Add explicit [pause=1.5s] tags or triple ellipsis (...).\n" +
                                "• Lacks Dramatic Energy? Increase Temperature to 0.45 or prefix sentences with [ENERGETIC] or [HIGH ENERGY].\n" +
                                "• Whispered Secrets? Use [WHISPER] or [INTIMATE GRAVITAS].\n" +
                                "• Climax Beats? Insert [CLIMAX] before the final punchline.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextPrimary
                    )
                }
            }
        }

        // Step 6
        item {
            GuideStepCard(
                stepNumber = 6,
                title = "Export to Python / Automation Pipeline",
                subtitle = "Integrate into automated script-to-voice production",
                isExpanded = expandedSteps[6] == true,
                onToggle = { expandedSteps[6] = !(expandedSteps[6] ?: false) }
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "DocuVoice automatically generates Python SDK code (`google-genai`), cURL commands, and AI Studio prompt configurations in the Pipeline tab for 1-click export.",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.setActiveTab(2) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF031E2B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Open Export Pipeline", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Step 7: Install on Android Device
        item {
            GuideStepCard(
                stepNumber = 7,
                title = "Install DocuVoice App on Your Android Phone",
                subtitle = "Step-by-step instructions to install and run the APK on your device",
                isExpanded = expandedSteps[7] == true,
                onToggle = { expandedSteps[7] = !(expandedSteps[7] ?: false) }
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Follow these simple steps to install DocuVoice Studio on any Android phone or tablet:\n\n" +
                                "1. Download the APK:\n" +
                                "   • In Google AI Studio, click the Settings / Export icon in the top toolbar and select 'Download APK' (or download `app-debug.apk` directly from `app/build/outputs/apk/debug/`).\n\n" +
                                "2. Transfer to your phone:\n" +
                                "   • Send the `.apk` file to your phone via Google Drive, Telegram/WhatsApp, email, or a USB cable to your phone's 'Downloads' folder.\n\n" +
                                "3. Open the APK file:\n" +
                                "   • On your Android device, open the Files / File Manager app, go to 'Downloads', and tap `app-debug.apk`.\n\n" +
                                "4. Enable 'Install Unknown Apps':\n" +
                                "   • If prompted by Android security ('For your security, your phone is not allowed to install unknown apps from this source'), tap Settings and toggle ON 'Allow from this source'.\n\n" +
                                "5. Tap Install & Launch:\n" +
                                "   • Tap 'Install'. When finished, tap 'Open'. DocuVoice will open with its full studio interface.\n\n" +
                                "6. Set Up Gemini Key:\n" +
                                "   • In DocuVoice, tap the Key icon in the top header and enter your Gemini API key (or configure it in AI Studio Secrets) to generate real voiceovers!",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guide_step_$stepNumber"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = BorderStroke(1.dp, if (isExpanded) GoldAmber else StudioBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                onClick = onToggle,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(GoldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$stepNumber",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldAmber
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (isExpanded) GoldAmber else TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                content()
            }
        }
    }
}
