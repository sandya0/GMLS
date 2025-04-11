package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling authentication-related operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(userRepository.isUserLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * Login a user with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = userRepository.loginUser(email, password)

            _authState.value = if (result.isSuccess) {
                _isLoggedIn.value = true
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    /**
     * Register a new user
     */
    fun register(registrationData: RegistrationData) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = userRepository.registerUser(registrationData)

            _authState.value = if (result.isSuccess) {
                _isLoggedIn.value = true
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    /**
     * Logout the current user
     */
    fun logout() {
        userRepository.logoutUser()
        _isLoggedIn.value = false
        _authState.value = AuthState.Initial
    }

    /**
     * Reset the auth state
     */
    fun resetAuthState() {
        _authState.value = AuthState.Initial
    }
}

/**
 * Sealed class representing different authentication states
 */
sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}