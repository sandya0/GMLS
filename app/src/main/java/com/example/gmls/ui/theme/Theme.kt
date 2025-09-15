package com.example.gmls.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AppTheme enum for theme selection
enum class AppTheme { LIGHT, DARK, SYSTEM }

// Modern Light Theme Colors with improved contrast and accessibility
private val LightColorScheme = lightColorScheme(
    primary = Red,
    onPrimary = White,
    primaryContainer = LightRed,
    onPrimaryContainer = DarkRed,
    secondary = AccentBlue,
    onSecondary = White,
    secondaryContainer = InfoContainer,
    onSecondaryContainer = Gray800,
    tertiary = AccentGreen,
    onTertiary = White,
    tertiaryContainer = SuccessContainer,
    onTertiaryContainer = Gray800,
    background = LightBackground,
    onBackground = Gray900,
    surface = SurfaceLight,
    onSurface = Gray900,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    onError = White,
    errorContainer = ErrorContainer,
    onErrorContainer = Gray800,
    inverseSurface = Gray800,
    inverseOnSurface = Gray100,
    inversePrimary = AccentRed
)

// Modern Dark Theme Colors with Oil Black background
private val DarkColorScheme = darkColorScheme(
    primary = AccentRed,
    onPrimary = OilBlack,
    primaryContainer = DarkRed,
    onPrimaryContainer = Gray100,
    secondary = AccentBlue,
    onSecondary = OilBlack,
    secondaryContainer = OilBlackVariant,
    onSecondaryContainer = Gray100,
    tertiary = AccentGreen,
    onTertiary = OilBlack,
    tertiaryContainer = OilBlackVariant,
    onTertiaryContainer = Gray100,
    background = OilBlack,
    onBackground = Gray100,
    surface = OilBlackSurface,
    onSurface = Gray100,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = Color(0xFFFF6B6B),
    onError = OilBlack,
    errorContainer = OilBlackVariant,
    onErrorContainer = Color(0xFFFFB3B3),
    inverseSurface = Gray100,
    inverseOnSurface = OilBlack,
    inversePrimary = Red
)

/**
 * Unified GMLS theme with modern design system and Oil Black background
 * This is the single theme function used throughout the application
 * @param appTheme Theme selection (Light, Dark, or System)
 * @param content The content to be styled
 */
@Composable
fun GMLSTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
