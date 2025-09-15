package com.example.gmls.ui.viewmodels

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.ui.screens.analytics.AnalyticsState
import com.example.gmls.ui.screens.analytics.AnalyticsDataProvider
import com.example.gmls.ui.screens.analytics.RecentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import javax.inject.Inject
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Handler
import android.os.Looper
import android.content.Context
import android.os.Environment
import android.app.Application
import android.widget.Toast
import androidx.core.content.FileProvider
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import com.example.gmls.domain.repository.DisasterRepository
import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.displayName
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.gmls.ui.theme.*
import com.example.gmls.R

/**
 * ViewModel for managing analytics data and operations using real GMLS data
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val disasterRepository: DisasterRepository,
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _analyticsState = MutableStateFlow(AnalyticsState(isLoading = true))
    val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()

    init {
        loadAnalyticsData()
    }

    /**
     * Load analytics data from repositories
     */
    fun loadAnalyticsData() {
        viewModelScope.launch {
            try {
                _analyticsState.value = _analyticsState.value.copy(isLoading = true, error = null)
                
                Log.d("AnalyticsViewModel", "Loading analytics data from repositories")
                
                // Load disasters and users from repositories
                val disastersResult = disasterRepository.getAllDisasters()
                val users = try {
                    userRepository.getAllUsers()
                } catch (e: Exception) {
                    Log.w("AnalyticsViewModel", "Could not load users: ${e.message}")
                    emptyList()
                }
                
                if (disastersResult.isSuccess) {
                    val disasters = disastersResult.getOrDefault(emptyList())
                    
                    // Generate analytics from real data
                    val analyticsData = AnalyticsDataProvider.generateAnalyticsState(disasters, users)
                    
                    _analyticsState.value = analyticsData.copy(isLoading = false)
                    
                    Log.d("AnalyticsViewModel", "Analytics data loaded successfully: ${disasters.size} disasters, ${users.size} users")
                } else {
                    throw disastersResult.exceptionOrNull() ?: Exception("Gagal memuat bencana")
                }
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error loading analytics data", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal memuat data analitik: ${e.message}"
                )
            }
        }
    }

    /**
     * Refresh analytics data
     */
    fun refreshData() {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Refreshing analytics data")
                
                // Show loading state briefly
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                delay(500)
                
                // Reload data
                loadAnalyticsData()
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error refreshing data", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal menyegarkan data: ${e.message}"
                )
            }
        }
    }

    /**
     * Export analytics data to Excel file
     */
    fun exportData() {
        viewModelScope.launch {
            try {
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                
                val currentState = _analyticsState.value
                val excelData = generateExcelReport(currentState)
                val fileName = "GMLS_Analytics_Report_${System.currentTimeMillis()}.xlsx"
                
                val file = saveReportToFile(excelData, fileName)
                
                if (file != null) {
                    shareReportFile(file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    _analyticsState.value = _analyticsState.value.copy(
                        isLoading = false,
                        error = null
                    )
                } else {
                    // Fallback to CSV format for clipboard
                    val csvData = generateCSVReport(currentState)
                    copyReportToClipboard(csvData)
                    _analyticsState.value = _analyticsState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal mengekspor laporan: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Generate Excel report from analytics data
     */
    private fun generateExcelReport(state: AnalyticsState): String {
        // For now, generate enhanced CSV format that Excel can read
        // In a real implementation, you would use Apache POI library to create actual Excel files
        val csvBuilder = StringBuilder()
        
        // UTF-8 BOM for Excel compatibility
        csvBuilder.append("\uFEFF")
        
        // Header with better formatting
        csvBuilder.appendLine("GMLS Analytics Report - Generated ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        csvBuilder.appendLine()
        
        // Executive Summary
        csvBuilder.appendLine("RINGKASAN EKSEKUTIF")
        csvBuilder.appendLine("Metrik,Nilai,Status")
        csvBuilder.appendLine("Total Bencana,${state.totalDisasters},${if (state.totalDisasters > 50) "Tinggi" else "Normal"}")
        csvBuilder.appendLine("Insiden Aktif,${state.activeIncidents},${if (state.activeIncidents > 10) "Kritis" else "Terkendali"}")
        csvBuilder.appendLine("Kasus Teratasi,${state.resolvedIncidents},${if (state.resolvedIncidents > state.activeIncidents) "Baik" else "Perlu Perhatian"}")
        csvBuilder.appendLine("Total Pengguna,${state.totalUsers},${if (state.totalUsers > 100) "Berkembang" else "Membangun"}")
        csvBuilder.appendLine("Pengguna Terverifikasi,${state.verifiedUsers},${((state.verifiedUsers.toFloat() / state.totalUsers) * 100).toInt()}%")
        csvBuilder.appendLine("Pengguna Admin,${state.adminUsers},Aktif")
        csvBuilder.appendLine()
        
        // Disaster Analysis
        csvBuilder.appendLine("DISASTER TYPE ANALYSIS")
        csvBuilder.appendLine("Type,Count,Percentage,Trend")
        state.disasterTypeDistribution.forEach { typeData ->
            val trend = when {
                typeData.percentage > 25 -> "Frekuensi Tinggi"
                typeData.percentage > 15 -> "Frekuensi Sedang"
                else -> "Frekuensi Rendah"
            }
            csvBuilder.appendLine("${typeData.type},${typeData.count},${typeData.percentage}%,$trend")
        }
        csvBuilder.appendLine()
        
        // Geographic Impact
        csvBuilder.appendLine("GEOGRAPHIC IMPACT ANALYSIS")
        csvBuilder.appendLine("Location,Disaster Count,Most Common Type,Risk Level")
        state.locationStats.forEach { location ->
            val riskLevel = when {
                location.disasterCount > 10 -> "Risiko Tinggi"
                location.disasterCount > 5 -> "Risiko Sedang"
                else -> "Risiko Rendah"
            }
            csvBuilder.appendLine("${location.location},${location.disasterCount},${location.mostCommonType},$riskLevel")
        }
        csvBuilder.appendLine()
        
        // Monthly Trends Analysis
        csvBuilder.appendLine("MONTHLY TRENDS ANALYSIS")
        csvBuilder.appendLine("Month,Year,Incident Count,Growth Rate")
        state.monthlyTrends.forEachIndexed { index, trend ->
            val growthRate = if (index > 0) {
                val previousCount = state.monthlyTrends.getOrNull(index - 1)?.count ?: trend.count
                val growth = ((trend.count - previousCount).toFloat() / previousCount * 100).toInt()
                "${if (growth >= 0) "+" else ""}$growth%"
            } else {
                "N/A"
            }
            csvBuilder.appendLine("${trend.month},${trend.year},${trend.count},$growthRate")
        }
        csvBuilder.appendLine()
        
        // Recent Critical Activities
        csvBuilder.appendLine("RECENT CRITICAL ACTIVITIES")
        csvBuilder.appendLine("Date,Activity Type,Title,Description,Priority")
        state.recentActivities.take(20).forEach { activity ->
            val priority = when {
                activity.description.contains("critical", ignoreCase = true) -> "Kritis"
                activity.description.contains("urgent", ignoreCase = true) -> "Tinggi"
                else -> "Normal"
            }
            csvBuilder.appendLine("${activity.time},${activity.type},\"${activity.title}\",\"${activity.description}\",$priority")
        }
        csvBuilder.appendLine()
        
        // User Engagement Metrics
        csvBuilder.appendLine("USER ENGAGEMENT METRICS")
        csvBuilder.appendLine("Metric,Count,Percentage,Benchmark")
        val verificationRate = if (state.totalUsers > 0) (state.verifiedUsers.toFloat() / state.totalUsers * 100).toInt() else 0
        val adminRate = if (state.totalUsers > 0) (state.adminUsers.toFloat() / state.totalUsers * 100).toInt() else 0
        csvBuilder.appendLine("Verification Rate,$verificationRate%,${verificationRate}%,${if (verificationRate > 80) "Sangat Baik" else if (verificationRate > 60) "Baik" else "Perlu Perbaikan"}")
        csvBuilder.appendLine("Admin Coverage,$adminRate%,${adminRate}%,${if (adminRate > 5) "Memadai" else "Tidak Memadai"}")
        csvBuilder.appendLine("Users with Location,${state.usersWithLocation},${if (state.totalUsers > 0) (state.usersWithLocation.toFloat() / state.totalUsers * 100).toInt() else 0}%,Layanan Lokasi")
        
        return csvBuilder.toString()
    }
    
    /**
     * Generate CSV report from analytics data (fallback)
     */
    private fun generateCSVReport(state: AnalyticsState): String {
        val csvBuilder = StringBuilder()
        
        // Header
        csvBuilder.appendLine("GMLS Analytics Report")
        csvBuilder.appendLine("Generated on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        csvBuilder.appendLine()
        
        // Overview Statistics
        csvBuilder.appendLine("OVERVIEW STATISTICS")
        csvBuilder.appendLine("Metric,Value")
        csvBuilder.appendLine("Total Disasters,${state.totalDisasters}")
        csvBuilder.appendLine("Active Incidents,${state.activeIncidents}")
        csvBuilder.appendLine("Resolved Cases,${state.resolvedIncidents}")
        csvBuilder.appendLine("Total Users,${state.totalUsers}")
        csvBuilder.appendLine("Active Incidents,${state.activeIncidents}")
        csvBuilder.appendLine("Resolved Incidents,${state.resolvedIncidents}")
        csvBuilder.appendLine()
        
        // Disaster Types
        csvBuilder.appendLine("DISASTERS BY TYPE")
        csvBuilder.appendLine("Type,Count,Percentage")
        state.disasterTypeDistribution.forEach { typeData ->
            csvBuilder.appendLine("${typeData.type},${typeData.count},${typeData.percentage}%")
        }
        csvBuilder.appendLine()
        
        // Geographic Distribution
        csvBuilder.appendLine("GEOGRAPHIC DISTRIBUTION")
        csvBuilder.appendLine("Location,Disaster Count,Description")
        state.locationStats.forEach { location ->
            csvBuilder.appendLine("${location.location},${location.disasterCount},${location.mostCommonType}")
        }
        csvBuilder.appendLine()
        
        // Recent Activities
        csvBuilder.appendLine("RECENT ACTIVITIES")
        csvBuilder.appendLine("Date,Type,Title,Status,Description")
        state.recentActivities.forEach { activity ->
            csvBuilder.appendLine("${activity.time},${activity.type},\"${activity.title}\",${activity.description}")
        }
        csvBuilder.appendLine()
        
        // Trends Data
        csvBuilder.appendLine("MONTHLY TRENDS")
        csvBuilder.appendLine("Month,Disaster Count,Year")
        state.monthlyTrends.forEach { trend ->
            csvBuilder.appendLine("${trend.month},${trend.count},${trend.year}")
        }
        
        return csvBuilder.toString()
    }
    
    /**
     * Save report to external storage
     */
    private suspend fun saveReportToFile(reportData: String, fileName: String): File? {
        return try {
            val context = getApplication<Application>()
            
            // Try to save to Downloads directory first
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            
            if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                FileWriter(file).use { writer ->
                    writer.write(reportData)
                }
                file
            } else {
                // Fallback to internal storage
                val internalFile = File(context.filesDir, fileName)
                FileWriter(internalFile).use { writer ->
                    writer.write(reportData)
                }
                internalFile
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Share report file using Android share intent
     */
    private fun shareReportFile(file: File, mimeType: String = "text/csv") {
        try {
            val context = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Analitik GMLS")
                putExtra(Intent.EXTRA_TEXT, "Silakan temukan laporan analitik GMLS yang terlampir. Laporan komprehensif ini mencakup statistik bencana, metrik pengguna, dan data performa sistem.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Bagikan Laporan Analitik")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            val context = getApplication<Application>()
            Toast.makeText(context, context.getString(R.string.failed_to_share_file, e.message), Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Copy report data to clipboard as fallback
     */
    private fun copyReportToClipboard(csvData: String) {
        try {
            val context = getApplication<Application>()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(context.getString(R.string.analytics_report), csvData)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, context.getString(R.string.report_copied_to_clipboard), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.failed_to_copy_clipboard), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Filter analytics data by date range
     */
    fun filterByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Filtering data from $startDate to $endDate")
                
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                
                // Parse date strings
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startTime = dateFormat.parse(startDate)?.time ?: 0L
                val endTime = dateFormat.parse(endDate)?.time ?: Long.MAX_VALUE
                
                // Load disasters and users from repositories
                val disastersResult = disasterRepository.getAllDisasters()
                val users = try {
                    userRepository.getAllUsers()
                } catch (e: Exception) {
                    Log.w("AnalyticsViewModel", "Could not load users: ${e.message}")
                    emptyList()
                }
                
                if (disastersResult.isSuccess) {
                    val allDisasters = disastersResult.getOrDefault(emptyList())
                    
                    // Filter disasters by date range
                    val filteredDisasters = allDisasters.filter { disaster ->
                        disaster.timestamp in startTime..endTime
                    }
                    
                    // Generate analytics from filtered data
                    val analyticsData = AnalyticsDataProvider.generateAnalyticsState(filteredDisasters, users)
                    
                    _analyticsState.value = analyticsData.copy(isLoading = false)
                    
                    Log.d("AnalyticsViewModel", "Filtered ${filteredDisasters.size} disasters from ${allDisasters.size} total")
                } else {
                    throw disastersResult.exceptionOrNull() ?: Exception("Gagal memuat bencana")
                }
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error filtering data by date range", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal memfilter data berdasarkan rentang tanggal: ${e.message}"
                )
            }
        }
    }

    /**
     * Filter analytics data by disaster type
     */
    fun filterByDisasterType(disasterType: String) {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Filtering data for disaster type: $disasterType")
                
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                
                // Load disasters and users from repositories
                val disastersResult = disasterRepository.getAllDisasters()
                val users = try {
                    userRepository.getAllUsers()
                } catch (e: Exception) {
                    Log.w("AnalyticsViewModel", "Could not load users: ${e.message}")
                    emptyList()
                }
                
                if (disastersResult.isSuccess) {
                    val allDisasters = disastersResult.getOrDefault(emptyList())
                    
                    // Filter disasters by type (if not "Semua")
                    val filteredDisasters = if (disasterType == "Semua" || disasterType.isEmpty()) {
                        allDisasters
                    } else {
                        allDisasters.filter { disaster ->
                            (when (disaster.type) {
                                DisasterType.EARTHQUAKE -> "Gempa Bumi"
                                DisasterType.FLOOD -> "Banjir"
                                DisasterType.WILDFIRE -> "Kebakaran Hutan"
                                DisasterType.LANDSLIDE -> "Tanah Longsor"
                                DisasterType.VOLCANO -> "Gunung Berapi"
                                DisasterType.TSUNAMI -> "Tsunami"
                                DisasterType.HURRICANE -> "Badai"
                                DisasterType.TORNADO -> "Tornado"
                                DisasterType.OTHER -> "Lainnya"
                            }).equals(disasterType, ignoreCase = true) ||
                            disaster.type.name.equals(disasterType, ignoreCase = true)
                        }
                    }
                    
                    // Generate analytics from filtered data
                    val analyticsData = AnalyticsDataProvider.generateAnalyticsState(filteredDisasters, users)
                    
                    _analyticsState.value = analyticsData.copy(isLoading = false)
                    
                    Log.d("AnalyticsViewModel", "Filtered ${filteredDisasters.size} disasters of type '$disasterType' from ${allDisasters.size} total")
                } else {
                    throw disastersResult.exceptionOrNull() ?: Exception("Gagal memuat bencana")
                }
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error filtering by disaster type", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal memfilter berdasarkan jenis bencana: ${e.message}"
                )
            }
        }
    }

    /**
     * Filter analytics data by region
     */
    fun filterByRegion(region: String) {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Filtering data for region: $region")
                
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                
                // Load disasters and users from repositories
                val disastersResult = disasterRepository.getAllDisasters()
                val users = try {
                    userRepository.getAllUsers()
                } catch (e: Exception) {
                    Log.w("AnalyticsViewModel", "Could not load users: ${e.message}")
                    emptyList()
                }
                
                if (disastersResult.isSuccess) {
                    val allDisasters = disastersResult.getOrDefault(emptyList())
                    
                    // Filter disasters by region (if not "Semua")
                    val filteredDisasters = if (region == "Semua" || region.isEmpty()) {
                        allDisasters
                    } else {
                        allDisasters.filter { disaster ->
                            disaster.location.contains(region, ignoreCase = true)
                        }
                    }
                    
                    // Filter users by region if they have location data
                    val filteredUsers = if (region == "Semua" || region.isEmpty()) {
                        users
                    } else {
                        users.filter { user ->
                            user.address.contains(region, ignoreCase = true)
                        }
                    }
                    
                    // Generate analytics from filtered data
                    val analyticsData = AnalyticsDataProvider.generateAnalyticsState(filteredDisasters, filteredUsers)
                    
                    _analyticsState.value = analyticsData.copy(isLoading = false)
                    
                    Log.d("AnalyticsViewModel", "Filtered ${filteredDisasters.size} disasters and ${filteredUsers.size} users for region '$region'")
                } else {
                    throw disastersResult.exceptionOrNull() ?: Exception("Gagal memuat bencana")
                }
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error filtering by region", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal memfilter berdasarkan wilayah: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Apply multiple filters at once
     */
    fun applyFilters(
        startDate: String? = null,
        endDate: String? = null,
        disasterType: String? = null,
        region: String? = null
    ) {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Applying multiple filters: dateRange=[$startDate to $endDate], type=$disasterType, region=$region")
                
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                
                // Load disasters and users from repositories
                val disastersResult = disasterRepository.getAllDisasters()
                val users = try {
                    userRepository.getAllUsers()
                } catch (e: Exception) {
                    Log.w("AnalyticsViewModel", "Could not load users: ${e.message}")
                    emptyList()
                }
                
                if (disastersResult.isSuccess) {
                    var filteredDisasters = disastersResult.getOrDefault(emptyList())
                    var filteredUsers = users
                    
                    // Apply date range filter
                    if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val startTime = dateFormat.parse(startDate)?.time ?: 0L
                        val endTime = dateFormat.parse(endDate)?.time ?: Long.MAX_VALUE
                        
                        filteredDisasters = filteredDisasters.filter { disaster ->
                            disaster.timestamp in startTime..endTime
                        }
                    }
                    
                    // Apply disaster type filter
                    if (!disasterType.isNullOrEmpty() && disasterType != "Semua") {
                        filteredDisasters = filteredDisasters.filter { disaster ->
                            (when (disaster.type) {
                                DisasterType.EARTHQUAKE -> "Gempa Bumi"
                                DisasterType.FLOOD -> "Banjir"
                                DisasterType.WILDFIRE -> "Kebakaran Hutan"
                                DisasterType.LANDSLIDE -> "Tanah Longsor"
                                DisasterType.VOLCANO -> "Gunung Berapi"
                                DisasterType.TSUNAMI -> "Tsunami"
                                DisasterType.HURRICANE -> "Badai"
                                DisasterType.TORNADO -> "Tornado"
                                DisasterType.OTHER -> "Lainnya"
                            }).equals(disasterType, ignoreCase = true) ||
                            disaster.type.name.equals(disasterType, ignoreCase = true)
                        }
                    }
                    
                    // Apply region filter
                    if (!region.isNullOrEmpty() && region != "Semua") {
                        filteredDisasters = filteredDisasters.filter { disaster ->
                            disaster.location.contains(region, ignoreCase = true)
                        }
                        
                        filteredUsers = filteredUsers.filter { user ->
                            user.address.contains(region, ignoreCase = true)
                        }
                    }
                    
                    // Generate analytics from filtered data
                    val analyticsData = AnalyticsDataProvider.generateAnalyticsState(filteredDisasters, filteredUsers)
                    
                    _analyticsState.value = analyticsData.copy(isLoading = false)
                    
                    Log.d("AnalyticsViewModel", "Applied filters: ${filteredDisasters.size} disasters, ${filteredUsers.size} users")
                } else {
                    throw disastersResult.exceptionOrNull() ?: Exception("Gagal memuat bencana")
                }
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error applying filters", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal menerapkan filter: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear all filters and reload original data
     */
    fun clearFilters() {
        Log.d("AnalyticsViewModel", "Clearing all filters")
        loadAnalyticsData()
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _analyticsState.value = _analyticsState.value.copy(error = null)
    }

    /**
     * Get specific metric data
     */
    fun getMetricData(metricType: String): Any? {
        return when (metricType) {
            "total_disasters" -> _analyticsState.value.totalDisasters
            "active_incidents" -> _analyticsState.value.activeIncidents
            "resolved_incidents" -> _analyticsState.value.resolvedIncidents
            "total_users" -> _analyticsState.value.totalUsers
            "verified_users" -> _analyticsState.value.verifiedUsers
            "admin_users" -> _analyticsState.value.adminUsers
            "total_affected_people" -> _analyticsState.value.totalAffectedPeople
            "avg_affected_count" -> _analyticsState.value.avgAffectedCount
            else -> null
        }
    }

    /**
     * Get analytics summary for quick overview
     */
    fun getAnalyticsSummary(): String {
        val state = _analyticsState.value
        return buildString {
            append("${state.totalDisasters} disasters, ")
            append("${state.activeIncidents} active, ")
            append("${state.totalUsers} users")
        }
    }

    /**
     * Generate predictive insights
     */
    fun generatePredictiveInsights() {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Generating predictive insights")
                
                _analyticsState.value = _analyticsState.value.copy(isLoading = true)
                
                // Load historical disaster data
                val disastersResult = disasterRepository.getAllDisasters()
                val users = try {
                    userRepository.getAllUsers()
                } catch (e: Exception) {
                    Log.w("AnalyticsViewModel", "Could not load users: ${e.message}")
                    emptyList()
                }
                
                if (disastersResult.isSuccess) {
                    val disasters = disastersResult.getOrDefault(emptyList())
                    
                    // Generate predictive insights based on historical data
                    val insights = generatePredictiveAnalysis(disasters)
                    
                    // Create enhanced analytics state with predictions
                    val analyticsData = AnalyticsDataProvider.generateAnalyticsState(disasters, users)
                    val enhancedData = analyticsData.copy(
                        recentActivities = insights + analyticsData.recentActivities.take(7)
                    )
                    
                    _analyticsState.value = enhancedData.copy(isLoading = false)
                    
                    Log.d("AnalyticsViewModel", "Generated ${insights.size} predictive insights")
                } else {
                    throw disastersResult.exceptionOrNull() ?: Exception("Gagal memuat bencana")
                }
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error generating insights", e)
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = "Gagal menghasilkan wawasan: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Generate predictive analysis based on historical disaster data
     */
    private fun generatePredictiveAnalysis(disasters: List<Disaster>): List<RecentActivity> {
        val insights = mutableListOf<RecentActivity>()
        
        try {
            // Analyze seasonal patterns
            val seasonalInsights = analyzeSeasonalPatterns(disasters)
            insights.addAll(seasonalInsights)
            
            // Analyze location-based patterns
            val locationInsights = analyzeLocationPatterns(disasters)
            insights.addAll(locationInsights)
            
            // Analyze disaster type trends
            val typeInsights = analyzeDisasterTypeTrends(disasters)
            insights.addAll(typeInsights)
            
            // Analyze severity escalation patterns
            val severityInsights = analyzeSeverityPatterns(disasters)
            insights.addAll(severityInsights)
            
        } catch (e: Exception) {
            Log.w("AnalyticsViewModel", "Error in predictive analysis", e)
        }
        
        return insights.take(5) // Return top 5 insights
    }
    
    /**
     * Analyze seasonal disaster patterns
     */
    private fun analyzeSeasonalPatterns(disasters: List<Disaster>): List<RecentActivity> {
        val insights = mutableListOf<RecentActivity>()
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        
        // Group disasters by month
        val monthlyData = disasters.groupBy { disaster ->
            calendar.timeInMillis = disaster.timestamp
            calendar.get(Calendar.MONTH)
        }
        
        // Find peak months
        val peakMonth = monthlyData.maxByOrNull { it.value.size }
        if (peakMonth != null && peakMonth.value.size > 2) {
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).apply {
                calendar.set(Calendar.MONTH, peakMonth.key)
            }.format(calendar.time)
            
            insights.add(
                RecentActivity(
                    id = "seasonal_insight_1",
                    title = "Pola Musiman Terdeteksi",
                    description = "$monthName menunjukkan frekuensi bencana tertinggi (${peakMonth.value.size} insiden). Pertimbangkan kesiapsiagaan yang ditingkatkan.",
                    time = "Wawasan Prediktif",
                    icon = Icons.Default.TrendingUp,
                    color = AccentBlue,
                    type = "prediction"
                )
            )
        }
        
        // Check if current month is approaching peak season
        val currentMonthData = monthlyData[currentMonth]?.size ?: 0
        val avgMonthlyDisasters = monthlyData.values.map { it.size }.average()
        
        if (currentMonthData > avgMonthlyDisasters * 1.5) {
            insights.add(
                RecentActivity(
                    id = "seasonal_insight_2",
                    title = "Peringatan Periode Berisiko Tinggi",
                    description = "Bulan ini menunjukkan aktivitas bencana di atas rata-rata. Disarankan meningkatkan pemantauan.",
                    time = "Wawasan Prediktif",
                    icon = Icons.Default.Warning,
                    color = Warning,
                    type = "alert"
                )
            )
        }
        
        return insights
    }
    
    /**
     * Analyze location-based disaster patterns
     */
    private fun analyzeLocationPatterns(disasters: List<Disaster>): List<RecentActivity> {
        val insights = mutableListOf<RecentActivity>()
        
        // Group disasters by location
        val locationData = disasters.groupBy { it.location }
        
        // Find high-risk locations
        val highRiskLocation = locationData.maxByOrNull { it.value.size }
        if (highRiskLocation != null && highRiskLocation.value.size > 3) {
            val mostCommonType = highRiskLocation.value
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
                } ?: "disasters"
            
            insights.add(
                RecentActivity(
                    id = "location_insight_1",
                    title = "Lokasi Berisiko Tinggi Teridentifikasi",
                    description = "${highRiskLocation.key} menunjukkan kerentanan tertinggi terhadap $mostCommonType (${highRiskLocation.value.size} insiden).",
                    time = "Wawasan Prediktif",
                    icon = Icons.Default.LocationOn,
                    color = Red,
                    type = "risk_assessment"
                )
            )
        }
        
        // Analyze recent activity in locations
        val recentDisasters = disasters.filter { 
            System.currentTimeMillis() - it.timestamp < 30L * 24 * 60 * 60 * 1000 // Last 30 days
        }
        
        val recentLocationActivity = recentDisasters.groupBy { it.location }
        val activeLocation = recentLocationActivity.maxByOrNull { it.value.size }
        
        if (activeLocation != null && activeLocation.value.size > 1) {
            insights.add(
                RecentActivity(
                    id = "location_insight_2",
                    title = "Peningkatan Aktivitas Terdeteksi",
                    description = "${activeLocation.key} memiliki ${activeLocation.value.size} insiden terbaru. Pantau untuk kemungkinan eskalasi.",
                    time = "Wawasan Prediktif",
                    icon = Icons.Default.Timeline,
                    color = Warning,
                    type = "monitoring"
                )
            )
        }
        
        return insights
    }
    
    /**
     * Analyze disaster type trends
     */
    private fun analyzeDisasterTypeTrends(disasters: List<Disaster>): List<RecentActivity> {
        val insights = mutableListOf<RecentActivity>()
        
        // Group disasters by type and analyze trends
        val typeData = disasters.groupBy { it.type }
        val dominantType = typeData.maxByOrNull { it.value.size }
        
        if (dominantType != null && dominantType.value.size > disasters.size * 0.3) {
            insights.add(
                RecentActivity(
                    id = "type_insight_1",
                    title = "Jenis Bencana Dominan",
                    description = "${when (dominantType.key) {
                        DisasterType.EARTHQUAKE -> "Gempa Bumi"
                        DisasterType.FLOOD -> "Banjir"
                        DisasterType.WILDFIRE -> "Kebakaran Hutan"
                        DisasterType.LANDSLIDE -> "Tanah Longsor"
                        DisasterType.VOLCANO -> "Gunung Berapi"
                        DisasterType.TSUNAMI -> "Tsunami"
                        DisasterType.HURRICANE -> "Badai"
                        DisasterType.TORNADO -> "Tornado"
                        DisasterType.OTHER -> "Lainnya"
                    }} menyumbang ${dominantType.value.size} dari ${disasters.size} total insiden (${(dominantType.value.size * 100 / disasters.size)}%).",
                    time = "Wawasan Prediktif",
                    icon = DisasterType.getIconForType(dominantType.key),
                    color = DisasterType.getColorForType(dominantType.key),
                    type = "trend_analysis"
                )
            )
        }
        
        // Analyze recent trends
        val recentDisasters = disasters.filter { 
            System.currentTimeMillis() - it.timestamp < 60L * 24 * 60 * 60 * 1000 // Last 60 days
        }
        
        val recentTypeData = recentDisasters.groupBy { it.type }
        val emergingType = recentTypeData.maxByOrNull { it.value.size }
        
        if (emergingType != null && emergingType.value.size > 2) {
            insights.add(
                RecentActivity(
                    id = "type_insight_2",
                    title = "Pola Ancaman Baru",
                    description = "Peningkatan ${when (emergingType.key) {
                        DisasterType.EARTHQUAKE -> "Gempa Bumi"
                        DisasterType.FLOOD -> "Banjir"
                        DisasterType.WILDFIRE -> "Kebakaran Hutan"
                        DisasterType.LANDSLIDE -> "Tanah Longsor"
                        DisasterType.VOLCANO -> "Gunung Berapi"
                        DisasterType.TSUNAMI -> "Tsunami"
                        DisasterType.HURRICANE -> "Badai"
                        DisasterType.TORNADO -> "Tornado"
                        DisasterType.OTHER -> "Lainnya"
                    }} dalam periode terkini. Pertimbangkan langkah kesiapsiagaan yang tepat sasaran.",
                    time = "Wawasan Prediktif",
                    icon = Icons.Default.TrendingUp,
                    color = AccentBlue,
                    type = "emerging_trend"
                )
            )
        }
        
        return insights
    }
    
    /**
     * Analyze severity escalation patterns
     */
    private fun analyzeSeverityPatterns(disasters: List<Disaster>): List<RecentActivity> {
        val insights = mutableListOf<RecentActivity>()
        
        // Analyze affected count patterns
        val avgAffectedCount = disasters.map { it.affectedCount }.average()
        val highImpactDisasters = disasters.filter { it.affectedCount > avgAffectedCount * 2 }
        
        if (highImpactDisasters.isNotEmpty()) {
            val totalAffected = highImpactDisasters.sumOf { it.affectedCount }
            val percentage = (highImpactDisasters.size * 100) / disasters.size
            
            insights.add(
                RecentActivity(
                    id = "severity_insight_1",
                    title = "Analisis Insiden Berdampak Tinggi",
                    description = "${highImpactDisasters.size} insiden ($percentage%) mempengaruhi $totalAffected orang. Fokuskan pada kemampuan respons cepat.",
                    time = "Wawasan Prediktif",
                    icon = Icons.Default.Groups,
                    color = Red,
                    type = "impact_analysis"
                )
            )
        }
        
        // Analyze severity trends over time
        val recentDisasters = disasters.filter { 
            System.currentTimeMillis() - it.timestamp < 90L * 24 * 60 * 60 * 1000 // Last 90 days
        }.sortedBy { it.timestamp }
        
        if (recentDisasters.size > 5) {
            val recentAvgAffected = recentDisasters.takeLast(recentDisasters.size / 2).map { it.affectedCount }.average()
            val earlierAvgAffected = recentDisasters.take(recentDisasters.size / 2).map { it.affectedCount }.average()
            
            if (recentAvgAffected > earlierAvgAffected * 1.5) {
                insights.add(
                    RecentActivity(
                        id = "severity_insight_2",
                        title = "Tren Peningkatan Dampak",
                        description = "Insiden terbaru menunjukkan peningkatan keparahan. Rata-rata jumlah terdampak meningkat ${((recentAvgAffected - earlierAvgAffected) / earlierAvgAffected * 100).toInt()}%.",
                        time = "Wawasan Prediktif",
                        icon = Icons.Default.TrendingUp,
                        color = Red,
                        type = "escalation_warning"
                    )
                )
            }
        }
        
        return insights
    }

    /**
     * Update resource optimization recommendations
     */
    fun updateResourceOptimization() {
        viewModelScope.launch {
            try {
                Log.d("AnalyticsViewModel", "Updating resource optimization")
                
                // In production, this would:
                // 1. Analyze current resource allocation
                // 2. Calculate optimization opportunities
                // 3. Generate recommendations
                
                delay(800)
                loadAnalyticsData()
                
            } catch (e: Exception) {
                Log.e("AnalyticsViewModel", "Error updating optimization", e)
                _analyticsState.value = _analyticsState.value.copy(
                    error = "Gagal memperbarui optimisasi: ${e.message}"
                )
            }
        }
    }
} 
