package com.example.gmls.domain.model

import java.util.Date

/**
 * Notification data class for the GMLS application
 */
data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val userId: String = "",
    val data: Map<String, Any> = emptyMap(),
    val priority: NotificationPriority = NotificationPriority.NORMAL
) {
    val date: Date
        get() = Date(timestamp)
}

/**
 * Notification types for categorizing different kinds of notifications
 */
enum class NotificationType(val displayName: String) {
    DISASTER_ALERT("Peringatan Bencana"),
    SYSTEM_UPDATE("Pembaruan Sistem"),
    USER_ACTION("Aksi Pengguna"),
    EMERGENCY("Darurat"),
    INFO("Informasi"),
    WARNING("Peringatan"),
    SUCCESS("Berhasil"),
    ERROR("Kesalahan")
}

/**
 * Priority levels for notifications
 */
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
} 