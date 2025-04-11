package com.example.gmls.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Define shapes for UI elements
val Shapes = Shapes(
    // Buttons, text fields, and smaller elements
    small = RoundedCornerShape(4.dp),
    // Cards, dialogs, and medium-sized containers
    medium = RoundedCornerShape(8.dp),
    // Bottom sheets, large containers
    large = RoundedCornerShape(12.dp)
)