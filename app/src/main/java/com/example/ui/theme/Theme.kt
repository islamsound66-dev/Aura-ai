package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val StudioColorScheme = darkColorScheme(
    primary = GoldAmber,
    onPrimary = Color(0xFF1E1202),
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldAmberLight,

    secondary = CyanAccent,
    onSecondary = Color(0xFF031E2B),
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanLight,

    tertiary = StudioCrimson,
    onTertiary = Color(0xFF28030B),
    tertiaryContainer = StudioCrimsonContainer,
    onTertiaryContainer = Color(0xFFFFCCD3),

    background = StudioDarkBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = Color(0xFF243041)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep consistent cinematic studio aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
