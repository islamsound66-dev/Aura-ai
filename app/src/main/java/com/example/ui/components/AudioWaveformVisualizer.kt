package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDarkBg
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    amplitudes: List<Float>,
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val defaultAmplitudes = rememberSampleAmplitudes()
    val displayAmplitudes = if (amplitudes.isNotEmpty()) amplitudes else defaultAmplitudes

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(StudioDarkBg)
            .testTag("waveform_visualizer")
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(fraction)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val barCount = displayAmplitudes.size
            if (barCount == 0) return@Canvas

            val totalSpacing = width * 0.25f
            val spacing = totalSpacing / (barCount + 1)
            val barWidth = (width - totalSpacing) / barCount

            for (i in 0 until barCount) {
                val baseAmp = displayAmplitudes[i]
                val dynamicMultiplier = if (isPlaying) {
                    val oscillation = sin(wavePhase + (i * 0.35f)) * 0.25f
                    (1f + oscillation).coerceIn(0.6f, 1.4f)
                } else {
                    1.0f
                }

                val barHeight = (height * baseAmp * dynamicMultiplier).coerceIn(8.dp.toPx(), height * 0.88f)
                val x = spacing + i * (barWidth + spacing)
                val y = (height - barHeight) / 2f

                val barFraction = i.toFloat() / barCount
                val isPlayed = barFraction <= progress

                val color = when {
                    isPlayed -> GoldAmber
                    barFraction <= progress + 0.05f && isPlaying -> CyanAccent
                    else -> StudioBorder
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }

            // Playback cursor line
            val cursorX = width * progress.coerceIn(0f, 1f)
            drawLine(
                color = GoldAmber,
                start = Offset(cursorX, 0f),
                end = Offset(cursorX, height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

private fun rememberSampleAmplitudes(): List<Float> {
    return listOf(
        0.25f, 0.40f, 0.65f, 0.85f, 0.70f, 0.45f, 0.30f, 0.55f, 0.90f, 0.80f,
        0.60f, 0.35f, 0.50f, 0.75f, 0.95f, 0.88f, 0.62f, 0.40f, 0.28f, 0.48f,
        0.72f, 0.86f, 0.92f, 0.68f, 0.45f, 0.32f, 0.58f, 0.82f, 0.76f, 0.52f,
        0.38f, 0.64f, 0.91f, 0.84f, 0.60f, 0.42f, 0.30f, 0.50f, 0.70f, 0.40f
    )
}
