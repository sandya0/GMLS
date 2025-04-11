package com.example.gmls.data.repository

import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.data.remote.LocationService
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.repository.DisasterRepository
import com.example.gmls.ui.screens.disaster.DisasterReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of DisasterRepository that uses Firebase for data storage
 */
class DisasterRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService,
    private val locationService: LocationService
) : DisasterRepository {

    override suspend fun getAllDisasters(): Result<List<Disaster>> {
        return firebaseService.getAllDisasters()
    }

    override suspend fun getDisastersByType(type: DisasterType): Result<List<Disaster>> {
        return firebaseService.getDisastersByType(type)
    }

    override suspend fun getDisasterById(id: String): Result<Disaster> {
        return firebaseService.getDisasterById(id)
    }

    override fun observeDisasters(): Flow<List<Disaster>> = flow {
        val result = getAllDisasters()
        if (result.isSuccess) {
            emit(result.getOrThrow())
        } else {
            emit(emptyList())
        }
    }

    override suspend fun reportDisaster(report: DisasterReport): Result<String> {
        return try {
            // Get current location coordinates if using current location
            val coords = if (report.useCurrentLocation) {
                locationService.getCurrentLocation()
            } else {
                // Try to geocode the provided location address
                locationService.geocodeAddress(report.location)
            }

            if (coords != null) {
                firebaseService.reportDisaster(report, coords.latitude, coords.longitude)
            } else {
                // Fallback to default coordinates if location couldn't be determined
                firebaseService.reportDisaster(report, 0.0, 0.0)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDisasterStatus(disasterId: String, status: Disaster.Status): Result<Unit> {
        return firebaseService.updateDisasterStatus(disasterId, status)
    }

    override suspend fun getNearbyDisasters(radiusKm: Double): Result<List<Disaster>> {
        return try {
            val currentLocation = locationService.getCurrentLocation()
            if (currentLocation != null) {
                val allDisasters = getAllDisasters().getOrDefault(emptyList())

                // Filter disasters by distance
                val nearbyDisasters = allDisasters.filter { disaster ->
                    val distance = locationService.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        disaster.latitude, disaster.longitude
                    )
                    distance <= radiusKm
                }

                Result.success(nearbyDisasters)
            } else {
                Result.failure(Exception("Could not get current location"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}