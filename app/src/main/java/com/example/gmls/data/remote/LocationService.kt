package com.example.gmls.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import kotlinx.coroutines.tasks.await
import android.Manifest

/**
 * Data class to represent geographic coordinates
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/**
 * Service for handling location-related functionality including real-time tracking
 */
class LocationService @Inject constructor(
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationCallback: LocationCallback? = null

    companion object {
        private const val TAG = "LocationService"
        private const val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
        private const val LOCATION_FASTEST_INTERVAL = 15000L // 15 seconds
        private const val LOCATION_DISPLACEMENT = 10f // 10 meters
    }

    /**
     * Check if location services are enabled
     */
    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Check if all required location permissions are granted
     */
    fun hasLocationPermissions(): Boolean {
        try {
            val fineLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            val coarseLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d(TAG, "=== PERMISSION CHECK DETAILS ===")
            Log.d(TAG, "Fine location permission: $fineLocationGranted")
            Log.d(TAG, "Coarse location permission: $coarseLocationGranted")
            Log.d(TAG, "Location services enabled: ${isLocationEnabled()}")
            Log.d(TAG, "GPS provider enabled: ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)}")
            Log.d(TAG, "Network provider enabled: ${locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)}")
            
            val result = fineLocationGranted && coarseLocationGranted
            Log.d(TAG, "Final permission check result: $result")
            Log.d(TAG, "=== END PERMISSION CHECK ===")
            
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Exception during permission check", e)
            return false
        }
    }

    /**
     * Check if background location permission is granted (for background tracking)
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val backgroundLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Background location permission: $backgroundLocationGranted")
            backgroundLocationGranted
        } else {
            true // Not required for older versions
        }
    }

    /**
     * Check location settings and prompt user if needed
     */
    suspend fun checkLocationSettings(locationRequest: LocationRequest): Result<Unit> {
        return try {
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

            val client = LocationServices.getSettingsClient(context)
            client.checkLocationSettings(builder.build()).await()
            Result.success(Unit)
        } catch (e: ResolvableApiException) {
            Log.w(TAG, "Location settings need to be resolved", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Location settings check failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get the current device location
     * Note: Requires location permissions to be granted
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<Coordinates> = suspendCancellableCoroutine { continuation ->
        try {
            // Check if location permissions are granted
            if (!hasLocationPermissions()) {
                Log.e(TAG, "Location permission not granted before attempting getCurrentLocation")
                continuation.resumeWith(Result.failure(Exception("Izin lokasi tidak diberikan.")))
                return@suspendCancellableCoroutine
            }

            // Check if location services are enabled
            if (!isLocationEnabled()) {
                Log.w(TAG, "Location services are disabled")
                continuation.resumeWith(Result.failure(Exception("Layanan lokasi dinonaktifkan. Silakan aktifkan GPS di pengaturan.")))
                return@suspendCancellableCoroutine
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMaxUpdateDelayMillis(15000)
                .build()

            // Check location settings first
            val settingsClient = LocationServices.getSettingsClient(context)
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

            settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener {
                    // Location settings are satisfied, proceed with location request
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            fusedLocationClient.removeLocationUpdates(this)

                            val location = locationResult.lastLocation
                            if (location != null) {
                                val coordinates = Coordinates(location.latitude, location.longitude)
                                Log.d(TAG, "Current location obtained: ${coordinates.latitude}, ${coordinates.longitude}")
                                continuation.resume(Result.success(coordinates))
                            } else {
                                Log.w(TAG, "Location result is null")
                                continuation.resumeWith(Result.failure(Exception("Tidak ada lokasi terbaru yang tersedia.")))
                            }
                        }

                        override fun onLocationAvailability(availability: LocationAvailability) {
                            if (!availability.isLocationAvailable) {
                                Log.w(TAG, "Location is not available")
                                fusedLocationClient.removeLocationUpdates(this)
                                continuation.resumeWith(Result.failure(Exception("Lokasi sementara tidak tersedia.")))
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Location settings check failed", exception)
                    continuation.resumeWith(Result.failure(exception))
                }

        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
            continuation.resumeWith(Result.failure(Exception("Izin lokasi tidak diberikan.")))
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current location", e)
            continuation.resumeWith(Result.failure(Exception("Kesalahan saat mendapatkan lokasi saat ini: ${e.message}")))
        }
    }

    /**
     * Start real-time location tracking
     * Returns a Flow that emits location updates
     * Note: Requires location permissions to be granted
     */
    @SuppressLint("MissingPermission")
    fun startLocationTracking(): Flow<Coordinates> = callbackFlow {
        Log.d(TAG, "=== STARTING LOCATION TRACKING ===")
        
        if (locationCallback != null) {
            Log.w(TAG, "Location tracking is already active. Closing new request.")
            close(IllegalStateException("Location tracking is already active."))
            return@callbackFlow
        }
        
        try {
            // Check permissions first
            if (!hasLocationPermissions()) {
                val errorMsg = "Location permissions not granted. Please grant all required location permissions."
                Log.e(TAG, errorMsg)
                close(Exception(errorMsg))
                return@callbackFlow
            }
            
            // Check if location services are enabled
            if (!isLocationEnabled()) {
                val errorMsg = "Layanan lokasi dinonaktifkan. Silakan aktifkan GPS di pengaturan."
                Log.e(TAG, errorMsg)
                close(Exception(errorMsg))
                return@callbackFlow
            }
            
            Log.d(TAG, "All checks passed, creating location request...")
            
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL
            ).apply {
                setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                setMinUpdateDistanceMeters(LOCATION_DISPLACEMENT)
                setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)
            }.build()
            
            Log.d(TAG, "Location request created successfully")

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    Log.d(TAG, "Received location result")
                    locationResult.lastLocation?.let { location ->
                        val coordinates = Coordinates(location.latitude, location.longitude)
                        Log.d(TAG, "Location update: ${coordinates.latitude}, ${coordinates.longitude}")
                        trySend(coordinates)
                    } ?: Log.w(TAG, "Location result was null")
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    Log.d(TAG, "Location availability changed: ${availability.isLocationAvailable}")
                    if (!availability.isLocationAvailable) {
                        Log.w(TAG, "Location is temporarily unavailable. Waiting for a new location fix...")
                    }
                }
            }

            Log.d(TAG, "Starting location updates...")
            
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback!!,
                    Looper.getMainLooper()
                )
                Log.d(TAG, "Location updates started successfully")
            } catch (e: SecurityException) {
                val errorMsg = "Security exception during location updates: ${e.message}"
                Log.e(TAG, errorMsg, e)
                close(Exception(errorMsg))
            } catch (e: Exception) {
                val errorMsg = "Unexpected exception during location updates: ${e.message}"
                Log.e(TAG, errorMsg, e)
                close(Exception(errorMsg))
            }

            awaitClose {
                Log.d(TAG, "Stopping location tracking in awaitClose")
                stopLocationTracking()
            }
            
        } catch (e: Exception) {
            val errorMsg = "Fatal error in location tracking setup: ${e.message}"
            Log.e(TAG, errorMsg, e)
            close(Exception(errorMsg))
        }
    }

    /**
     * Stop location tracking by removing all location updates
     */
    fun stopLocationTracking() {
        Log.d(TAG, "stopLocationTracking() called")
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
                locationCallback = null
                Log.d(TAG, "Location updates stopped successfully via stopLocationTracking()")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping location updates in stopLocationTracking()", e)
            }
        } ?: Log.d(TAG, "stopLocationTracking() called but locationCallback was null")
    }

    /**
     * Convert an address string to geographic coordinates
     */
    suspend fun geocodeAddress(address: String): Coordinates? {
        return try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.getDefault())

            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocationName(address, 1)

            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                Coordinates(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert coordinates to a readable address
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        return try {
            @Suppress("DEPRECATION")
            val geocoder = Geocoder(context, Locale.getDefault())

            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // Format the address
                val parts = mutableListOf<String>()

                for (i in 0..address.maxAddressLineIndex) {
                    parts.add(address.getAddressLine(i))
                }

                parts.joinToString(", ")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate the distance between two geographic coordinates using the Haversine formula
     * Returns the distance in kilometers
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Test method to verify basic location functionality
     */
    @SuppressLint("MissingPermission")
    suspend fun testLocationAccess(): Result<String> {
        return try {
            Log.d(TAG, "=== TESTING LOCATION ACCESS ===")
            
            if (!hasLocationPermissions()) {
                val error = "Permissions not granted"
                Log.e(TAG, error)
                return Result.failure(Exception(error))
            }
            
            if (!isLocationEnabled()) {
                val error = "Location services disabled"
                Log.e(TAG, error)
                return Result.failure(Exception(error))
            }
            
            Log.d(TAG, "Attempting to get last known location...")
            val lastLocation = fusedLocationClient.lastLocation.await()
            
            if (lastLocation != null) {
                val message = "Last known location: ${lastLocation.latitude}, ${lastLocation.longitude}"
                Log.d(TAG, message)
                return Result.success(message)
            } else {
                val message = "No last known location available, location services appear to be working but no recent location data"
                Log.d(TAG, message)
                return Result.success(message)
            }
            
        } catch (e: SecurityException) {
            val error = "Security exception: ${e.message}"
            Log.e(TAG, error, e)
            Result.failure(Exception(error))
        } catch (e: Exception) {
            val error = "Test failed: ${e.message}"
            Log.e(TAG, error, e)
            Result.failure(Exception(error))
        }
    }
}
