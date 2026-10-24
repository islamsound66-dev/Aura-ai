package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VoiceCatalog
import com.example.data.model.VoiceProfile
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VoiceSelectorRow(
    selectedVoice: VoiceProfile,
    onVoiceSelected: (VoiceProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAllVoicesMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PRIMARY NARRATOR VOICE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )

            Box {
                Surface(
                    onClick = { showAllVoicesMenu = true },
                    shape = RoundedCornerShape(16.dp),
                    color = StudioSurfaceVariant,
                    border = BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier.testTag("more_voices_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "All Voices",
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "More Voices (${VoiceCatalog.allVoices.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showAllVoicesMenu,
                    onDismissRequest = { showAllVoicesMenu = false },
                    modifier = Modifier.background(StudioCardBg)
                ) {
                    VoiceCatalog.allVoices.forEach { voice ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${voice.characterName} • [${voice.id}]",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (voice.id == selectedVoice.id) GoldAmber else TextPrimary
                                    )
                                    Text(
                                        text = "${voice.accentTag} • ${voice.tone.take(45)}...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            },
                            onClick = {
                                onVoiceSelected(voice)
                                showAllVoicesMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (voice.gender == "Male") Icons.Default.Mic else Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = if (voice.gender == "Male") GoldAmber else CyanAccent
                                )
                            }
                        )
                    }
                }
            }
        }

        // Two prominent cards: Victor & Elena
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NarratorCard(
                voice = VoiceCatalog.victor,
                isSelected = selectedVoice.id == VoiceCatalog.victor.id,
                accentColor = GoldAmber,
                containerColor = GoldContainer,
                onClick = { onVoiceSelected(VoiceCatalog.victor) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("voice_card_victor")
            )

            NarratorCard(
                voice = VoiceCatalog.elena,
                isSelected = selectedVoice.id == VoiceCatalog.elena.id,
                accentColor = CyanAccent,
                containerColor = CyanContainer,
                onClick = { onVoiceSelected(VoiceCatalog.elena) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("voice_card_elena")
            )
        }

        // If an alternate voice from dropdown is active, show small badge
        if (selectedVoice.id != VoiceCatalog.victor.id && selectedVoice.id != VoiceCatalog.elena.id) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                color = StudioSurfaceVariant,
                border = BorderStroke(1.dp, GoldAmber)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GoldAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Active Custom Voice: ${selectedVoice.characterName} (${selectedVoice.id})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = selectedVoice.tone,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NarratorCard(
    voice: VoiceProfile,
    isSelected: Boolean,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) containerColor.copy(alpha = 0.5f) else StudioCardBg
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accentColor else StudioBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voice.gender == "Male") Icons.Default.Mic else Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = voice.id,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = voice.characterName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = voice.accentTag,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = voice.tone,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}
