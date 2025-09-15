package com.example.gmls.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.R
import com.example.gmls.domain.model.User
import com.example.gmls.domain.repository.AuthRepository
import com.example.gmls.domain.repository.UserRepository
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
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            try {
                val userId = userRepository.getCurrentUserId()
                if (userId == null) {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false, 
                        error = getApplication<Application>().getString(R.string.user_not_logged_in), 
                        success = null
                    )
                    return@launch
                }
                val user = userRepository.getUserProfile(userId)
                _profileState.value = _profileState.value.copy(
                    user = user,
                    isLoading = false,
                    error = null,
                    success = null
                )
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false,
                    error = e.message ?: getApplication<Application>().getString(R.string.failed_to_load_profile),
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
                val userId = userRepository.getCurrentUserId()
                if (userId == null) {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false, 
                        error = getApplication<Application>().getString(R.string.user_not_logged_in), 
                        success = null
                    )
                    return@launch
                }
                val result = userRepository.updateUserProfile(userId, updates)
                if (result.isSuccess) {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false, 
                        success = getApplication<Application>().getString(R.string.profile_updated_successfully), 
                        error = null
                    )
                    loadUserProfile()
                } else {
                    _profileState.value = _profileState.value.copy(
                        isLoading = false, 
                        error = result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.failed_to_update_profile), 
                        success = null
                    )
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    isLoading = false, 
                    error = e.message ?: getApplication<Application>().getString(R.string.failed_to_update_profile), 
                    success = null
                )
            }
        }
    }

    /**
     * Save Firebase Cloud Messaging token for push notifications
     * @param token The FCM token
     */
    fun saveFCMToken(token: String) {
        viewModelScope.launch {
            try {
                val result = userRepository.saveFCMToken(token)
                if (result.isFailure) {
                    _profileState.value = _profileState.value.copy(
                        error = result.exceptionOrNull()?.message ?: getApplication<Application>().getString(R.string.failed_to_save_fcm_token)
                    )
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    error = e.message ?: getApplication<Application>().getString(R.string.failed_to_save_fcm_token)
                )
            }
        }
    }

    /**
     * Clear update status
     */
    fun clearUpdateStatus() {
        _profileState.value = _profileState.value.copy(success = null)
    }

    fun clearMessages() {
        _profileState.value = _profileState.value.copy(error = null, success = null)
    }

    // Observe user profile in real-time (if needed)
    fun observeUserProfile(userId: String) {
        viewModelScope.launch {
            userRepository.observeUserProfile(userId).collect { user ->
                _profileState.value = _profileState.value.copy(user = user)
            }
        }
    }
}
