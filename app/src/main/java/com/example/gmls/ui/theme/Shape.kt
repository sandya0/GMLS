package com.example.gmls.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Modern shapes with consistent rounded corners following current design trends
val Shapes = Shapes(
    // Small elements: buttons, chips, small cards
    small = RoundedCornerShape(12.dp),
    // Medium elements: cards, dialogs, input fields
    medium = RoundedCornerShape(16.dp),
    // Large elements: bottom sheets, large containers, modals
    large = RoundedCornerShape(24.dp)
)

// Additional custom shapes for specific use cases
val ExtraSmallShape = RoundedCornerShape(8.dp) // For very small elements
val ExtraLargeShape = RoundedCornerShape(32.dp) // For hero sections
val CircularShape = RoundedCornerShape(50) // For fully rounded elements
val TopRoundedShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) // For bottom sheets
