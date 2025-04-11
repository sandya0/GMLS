package com.example.gmls.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum representing different types of disasters that can be reported
 */
enum class DisasterType(val displayName: String) {
    EARTHQUAKE("Earthquake"),
    FLOOD("Flood"),
    WILDFIRE("Wildfire"),
    LANDSLIDE("Landslide"),
    VOLCANO("Volcano"),
    TSUNAMI("Tsunami"),
    HURRICANE("Hurricane"),
    TORNADO("Tornado"),
    OTHER("Other");

    companion object {
        /**
         * Get the associated icon for each disaster type
         */
        fun getIconForType(type: DisasterType): ImageVector {
            return when (type) {
                EARTHQUAKE -> Icons.Filled.Bolt
                FLOOD -> Icons.Filled.Water
                WILDFIRE -> Icons.Filled.LocalFireDepartment
                LANDSLIDE -> Icons.Filled.Terrain
                VOLCANO -> Icons.Filled.Volcano
                TSUNAMI -> Icons.Filled.Waves
                HURRICANE -> Icons.Filled.Storm
                TORNADO -> Icons.Filled.AirlineSeatFlatAngled
                OTHER -> Icons.Filled.Warning
            }
        }

        /**
         * Get the associated color for each disaster type
         */
        fun getColorForType(type: DisasterType): Color {
            return when (type) {
                EARTHQUAKE -> Color(0xFFE57373) // Light Red
                FLOOD -> Color(0xFF64B5F6)      // Light Blue
                WILDFIRE -> Color(0xFFFFB74D)   // Light Orange
                LANDSLIDE -> Color(0xFF8D6E63)  // Brown
                VOLCANO -> Color(0xFFFF8A65)    // Light Orange-Red
                TSUNAMI -> Color(0xFF4FC3F7)    // Light Blue
                HURRICANE -> Color(0xFF9575CD)  // Light Purple
                TORNADO -> Color(0xFF7986CB)    // Light Indigo
                OTHER -> Color(0xFF90A4AE)      // Light Blue Grey
            }
        }
    }
}