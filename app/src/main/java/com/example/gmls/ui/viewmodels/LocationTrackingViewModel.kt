package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.data.remote.Coordinates
import com.example.gmls.data.remote.LocationService
import com.example.gmls.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel for handling real-time location tracking with improved error handling
 */
@HiltViewModel
class LocationTrackingViewModel @Inject constructor(
    private val locationService: LocationService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _trackingState = MutableStateFlow(LocationTrackingState())
    val trackingState: StateFlow<LocationTrackingState> = _trackingState.asStateFlow()

    companion object {
        private const val TAG = "LocationTrackingViewModel"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    /**
     * Start location tracking for the current user with improved error handling
     */
    fun startLocationTracking() {
        viewModelScope.launch {
            try {
                // Check if user is logged in first
                val userId = userRepository.getCurrentUserId()
                if (userId.isNullOrBlank()) {
                    Log.e(TAG, "Cannot start location tracking: User not logged in")
                    _trackingState.update { 
                        it.copy(
                            isTracking = false,
                            error = "Silakan masuk untuk berbagi lokasi Anda"
                        ) 
                    }
                    return@launch
                }

                // Check if already tracking
                if (_trackingState.value.isTracking) {
                    Log.d(TAG, "Location tracking already active")
                    return@launch
                }

                // Check permissions before starting
                if (!hasRequiredPermissions()) {
                    Log.e(TAG, "Required permissions not granted")
                    _trackingState.update { 
                        it.copy(
                            isTracking = false,
                            error = "Izin lokasi tidak diberikan. Silakan berikan semua izin yang diperlukan."
                        ) 
                    }
                    return@launch
                }

                Log.d(TAG, "All checks passed, starting location tracking for user: $userId")
                
                // Test location access first
                try {
                    val testResult = locationService.testLocationAccess()
                    if (testResult.isFailure) {
                        val error = testResult.exceptionOrNull()
                        Log.e(TAG, "Location access test failed: ${error?.message}")
                        _trackingState.update { 
                            it.copy(
                                isTracking = false,
                                error = "Tes lokasi gagal: ${error?.message}"
                            ) 
                        }
                        return@launch
                    } else {
                        Log.d(TAG, "Location access test passed: ${testResult.getOrNull()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during location test", e)
                    _trackingState.update { 
                        it.copy(
                            isTracking = false,
                            error = "Pengecualian tes lokasi: ${e.message}"
                        ) 
                    }
                    return@launch
                }
                
                // Set tracking to true immediately to prevent multiple calls
                _trackingState.update { 
                    it.copy(
                        isTracking = true,
                        error = null,
                        lastUpdateTime = System.currentTimeMillis()
                    ) 
                }

                // Start location tracking with retry mechanism
                startLocationTrackingWithRetry(userId)

            } catch (e: Exception) {
                Log.e(TAG, "Error starting location tracking", e)
                _trackingState.update { 
                    it.copy(
                        isTracking = false,
                        error = "Gagal memulai pelacakan lokasi: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Check if all required permissions are granted
     */
    private fun hasRequiredPermissions(): Boolean {
        return try {
            // Use the location service to check permissions
            val hasPermissions = locationService.hasLocationPermissions()
            Log.d(TAG, "Required permissions check result: $hasPermissions")
            hasPermissions
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            false
        }
    }

    /**
     * Start location tracking with retry mechanism
     */
    private suspend fun startLocationTrackingWithRetry(userId: String, attempt: Int = 1) {
        try {
            locationService.startLocationTracking()
                .catch { exception ->
                    Log.e(TAG, "Location tracking error (attempt $attempt)", exception)
                    
                    if (attempt < MAX_RETRY_ATTEMPTS) {
                        Log.d(TAG, "Retrying location tracking in ${RETRY_DELAY_MS}ms...")
                        delay(RETRY_DELAY_MS)
                        startLocationTrackingWithRetry(userId, attempt + 1)
                    } else {
                        val errorMessage = exception.message ?: "Layanan lokasi tidak tersedia. Silakan coba lagi."
                        
                        _trackingState.update { 
                            it.copy(
                                isTracking = false,
                                error = errorMessage
                            ) 
                        }
                    }
                }
                .collect { coordinates ->
                    Log.d(TAG, "Received location update: ${coordinates.latitude}, ${coordinates.longitude}")
                    
                    // Set tracking=true only when we actually receive location updates
                    _trackingState.update { 
                        it.copy(
                            isTracking = true,
                            currentLocation = coordinates,
                            lastUpdateTime = System.currentTimeMillis(),
                            error = null
                        ) 
                    }

                    // Update user location in Firestore
                    updateUserLocationInFirestore(userId, coordinates)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in location tracking", e)
            _trackingState.update { 
                it.copy(
                    isTracking = false,
                    error = "Kesalahan tak terduga: ${e.message}"
                ) 
            }
        }
    }

    /**
     * Stop location tracking
     */
    fun stopLocationTracking() {
        Log.d(TAG, "Stopping location tracking")
        try {
            locationService.stopLocationTracking()
            _trackingState.update { 
                it.copy(
                    isTracking = false,
                    error = null
                ) 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping location tracking", e)
            _trackingState.update { 
                it.copy(
                    isTracking = false,
                    error = "Kesalahan menghentikan pelacakan: ${e.message}"
                ) 
            }
        }
    }

    /**
     * Update user location in Firestore with improved error handling
     */
    private suspend fun updateUserLocationInFirestore(userId: String, coordinates: Coordinates) {
        try {
            val result = userRepository.updateUserLocation(
                userId = userId,
                latitude = coordinates.latitude,
                longitude = coordinates.longitude
            )
            
            if (result.isFailure) {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to update user location in Firestore", error)
                
                val errorMessage = when {
                    error?.message?.contains("permission", ignoreCase = true) == true -> 
                        "Izin ditolak untuk memperbarui lokasi"
                    error?.message?.contains("network", ignoreCase = true) == true -> 
                        "Kesalahan jaringan saat memperbarui lokasi"
                    else -> "Gagal menyimpan lokasi"
                }
                
                _trackingState.update { 
                    it.copy(error = errorMessage) 
                }
            } else {
                Log.d(TAG, "Successfully updated user location in Firestore")
                _trackingState.update { 
                    it.copy(
                        lastSuccessfulUpdate = System.currentTimeMillis(),
                        error = null
                    ) 
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating user location", e)
            _trackingState.update { 
                it.copy(error = "Kesalahan menyimpan lokasi: ${e.message}") 
            }
        }
    }

    /**
     * Clear any error messages
     */
    fun clearError() {
        _trackingState.update { it.copy(error = null) }
    }

    /**
     * Toggle location tracking on/off
     */
    fun toggleLocationTracking() {
        if (_trackingState.value.isTracking) {
            stopLocationTracking()
        } else {
            startLocationTracking()
        }
    }

    /**
     * Check if user is properly authenticated
     */
    fun checkUserAuthentication(): Boolean {
        val userId = userRepository.getCurrentUserId()
        return !userId.isNullOrBlank()
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}

/**
 * Data class representing the location tracking state
 */
data class LocationTrackingState(
    val isTracking: Boolean = false,
    val currentLocation: Coordinates? = null,
    val lastUpdateTime: Long? = null,
    val lastSuccessfulUpdate: Long? = null,
    val error: String? = null
)