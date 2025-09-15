package com.example.gmls.domain.repository

import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.screens.disaster.DisasterReport
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for disaster-related operations
 */
interface DisasterRepository {
    /**
     * Get all disasters
     */
    suspend fun getAllDisasters(): Result<List<Disaster>>

    /**
     * Get disasters filtered by type
     * @param type The disaster type to filter by
     */
    suspend fun getDisastersByType(type: DisasterType): Result<List<Disaster>>

    /**
     * Get a specific disaster by ID
     * @param id The disaster ID
     */
    suspend fun getDisasterById(id: String): Result<Disaster>

    /**
     * Observe changes to the disaster list
     * @return Flow of disaster list updates
     */
    fun observeDisasters(): Flow<List<Disaster>>

    /**
     * Report a new disaster
     * @param report The disaster report data
     * @return Disaster ID if successful
     */
    suspend fun reportDisaster(report: DisasterReport): Result<String>

    /**
     * Report a new disaster with coordinates
     * @param report The disaster report data
     * @param latitude The latitude
     * @param longitude The longitude
     * @return Disaster ID if successful
     */
    suspend fun reportDisaster(report: DisasterReport, latitude: Double, longitude: Double): Result<String>

    /**
     * Update the status of a disaster
     * @param disasterId The disaster ID
     * @param status The new status
     */
    suspend fun updateDisasterStatus(disasterId: String, status: Disaster.Status): Result<Unit>

    /**
     * Get disasters near the user's current location
     * @param radiusKm Maximum distance in kilometers
     */
    suspend fun getNearbyDisasters(radiusKm: Double): Result<List<Disaster>>
}
