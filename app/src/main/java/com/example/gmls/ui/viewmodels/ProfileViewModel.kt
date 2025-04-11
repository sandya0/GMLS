package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.User
import com.example.gmls.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling user profile operations
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Load the current user's profile
     */
    fun loadUserProfile() {
        val userId = userRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = userRepository.getUserProfile(userId)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        user = result.getOrNull(),
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load user profile"
                    )
                }
            }
        }
    }

    /**
     * Update user profile information
     * @param updates Map of fields to update
     */
    fun updateProfile(updates: Map<String, Any>) {
        val userId = userRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }

            val result = userRepository.updateUserProfile(userId, updates)

            if (result.isSuccess) {
                // Reload the profile to reflect changes
                loadUserProfile()

                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        updateSuccess = true,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        updateSuccess = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    /**
     * Save Firebase Cloud Messaging token for push notifications
     * @param token The FCM token
     */
    fun saveFCMToken(token: String) {
        viewModelScope.launch {
            userRepository.saveFCMToken(token)
        }
    }

    /**
     * Clear update status
     */
    fun clearUpdateStatus() {
        _uiState.update {
            it.copy(
                updateSuccess = false,
                error = null
            )
        }
    }
}

/**
 * Data class representing the UI state for profile screens
 */
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)