package com.example.gmls.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = Red,
    onPrimary = White,
    primaryContainer = LightGray,
    onPrimaryContainer = Black,
    secondary = DarkRed,
    onSecondary = White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    tertiary = Gray,
    onTertiary = White,
    background = LightBackground,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    error = Error,
    onError = White
)

// Define Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = AccentRed,
    onPrimary = Black,
    primaryContainer = DarkRed,
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFB4AB),
    onSecondary = Color(0xFF690005),
    secondaryContainer = Color(0xFF93000A),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = LightGray,
    onTertiary = Black,
    background = DarkBackground,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

/**
 * Theme for the Disaster Response app
 * @param darkTheme Whether to use the dark theme
 * @param content The content to be styled
 */
@Composable
fun DisasterResponseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}