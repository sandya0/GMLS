package com.example.gmls.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.UserRoles
import com.example.gmls.domain.model.ErrorMessages
import com.example.gmls.domain.repository.AdminRepository
import com.example.gmls.domain.repository.DisasterRepository
import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.domain.validation.ValidationUtils
import com.example.gmls.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/**
 * Data class representing the state of admin operations
 */
data class AdminState(
    val users: List<User> = emptyList(),
    val disasters: List<Disaster> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingAuditLogs: Boolean = false,
    val auditLogHasMorePages: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val auditLogs: List<AdminAuditLog> = emptyList(),
    val userAnalytics: UserAnalytics = UserAnalytics(),
    val disasterAnalytics: DisasterAnalytics = DisasterAnalytics(),
    val targetType: String? = null,
    val validationErrors: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Data class for adding a user by admin
 */
data class AddUserByAdminFormData(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val temporaryPasswordMaybe: String,
    val address: String
)

/**
 * Data class for admin audit logging
 */
data class AdminAuditLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val adminId: String,
    val adminName: String,
    val action: String,
    val targetId: String?,
    val targetName: String?,
    val targetType: String? = null,
    val details: String,
    val timestamp: Date = Date()
)

/**
 * Data class for user analytics
 */
data class UserAnalytics(
    val totalUsers: Int = 0,
    val verifiedUsers: Int = 0,
    val activeUsers: Int = 0,
    val adminUsers: Int = 0,
    val recentRegistrations: Int = 0,  // Users registered in the last 7 days
    val usersByLocation: Map<String, Int> = emptyMap() // Location to count mapping
)

/**
 * Data class for disaster analytics
 */
data class DisasterAnalytics(
    val totalDisasters: Int = 0,
    val disastersByType: Map<String, Int> = emptyMap(),
    val disastersByStatus: Map<String, Int> = emptyMap(),
    val disasterTrend: List<MonthlyDisasterCount> = emptyList()
)

data class MonthlyDisasterCount(
    val month: String,
    val count: Int
)

/**
 * ViewModel for handling admin operations with comprehensive validation
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val disasterRepository: DisasterRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {
    
    private val _adminState = MutableStateFlow(AdminState())
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()
    
    init {
        loadAllData()
        observeAuditLogs()
    }
    
    /**
     * Clear validation errors and messages
     */
    fun clearMessages() {
        _adminState.value = _adminState.value.copy(
            error = null,
            success = null,
            validationErrors = emptyList()
        )
    }
    
    /**
     * Validate admin privileges before performing operations
     */
    private suspend fun validateAdminPrivileges(): Boolean {
        val isAdmin = adminRepository.isCurrentUserAdmin()
        if (!isAdmin) {
            _adminState.value = _adminState.value.copy(
                error = ErrorMessages.ADMIN_PERMISSION_DENIED,
                isLoading = false
            )
        }
        return isAdmin
    }
    
    /**
     * Load all admin data with validation
     */
    fun loadAllData() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true, error = null, success = null)
            try {
                // Check admin privileges
                if (!validateAdminPrivileges()) {
                    return@launch
                }
                
                // Load users
                val users = adminRepository.getAllUsers()
                
                // Load disasters
                val disastersResult = disasterRepository.getAllDisasters()
                val disasters = if (disastersResult.isSuccess) {
                    disastersResult.getOrDefault(emptyList())
                } else {
                    emptyList()
                }
                
                // Load audit logs
                val auditLogs = adminRepository.getAdminAuditLogs()
                
                // Calculate analytics
                val userAnalytics = calculateUserAnalytics(users)
                val disasterAnalytics = calculateDisasterAnalytics(disasters)
                
                _adminState.value = _adminState.value.copy(
                    users = users,
                    disasters = disasters,
                    auditLogs = auditLogs,
                    userAnalytics = userAnalytics,
                    disasterAnalytics = disasterAnalytics,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading admin data", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = e.message ?: ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Observe audit logs in real-time
     */
    private fun observeAuditLogs() {
        viewModelScope.launch {
            adminRepository.observeAdminAuditLogs().collectLatest { logs ->
                _adminState.value = _adminState.value.copy(auditLogs = logs)
            }
        }
    }
    
    /**
     * Export analytics data to CSV
     */
    fun exportAnalyticsData() {
        viewModelScope.launch {
            try {
                _adminState.value = _adminState.value.copy(isLoading = true, error = null)
                
                val currentState = _adminState.value
                val csvData = generateAnalyticsCSV(currentState)
                
                // Here you would typically save the CSV file or share it
                // For now, we'll just log it and show success message
                Log.d("AdminViewModel", "Analytics CSV generated: ${csvData.length} characters")
                
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    success = "Analytics data exported successfully!"
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error exporting analytics", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = "Gagal mengekspor analitik: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Generate CSV data for analytics
     */
    private fun generateAnalyticsCSV(state: AdminState): String {
        val csvBuilder = StringBuilder()
        
        // Header
        csvBuilder.appendLine("Analytics Report - Generated on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}")
        csvBuilder.appendLine()
        
        // User Analytics Section
        csvBuilder.appendLine("USER ANALYTICS")
        csvBuilder.appendLine("Metric,Value")
        csvBuilder.appendLine("Total Users,${state.userAnalytics.totalUsers}")
        csvBuilder.appendLine("Verified Users,${state.userAnalytics.verifiedUsers}")
        csvBuilder.appendLine("Active Users,${state.userAnalytics.activeUsers}")
        csvBuilder.appendLine("Admin Users,${state.userAnalytics.adminUsers}")
        csvBuilder.appendLine("Recent Registrations,${state.userAnalytics.recentRegistrations}")
        csvBuilder.appendLine()
        
        // Disaster Analytics Section
        csvBuilder.appendLine("DISASTER ANALYTICS")
        csvBuilder.appendLine("Metric,Value")
        csvBuilder.appendLine("Total Disasters,${state.disasterAnalytics.totalDisasters}")
        csvBuilder.appendLine()
        
        // Disasters by Type
        csvBuilder.appendLine("DISASTERS BY TYPE")
        csvBuilder.appendLine("Type,Count")
        state.disasterAnalytics.disastersByType.forEach { (type, count) ->
            csvBuilder.appendLine("$type,$count")
        }
        csvBuilder.appendLine()
        
        // Disasters by Status
        csvBuilder.appendLine("DISASTERS BY STATUS")
        csvBuilder.appendLine("Status,Count")
        state.disasterAnalytics.disastersByStatus.forEach { (status, count) ->
            csvBuilder.appendLine("$status,$count")
        }
        csvBuilder.appendLine()
        
        // User Details
        csvBuilder.appendLine("USER DETAILS")
        csvBuilder.appendLine("ID,Full Name,Email,Phone,Role,Verified,Active,Created At")
        state.users.forEach { user ->
            csvBuilder.appendLine("${user.id},${user.fullName},${user.email},${user.phoneNumber},${user.role},${user.isVerified},${user.isActive},${user.createdAt}")
        }
        csvBuilder.appendLine()
        
        // Disaster Details
        csvBuilder.appendLine("DISASTER DETAILS")
        csvBuilder.appendLine("ID,Title,Type,Severity,Status,Location,Created At,Updated At")
        state.disasters.forEach { disaster ->
            csvBuilder.appendLine("${disaster.id},${disaster.title},${disaster.type},${disaster.severityLevel},${disaster.status},${disaster.location},${disaster.timestamp},${disaster.updatedAt}")
        }
        
        return csvBuilder.toString()
    }
    
    /**
     * Verify a user with validation
     */
    fun verifyUser(user: User) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true, error = null, success = null)
            try {
                // Validate admin privileges
                if (!validateAdminPrivileges()) {
                    return@launch
                }
                
                // Validate user ID
                when (val userIdValidation = ValidationUtils.validateUserId(user.id)) {
                    is ValidationResult.Invalid -> {
                        _adminState.value = _adminState.value.copy(
                            isLoading = false,
                            error = userIdValidation.message
                        )
                        return@launch
                    }
                    ValidationResult.Valid -> {}
                }
                
                val result = adminRepository.updateUserVerifiedStatus(user.id, true)
                
                if (result.isSuccess) {
                    // Log this admin action
                    logAdminAction(
                        action = "verify_user",
                        targetId = user.id,
                        targetName = user.fullName,
                        details = "Akun pengguna terverifikasi"
                    )
                    
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        success = "User ${user.fullName} has been verified"
                    )
                    
                    // Reload data to reflect changes
                    loadAllData()
                } else {
                    throw result.exceptionOrNull() ?: Exception("Gagal memverifikasi pengguna")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error verifying user", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = e.message ?: ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Toggle user active status with validation
     */
    fun toggleUserStatus(user: User) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true, error = null, success = null)
            try {
                // Validate admin privileges
                if (!validateAdminPrivileges()) {
                    return@launch
                }
                
                // Validate user ID
                when (val userIdValidation = ValidationUtils.validateUserId(user.id)) {
                    is ValidationResult.Invalid -> {
                        _adminState.value = _adminState.value.copy(
                            isLoading = false,
                            error = userIdValidation.message
                        )
                        return@launch
                    }
                    ValidationResult.Valid -> {}
                }
                
                // Prevent admin from deactivating themselves
                val currentUserId = userRepository.getCurrentUserId()
                if (user.id == currentUserId && user.isActive) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        error = "Anda tidak dapat menonaktifkan akun Anda sendiri"
                    )
                    return@launch
                }
                
                val newStatus = !user.isActive
                val result = adminRepository.updateUserActiveStatus(user.id, newStatus)
                
                if (result.isSuccess) {
                    // Log this admin action
                    logAdminAction(
                        action = if (newStatus) "activate_user" else "deactivate_user",
                        targetId = user.id,
                        targetName = user.fullName,
                        details = if (newStatus) "Akun pengguna diaktifkan" else "Akun pengguna dinonaktifkan"
                    )
                    
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        success = "Pengguna ${user.fullName} telah ${if (newStatus) "diaktifkan" else "dinonaktifkan"}"
                    )
                    
                    // Reload data to reflect changes
                    loadAllData()
                } else {
                    throw result.exceptionOrNull() ?: Exception("Gagal memperbarui status pengguna")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error toggling user status", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = e.message ?: ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Create a new admin user with comprehensive validation
     */
    fun createAdmin(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true, error = null, success = null, validationErrors = emptyList())
            try {
                // Validate admin privileges
                if (!validateAdminPrivileges()) {
                    return@launch
                }
                
                // Sanitize inputs
                val sanitizedEmail = ValidationUtils.sanitizeInput(email)
                val sanitizedFullName = ValidationUtils.sanitizeInput(fullName)
                
                // Validate input data
                val validationErrors = ValidationUtils.validateAdminCreationData(
                    sanitizedEmail,
                    password,
                    sanitizedFullName
                )
                
                if (validationErrors.isNotEmpty()) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        validationErrors = validationErrors,
                        error = "Silakan perbaiki kesalahan validasi di bawah ini"
                    )
                    return@launch
                }
                
                val result = adminRepository.createAdminUser(sanitizedEmail, password, sanitizedFullName)
                
                if (result.isSuccess) {
                    val userId = result.getOrThrow()
                    
                    // Log this admin action
                    logAdminAction(
                        action = "create_admin",
                        targetId = userId,
                        targetName = sanitizedFullName,
                        details = "Created new admin user"
                    )
                    
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        success = "Pengguna admin $sanitizedFullName telah berhasil dibuat"
                    )
                    
                    // Reload data to reflect changes
                    loadAllData()
                } else {
                    throw result.exceptionOrNull() ?: Exception("Gagal membuat pengguna admin")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error creating admin user", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = e.message ?: ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Create a new user with validation
     */
    fun createUser(email: String, password: String, fullName: String, phoneNumber: String, address: String = "") {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true, error = null, success = null, validationErrors = emptyList())
            try {
                // Validate admin privileges
                if (!validateAdminPrivileges()) {
                    return@launch
                }
                
                // Sanitize inputs
                val sanitizedEmail = ValidationUtils.sanitizeInput(email)
                val sanitizedFullName = ValidationUtils.sanitizeInput(fullName)
                val sanitizedPhoneNumber = ValidationUtils.sanitizeInput(phoneNumber)
                val sanitizedAddress = ValidationUtils.sanitizeInput(address)
                
                // Validate input data
                val validationErrors = ValidationUtils.validateUserCreationData(
                    sanitizedEmail,
                    password,
                    sanitizedFullName,
                    sanitizedPhoneNumber,
                    sanitizedAddress
                )
                
                if (validationErrors.isNotEmpty()) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        validationErrors = validationErrors,
                        error = "Silakan perbaiki kesalahan validasi di bawah ini"
                    )
                    return@launch
                }
                
                // Create registration data
                val registrationData = com.example.gmls.ui.screens.auth.RegistrationData(
                    email = sanitizedEmail,
                    password = password,
                    fullName = sanitizedFullName,
                    phoneNumber = sanitizedPhoneNumber,
                    dateOfBirth = null,
                    gender = "",
                    locationPermissionGranted = false
                )
                
                val result = adminRepository.createUser(registrationData)
                
                if (result.isSuccess) {
                    val userId = result.getOrThrow()
                    
                    // Log this admin action
                    logAdminAction(
                        action = "create_user",
                        targetId = userId,
                        targetName = sanitizedFullName,
                        details = "Created new user account"
                    )
                    
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        success = "Pengguna $sanitizedFullName telah berhasil dibuat"
                    )
                    
                    // Reload data to reflect changes
                    loadAllData()
                } else {
                    throw result.exceptionOrNull() ?: Exception("Gagal membuat pengguna")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error creating user", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = e.message ?: ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Update a disaster's status with validation
     */
    fun updateDisasterStatus(disaster: Disaster, status: Disaster.Status) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true, error = null, success = null)
            try {
                // Validate admin privileges
                if (!validateAdminPrivileges()) {
                    return@launch
                }
                
                // Validate disaster ID
                if (disaster.id.isBlank()) {
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        error = "ID bencana tidak valid"
                    )
                    return@launch
                }
                
                val result = disasterRepository.updateDisasterStatus(disaster.id, status)
                
                if (result.isSuccess) {
                    // Log this admin action
                    logAdminAction(
                        action = "update_disaster_status",
                        targetId = disaster.id,
                        targetName = disaster.location,
                        details = "Updated disaster status to ${status.name}"
                    )
                    
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        success = "Status bencana diperbarui menjadi ${status.name}"
                    )
                    
                    // Reload data to reflect changes
                    loadAllData()
                } else {
                    throw result.exceptionOrNull() ?: Exception("Gagal memperbarui status bencana")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating disaster status", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = e.message ?: ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Log an admin action for audit purposes with validation
     */
    private fun logAdminAction(action: String, targetId: String?, targetName: String?, details: String) {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId() ?: return@launch
                val currentUser = userRepository.getUserProfile(currentUserId)
                
                if (currentUser.role != UserRoles.ADMIN) {
                    Log.e("AdminViewModel", "Non-admin tried to perform admin action: $action")
                    return@launch
                }
                
                // Sanitize inputs
                val sanitizedAction = ValidationUtils.sanitizeInput(action)
                val sanitizedTargetName = ValidationUtils.sanitizeInput(targetName)
                val sanitizedDetails = ValidationUtils.sanitizeInput(details)
                
                val log = AdminAuditLog(
                    adminId = currentUserId,
                    adminName = currentUser.fullName,
                    action = sanitizedAction,
                    targetId = targetId,
                    targetName = sanitizedTargetName,
                    details = sanitizedDetails,
                    timestamp = Date()
                )
                
                // Save to repository
                adminRepository.logAdminAction(log)
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error logging admin action", e)
            }
        }
    }
    
    /**
     * Search users with validation
     */
    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                // Validate search query
                when (val searchValidation = ValidationUtils.validateSearchQuery(query)) {
                    is ValidationResult.Invalid -> {
                        _adminState.value = _adminState.value.copy(
                            error = searchValidation.message
                        )
                        return@launch
                    }
                    ValidationResult.Valid -> {}
                }
                
                // Sanitize search query
                val sanitizedQuery = ValidationUtils.sanitizeInput(query)
                
                // Filter users based on search query
                val allUsers = _adminState.value.users
                val filteredUsers = if (sanitizedQuery.isBlank()) {
                    allUsers
                } else {
                    allUsers.filter { user ->
                        user.fullName.contains(sanitizedQuery, ignoreCase = true) ||
                        user.email.contains(sanitizedQuery, ignoreCase = true) ||
                        user.phoneNumber.contains(sanitizedQuery, ignoreCase = true)
                    }
                }
                
                _adminState.value = _adminState.value.copy(
                    users = filteredUsers,
                    error = null
                )
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error searching users", e)
                _adminState.value = _adminState.value.copy(
                    error = ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Calculate user analytics
     */
    private fun calculateUserAnalytics(users: List<User>): UserAnalytics {
        val now = Date().time
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)
        
        val recentUsers = users.count { it.createdAt.time > sevenDaysAgo }
        
        val locationMap = users
            .filter { it.address.isNotBlank() }
            .groupBy { it.address }
            .mapValues { it.value.size }
        
        return UserAnalytics(
            totalUsers = users.size,
            verifiedUsers = users.count { it.isVerified },
            activeUsers = users.count { it.isActive },
            adminUsers = users.count { it.role == UserRoles.ADMIN },
            recentRegistrations = recentUsers,
            usersByLocation = locationMap
        )
    }
    
    /**
     * Calculate disaster analytics
     */
    private fun calculateDisasterAnalytics(disasters: List<Disaster>): DisasterAnalytics {
        val typeMap = disasters
            .groupBy { it.type.name }
            .mapValues { it.value.size }
        
        val statusMap = disasters
            .groupBy { it.status.name }
            .mapValues { it.value.size }
        
        // Calculate monthly trends for the last 6 months
        val sixMonthsAgo = Date().time - (6 * 30 * 24 * 60 * 60 * 1000L)
        val recentDisasters = disasters.filter { it.timestamp > sixMonthsAgo }
        
        val monthlyFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
        val monthlyTrend = recentDisasters
            .groupBy { monthlyFormat.format(Date(it.timestamp)) }
            .map { MonthlyDisasterCount(it.key, it.value.size) }
            .sortedBy { it.month }
        
        return DisasterAnalytics(
            totalDisasters = disasters.size,
            disastersByType = typeMap,
            disastersByStatus = statusMap,
            disasterTrend = monthlyTrend
        )
    }

    // Pagination state for audit logs
    private var _lastAuditLogDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private val _pageSize = 20
    private var _isLoadingMoreLogs = false

    /**
     * Load more audit logs for pagination with real Firebase implementation
     */
    fun loadMoreAuditLogs() {
        if (_isLoadingMoreLogs) return // Prevent multiple simultaneous requests
        
        viewModelScope.launch {
            _isLoadingMoreLogs = true
            _adminState.value = _adminState.value.copy(isLoadingAuditLogs = true)
            
            try {
                // Check admin privileges
                if (!validateAdminPrivileges()) {
                    _isLoadingMoreLogs = false
                    return@launch
                }
                
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val currentLogs = _adminState.value.auditLogs.toMutableList()
                
                // Build query for pagination
                var query = firestore.collection("admin_audit_logs")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(_pageSize.toLong())
                
                // If we have a last document, start after it
                _lastAuditLogDocument?.let { lastDoc ->
                    query = query.startAfter(lastDoc)
                }
                
                val querySnapshot = query.get().await()
                val documents = querySnapshot.documents
                
                if (documents.isNotEmpty()) {
                    // Update last document for next pagination
                    _lastAuditLogDocument = documents.last()
                    
                    // Convert documents to AdminAuditLog objects
                    val newLogs = documents.mapNotNull { document ->
                        try {
                            val data = document.data ?: return@mapNotNull null
                            AdminAuditLog(
                                id = document.id,
                                adminId = data["adminId"] as? String ?: "",
                                adminName = data["adminName"] as? String ?: "Admin Tidak Dikenal",
                                action = data["action"] as? String ?: "",
                                targetId = data["targetId"] as? String,
                                targetName = data["targetName"] as? String,
                                targetType = data["targetType"] as? String,
                                details = data["details"] as? String ?: "",
                                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate() 
                                    ?: Date((data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis())
                            )
                        } catch (e: Exception) {
                            Log.w("AdminViewModel", "Error parsing audit log document ${document.id}", e)
                            null
                        }
                    }
                    
                    // Add new logs to existing list
                    currentLogs.addAll(newLogs)
                    
                    // Check if there are more logs available
                    val hasMorePages = documents.size == _pageSize
                    
                _adminState.value = _adminState.value.copy(
                        auditLogs = currentLogs,
                    isLoadingAuditLogs = false,
                        auditLogHasMorePages = hasMorePages,
                        success = if (newLogs.isNotEmpty()) {
                            "Berhasil memuat ${newLogs.size} log audit tambahan"
                        } else {
                            "Tidak ada log audit tambahan"
                        }
                    )
                    
                    Log.d("AdminViewModel", "Loaded ${newLogs.size} more audit logs. Total: ${currentLogs.size}")
                    
                } else {
                    // No more logs available
                    _adminState.value = _adminState.value.copy(
                        isLoadingAuditLogs = false,
                        auditLogHasMorePages = false,
                        success = "Semua log audit telah dimuat"
                    )
                    
                    Log.d("AdminViewModel", "No more audit logs available")
                }
                
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading more audit logs", e)
                _adminState.value = _adminState.value.copy(
                    isLoadingAuditLogs = false,
                    error = "Gagal memuat log audit tambahan: ${e.message}"
                )
            } finally {
                _isLoadingMoreLogs = false
            }
        }
    }
    
    /**
     * Refresh audit logs from the beginning
     */
    fun refreshAuditLogs() {
        viewModelScope.launch {
            try {
                _adminState.value = _adminState.value.copy(isLoadingAuditLogs = true)
                
                // Reset pagination state
                _lastAuditLogDocument = null
                _isLoadingMoreLogs = false
                
                // Load initial batch of audit logs
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val query = firestore.collection("admin_audit_logs")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(_pageSize.toLong())
                
                val querySnapshot = query.get().await()
                val documents = querySnapshot.documents
                
                if (documents.isNotEmpty()) {
                    _lastAuditLogDocument = documents.last()
                    
                    val auditLogs = documents.mapNotNull { document ->
                        try {
                            val data = document.data ?: return@mapNotNull null
                            AdminAuditLog(
                                id = document.id,
                                adminId = data["adminId"] as? String ?: "",
                                adminName = data["adminName"] as? String ?: "Admin Tidak Dikenal",
                                action = data["action"] as? String ?: "",
                                targetId = data["targetId"] as? String,
                                targetName = data["targetName"] as? String,
                                targetType = data["targetType"] as? String,
                                details = data["details"] as? String ?: "",
                                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate() 
                                    ?: Date((data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis())
                            )
                        } catch (e: Exception) {
                            Log.w("AdminViewModel", "Error parsing audit log document ${document.id}", e)
                            null
                        }
                    }
                    
                    val hasMorePages = documents.size == _pageSize
                    
                    _adminState.value = _adminState.value.copy(
                        auditLogs = auditLogs,
                        isLoadingAuditLogs = false,
                        auditLogHasMorePages = hasMorePages,
                        success = "Log audit berhasil dimuat ulang"
                    )
                    
                    Log.d("AdminViewModel", "Refreshed audit logs: ${auditLogs.size} loaded")
                    
                } else {
                    _adminState.value = _adminState.value.copy(
                        auditLogs = emptyList(),
                        isLoadingAuditLogs = false,
                        auditLogHasMorePages = false,
                        success = "Tidak ada log audit yang tersedia"
                    )
                }
                
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error refreshing audit logs", e)
                _adminState.value = _adminState.value.copy(
                    isLoadingAuditLogs = false,
                    error = "Gagal memuat ulang log audit: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Filter audit logs by action type
     */
    fun filterAuditLogsByAction(action: String?) {
        viewModelScope.launch {
            try {
                _adminState.value = _adminState.value.copy(isLoadingAuditLogs = true)
                
                // Reset pagination state for filtered results
                _lastAuditLogDocument = null
                _isLoadingMoreLogs = false
                
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                var query = firestore.collection("admin_audit_logs")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                
                // Add filter if action is specified
                if (!action.isNullOrBlank() && action != "all") {
                    query = query.whereEqualTo("action", action)
                }
                
                query = query.limit(_pageSize.toLong())
                
                val querySnapshot = query.get().await()
                val documents = querySnapshot.documents
                
                if (documents.isNotEmpty()) {
                    _lastAuditLogDocument = documents.last()
                    
                    val filteredLogs = documents.mapNotNull { document ->
                        try {
                            val data = document.data ?: return@mapNotNull null
                            AdminAuditLog(
                                id = document.id,
                                adminId = data["adminId"] as? String ?: "",
                                adminName = data["adminName"] as? String ?: "Admin Tidak Dikenal",
                                action = data["action"] as? String ?: "",
                                targetId = data["targetId"] as? String,
                                targetName = data["targetName"] as? String,
                                targetType = data["targetType"] as? String,
                                details = data["details"] as? String ?: "",
                                timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate() 
                                    ?: Date((data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis())
                            )
                        } catch (e: Exception) {
                            Log.w("AdminViewModel", "Error parsing filtered audit log document ${document.id}", e)
                            null
                        }
                    }
                    
                    val hasMorePages = documents.size == _pageSize
                    
                    _adminState.value = _adminState.value.copy(
                        auditLogs = filteredLogs,
                        isLoadingAuditLogs = false,
                        auditLogHasMorePages = hasMorePages,
                        success = if (action.isNullOrBlank() || action == "all") {
                            "Semua log audit berhasil dimuat"
                        } else {
                            "Log audit untuk aksi '$action' berhasil dimuat"
                        }
                    )
                    
                } else {
                    _adminState.value = _adminState.value.copy(
                        auditLogs = emptyList(),
                        isLoadingAuditLogs = false,
                        auditLogHasMorePages = false,
                        success = "Tidak ada log audit yang sesuai dengan filter"
                    )
                }
                
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error filtering audit logs", e)
                _adminState.value = _adminState.value.copy(
                    isLoadingAuditLogs = false,
                    error = "Gagal memfilter log audit: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Add user by admin
     */
    fun addUserByAdmin(formData: AddUserByAdminFormData) {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(isLoading = true, error = null)
            try {
                // Create registration data from form
                val registrationData = com.example.gmls.ui.screens.auth.RegistrationData(
                    email = formData.email,
                    password = formData.temporaryPasswordMaybe.ifEmpty { "temp123456" },
                    fullName = formData.fullName,
                    phoneNumber = formData.phoneNumber,
                    dateOfBirth = null,
                    gender = "",
                    locationPermissionGranted = false
                )
                
                val result = adminRepository.createUser(registrationData)
                
                if (result.isSuccess) {
                    val userId = result.getOrThrow()
                    
                    // Log this admin action
                    logAdminAction(
                        action = "create_user_by_admin",
                        targetId = userId,
                        targetName = formData.fullName,
                        details = "Created new user account via admin panel"
                    )
                    
                    _adminState.value = _adminState.value.copy(
                        isLoading = false,
                        success = "Pengguna ${formData.fullName} telah berhasil dibuat"
                    )
                    
                    // Reload data to reflect changes
                    loadAllData()
                } else {
                    throw result.exceptionOrNull() ?: Exception("Gagal membuat pengguna")
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error creating user by admin", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = e.message ?: ErrorMessages.OPERATION_FAILED
                )
            }
        }
    }
    
    /**
     * Get users with location data
     */
    fun getUsersWithLocation(): List<User> {
        return _adminState.value.users.filter { 
            it.latitude != null && it.longitude != null 
        }
    }
    
    /**
     * Load users with location data
     */
    fun loadUsersWithLocationData() {
        viewModelScope.launch {
            try {
                val users = adminRepository.getAllUsersWithLocation()
                _adminState.value = _adminState.value.copy(users = users)
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading users with location", e)
            }
        }
    }
    
    /**
     * Get online users with real-time presence detection
     */
    fun getOnlineUsers(): List<User> {
        return _adminState.value.users.filter { user ->
            _onlineUserIds.contains(user.id) && 
            user.latitude != null && 
            user.longitude != null
        }
    }
    
    /**
     * Get users with recent locations (within last 24 hours)
     */
    fun getUsersWithRecentLocations(): List<User> {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return _adminState.value.users.filter { user ->
            user.latitude != null && 
            user.longitude != null &&
            (user.updatedAt.time > twentyFourHoursAgo || _onlineUserIds.contains(user.id))
        }
    }
    
    // Private properties for real-time tracking
    private val _locationListeners = mutableMapOf<String, com.google.firebase.firestore.ListenerRegistration>()
    private val _onlineUserIds = mutableSetOf<String>()
    
    /**
     * Start observing user locations with real-time Firebase listeners
     */
    fun startObservingUserLocations() {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Starting real-time location tracking")
                
                // First load initial user data
        loadUsersWithLocationData()
                
                // Set up real-time listeners for user location updates
                setupLocationListeners()
                
                // Set up simplified presence tracking
                setupSimplifiedPresenceTracking()
                
                Log.d("AdminViewModel", "Real-time location tracking started successfully")
                
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error starting location observation", e)
                _adminState.value = _adminState.value.copy(
                    error = "Gagal memulai pelacakan lokasi real-time: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Stop observing user locations and clean up listeners
     */
    fun stopObservingUserLocations() {
        viewModelScope.launch {
            try {
                Log.d("AdminViewModel", "Stopping real-time location tracking")
                
                // Remove all location listeners
                _locationListeners.values.forEach { listener ->
                    listener.remove()
                }
                _locationListeners.clear()
                
                // Clear online user tracking
                _onlineUserIds.clear()
                
                Log.d("AdminViewModel", "Real-time location tracking stopped")
                
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error stopping location observation", e)
            }
        }
    }
    
    /**
     * Set up Firebase Firestore listeners for user location updates
     */
    private suspend fun setupLocationListeners() {
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            // Listen to all users collection for location updates
            val usersListener = firestore.collection("users")
                .whereNotEqualTo("latitude", null)
                .whereNotEqualTo("longitude", null)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AdminViewModel", "Error listening to user locations", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        viewModelScope.launch {
                            try {
                                val updatedUsers = mutableListOf<User>()
                                val currentUsers = _adminState.value.users.toMutableList()
                                
                                for (document in snapshot.documents) {
                                    val userData = document.data ?: continue
                                    val userId = document.id
                                    
                                    // Update or add user with new location data
                                    val existingUserIndex = currentUsers.indexOfFirst { it.id == userId }
                                    if (existingUserIndex >= 0) {
                                        val existingUser = currentUsers[existingUserIndex]
                                        val updatedUser = existingUser.copy(
                                            latitude = (userData["latitude"] as? Number)?.toDouble() ?: existingUser.latitude,
                                            longitude = (userData["longitude"] as? Number)?.toDouble() ?: existingUser.longitude,
                                            updatedAt = java.util.Date((userData["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis())
                                        )
                                        currentUsers[existingUserIndex] = updatedUser
                                    }
                                }
                                
                                _adminState.value = _adminState.value.copy(users = currentUsers)
                                Log.d("AdminViewModel", "Updated user locations from real-time listener")
                                
                            } catch (e: Exception) {
                                Log.e("AdminViewModel", "Error processing location updates", e)
                            }
                        }
                    }
                }
            
            _locationListeners["all_users"] = usersListener
            
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error setting up location listeners", e)
            throw e
        }
    }
    
    /**
     * Set up simplified presence tracking based on recent location updates
     */
    private suspend fun setupSimplifiedPresenceTracking() {
        try {
            // For simplified presence, we'll consider users online if they have recent location updates
            val users = _adminState.value.users
            val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
            
            users.forEach { user ->
                if (user.latitude != null && user.longitude != null && user.updatedAt.time > fiveMinutesAgo) {
                    _onlineUserIds.add(user.id)
                }
            }
            
            Log.d("AdminViewModel", "Set up simplified presence tracking for ${_onlineUserIds.size} online users")
            
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error setting up simplified presence tracking", e)
            throw e
        }
    }
    
    /**
     * Check if a user is currently online with enhanced logic
     */
    fun isUserOnline(userId: String): Boolean {
        val isPresenceOnline = _onlineUserIds.contains(userId)
        val user = _adminState.value.users.find { it.id == userId }
        
        if (user == null) return false
        
        // Consider user online if:
        // 1. They have active presence, OR
        // 2. Their location was updated within the last 5 minutes
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        val hasRecentLocation = user.updatedAt.time > fiveMinutesAgo
        
        return isPresenceOnline || (hasRecentLocation && user.latitude != null && user.longitude != null)
    }
    
    /**
     * Get user location history for a specific user
     */
    fun getUserLocationHistory(userId: String): List<LocationUpdate> {
        // This could be expanded to fetch historical location data from Firebase
        // For now, return current location as single point
        val user = _adminState.value.users.find { it.id == userId }
        return if (user?.latitude != null && user.longitude != null) {
            listOf(
                LocationUpdate(
                    userId = userId,
                    latitude = user.latitude!!,
                    longitude = user.longitude!!,
                    timestamp = user.updatedAt.time,
                    isOnline = isUserOnline(userId)
                )
            )
        } else {
            emptyList()
        }
    }
    
    /**
     * Force refresh location data for all users
     */
    fun refreshAllUserLocations() {
        viewModelScope.launch {
            try {
                _adminState.value = _adminState.value.copy(isLoading = true)
                
                // Reload user data
                loadUsersWithLocationData()
                
                // Restart listeners to ensure fresh connections
                stopObservingUserLocations()
                delay(1000) // Brief delay to ensure cleanup
                startObservingUserLocations()
                
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    success = "Data lokasi pengguna berhasil dimuat ulang"
                )
                
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error refreshing user locations", e)
                _adminState.value = _adminState.value.copy(
                    isLoading = false,
                    error = "Gagal memuat ulang data lokasi: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Data class for location updates
     */
    data class LocationUpdate(
        val userId: String,
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long,
        val isOnline: Boolean
    )
    
    // Clean up listeners when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        stopObservingUserLocations()
    }
} 
