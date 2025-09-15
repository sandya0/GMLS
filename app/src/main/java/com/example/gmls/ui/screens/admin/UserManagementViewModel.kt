package com.example.gmls.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.HouseholdMember
import com.example.gmls.domain.repository.AdminRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers(showOnlyAdminCreated: Boolean = false) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val users = adminRepository.getAllUsers(showOnlyAdminCreated)
                _uiState.update {
                    it.copy(
                        users = users,
                        filteredUsers = users,
                        isLoading = false,
                        showOnlyAdminCreated = showOnlyAdminCreated
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Gagal memuat pengguna", 
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun createUser(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String,
        additionalData: Map<String, String> = emptyMap()
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                // Create enhanced registration data with additional fields
                val registrationData = RegistrationData(
                    email = email,
                    password = password, // Temporary password for admin-created users
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    dateOfBirth = null,
                    gender = additionalData["gender"] ?: "",
                    nationalId = additionalData["nationalId"] ?: "",
                    familyCardNumber = additionalData["familyCardNumber"] ?: "",
                    placeOfBirth = additionalData["placeOfBirth"] ?: "",
                    religion = additionalData["religion"] ?: "",
                    maritalStatus = additionalData["maritalStatus"] ?: "",
                    familyRelationshipStatus = additionalData["familyRelationshipStatus"] ?: "",
                    lastEducation = additionalData["lastEducation"] ?: "",
                    occupation = additionalData["occupation"] ?: "",
                    economicStatus = additionalData["economicStatus"] ?: "",
                    latitude = null,
                    longitude = null,
                    address = additionalData["address"] ?: "",
                    bloodType = additionalData["bloodType"] ?: "",
                    medicalConditions = additionalData["medicalConditions"] ?: "",
                    disabilities = additionalData["disabilities"] ?: "",
                    emergencyContactName = additionalData["emergencyContactName"] ?: "",
                    emergencyContactRelationship = additionalData["emergencyContactRelationship"] ?: "",
                    emergencyContactPhone = additionalData["emergencyContactPhone"] ?: "",
                    householdMembers = 1, // RegistrationData expects Int, not List
                    locationPermissionGranted = false
                )
                
                val result = adminRepository.createUser(registrationData)
                result.fold(
                    onSuccess = { userId: String ->
                        _uiState.update { state ->
                            state.copy(
                                showUserCreatedDialog = true,
                                isLoading = false,
                                lastCreatedUserId = userId
                            )
                        }
                        // Reload users to show the new user
                        loadUsers(_uiState.value.showOnlyAdminCreated)
                    },
                    onFailure = { e: Throwable ->
                        _uiState.update { 
                            it.copy(
                                error = when {
                                    e.message?.contains("email", ignoreCase = true) == true -> 
                                        "Alamat email sudah digunakan atau tidak valid"
                                    e.message?.contains("permission", ignoreCase = true) == true -> 
                                        "You don't have permission to create users"
                                    e.message?.contains("network", ignoreCase = true) == true -> 
                                        "Network error. Please check your connection"
                                    else -> e.message ?: "Gagal membuat pengguna"
                                }, 
                                isLoading = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "An unexpected error occurred: ${e.message}", 
                        isLoading = false
                    )
                }
            }
        }
    }

    fun verifyUser(userId: String) {
        viewModelScope.launch {
            try {
                adminRepository.updateUserVerifiedStatus(userId, true).fold(
                    onSuccess = { _: Unit -> 
                        loadUsers(_uiState.value.showOnlyAdminCreated)
                        _uiState.update { 
                            it.copy(successMessage = "Pengguna berhasil diverifikasi")
                        }
                    },
                    onFailure = { e: Throwable ->
                        _uiState.update { 
                            it.copy(error = e.message ?: "Gagal memverifikasi pengguna") 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "An unexpected error occurred: ${e.message}") 
                }
            }
        }
    }
    
    fun updateUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                adminRepository.updateUserActiveStatus(userId, isActive).fold(
                    onSuccess = { _: Unit -> 
                        loadUsers(_uiState.value.showOnlyAdminCreated)
                        _uiState.update { 
                            it.copy(successMessage = "Status pengguna berhasil diperbarui")
                        }
                    },
                    onFailure = { e: Throwable ->
                        _uiState.update { 
                            it.copy(error = e.message ?: "Gagal memperbarui status pengguna") 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "An unexpected error occurred: ${e.message}") 
                }
            }
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                adminRepository.deleteUser(userId).fold(
                    onSuccess = { 
                        loadUsers(_uiState.value.showOnlyAdminCreated)
                        _uiState.update { 
                            it.copy(successMessage = "Pengguna berhasil dihapus")
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(error = e.message ?: "Gagal menghapus pengguna") 
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "An unexpected error occurred: ${e.message}") 
                }
            }
        }
    }
    
    fun searchUsers(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.users
        } else {
            _uiState.value.users.filter { user ->
                user.fullName.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true) ||
                user.phoneNumber.contains(query, ignoreCase = true) ||
                user.nationalId.contains(query, ignoreCase = true) ||
                user.address.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.update { it.copy(filteredUsers = filtered, searchQuery = query) }
    }
    
    fun filterUsers(
        role: String? = null,
        status: String? = null,
        verified: Boolean? = null
    ) {
        val filtered = _uiState.value.users.filter { user ->
            val matchesRole = role == null || user.role == role
            val matchesStatus = when (status) {
                "active" -> user.isActive
                "inactive" -> !user.isActive
                else -> true
            }
            val matchesVerified = verified == null || user.isVerified == verified
            
            matchesRole && matchesStatus && matchesVerified
        }
        
        _uiState.update { 
            it.copy(
                filteredUsers = filtered,
                currentFilters = UserFilters(role, status, verified)
            ) 
        }
    }
    
    fun sortUsers(sortBy: String) {
        val sorted = when (sortBy) {
            "name" -> _uiState.value.filteredUsers.sortedBy { it.fullName }
            "email" -> _uiState.value.filteredUsers.sortedBy { it.email }
            "date" -> _uiState.value.filteredUsers.sortedByDescending { it.createdAt }
            "status" -> _uiState.value.filteredUsers.sortedWith(
                compareBy({ !it.isActive }, { !it.isVerified }, { it.fullName })
            )
            "role" -> _uiState.value.filteredUsers.sortedWith(
                compareBy({ it.role }, { it.fullName })
            )
            else -> _uiState.value.filteredUsers
        }
        
        _uiState.update { 
            it.copy(
                filteredUsers = sorted,
                currentSortBy = sortBy
            ) 
        }
    }
    
    fun clearFilters() {
        _uiState.update { 
            it.copy(
                filteredUsers = it.users,
                searchQuery = "",
                currentFilters = UserFilters(),
                currentSortBy = "name"
            ) 
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun dismissSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun dismissUserCreatedDialog() {
        _uiState.update { it.copy(showUserCreatedDialog = false) }
    }
    
    fun refreshUsers() {
        loadUsers(_uiState.value.showOnlyAdminCreated)
    }
    
    fun exportUserData(): String {
        // Simple CSV export functionality
        val users = _uiState.value.filteredUsers
        val header = "Full Name,Email,Phone,Role,Status,Verified,Created Date"
        val rows = users.map { user ->
            "${user.fullName},${user.email},${user.phoneNumber},${user.role}," +
                            "${if (user.isActive) "Aktif" else "Tidak Aktif"}," +
                            "${if (user.isVerified) "Terverifikasi" else "Belum Terverifikasi"}," +
            "${user.createdAt}"
        }
        return (listOf(header) + rows).joinToString("\n")
    }
    
    fun getUserStatistics(): UserStatistics {
        val users = _uiState.value.users
        return UserStatistics(
            totalUsers = users.size,
            activeUsers = users.count { it.isActive },
            verifiedUsers = users.count { it.isVerified },
            adminUsers = users.count { it.role == "admin" },
            adminCreatedUsers = users.count { it.createdByAdmin },
            usersWithEmergencyContact = users.count { it.emergencyContact.name.isNotEmpty() },
            usersWithMedicalInfo = users.count { it.medicalConditions.isNotEmpty() || it.bloodType.isNotEmpty() }
        )
    }
}

data class UserManagementUiState(
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val showOnlyAdminCreated: Boolean = false,
    val showUserCreatedDialog: Boolean = false,
    val lastCreatedUserId: String? = null,
    val currentFilters: UserFilters = UserFilters(),
    val currentSortBy: String = "name"
)

data class UserFilters(
    val role: String? = null,
    val status: String? = null,
    val verified: Boolean? = null
)

data class UserStatistics(
    val totalUsers: Int,
    val activeUsers: Int,
    val verifiedUsers: Int,
    val adminUsers: Int,
    val adminCreatedUsers: Int,
    val usersWithEmergencyContact: Int,
    val usersWithMedicalInfo: Int
)
