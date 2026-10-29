package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = GalleryBlack,
    primaryContainer = SurfaceContainerHighDark,
    onPrimaryContainer = GoldAccent,
    secondary = GoldMuted,
    onSecondary = GalleryBlack,
    secondaryContainer = SurfaceContainerDark,
    onSecondaryContainer = ParchmentWhite,
    tertiary = SageAccent,
    onTertiary = GalleryBlack,
    background = GalleryBlack,
    onBackground = ParchmentWhite,
    surface = SurfaceDark,
    onSurface = ParchmentWhite,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = ParchmentMuted,
    outline = DividerDark,
    outlineVariant = SurfaceContainerHighDark
)

// Fine art gallery is designed with deep gallery atmosphere
private val LightColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = GalleryBlack,
    primaryContainer = SurfaceContainerHighDark,
    onPrimaryContainer = GoldAccent,
    secondary = GoldMuted,
    onSecondary = GalleryBlack,
    secondaryContainer = SurfaceContainerDark,
    onSecondaryContainer = ParchmentWhite,
    tertiary = SageAccent,
    onTertiary = GalleryBlack,
    background = GalleryBlack,
    onBackground = ParchmentWhite,
    surface = SurfaceDark,
    onSurface = ParchmentWhite,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = ParchmentMuted,
    outline = DividerDark,
    outlineVariant = SurfaceContainerHighDark
)

@Composable
fun DailyCuratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = DailyCuratorTypography,
        content = content
    )
}
