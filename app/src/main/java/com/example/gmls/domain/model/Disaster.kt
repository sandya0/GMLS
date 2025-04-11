package com.example.gmls.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Domain model representing a disaster event
 */
data class Disaster(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val type: DisasterType,
    val timestamp: Long,
    val affectedCount: Int,
    val images: List<String>,
    val status: Status,
    val latitude: Double,
    val longitude: Double,
    val reportedBy: String,
    val updatedAt: Long = timestamp
) {
    /**
     * Possible status values for a disaster report
     */
    enum class Status {
        REPORTED,    // Initial status when a disaster is first reported
        VERIFIED,    // Confirmed by authorities or multiple reports
        IN_PROGRESS, // Response teams are actively working
        RESOLVED     // Situation has been resolved
    }

    /**
     * Returns a human-readable relative timestamp (e.g., "2 hours ago")
     */
    val formattedTimestamp: String
        get() {
            val now = System.currentTimeMillis()
            val diffInMillis = now - timestamp

            return when {
                diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diffInMillis < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diffInMillis)} minutes ago"
                diffInMillis < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diffInMillis)} hours ago"
                diffInMillis < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diffInMillis)} days ago"
                else -> {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }

    /**
     * Returns a full formatted timestamp
     */
    val fullFormattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    /**
     * Returns a severity level based on affected count and disaster type
     */
    val severityLevel: SeverityLevel
        get() = when {
            affectedCount > 1000 -> SeverityLevel.CRITICAL
            affectedCount > 500 -> SeverityLevel.HIGH
            affectedCount > 100 -> SeverityLevel.MEDIUM
            else -> SeverityLevel.LOW
        }
}

/**
 * Severity levels for disasters
 */
enum class SeverityLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Firebase data mapper for Disaster objects
 */
class DisasterFirebaseMapper {
    fun mapToDisaster(id: String, data: Map<String, Any>): Disaster {
        return Disaster(
            id = id,
            title = data["title"] as String,
            description = data["description"] as String,
            location = data["location"] as String,
            type = DisasterType.valueOf(data["type"] as String),
            timestamp = data["timestamp"] as Long,
            affectedCount = (data["affectedCount"] as Number).toInt(),
            images = (data["images"] as? List<String>) ?: emptyList(),
            status = Disaster.Status.valueOf(data["status"] as String),
            latitude = (data["latitude"] as Number).toDouble(),
            longitude = (data["longitude"] as Number).toDouble(),
            reportedBy = data["reportedBy"] as String,
            updatedAt = (data["updatedAt"] as? Long) ?: (data["timestamp"] as Long)
        )
    }

    fun mapToFirebaseObject(disaster: Disaster): Map<String, Any> {
        return mapOf(
            "title" to disaster.title,
            "description" to disaster.description,
            "location" to disaster.location,
            "type" to disaster.type.name,
            "timestamp" to disaster.timestamp,
            "affectedCount" to disaster.affectedCount,
            "images" to disaster.images,
            "status" to disaster.status.name,
            "latitude" to disaster.latitude,
            "longitude" to disaster.longitude,
            "reportedBy" to disaster.reportedBy,
            "updatedAt" to System.currentTimeMillis()
        )
    }
}