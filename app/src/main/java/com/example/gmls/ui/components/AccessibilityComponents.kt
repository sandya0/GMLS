package com.example.gmls.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gmls.ui.theme.*

/**
 * Accessible button with enhanced semantics
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    contentDescription: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
            if (loading) {
                this.stateDescription = "Memuat"
            }
            if (!enabled) {
                this.disabled()
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Red,
            disabledContainerColor = Red.copy(alpha = 0.3f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Accessible card with proper semantics
 */
@Composable
fun AccessibleCard(
    onClick: (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = buildString {
                append(title)
                subtitle?.let { append(", $it") }
            }
            if (onClick != null) {
                role = Role.Button
            }
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Accessible radio button group
 */
@Composable
fun AccessibleRadioGroup(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    Column(
        modifier = modifier.selectableGroup()
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { onOptionSelected(option) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null // handled by selectable modifier
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Accessible status indicator
 */
@Composable
fun AccessibleStatusIndicator(
    status: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.semantics {
                    contentDescription = "$status: ${if (isActive) "Aktif" else "Tidak Aktif"}"
        stateDescription = if (isActive) "Aktif" else "Tidak Aktif"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isActive) Success else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) Success else MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Accessible progress indicator with description
 */
@Composable
fun AccessibleProgressIndicator(
    progress: Float? = null,
    description: String = "Memuat",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics {
            contentDescription = if (progress != null) {
                "$description: ${(progress * 100).toInt()}% complete"
            } else {
                description
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Red
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            CircularProgressIndicator(
                color = Red,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 
