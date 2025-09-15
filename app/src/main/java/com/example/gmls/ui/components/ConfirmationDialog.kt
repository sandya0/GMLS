package com.example.gmls.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.gmls.R

/**
 * A reusable confirmation dialog component.
 *
 * @param title The title of the dialog.
 * @param message The message to display in the dialog.
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onConfirm Callback when the confirm button is clicked.
 * @param confirmText The text to display on the confirm button.
 * @param dismissText The text to display on the dismiss button.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = stringResource(R.string.confirmation_button),
    dismissText: String = stringResource(R.string.cancel_button)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
