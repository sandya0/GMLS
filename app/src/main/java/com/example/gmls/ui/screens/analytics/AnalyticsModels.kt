package com.example.gmls.ui.screens.analytics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.gmls.ui.theme.*
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.displayName
import com.example.gmls.domain.model.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * Analytics State - Main state holder for analytics data based on actual GMLS data
 */
data class AnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Overview Analytics - Based on actual disaster data
    val totalDisasters: Int = 0,
    val activeIncidents: Int = 0, // Disasters with status REPORTED, VERIFIED, IN_PROGRESS
    val resolvedIncidents: Int = 0, // Disasters with status RESOLVED
    val recentActivities: List<RecentActivity> = emptyList(),
    val disasterTypeDistribution: List<DisasterTypeData> = emptyList(),
    
    // Geographic Analytics - Based on actual location data
    val locationStats: List<LocationStat> = emptyList(),
    
    // Trends Analytics - Based on actual timestamps
    val monthlyTrends: List<MonthlyTrend> = emptyList(),
    val statusDistribution: List<StatusData> = emptyList(),
    
    // User Analytics - Based on actual user data
    val totalUsers: Int = 0,
    val verifiedUsers: Int = 0,
    val adminUsers: Int = 0,
    val usersWithLocation: Int = 0,
    
    // Severity Analytics - Based on affected count
    val severityDistribution: List<SeverityData> = emptyList(),
    val avgAffectedCount: Int = 0,
    val totalAffectedPeople: Int = 0
)

/**
 * Recent Activity Data - Based on actual disaster reports
 */
data class RecentActivity(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val color: Color,
    val type: String // "disaster_reported", "status_updated", etc.
)

/**
 * Disaster Type Distribution Data - Based on actual DisasterType enum
 */
data class DisasterTypeData(
    val type: String,
    val count: Int,
    val percentage: Int,
    val color: Color
)

/**
 * Location Statistics Data - Based on actual disaster locations
 */
data class LocationStat(
    val location: String,
    val disasterCount: Int,
    val lastIncident: String,
    val mostCommonType: String
)

/**
 * Monthly Trend Data - Based on actual disaster timestamps
 */
data class MonthlyTrend(
    val month: String,
    val count: Int,
    val year: Int
)

/**
 * Status Distribution Data - Based on actual Disaster.Status enum
 */
data class StatusData(
    val status: String,
    val count: Int,
    val percentage: Int,
    val color: Color
)

/**
 * Severity Distribution Data - Based on actual affected count
 */
data class SeverityData(
    val level: String,
    val count: Int,
    val percentage: Int,
    val color: Color,
    val description: String
)

/**
 * Analytics Data Provider - Generates analytics from actual GMLS data
 */
object AnalyticsDataProvider {
    
    fun generateAnalyticsState(disasters: List<Disaster>, users: List<User>): AnalyticsState {
        return AnalyticsState(
            totalDisasters = disasters.size,
            activeIncidents = disasters.count { it.status != Disaster.Status.RESOLVED },
            resolvedIncidents = disasters.count { it.status == Disaster.Status.RESOLVED },
            recentActivities = generateRecentActivities(disasters),
            disasterTypeDistribution = generateDisasterTypeDistribution(disasters),
            locationStats = generateLocationStats(disasters),
            monthlyTrends = generateMonthlyTrends(disasters),
            statusDistribution = generateStatusDistribution(disasters),
            totalUsers = users.size,
            verifiedUsers = users.count { it.isVerified },
            adminUsers = users.count { it.role == "admin" },
            usersWithLocation = users.count { it.latitude != null && it.longitude != null },
            severityDistribution = generateSeverityDistribution(disasters),
            avgAffectedCount = if (disasters.isNotEmpty()) disasters.map { it.affectedCount }.average().toInt() else 0,
            totalAffectedPeople = disasters.sumOf { it.affectedCount }
        )
    }
    
    private fun generateRecentActivities(disasters: List<Disaster>): List<RecentActivity> {
        return disasters
            .sortedByDescending { it.timestamp }
            .take(10)
            .map { disaster ->
                RecentActivity(
                    id = disaster.id,
                    title = when (disaster.status) {
                        Disaster.Status.REPORTED -> "Bencana Baru Dilaporkan"
                        Disaster.Status.VERIFIED -> "Bencana Terverifikasi"
                        Disaster.Status.IN_PROGRESS -> "Respons Sedang Berlangsung"
                        Disaster.Status.RESOLVED -> "Bencana Teratasi"
                    },
                    description = "${when (disaster.type) {
                        DisasterType.EARTHQUAKE -> "Gempa Bumi"
                        DisasterType.FLOOD -> "Banjir"
                        DisasterType.WILDFIRE -> "Kebakaran Hutan"
                        DisasterType.LANDSLIDE -> "Tanah Longsor"
                        DisasterType.VOLCANO -> "Gunung Berapi"
                        DisasterType.TSUNAMI -> "Tsunami"
                        DisasterType.HURRICANE -> "Badai"
                        DisasterType.TORNADO -> "Tornado"
                        DisasterType.OTHER -> "Lainnya"
                    }} di ${disaster.location}",
                    time = disaster.formattedTimestamp,
                    icon = when (disaster.status) {
                        Disaster.Status.REPORTED -> Icons.Default.Report
                        Disaster.Status.VERIFIED -> Icons.Default.Verified
                        Disaster.Status.IN_PROGRESS -> Icons.Default.Engineering
                        Disaster.Status.RESOLVED -> Icons.Default.CheckCircle
                    },
                    color = when (disaster.status) {
                        Disaster.Status.REPORTED -> Warning
                        Disaster.Status.VERIFIED -> AccentBlue
                        Disaster.Status.IN_PROGRESS -> Red
                        Disaster.Status.RESOLVED -> Success
                    },
                    type = "disaster_${disaster.status.name.lowercase()}"
                )
            }
    }
    
    private fun generateDisasterTypeDistribution(disasters: List<Disaster>): List<DisasterTypeData> {
        val typeGroups = disasters.groupBy { it.type }
        val total = disasters.size
        
        return typeGroups.map { (type, disasterList) ->
            val count = disasterList.size
            val percentage = if (total > 0) (count * 100) / total else 0
            
            DisasterTypeData(
                type = when (type) {
                    DisasterType.EARTHQUAKE -> "Gempa Bumi"
                    DisasterType.FLOOD -> "Banjir"
                    DisasterType.WILDFIRE -> "Kebakaran Hutan"
                    DisasterType.LANDSLIDE -> "Tanah Longsor"
                    DisasterType.VOLCANO -> "Gunung Berapi"
                    DisasterType.TSUNAMI -> "Tsunami"
                    DisasterType.HURRICANE -> "Badai"
                    DisasterType.TORNADO -> "Tornado"
                    DisasterType.OTHER -> "Lainnya"
                },
                count = count,
                percentage = percentage,
                color = DisasterType.getColorForType(type)
            )
        }.sortedByDescending { it.count }
    }
    
    private fun generateLocationStats(disasters: List<Disaster>): List<LocationStat> {
        val locationGroups = disasters.groupBy { it.location }
        
        return locationGroups.map { (location, disasterList) ->
            val mostCommonType = disasterList
                .groupBy { it.type }
                .maxByOrNull { it.value.size }
                ?.key?.let { type ->
                    when (type) {
                        DisasterType.EARTHQUAKE -> "Gempa Bumi"
                        DisasterType.FLOOD -> "Banjir"
                        DisasterType.WILDFIRE -> "Kebakaran Hutan"
                        DisasterType.LANDSLIDE -> "Tanah Longsor"
                        DisasterType.VOLCANO -> "Gunung Berapi"
                        DisasterType.TSUNAMI -> "Tsunami"
                        DisasterType.HURRICANE -> "Badai"
                        DisasterType.TORNADO -> "Tornado"
                        DisasterType.OTHER -> "Lainnya"
                    }
                } ?: "Tidak Diketahui"
            
            val lastIncident = disasterList
                .maxByOrNull { it.timestamp }
                ?.formattedTimestamp ?: "Tidak Diketahui"
            
            LocationStat(
                location = location,
                disasterCount = disasterList.size,
                lastIncident = lastIncident,
                mostCommonType = mostCommonType
            )
        }.sortedByDescending { it.disasterCount }
    }
    
    private fun generateMonthlyTrends(disasters: List<Disaster>): List<MonthlyTrend> {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        
        val monthGroups = disasters.groupBy { 
            dateFormat.format(Date(it.timestamp))
        }
        
        return monthGroups.map { (monthYear, disasterList) ->
            val date = Date(disasterList.first().timestamp)
            MonthlyTrend(
                month = monthFormat.format(date),
                count = disasterList.size,
                year = yearFormat.format(date).toInt()
            )
        }.sortedBy { "${it.year}-${it.month}" }
    }
    
    private fun generateStatusDistribution(disasters: List<Disaster>): List<StatusData> {
        val statusGroups = disasters.groupBy { it.status }
        val total = disasters.size
        
        return statusGroups.map { (status, disasterList) ->
            val count = disasterList.size
            val percentage = if (total > 0) (count * 100) / total else 0
            
            StatusData(
                status = when (status) {
                    Disaster.Status.REPORTED -> "Dilaporkan"
                    Disaster.Status.VERIFIED -> "Terverifikasi"
                    Disaster.Status.IN_PROGRESS -> "Sedang Berlangsung"
                    Disaster.Status.RESOLVED -> "Teratasi"
                }.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                count = count,
                percentage = percentage,
                color = when (status) {
                    Disaster.Status.REPORTED -> Warning
                    Disaster.Status.VERIFIED -> AccentBlue
                    Disaster.Status.IN_PROGRESS -> Red
                    Disaster.Status.RESOLVED -> Success
                }
            )
        }.sortedByDescending { it.count }
    }
    
    private fun generateSeverityDistribution(disasters: List<Disaster>): List<SeverityData> {
        val severityGroups = disasters.groupBy { disaster ->
            when {
                disaster.affectedCount > 1000 -> "Kritis"
                disaster.affectedCount > 500 -> "Tinggi"
                disaster.affectedCount > 100 -> "Sedang"
                else -> "Rendah"
            }
        }
        val total = disasters.size
        
        return severityGroups.map { (level, disasterList) ->
            val count = disasterList.size
            val percentage = if (total > 0) (count * 100) / total else 0
            
            SeverityData(
                level = level,
                count = count,
                percentage = percentage,
                color = when (level) {
                    "Kritis" -> Red
                    "Tinggi" -> Warning
                    "Sedang" -> AccentBlue
                    else -> Success
                },
                description = when (level) {
                    "Kritis" -> "1000+ orang terdampak"
                    "Tinggi" -> "500-1000 orang terdampak"
                    "Sedang" -> "100-500 orang terdampak"
                    else -> "Kurang dari 100 orang terdampak"
                }
            )
        }.sortedByDescending { it.count }
    }
} 
