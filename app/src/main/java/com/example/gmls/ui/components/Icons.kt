package com.example.gmls.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.theme.Red

/**
 * Circular icon with background
 */
@Composable
fun CircularIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Red.copy(alpha = 0.1f),
    iconColor: Color = Red,
    iconSize: Dp = 24.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Disaster type icon with appropriate color based on type
 */
@Composable
fun DisasterTypeIcon(
    type: DisasterType,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    contentDescription: String? = null
) {
    val icon = DisasterType.getIconForType(type)
    val color = DisasterType.getColorForType(type)

    CircularIcon(
        icon = icon,
        backgroundColor = color.copy(alpha = 0.1f),
        iconColor = color,
        iconSize = iconSize,
        contentDescription = contentDescription ?: type.displayName,
        modifier = modifier.size(size)
    )
}

/**
 * Status badge with color and rounded corners
 */
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}