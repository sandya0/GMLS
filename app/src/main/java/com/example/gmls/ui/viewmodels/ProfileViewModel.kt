package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.User
import com.example.gmls.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

/**
 * ViewModel for handling user profile operations
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            try {
                val user = authRepository.getCurrentUser()
                _profileState.value = _profileState.value.copy(
                    user = user,
                    isLoading = false,
                    error = null,
                    success = null
                )
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load profile",
                    success = null
                )
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    /**
     * Update user profile information
     * @param updates Map of fields to update
     */
    fun updateProfile(updates: Map<String, Any>) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, error = null, success = null)
            try {
                // Simulate update (replace with real update logic)
                // authRepository.updateUserProfile(updates)
                _profileState.value = _profileState.value.copy(isLoading = false, success = "Profile updated successfully", error = null)
                loadUserProfile()
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(isLoading = false, error = e.message ?: "Failed to update profile", success = null)
            }
        }
    }

    /**
     * Save Firebase Cloud Messaging token for push notifications
     * @param token The FCM token
     */
    fun saveFCMToken(token: String) {
        // Implementation needed
    }

    /**
     * Clear update status
     */
    fun clearUpdateStatus() {
        // Implementation needed
    }

    fun clearMessages() {
        _profileState.value = _profileState.value.copy(error = null, success = null)
    }
}