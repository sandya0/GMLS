package com.example.gmls.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Primary Colors - Sophisticated red palette
val Red = Color(0xFFDC2626) // Modern red, less harsh
val DarkRed = Color(0xFF991B1B) // Deeper red for contrast
val LightRed = Color(0xFFFEF2F2) // Very light red for backgrounds
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF0F172A) // Softer black with blue undertone

// Oil Black Background System
val OilBlack = Color(0xFF0C0C0C) // Main Oil Black background
val OilBlackSurface = Color(0xFF1A1A1A) // Slightly lighter for surfaces
val OilBlackVariant = Color(0xFF2A2A2A) // Even lighter for elevated surfaces

// Modern Background and Surface Colors
val LightBackground = Color(0xFFFAFAFA) // Warmer light background
val DarkBackground = OilBlack // Oil Black for dark theme
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = OilBlackSurface // Oil Black surface for dark theme

// Modern Accent Colors
val AccentRed = Color(0xFFEF4444) // Vibrant but not harsh
val AccentBlue = Color(0xFF3B82F6) // Modern blue
val AccentGreen = Color(0xFF10B981) // Fresh green
val AccentOrange = Color(0xFFF59E0B) // Warm orange

// Neutral Colors - Modern gray scale adapted for Oil Black
val Gray50 = Color(0xFFF8FAFC)
val Gray100 = Color(0xFFF1F5F9)
val Gray200 = Color(0xFFE2E8F0)
val Gray300 = Color(0xFFCBD5E1)
val Gray400 = Color(0xFF94A3B8)
val Gray500 = Color(0xFF64748B)
val Gray600 = Color(0xFF475569)
val Gray700 = Color(0xFF334155)
val Gray800 = Color(0xFF1E293B)
val Gray900 = OilBlack // Use Oil Black for darkest gray

// Legacy support
val Gray = Gray500
val LightGray = Gray200
val DarkGray = Gray700

// Modern Status Colors with better accessibility
val Success = Color(0xFF059669) // Deeper green
val Warning = Color(0xFFD97706) // Warmer orange
val Error = Color(0xFFDC2626) // Consistent with primary red
val Info = Color(0xFF0284C7) // Modern blue

// Material Design Theme Colors
val md_theme_light_primary = Red
val md_theme_light_error = Error

// Modern Disaster Type Colors - Softer, more sophisticated
val EarthquakeColor = Color(0xFF8B5A3C) // Earthy brown
val FloodColor = Color(0xFF0EA5E9) // Clear blue
val WildfireColor = Color(0xFFEA580C) // Warm orange-red
val LandslideColor = Color(0xFF78716C) // Natural stone
val VolcanoColor = Color(0xFFDC2626) // Deep red
val TsunamiColor = Color(0xFF0891B2) // Ocean blue
val HurricaneColor = Color(0xFF7C3AED) // Storm purple
val TornadoColor = Color(0xFF6366F1) // Wind indigo
val OtherDisasterColor = Color(0xFF6B7280) // Neutral gray

// Modern semantic colors for better UX
val SuccessContainer = Color(0xFFDCFCE7)
val WarningContainer = Color(0xFFFEF3C7)
val ErrorContainer = Color(0xFFFEE2E2)
val InfoContainer = Color(0xFFDBEAFE)

// Surface variants for depth - adapted for Oil Black theme
val SurfaceVariant = Color(0xFFF1F5F9) // Light theme
val SurfaceVariantDark = OilBlackVariant // Dark theme with Oil Black
val OnSurfaceVariant = Color(0xFF64748B)
val OnSurfaceVariantDark = Color(0xFFE2E8F0) // Better contrast on Oil Black
val Outline = Color(0xFFCBD5E1)
val OutlineVariant = Color(0xFFE2E8F0)
val OutlineDark = Color(0xFF475569) // Darker outline for Oil Black theme
val OutlineVariantDark = Color(0xFF334155) // Darker outline variant for Oil Black theme
