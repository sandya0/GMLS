package com.example.gmls.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gmls.MainActivity
import com.example.gmls.R
import com.example.gmls.domain.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service for handling Firebase Cloud Messaging (Push Notifications)
 */
@AndroidEntryPoint
class DisasterMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Called when a new token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Save the new token to the server
        scope.launch {
            userRepository.saveFCMToken(token)
        }
    }

    /**
     * Called when a message is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if the message contains a notification
        remoteMessage.notification?.let { notification ->
            // Handle notification message
            sendNotification(
                title = notification.title ?: getString(R.string.app_name),
                message = notification.body ?: "",
                data = remoteMessage.data
            )
        } ?: run {
            // Handle data message
            if (remoteMessage.data.isNotEmpty()) {
                val title = remoteMessage.data["title"] ?: getString(R.string.app_name)
                val message = remoteMessage.data["message"] ?: ""

                sendNotification(title, message, remoteMessage.data)
            }
        }
    }

    /**
     * Create and show a notification
     */
    private fun sendNotification(title: String, message: String, data: Map<String, String>) {
        // Create an intent to open the app
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Add data from the notification
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        // Create a pending intent
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Determine notification channel ID based on notification type
        val channelId = when (data["type"]) {
            "emergency" -> EMERGENCY_CHANNEL_ID
            "disaster" -> DISASTER_CHANNEL_ID
            else -> DEFAULT_CHANNEL_ID
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(getNotificationPriority(data["type"]))
            .setContentIntent(pendingIntent)

        // Get the notification manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channels for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels(notificationManager)
        }

        // Show the notification
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Create notification channels for Android O and above
     */
    private fun createNotificationChannels(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Emergency channel (high importance)
            val emergencyChannel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical emergency alerts that require immediate attention"
                enableLights(true)
                enableVibration(true)
            }

            // Disaster channel (high importance)
            val disasterChannel = NotificationChannel(
                DISASTER_CHANNEL_ID,
                "Disaster Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important alerts about disasters in your area"
                enableLights(true)
                enableVibration(true)
            }

            // Default channel (default importance)
            val defaultChannel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            // Register the channels with the system
            notificationManager.createNotificationChannels(
                listOf(emergencyChannel, disasterChannel, defaultChannel)
            )
        }
    }

    /**
     * Get notification priority based on notification type
     */
    private fun getNotificationPriority(type: String?): Int {
        return when (type) {
            "emergency" -> NotificationCompat.PRIORITY_MAX
            "disaster" -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    companion object {
        private const val EMERGENCY_CHANNEL_ID = "emergency_channel"
        private const val DISASTER_CHANNEL_ID = "disaster_channel"
        private const val DEFAULT_CHANNEL_ID = "default_channel"
    }
}