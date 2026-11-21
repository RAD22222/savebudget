package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalBuckwheatColors = staticCompositionLocalOf { MidnightPalette }

object BuckwheatThemeTokens {
    val colors: BuckwheatColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBuckwheatColors.current
}

@Composable
fun BuckwheatTheme(
    themeMode: AppThemeMode = AppThemeMode.MIDNIGHT,
    content: @Composable () -> Unit
) {
    val palette = paletteForTheme(themeMode)
    val colorScheme = if (palette.isLight) {
        lightColorScheme(
            primary = palette.blue,
            onPrimary = palette.onBlue,
            primaryContainer = palette.blueBright,
            onPrimaryContainer = palette.onBlue,
            secondary = palette.greenFill,
            onSecondary = palette.greenText,
            secondaryContainer = palette.greenTrack,
            onSecondaryContainer = palette.greenText,
            tertiary = palette.plumCardBg,
            onTertiary = palette.plumText,
            background = palette.bgDark,
            onBackground = palette.onDark,
            surface = palette.bgDark,
            onSurface = palette.onDark,
            surfaceVariant = palette.panelSurface,
            onSurfaceVariant = palette.onDarkSecondary,
            surfaceContainer = palette.cardSurface,
            surfaceContainerHigh = palette.panelSurface,
            outline = palette.divider,
            error = palette.redFill,
            onError = palette.dangerText
        )
    } else {
        darkColorScheme(
            primary = palette.blue,
            onPrimary = palette.onBlue,
            primaryContainer = palette.blueBright,
            onPrimaryContainer = palette.onBlue,
            secondary = palette.greenFill,
            onSecondary = palette.greenText,
            secondaryContainer = palette.greenTrack,
            onSecondaryContainer = palette.greenText,
            tertiary = palette.plumCardBg,
            onTertiary = palette.plumText,
            background = palette.bgDark,
            onBackground = palette.onDark,
            surface = palette.bgDark,
            onSurface = palette.onDark,
            surfaceVariant = palette.panelSurface,
            onSurfaceVariant = palette.onDarkSecondary,
            surfaceContainer = palette.cardSurface,
            surfaceContainerHigh = palette.panelSurface,
            outline = palette.divider,
            error = palette.redFill,
            onError = palette.dangerText
        )
    }

    CompositionLocalProvider(LocalBuckwheatColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BuckwheatTypography,
            content = content
        )
    }
}
