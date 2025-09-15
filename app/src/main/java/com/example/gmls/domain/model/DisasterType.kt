package com.example.gmls.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import android.content.Context
import com.example.gmls.R

/**
 * Enum representing different types of disasters that can be reported
 */
enum class DisasterType {
    EARTHQUAKE,
    FLOOD,
    WILDFIRE,
    LANDSLIDE,
    VOLCANO,
    TSUNAMI,
    HURRICANE,
    TORNADO,
    OTHER;

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

/**
 * Extension function to get localized display name for disaster type (for Composables)
 */
@Composable
fun DisasterType.getDisplayName(): String {
    return when (this) {
        DisasterType.EARTHQUAKE -> stringResource(R.string.disaster_type_earthquake)
        DisasterType.FLOOD -> stringResource(R.string.disaster_type_flood)
        DisasterType.WILDFIRE -> stringResource(R.string.disaster_type_wildfire)
        DisasterType.LANDSLIDE -> stringResource(R.string.disaster_type_landslide)
        DisasterType.VOLCANO -> stringResource(R.string.disaster_type_volcano)
        DisasterType.TSUNAMI -> stringResource(R.string.disaster_type_tsunami)
        DisasterType.HURRICANE -> stringResource(R.string.disaster_type_hurricane)
        DisasterType.TORNADO -> stringResource(R.string.disaster_type_tornado)
        DisasterType.OTHER -> stringResource(R.string.disaster_type_other)
    }
}

/**
 * Extension function to get localized display name for disaster type (for non-Composables)
 */
fun DisasterType.getDisplayName(context: android.content.Context): String {
    return when (this) {
        DisasterType.EARTHQUAKE -> context.getString(R.string.disaster_type_earthquake)
        DisasterType.FLOOD -> context.getString(R.string.disaster_type_flood)
        DisasterType.WILDFIRE -> context.getString(R.string.disaster_type_wildfire)
        DisasterType.LANDSLIDE -> context.getString(R.string.disaster_type_landslide)
        DisasterType.VOLCANO -> context.getString(R.string.disaster_type_volcano)
        DisasterType.TSUNAMI -> context.getString(R.string.disaster_type_tsunami)
        DisasterType.HURRICANE -> context.getString(R.string.disaster_type_hurricane)
        DisasterType.TORNADO -> context.getString(R.string.disaster_type_tornado)
        DisasterType.OTHER -> context.getString(R.string.disaster_type_other)
    }
}

/**
 * Extension property to get Indonesian display name for disaster type (for non-UI contexts)
 * This provides the hardcoded Indonesian names for filtering and comparison purposes
 */
val DisasterType.displayName: String
    get() = when (this) {
        DisasterType.EARTHQUAKE -> "Gempa Bumi"
        DisasterType.FLOOD -> "Banjir"
        DisasterType.WILDFIRE -> "Kebakaran Hutan"
        DisasterType.LANDSLIDE -> "Tanah Longsor"
        DisasterType.VOLCANO -> "Gunung Berapi"
        DisasterType.TSUNAMI -> "Tsunami"
        DisasterType.HURRICANE -> "Badai"
        DisasterType.TORNADO -> "Tornado"
        DisasterType.OTHER -> "Lainnya"
}
