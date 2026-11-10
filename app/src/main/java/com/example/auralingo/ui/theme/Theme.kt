package com.example.auralingo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuraLingoDarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = TextPrimary,
    primaryContainer = CardElevated,
    onPrimaryContainer = TextPrimary,
    secondary = NeonIndigo,
    onSecondary = TextPrimary,
    secondaryContainer = CardBorder,
    onSecondaryContainer = TextSecondary,
    background = ObsidianDark,
    onBackground = TextPrimary,
    surface = SlateVioletSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = NeonPink,
    onError = TextPrimary
)

@Composable
fun AuraLingoTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AuraLingoDarkColorScheme,
        content = content
    )
}
