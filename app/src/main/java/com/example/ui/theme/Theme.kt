package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SafetyFlame,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5A1605),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = IndustrialTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = SafetyCrimson,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = SafetyFlame,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE5),
    onPrimaryContainer = Color(0xFF4A1002),
    secondary = IndustrialTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6FFFA),
    onSecondaryContainer = Color(0xFF003830),
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = SafetyCrimson,
    outline = LightOutline
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "system",
    accent: String = "flame",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val (lightAccent, darkAccent, darkContainer, darkContainerText) = when (accent) {
        "teal" -> listOf(Color(0xFF00796B), Color(0xFF4DB6AC), Color(0xFF003C38), Color(0xFFB2DFDB))
        "blue" -> listOf(Color(0xFF1565C0), Color(0xFF64B5F6), Color(0xFF0D2E52), Color(0xFFBBDEFB))
        "purple" -> listOf(Color(0xFF6A4C93), Color(0xFFB39DDB), Color(0xFF39285A), Color(0xFFD1C4E9))
        else -> listOf(SafetyFlame, Color(0xFFFF8A65), Color(0xFF5A1605), Color(0xFFFFDBCF))
    }
    val colorScheme = if (darkTheme) {
        DarkColorScheme.copy(
            primary = darkAccent,
            onPrimary = DarkBackground,
            primaryContainer = darkContainer,
            onPrimaryContainer = darkContainerText
        )
    } else {
        LightColorScheme.copy(
            primary = lightAccent,
            primaryContainer = lightAccent.copy(alpha = 0.12f),
            onPrimaryContainer = lightAccent
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
