package com.example.gmls.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gmls.ui.components.EmergencyAlertCard
import com.example.gmls.ui.components.EmergencySeverity
import com.example.gmls.ui.theme.Error
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Warning
import java.util.*

/**
 * Data class representing a notification
 */
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    val type: NotificationType,
    val read: Boolean,
    val disasterId: String? = null
)

/**
 * Enum representing different notification types
 */
enum class NotificationType {
    EMERGENCY, DISASTER_ALERT, SYSTEM, INFO
}

/**
 * Screen for displaying user notifications
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (Notification) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    // In a real app, these would come from a ViewModel
    val notifications = remember {
        listOf(
            Notification(
                id = "1",
                title = "Flash Flood Warning",
                message = "Heavy rainfall could cause flash flooding in your area. Stay alert and avoid low-lying areas.",
                timestamp = Date(System.currentTimeMillis() - 3_600_000), // 1 hour ago
                type = NotificationType.EMERGENCY,
                read = false,
                disasterId = "flood123"
            ),
            Notification(
                id = "2",
                title = "Earthquake Reported",
                message = "A magnitude 5.5 earthquake has been reported near your location. Check for damage and stay away from damaged buildings.",
                timestamp = Date(System.currentTimeMillis() - 86_400_000), // 1 day ago
                type = NotificationType.DISASTER_ALERT,
                read = true,
                disasterId = "quake456"
            ),
            Notification(
                id = "3",
                title = "Profile Updated",
                message = "Your emergency contact information has been successfully updated.",
                timestamp = Date(System.currentTimeMillis() - 259_200_000), // 3 days ago
                type = NotificationType.SYSTEM,
                read = true
            ),
            Notification(
                id = "4",
                title = "New Safety Tips",
                message = "Check out our new safety tips for earthquake preparedness.",
                timestamp = Date(System.currentTimeMillis() - 604_800_000), // 1 week ago
                type = NotificationType.INFO,
                read = true
            )
        )
    }

    var showUnreadOnly by remember { mutableStateOf(false) }

    val filteredNotifications = remember(notifications, showUnreadOnly) {
        if (showUnreadOnly) {
            notifications.filter { !it.read }
        } else {
            notifications
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showUnreadOnly = !showUnreadOnly }) {
                        Icon(
                            imageVector = if (showUnreadOnly) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = if (showUnreadOnly) "Show all" else "Show unread only"
                        )
                    }

                    IconButton(onClick = { /* Mark all as read */ }) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = "Mark all as read"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredNotifications.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (showUnreadOnly) {
                            "No unread notifications"
                        } else {
                            "No notifications yet"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (showUnreadOnly) {
                            "You've read all your notifications"
                        } else {
                            "When you receive notifications, they will appear here"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                // List of notifications
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = { onNotificationClick(notification) }
                        )
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Red
                )
            }
        }
    }
}

/**
 * Individual notification item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (notification.type) {
        NotificationType.EMERGENCY -> Pair(Error, Icons.Default.Warning)
        NotificationType.DISASTER_ALERT -> Pair(Warning, Icons.Default.Notifications)
        NotificationType.SYSTEM -> Pair(Info, Icons.Default.Info)
        NotificationType.INFO -> Pair(MaterialTheme.colorScheme.secondary, Icons.Default.Lightbulb)
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.read) {
                color.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Read/unread indicator and type icon
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )

                if (!notification.read) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd),
                        containerColor = Red
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Notification content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = getFormattedTime(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Format a timestamp for display
 */
fun getFormattedTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        else -> {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            "$month $day"
        }
    }
}