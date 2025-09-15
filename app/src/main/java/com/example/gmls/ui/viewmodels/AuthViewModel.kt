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
import com.example.gmls.data.remote.LocationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull

/**
 * ViewModel for handling authentication-related operations with comprehensive error handling
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val locationService: LocationService
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
        private const val AUTH_CHECK_INTERVAL = 60000L // 1 minute
        private const val OPERATION_TIMEOUT = 30000L // 30 seconds
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    val isLoggedIn: StateFlow<Boolean> = _authState
        .map { it is AuthState.Authenticated }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Exception handler for this ViewModel's coroutines
    private val viewModelExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "ViewModel coroutine exception", exception)
        handleViewModelException(exception)
    }

    // Job for periodic auth state checking
    private var authCheckJob: Job? = null

    init {
        try {
            initializeViewModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AuthViewModel", e)
            handleViewModelException(e)
        }
    }

    private fun initializeViewModel() {
        // Initial auth state check
        checkAuthState()
        
        // Start periodic auth state check with error handling
        startPeriodicAuthCheck()
    }

    private fun startPeriodicAuthCheck() {
        authCheckJob?.cancel()
        authCheckJob = viewModelScope.launch(viewModelExceptionHandler) {
            while (isActive) {
                try {
                    delay(AUTH_CHECK_INTERVAL)
                    if (isActive) {
                        checkAuthState()
                    }
                } catch (e: CancellationException) {
                    Log.d(TAG, "Auth check cancelled")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Error in periodic auth check", e)
                    // Continue the loop even if one check fails
                }
            }
        }
    }

    fun checkAuthState() {
        viewModelScope.launch(viewModelExceptionHandler) {
            try {
                val user = withTimeoutOrNull(OPERATION_TIMEOUT) {
                    // Use a safe version that doesn't fail on permission errors
                    authRepository.getCurrentUserSafe()
                }
                
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                    
                    // If user exists but has a different role than before, update the state
                    val currentUser = _currentUser.value
                    if (currentUser?.role != user.role) {
                        _currentUser.value = user
                        Log.d(TAG, "User role updated from ${currentUser?.role} to ${user.role}")
                    }
                } else {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking auth state: ${e.message}", e)
                // Don't change state on error to avoid accidentally logging out users
                // Only log the error for debugging
                handleAuthError(e, "checking authentication state")
                
                // If it's a permission error, don't update the auth state
                if (e.message?.contains("PERMISSION_DENIED") == true) {
                    Log.w(TAG, "Permission denied during auth check, user document may not exist yet")
                } else {
                    // For other errors, set to unauthenticated
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Get the current user ID with null safety
     */
    fun getCurrentUserId(): String? {
        return try {
            val uid = authRepository.getCurrentUserId()
            Log.d(TAG, "getCurrentUserId() called, UID: ${uid ?: "null"}")
            uid
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID", e)
            null
        }
    }

    /**
     * Login a user with email and password with comprehensive error handling
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan kata sandi tidak boleh kosong")
            return
        }

        viewModelScope.launch(viewModelExceptionHandler) {
            _authState.value = AuthState.Loading
            try {
                val user = withTimeoutOrNull(OPERATION_TIMEOUT) {
                    authRepository.login(email.trim(), password)
                }
                
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                    Log.d(TAG, "Login successful for user: ${user.email}")
                } else {
                    _authState.value = AuthState.Error("Login gagal - tidak ada pengguna yang dikembalikan")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                val message = getLoginErrorMessage(e)
                _authState.value = AuthState.Error(message)
            }
        }
    }

    /**
     * Fetch current location and update registration data with error handling
     */
    fun fetchAndSetLocation(
        registrationData: RegistrationData,
        onResult: (RegistrationData) -> Unit
    ) {
        viewModelScope.launch(viewModelExceptionHandler) {
            try {
                if (registrationData.locationPermissionGranted) {
                    val coordsResult = withTimeoutOrNull(OPERATION_TIMEOUT) {
                        locationService.getCurrentLocation()
                    }
                    
                    if (coordsResult != null && coordsResult.isSuccess) {
                        val coords = coordsResult.getOrThrow()
                        Log.d(TAG, "Location fetched: ${coords.latitude}, ${coords.longitude}")
                        onResult(registrationData.copy(
                            latitude = coords.latitude, 
                            longitude = coords.longitude
                        ))
                    } else {
                        Log.w(TAG, "Failed to get location coordinates")
                        onResult(registrationData)
                    }
                } else {
                    Log.d(TAG, "Location permission not granted")
                    onResult(registrationData)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching location", e)
                onResult(registrationData) // Continue without location
            }
        }
    }

    /**
     * Register a new user with comprehensive validation and error handling
     */
    fun register(registrationData: RegistrationData) {
        if (!validateRegistrationData(registrationData)) {
            return
        }

        viewModelScope.launch(viewModelExceptionHandler) {
            _authState.value = AuthState.Loading
            try {
                val user = withTimeoutOrNull(OPERATION_TIMEOUT) {
                    authRepository.register(registrationData)
                }
                
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                    Log.d(TAG, "Registration successful for user: ${user.email}")
                } else {
                    _authState.value = AuthState.Error("Registrasi gagal - tidak ada pengguna yang dikembalikan")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                val message = getRegistrationErrorMessage(e)
                _authState.value = AuthState.Error(message)
            }
        }
    }

    /**
     * Logout the current user with error handling
     */
    fun logout() {
        val currentUserId = getCurrentUserId()
        Log.d(TAG, "Logout called. Current user before logout: $currentUserId")
        
        viewModelScope.launch(viewModelExceptionHandler) {
            _authState.value = AuthState.Loading
            try {
                withTimeoutOrNull(OPERATION_TIMEOUT) {
                    authRepository.logout()
                }
                
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
                Log.d(TAG, "Logout successful")
            } catch (e: Exception) {
                Log.e(TAG, "Logout error", e)
                _authState.value = AuthState.Error("Terjadi kesalahan yang tidak terduga")
            }
        }
    }

    /**
     * Reset the auth state safely
     */
    fun resetAuthState() {
        try {
            Log.d(TAG, "resetAuthState called. Current state: ${_authState.value}")
            _authState.value = AuthState.Initial
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting auth state", e)
            handleViewModelException(e)
        }
    }

    private fun validateRegistrationData(data: RegistrationData): Boolean {
        return when {
            data.email.isBlank() -> {
                _authState.value = AuthState.Error("Email tidak boleh kosong")
                false
            }
            data.password.isBlank() -> {
                _authState.value = AuthState.Error("Kata sandi tidak boleh kosong")
                false
            }
            data.fullName.isBlank() -> {
                _authState.value = AuthState.Error("Nama lengkap tidak boleh kosong")
                false
            }
            data.phoneNumber.isBlank() -> {
                _authState.value = AuthState.Error("Nomor telepon tidak boleh kosong")
                false
            }
            else -> true
        }
    }

    private fun getLoginErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("password") == true -> "Kata sandi salah. Silakan coba lagi."
            exception.message?.contains("no user record") == true -> "Tidak ditemukan akun dengan email ini."
            exception.message?.contains("network") == true -> "Kesalahan jaringan. Periksa koneksi Anda."
            exception.message?.contains("timeout") == true -> "Permintaan timeout. Silakan coba lagi."
            else -> exception.message ?: "Login gagal. Periksa kredensial Anda."
        }
    }

    private fun getRegistrationErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("email address is already in use") == true -> "Email ini sudah terdaftar."
            exception.message?.contains("badly formatted") == true -> "Format email tidak valid."
            exception.message?.contains("Password should be at least") == true -> "Kata sandi terlalu lemah."
            exception.message?.contains("network") == true -> "Kesalahan jaringan. Periksa koneksi Anda."
            exception.message?.contains("timeout") == true -> "Permintaan timeout. Silakan coba lagi."
            else -> exception.message ?: "Registrasi gagal. Periksa detail Anda."
        }
    }

    private fun handleViewModelException(exception: Throwable) {
        Log.e(TAG, "ViewModel exception handled", exception)
        
        // Don't update auth state for non-critical errors
        if (exception !is CancellationException) {
            // Only update state if it's currently loading
            if (_authState.value is AuthState.Loading) {
                _authState.value = AuthState.Error("Terjadi kesalahan yang tidak terduga")
            }
        }
    }

    private fun handleAuthError(exception: Throwable, operation: String) {
        Log.e(TAG, "Auth error during $operation", exception)
        
        // For critical auth errors, you might want to force logout
        if (exception.message?.contains("token") == true || 
            exception.message?.contains("unauthorized") == true) {
            Log.w(TAG, "Auth token issue detected, may need to logout")
            // Optionally force logout here
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            authCheckJob?.cancel()
            Log.d(TAG, "AuthViewModel cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing AuthViewModel", e)
        }
    }
}
