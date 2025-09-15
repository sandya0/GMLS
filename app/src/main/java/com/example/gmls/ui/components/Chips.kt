package com.example.gmls.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.gmls.ui.theme.Red

/**
 * Custom chip for categories like disaster types
 */
@Composable
fun DisasterTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = if (selected) Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
            labelColor = if (selected) Red else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Red else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        }
    )
}
