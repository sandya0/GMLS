package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.repository.DisasterRepository
import com.example.gmls.ui.screens.disaster.DisasterReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling disaster-related operations
 */
@HiltViewModel
class DisasterViewModel @Inject constructor(
    private val disasterRepository: DisasterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisasterUiState())
    val uiState: StateFlow<DisasterUiState> = _uiState.asStateFlow()

    init {
        loadDisasters()
    }

    /**
     * Load all disasters
     */
    fun loadDisasters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = disasterRepository.getAllDisasters()

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        disasters = result.getOrDefault(emptyList()),
                        filteredDisasters = result.getOrDefault(emptyList()),
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load disasters"
                    )
                }
            }
        }
    }

    /**
     * Filter disasters by type
     * @param type The disaster type to filter by, or null to show all
     */
    fun filterByType(type: DisasterType?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = if (type == null) {
                disasterRepository.getAllDisasters()
            } else {
                disasterRepository.getDisastersByType(type)
            }

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        filteredDisasters = result.getOrDefault(emptyList()),
                        selectedType = type,
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to filter disasters"
                    )
                }
            }
        }
    }

    /**
     * Get a specific disaster by ID
     * @param id The disaster ID
     */
    fun getDisasterById(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }

            val result = disasterRepository.getDisasterById(id)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        selectedDisaster = result.getOrNull(),
                        isLoadingDetail = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingDetail = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to get disaster details"
                    )
                }
            }
        }
    }

    /**
     * Report a new disaster
     * @param report The disaster report
     */
    fun reportDisaster(report: DisasterReport) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val result = disasterRepository.reportDisaster(report)

            if (result.isSuccess) {
                // Refresh the disaster list
                loadDisasters()

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccess = true,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionSuccess = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to report disaster"
                    )
                }
            }
        }
    }

    /**
     * Update the status of a disaster
     * @param disasterId The disaster ID
     * @param status The new status
     */
    fun updateDisasterStatus(disasterId: String, status: Disaster.Status) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }

            val result = disasterRepository.updateDisasterStatus(disasterId, status)

            if (result.isSuccess) {
                // Refresh the disaster detail
                getDisasterById(disasterId)

                // Also refresh the list
                loadDisasters()

                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to update disaster status"
                    )
                }
            }
        }
    }

    /**
     * Get disasters near the user's current location
     * @param radiusKm Maximum distance in kilometers
     */
    fun getNearbyDisasters(radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = disasterRepository.getNearbyDisasters(radiusKm)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        nearbyDisasters = result.getOrDefault(emptyList()),
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to get nearby disasters"
                    )
                }
            }
        }
    }

    /**
     * Clear submission state
     */
    fun clearSubmissionState() {
        _uiState.update {
            it.copy(
                submissionSuccess = false,
                error = null
            )
        }
    }
}

/**
 * Data class representing the UI state for disaster screens
 */
data class DisasterUiState(
    val disasters: List<Disaster> = emptyList(),
    val filteredDisasters: List<Disaster> = emptyList(),
    val nearbyDisasters: List<Disaster> = emptyList(),
    val selectedDisaster: Disaster? = null,
    val selectedType: DisasterType? = null,
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUpdating: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null
)