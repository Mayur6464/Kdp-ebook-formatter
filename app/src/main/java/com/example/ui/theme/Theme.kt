package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NaturalTaupeLight,
    onPrimary = NaturalDark,
    primaryContainer = Navy800,
    onPrimaryContainer = NaturalContainer,
    secondary = NaturalTaupeLight,
    onSecondary = NaturalDark,
    background = NaturalDark,
    onBackground = NaturalBg,
    surface = Navy800,
    onSurface = NaturalBg,
    surfaceVariant = Navy800,
    onSurfaceVariant = NaturalContainer,
    outline = NaturalTaupe
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalTaupe,
    onPrimary = Color.White,
    primaryContainer = NaturalContainer,
    onPrimaryContainer = NaturalDark,
    secondary = NaturalTaupeLight,
    onSecondary = Color.White,
    background = NaturalBg,
    onBackground = NaturalDark,
    surface = Color.White,
    onSurface = NaturalDark,
    surfaceVariant = NaturalCardBg,
    onSurfaceVariant = NaturalDark,
    outline = NaturalBorder
)

@Composable
fun KdpFormatterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
