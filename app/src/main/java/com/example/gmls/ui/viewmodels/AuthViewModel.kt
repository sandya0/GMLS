package com.example.gmls.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.User
import com.example.gmls.domain.repository.AuthRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.gmls.ui.viewmodels.AuthState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling authentication-related operations
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    val isLoggedIn: StateFlow<Boolean> = _authState
        .map { it is AuthState.Authenticated }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.getCurrentUser()
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    /**
     * Get the current user ID
     */
    fun getCurrentUserId(): String? {
        val uid = authRepository.getCurrentUserId() ?: ""
        Log.d("AuthVM", "getCurrentUserId() called, UID: $uid")
        return uid
    }

    /**
     * Login a user with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.login(email, password)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    /**
     * Register a new user
     */
    fun register(registrationData: RegistrationData) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = authRepository.register(registrationData)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    /**
     * Logout the current user
     */
    fun logout() {
        Log.d("AuthVM", "Logout called. Current user before logout call: ${authRepository.getCurrentUserId()}")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.logout()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Logout failed")
            }
        }
    }

    /**
     * Reset the auth state
     */
    fun resetAuthState() {
        Log.d("AuthVM", "resetAuthState called. isLoggedIn: ${_authState.value}")
        _authState.value = AuthState.Initial
    }
}
