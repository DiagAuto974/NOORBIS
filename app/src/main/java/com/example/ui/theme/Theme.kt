package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AstroDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = SpaceBackground,
    primaryContainer = SpaceSurfaceVariant,
    onPrimaryContainer = CyberCyanGlow,
    secondary = SolarGold,
    onSecondary = SpaceBackground,
    secondaryContainer = SpaceSurfaceVariant,
    onSecondaryContainer = SolarGoldGlow,
    tertiary = PlasmaRose,
    onTertiary = StarlightWhite,
    background = SpaceBackground,
    onBackground = StarlightWhite,
    surface = SpaceSurface,
    onSurface = StarlightWhite,
    surfaceVariant = SpaceSurfaceVariant,
    onSurfaceVariant = NebulaGray,
    outline = SpaceCardBorder,
    error = DangerRed
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AstroDarkColorScheme,
        typography = Typography,
        content = content
    )
}
