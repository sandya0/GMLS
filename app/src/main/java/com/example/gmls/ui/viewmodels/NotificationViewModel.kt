package com.example.gmls.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.R
import com.example.gmls.domain.model.Notification
import com.example.gmls.domain.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val showUnreadOnly: Boolean = false,
    val error: String? = null,
    val isRealTimeEnabled: Boolean = false,
    val lastSyncTime: Long = 0L
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "NotificationViewModel"
        private const val NOTIFICATIONS_COLLECTION = "notifications"
        private const val USER_NOTIFICATIONS_COLLECTION = "user_notifications"
        private const val MAX_NOTIFICATIONS = 100
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    // Real-time listeners
    private var userNotificationsListener: ListenerRegistration? = null
    private var globalNotificationsListener: ListenerRegistration? = null

    init {
        Log.d(TAG, "NotificationViewModel initialized")
        viewModelScope.launch {
            try {
                // Check auth state and start loading notifications
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    Log.d(TAG, "User authenticated, loading notifications")
                    startRealTimeNotifications()
                } else {
                    Log.w(TAG, "No authenticated user, creating empty state")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "User tidak terautentikasi"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in NotificationViewModel init", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Gagal menginisialisasi notifikasi: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Start real-time notification listeners
     */
    fun startRealTimeNotifications() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Log.w(TAG, "No authenticated user for real-time notifications")
                    return@launch
                }

                val userId = currentUser.uid
                Log.d(TAG, "Starting real-time notifications for user: $userId")
                
                _state.update { it.copy(isLoading = true, error = null) }

                // First verify user exists
                val userDoc = firestore.collection("users").document(userId).get().await()
                if (!userDoc.exists()) {
                    Log.w(TAG, "User document doesn't exist, creating empty notification state")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            notifications = emptyList(),
                            unreadCount = 0,
                            isRealTimeEnabled = false,
                            error = "Dokumen pengguna tidak ditemukan"
                        )
                    }
                    return@launch
                }

                // Set up real-time listener for user-specific notifications
                setupUserNotificationsListener(userId)
                
                // Set up real-time listener for global/broadcast notifications
                setupGlobalNotificationsListener()

                _state.update { 
                    it.copy(
                        isRealTimeEnabled = true,
                        lastSyncTime = System.currentTimeMillis()
                    )
                }

                Log.d(TAG, "Real-time notifications started successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error starting real-time notifications", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Gagal memulai notifikasi real-time: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Set up real-time listener for user-specific notifications
     */
    private fun setupUserNotificationsListener(userId: String) {
        userNotificationsListener?.remove()
        
        userNotificationsListener = firestore
            .collection(USER_NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(MAX_NOTIFICATIONS.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user notifications", error)
                    _state.update { it.copy(error = "Error dengan notifikasi pengguna: ${error.message}") }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val userNotifications = snapshot.documents.mapNotNull { doc ->
                        try {
                            Notification(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = doc.getDate("timestamp")?.time ?: System.currentTimeMillis(),
                                type = NotificationType.valueOf(doc.getString("type") ?: "INFO"),
                                read = doc.getBoolean("read") ?: false,
                                userId = doc.getString("userId") ?: "",
                                data = (doc.getString("disasterId")?.let { mapOf("disasterId" to it) } ?: emptyMap())
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing user notification document: ${doc.id}", e)
                            null
                        }
                    }
                    
                    // Merge with global notifications and update state
                    mergeAndUpdateNotifications(userNotifications = userNotifications)
                    Log.d(TAG, "Updated ${userNotifications.size} user notifications from real-time listener")
                }
            }
    }

    /**
     * Set up real-time listener for global/broadcast notifications
     */
    private fun setupGlobalNotificationsListener() {
        globalNotificationsListener?.remove()
        
        // Listen for global notifications from the last 30 days
        val thirtyDaysAgo = Date(System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000))
        
        globalNotificationsListener = firestore
            .collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("type", "BROADCAST")
            .whereGreaterThan("timestamp", thirtyDaysAgo)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to global notifications", error)
                    _state.update { it.copy(error = "Error dengan notifikasi global: ${error.message}") }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val globalNotifications = snapshot.documents.mapNotNull { doc ->
                        try {
                            Notification(
                                id = "global_${doc.id}",
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = doc.getDate("timestamp")?.time ?: System.currentTimeMillis(),
                                type = NotificationType.valueOf(doc.getString("notificationType") ?: "INFO"),
                                read = false, // Global notifications start as unread
                                userId = "",
                                data = (doc.getString("disasterId")?.let { mapOf("disasterId" to it) } ?: emptyMap())
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing global notification document: ${doc.id}", e)
                            null
                        }
                    }
                    
                    // Merge with user notifications and update state
                    mergeAndUpdateNotifications(globalNotifications = globalNotifications)
                    Log.d(TAG, "Updated ${globalNotifications.size} global notifications from real-time listener")
                }
            }
    }

    /**
     * Merge user and global notifications and update state
     */
    private fun mergeAndUpdateNotifications(
        userNotifications: List<Notification>? = null,
        globalNotifications: List<Notification>? = null
    ) {
        val currentState = _state.value
        val currentUserNotifications = if (userNotifications != null) {
            userNotifications
        } else {
            currentState.notifications.filter { !it.id.startsWith("global_") }
        }
        
        val currentGlobalNotifications = if (globalNotifications != null) {
            globalNotifications
        } else {
            currentState.notifications.filter { it.id.startsWith("global_") }
        }

        val allNotifications = (currentUserNotifications + currentGlobalNotifications)
            .sortedByDescending { it.timestamp }
            .take(MAX_NOTIFICATIONS)

        _state.update { 
            it.copy(
                notifications = allNotifications,
                unreadCount = allNotifications.count { notification -> !notification.read },
                isLoading = false,
                error = null,
                lastSyncTime = System.currentTimeMillis()
            )
        }
    }

    /**
     * Stop real-time listeners
     */
    fun stopRealTimeNotifications() {
        userNotificationsListener?.remove()
        globalNotificationsListener?.remove()
        userNotificationsListener = null
        globalNotificationsListener = null
        
        _state.update { it.copy(isRealTimeEnabled = false) }
        Log.d(TAG, "Real-time notifications stopped")
    }

    fun toggleUnreadFilter() {
        _state.update { it.copy(showUnreadOnly = !it.showUnreadOnly) }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null && !notificationId.startsWith("global_")) {
                    // Update in Firestore for user-specific notifications
                    firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                        .document(notificationId)
                        .update("read", true)
                        .await()
                    Log.d(TAG, "Marked notification $notificationId as read in Firestore")
                }
                
                // Update local state immediately for better UX
                val currentNotifications = _state.value.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(read = true)
                    } else {
                        notification
                    }
                }
                _state.update { 
                    it.copy(
                        notifications = currentNotifications,
                        unreadCount = currentNotifications.count { !it.read }
                    )
                }
                Log.d(TAG, "Marked notification $notificationId as read locally")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
                _state.update { it.copy(error = "Gagal menandai notifikasi sebagai dibaca: ${e.message}") }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Update all unread user notifications in Firestore
                    val unreadUserNotifications = _state.value.notifications.filter { 
                        !it.read && !it.id.startsWith("global_") 
                    }
                    
                    for (notification in unreadUserNotifications) {
                        firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                            .document(notification.id)
                            .update("read", true)
                            .await()
                    }
                    Log.d(TAG, "Marked ${unreadUserNotifications.size} user notifications as read in Firestore")
                }
                
                // Update local state
                val currentNotifications = _state.value.notifications.map { it.copy(read = true) }
                _state.update { 
                    it.copy(
                        notifications = currentNotifications,
                        unreadCount = 0
                    )
                }
                Log.d(TAG, "Marked all notifications as read locally")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking all notifications as read", e)
                _state.update { it.copy(error = "Gagal menandai semua notifikasi sebagai dibaca: ${e.message}") }
            }
        }
    }
    
    fun refreshNotifications() {
        Log.d(TAG, "Refreshing notifications")
        stopRealTimeNotifications()
        startRealTimeNotifications()
    }
    
    /**
     * Create a new notification (admin function)
     */
    fun createNotification(
        title: String,
        message: String,
        type: NotificationType,
        disasterId: String? = null,
        targetUserId: String? = null
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                
                val notificationData = hashMapOf(
                    "title" to title,
                    "message" to message,
                    "type" to type.name,
                    "timestamp" to Date(),
                    "read" to false,
                    "createdBy" to currentUser.uid
                )
                
                if (disasterId != null) {
                    notificationData["disasterId"] = disasterId
                }
                
                if (targetUserId != null) {
                    // Create user-specific notification
                    notificationData["userId"] = targetUserId
                    firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                        .add(notificationData)
                        .await()
                    Log.d(TAG, "Created user-specific notification for $targetUserId")
                } else {
                    // Create broadcast notification
                    notificationData["type"] = "BROADCAST"
                    notificationData["notificationType"] = type.name
                    firestore.collection(NOTIFICATIONS_COLLECTION)
                        .add(notificationData)
                        .await()
                    Log.d(TAG, "Created broadcast notification")
                }
                
                Log.d(TAG, "Notification created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification", e)
                _state.update { it.copy(error = "Gagal membuat notifikasi: ${e.message}") }
            }
        }
    }

    /**
     * Delete a notification (user can delete their own notifications)
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null && !notificationId.startsWith("global_")) {
                    // Delete from Firestore
                    firestore.collection(USER_NOTIFICATIONS_COLLECTION)
                        .document(notificationId)
                        .delete()
                        .await()
                    Log.d(TAG, "Deleted notification $notificationId from Firestore")
                }
                
                // Update local state
                val currentNotifications = _state.value.notifications.filter { it.id != notificationId }
                _state.update { 
                    it.copy(
                        notifications = currentNotifications,
                        unreadCount = currentNotifications.count { !it.read }
                    )
                }
                Log.d(TAG, "Deleted notification $notificationId locally")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notification", e)
                _state.update { it.copy(error = "Gagal menghapus notifikasi: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopRealTimeNotifications()
        Log.d(TAG, "NotificationViewModel cleared")
    }
} 
