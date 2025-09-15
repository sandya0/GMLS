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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.res.stringResource
import com.example.gmls.R
import com.example.gmls.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Global notification system for showing toast-like messages
 */
object NotificationSystem {
    private var currentNotification: NotificationData? by mutableStateOf(null)
    
    fun showNotification(
        message: String,
        type: NotificationType = NotificationType.INFO,
        duration: Long = 3000L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        currentNotification = NotificationData(
            message = message,
            type = type,
            duration = duration,
            actionLabel = actionLabel,
            onAction = onAction
        )
    }
    
    fun hideNotification() {
        currentNotification = null
    }
    
    @Composable
    fun NotificationHost() {
        currentNotification?.let { notification ->
            NotificationPopup(
                notification = notification,
                onDismiss = { hideNotification() }
            )
        }
    }
}

data class NotificationData(
    val message: String,
    val type: NotificationType,
    val duration: Long,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

enum class NotificationType {
    SUCCESS, ERROR, WARNING, INFO
}

@Composable
private fun NotificationPopup(
    notification: NotificationData,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(notification) {
        visible = true
        delay(notification.duration)
        visible = false
        delay(300) // Animation duration
        onDismiss()
    }
    
    if (visible) {
        Popup(
            alignment = Alignment.TopCenter,
            properties = PopupProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
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
                NotificationCard(
                    notification = notification,
                    onDismiss = { visible = false }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationData,
    onDismiss: () -> Unit
) {
    val backgroundColor = when (notification.type) {
        NotificationType.SUCCESS -> Success
        NotificationType.ERROR -> MaterialTheme.colorScheme.error
        NotificationType.WARNING -> Warning
        NotificationType.INFO -> MaterialTheme.colorScheme.primary
    }
    
    val icon = when (notification.type) {
        NotificationType.SUCCESS -> Icons.Default.CheckCircle
        NotificationType.ERROR -> Icons.Default.Error
        NotificationType.WARNING -> Icons.Default.Warning
        NotificationType.INFO -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
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
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            if (notification.actionLabel != null && notification.onAction != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = notification.onAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = notification.actionLabel,
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

/**
 * Enhanced floating action button with better animations
 */
@Composable
fun AnimatedFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String? = null,
    modifier: Modifier = Modifier,
    expanded: Boolean = true
) {
    val density = LocalDensity.current
    
    AnimatedVisibility(
        visible = true,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        if (text != null && expanded) {
            ExtendedFloatingActionButton(
                onClick = onClick,
                icon = { 
                    Icon(
                        imageVector = icon, 
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                text = { 
                    Text(
                        text = text,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                containerColor = Red,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            )
        } else {
            FloatingActionButton(
                onClick = onClick,
                containerColor = Red,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Loading overlay for full-screen operations
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Memuat...",
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Red,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
} 
