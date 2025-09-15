package com.example.gmls.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gmls.R
import com.example.gmls.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Enhanced snackbar with custom styling and actions
 */
@Composable
fun EnhancedSnackbar(
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        SnackbarType.SUCCESS -> Success
        SnackbarType.ERROR -> MaterialTheme.colorScheme.error
        SnackbarType.WARNING -> Warning
        SnackbarType.INFO -> MaterialTheme.colorScheme.primary
    }
    
    val icon = when (type) {
        SnackbarType.SUCCESS -> Icons.Default.CheckCircle
        SnackbarType.ERROR -> Icons.Default.Error
        SnackbarType.WARNING -> Icons.Default.Warning
        SnackbarType.INFO -> Icons.Default.Info
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onActionClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = actionLabel,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                                                    contentDescription = stringResource(R.string.close_description),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

enum class SnackbarType {
    SUCCESS, ERROR, WARNING, INFO
}

/**
 * Toast-like notification that appears temporarily
 */
@Composable
fun ToastNotification(
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    duration: Long = 3000L,
    onDismiss: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
        delay(duration)
        visible = false
        delay(300) // Animation duration
        onDismiss()
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        EnhancedSnackbar(
            message = message,
            type = type,
            onDismiss = { 
                visible = false
            }
        )
    }
}

/**
 * Confirmation dialog with enhanced styling
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.confirmation_button),
    dismissText: String = stringResource(R.string.cancel_button),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    type: DialogType = DialogType.WARNING
) {
    val iconColor = when (type) {
        DialogType.SUCCESS -> Success
        DialogType.ERROR -> MaterialTheme.colorScheme.error
        DialogType.WARNING -> Warning
        DialogType.INFO -> MaterialTheme.colorScheme.primary
    }
    
    val icon = when (type) {
        DialogType.SUCCESS -> Icons.Default.CheckCircle
        DialogType.ERROR -> Icons.Default.Error
        DialogType.WARNING -> Icons.Default.Warning
        DialogType.INFO -> Icons.Default.Info
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (type) {
                        DialogType.ERROR -> MaterialTheme.colorScheme.error
                        DialogType.WARNING -> Warning
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
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

enum class DialogType {
    SUCCESS, ERROR, WARNING, INFO
}

/**
 * Progress dialog for long-running operations
 */
@Composable
fun ProgressDialog(
    title: String,
    message: String,
    progress: Float? = null, // null for indeterminate
    onDismiss: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            dismissOnBackPress = onDismiss != null,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    CircularProgressIndicator(
                        color = Red,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                if (onDismiss != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

/**
 * Floating action button with tooltip
 */
@Composable
fun EnhancedFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String? = null,
    tooltip: String? = null,
    modifier: Modifier = Modifier
) {
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = { Icon(icon, contentDescription = tooltip) },
            text = { Text(text) },
            containerColor = Red,
            contentColor = Color.White,
            modifier = modifier
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            containerColor = Red,
            contentColor = Color.White,
            modifier = modifier
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip
            )
        }
    }
}

/**
 * Status indicator with animation
 */
@Composable
fun StatusIndicator(
    status: String,
    type: SnackbarType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val color = when (type) {
        SnackbarType.SUCCESS -> Success
        SnackbarType.ERROR -> MaterialTheme.colorScheme.error
        SnackbarType.WARNING -> Warning
        SnackbarType.INFO -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = alpha * 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = alpha))
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 
