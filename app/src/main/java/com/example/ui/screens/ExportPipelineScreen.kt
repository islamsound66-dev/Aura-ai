package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.CodeExportUtil
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
fun ExportPipelineScreen(
    viewModel: DocuVoiceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedFormatIndex by remember { mutableStateOf(0) }
    val formats = listOf("Python SDK", "cURL", "AI Studio JSON")

    val exportCode = remember(selectedFormatIndex, uiState.selectedVoice, uiState.temperature, uiState.scriptText, uiState.systemInstruction) {
        when (selectedFormatIndex) {
            0 -> CodeExportUtil.generatePythonCode(
                modelName = uiState.selectedModel,
                voiceName = uiState.selectedVoice.id,
                temperature = uiState.temperature,
                systemInstruction = uiState.systemInstruction,
                scriptText = uiState.scriptText
            )
            1 -> CodeExportUtil.generateCurlCommand(
                modelName = uiState.selectedModel,
                voiceName = uiState.selectedVoice.id,
                temperature = uiState.temperature,
                systemInstruction = uiState.systemInstruction,
                scriptText = uiState.scriptText
            )
            else -> CodeExportUtil.generateAiStudioPromptJson(
                modelName = uiState.selectedModel,
                voiceName = uiState.selectedVoice.id,
                temperature = uiState.temperature,
                systemInstruction = uiState.systemInstruction,
                scriptText = uiState.scriptText
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = GoldAmber,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "AUTOMATION & EXPORT PIPELINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GoldAmber
                )
                Text(
                    text = "Export your active voiceover script and voice director configuration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Format Tabs
        TabRow(
            selectedTabIndex = selectedFormatIndex,
            containerColor = StudioCardBg,
            contentColor = GoldAmber,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedFormatIndex]),
                    color = GoldAmber
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            formats.forEachIndexed { index, title ->
                Tab(
                    selected = selectedFormatIndex == index,
                    onClick = { selectedFormatIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selectedFormatIndex == index) GoldAmber else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = StudioSurfaceVariant,
            border = BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (selectedFormatIndex) {
                        0 -> "Requires official Google GenAI SDK: pip install google-genai. Outputs .wav audio directly."
                        1 -> "Executes via standard terminal shell or CI/CD automated video production pipelines."
                        else -> "Paste directly into Google AI Studio system instruction and user prompt fields."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Code Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B10)),
            border = BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formats[selectedFormatIndex].uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GoldAmber
                    )

                    Button(
                        onClick = { viewModel.copyToClipboard(exportCode, formats[selectedFormatIndex]) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAmber,
                            contentColor = Color(0xFF1E1202)
                        ),
                        modifier = Modifier.testTag("copy_export_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Code", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = exportCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF93C5FD),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
