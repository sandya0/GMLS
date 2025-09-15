package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.User
import com.example.gmls.domain.repository.DisasterRepository
import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.data.repository.UserRepositoryImpl
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
    private val disasterRepository: DisasterRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisasterUiState())
    val uiState: StateFlow<DisasterUiState> = _uiState.asStateFlow()

    init {
        loadDisasters()
        loadUsers()
    }

    /**
     * Load all data (disasters and users)
     * Public method for use in admin dashboard
     */
    fun loadData() {
        loadDisasters()
        loadUsers()
    }

    /**
     * Load all disasters
     */
    fun loadDisasters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, success = null) }
            try {
                val result = disasterRepository.getAllDisasters()
                if (result.isSuccess) {
                    _uiState.update { it.copy(disasters = result.getOrDefault(emptyList()), isLoading = false, success = "Bencana berhasil dimuat", error = null) }
                } else {
                    _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "Gagal memuat bencana", isLoading = false, success = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false, success = null) }
            }
        }
    }

    /**
     * Load all users for admin dashboard
     */
    private fun loadUsers() {
        viewModelScope.launch {
            try {
                val users = userRepository.getAllUsers()
                _uiState.update { it.copy(users = users) }
            } catch (e: Exception) {
                // Just log the error but don't update UI state as this is background operation
                e.printStackTrace()
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
                        error = result.exceptionOrNull()?.message ?: "Gagal memfilter bencana"
                    )
                }
            }
        }
    }

    /**
     * Get a specific disaster by ID
     * @param id The disaster ID
     */
    fun getDisasterById(id: String): Disaster? {
        return _uiState.value.disasters.find { it.id == id }
    }

    /**
     * Get disasters by type
     * @param type The disaster type to filter by, or null to show all
     */
    fun getDisastersByType(type: DisasterType?): List<Disaster> {
        return if (type == null) {
            _uiState.value.disasters
        } else {
            _uiState.value.disasters.filter { it.type == type }
        }
    }

    /**
     * Get disasters by status
     * @param status The disaster status to filter by
     */
    fun getDisastersByStatus(status: Disaster.Status): List<Disaster> {
        return _uiState.value.disasters.filter { it.status == status }
    }

    /**
     * Report a new disaster
     * @param report The disaster report
     * @param latLng The latitude and longitude (nullable)
     */
    fun reportDisaster(report: DisasterReport, latLng: Pair<Double, Double>? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val result = if (latLng != null) {
                disasterRepository.reportDisaster(report, latLng.first, latLng.second)
            } else {
                disasterRepository.reportDisaster(report)
            }

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
                        error = result.exceptionOrNull()?.message ?: "Gagal melaporkan bencana"
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
                        error = result.exceptionOrNull()?.message ?: "Gagal memperbarui status bencana"
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
                        error = result.exceptionOrNull()?.message ?: "Gagal mendapatkan bencana terdekat"
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

    fun clearMessages() {
        _uiState.update { it.copy(error = null, success = null) }
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
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUpdating: Boolean = false,
    val submissionSuccess: Boolean = false,
    val error: String? = null,
    val success: String? = null
)
