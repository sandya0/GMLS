package com.example.gmls.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.*

/**
 * Data class to represent geographic coordinates
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

/**
 * Service for handling location-related functionality
 */
class LocationService @Inject constructor(
    private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Get the current device location
     * Note: Requires location permissions to be granted
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Coordinates? = suspendCancellableCoroutine { continuation ->
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)

                    val location = locationResult.lastLocation
                    if (location != null) {
                        val coordinates = Coordinates(location.latitude, location.longitude)
                        continuation.resume(coordinates)
                    } else {
                        continuation.resume(null)
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

        } catch (e: Exception) {
            continuation.resume(null)
        }
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
}