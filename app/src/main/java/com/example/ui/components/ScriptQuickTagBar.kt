package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioMarker
import com.example.data.model.AudioMarkerCatalog
import com.example.data.model.MarkerCategory
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCrimson
import com.example.ui.theme.StudioCrimsonContainer
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScriptQuickTagBar(
    onInsertTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DIRECTOR AUDIO CUES & MARKERS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextSecondary
            )
            Text(
                text = "Tap to insert",
                style = MaterialTheme.typography.labelSmall,
                color = GoldAmber
            )
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AudioMarkerCatalog.markers.forEach { marker ->
                val (bg, fg, border) = getMarkerColors(marker)

                Surface(
                    onClick = { onInsertTag(marker.tagText) },
                    shape = RoundedCornerShape(8.dp),
                    color = bg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, border),
                    modifier = Modifier.testTag("tag_chip_${marker.label.replace(" ", "_")}")
                ) {
                    Text(
                        text = marker.label,
                        color = fg,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getMarkerColors(marker: AudioMarker): Triple<Color, Color, Color> {
    return when (marker.category) {
        MarkerCategory.SPEAKER -> {
            if (marker.label.contains("Victor")) {
                Triple(GoldContainer, GoldAmber, GoldAmber)
            } else {
                Triple(CyanContainer, CyanAccent, CyanAccent)
            }
        }
        MarkerCategory.PAUSE -> Triple(StudioSurfaceVariant, TextPrimary, StudioBorder)
        MarkerCategory.PACING -> Triple(Color(0xFF1E2838), CyanAccent, Color(0xFF334B68))
        MarkerCategory.EMOTION -> Triple(StudioCrimsonContainer, Color(0xFFFFB4C2), StudioCrimson)
    }
}
