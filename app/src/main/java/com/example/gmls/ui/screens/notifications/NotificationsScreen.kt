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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gmls.R
import com.example.gmls.ui.components.EmergencyAlertCard
import com.example.gmls.ui.components.EmergencySeverity
import com.example.gmls.ui.theme.Error
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Warning
import com.example.gmls.ui.viewmodels.NotificationViewModel
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.example.gmls.domain.model.Notification as DomainNotification
import com.example.gmls.domain.model.NotificationType

/**
 * Data class representing a notification in UI
 */
data class UINotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    val type: UINotificationType,
    val read: Boolean,
    val disasterId: String? = null
)

/**
 * Enum representing different notification types in UI
 */
enum class UINotificationType {
    EMERGENCY, DISASTER_ALERT, SYSTEM, INFO
}

/**
 * Screen for displaying user notifications
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (UINotification) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val filteredNotifications = remember(state.notifications, state.showUnreadOnly) {
        val uiNotifications = state.notifications.map { it.toUINotification() }
        if (state.showUnreadOnly) {
            uiNotifications.filter { !it.read }
        } else {
            uiNotifications
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.semantics { contentDescription = "Kembali" }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleUnreadFilter() },
                        modifier = Modifier.semantics {
                            contentDescription = if (state.showUnreadOnly) "Tampilkan semua notifikasi" else "Tampilkan hanya notifikasi belum dibaca"
                        }
                    ) {
                        Icon(
                            imageVector = if (state.showUnreadOnly) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = { viewModel.markAllAsRead() },
                                                    modifier = Modifier.semantics { contentDescription = "Tandai semua sebagai sudah dibaca" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null
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
            when {
                state.error != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = state.error ?: stringResource(R.string.an_error_occurred),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.clearError() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                state.isLoading -> {
                    // Loading state
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Red
                    )
                }
                filteredNotifications.isEmpty() -> {
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
                            text = if (state.showUnreadOnly) {
                                stringResource(R.string.no_unread_notifications)
                            } else {
                                stringResource(R.string.no_notifications_yet)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (state.showUnreadOnly) {
                                stringResource(R.string.all_notifications_read)
                            } else {
                                stringResource(R.string.notifications_will_appear_here)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    // List of notifications
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredNotifications,
                            key = { notification -> notification.id }
                        ) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { onNotificationClick(notification) }
                            )
                        }
                    }
                }
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
    notification: UINotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (notification.type) {
        UINotificationType.EMERGENCY -> Pair(Error, Icons.Default.Warning)
        UINotificationType.DISASTER_ALERT -> Pair(Warning, Icons.Default.Notifications)
        UINotificationType.SYSTEM -> Pair(Info, Icons.Default.Info)
        UINotificationType.INFO -> Pair(MaterialTheme.colorScheme.secondary, Icons.Default.Lightbulb)
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Notifikasi: ${notification.title}. ${notification.message}" },
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
                    contentDescription = notification.type.name,
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
@Composable
fun getFormattedTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time

    return when {
        diff < 60 * 1000 -> stringResource(R.string.just_now)
        diff < 60 * 60 * 1000 -> {
            val minutes = (diff / (60 * 1000)).toInt()
            stringResource(R.string.minutes_ago, minutes)
        }
        diff < 24 * 60 * 60 * 1000 -> {
            val hours = (diff / (60 * 60 * 1000)).toInt()
            stringResource(R.string.hours_ago, hours)
        }
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            val days = (diff / (24 * 60 * 60 * 1000)).toInt()
            stringResource(R.string.days_ago, days)
        }
        else -> {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale("id", "ID"))
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            "$month $day"
        }
    }
}

fun DomainNotification.toUINotification(): UINotification {
    return UINotification(
        id = id,
        title = title,
        message = message,
        timestamp = java.util.Date(timestamp),
        type = type.toUINotificationType(),
        read = read,
        disasterId = data["disasterId"] as? String
    )
}

fun NotificationType.toUINotificationType(): UINotificationType = when (this) {
    NotificationType.EMERGENCY -> UINotificationType.EMERGENCY
    NotificationType.DISASTER_ALERT -> UINotificationType.DISASTER_ALERT
    NotificationType.SYSTEM_UPDATE, NotificationType.USER_ACTION -> UINotificationType.SYSTEM
    NotificationType.INFO, NotificationType.WARNING, NotificationType.SUCCESS, NotificationType.ERROR -> UINotificationType.INFO
}
