package com.example.gmls.domain.usecase

import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.repository.DisasterRepository
import com.example.gmls.ui.screens.disaster.DisasterReport
import javax.inject.Inject

/**
 * Use case for getting all disasters
 */
class GetAllDisastersUseCase @Inject constructor(
    private val disasterRepository: DisasterRepository
) {
    suspend operator fun invoke(): Result<List<Disaster>> {
        return disasterRepository.getAllDisasters()
    }
}

/**
 * Use case for getting disasters by type
 */
class GetDisastersByTypeUseCase @Inject constructor(
    private val disasterRepository: DisasterRepository
) {
    suspend operator fun invoke(type: DisasterType): Result<List<Disaster>> {
        return disasterRepository.getDisastersByType(type)
    }
}

/**
 * Use case for getting a specific disaster by ID
 */
class GetDisasterByIdUseCase @Inject constructor(
    private val disasterRepository: DisasterRepository
) {
    suspend operator fun invoke(id: String): Result<Disaster> {
        return disasterRepository.getDisasterById(id)
    }
}

/**
 * Use case for reporting a new disaster
 */
class ReportDisasterUseCase @Inject constructor(
    private val disasterRepository: DisasterRepository
) {
    suspend operator fun invoke(report: DisasterReport): Result<String> {
        return disasterRepository.reportDisaster(report)
    }
}

/**
 * Use case for updating a disaster's status
 */
class UpdateDisasterStatusUseCase @Inject constructor(
    private val disasterRepository: DisasterRepository
) {
    suspend operator fun invoke(disasterId: String, status: Disaster.Status): Result<Unit> {
        return disasterRepository.updateDisasterStatus(disasterId, status)
    }
}

/**
 * Use case for getting disasters near the user's current location
 */
class GetNearbyDisastersUseCase @Inject constructor(
    private val disasterRepository: DisasterRepository
) {
    suspend operator fun invoke(radiusKm: Double = 10.0): Result<List<Disaster>> {
        return disasterRepository.getNearbyDisasters(radiusKm)
    }
}
