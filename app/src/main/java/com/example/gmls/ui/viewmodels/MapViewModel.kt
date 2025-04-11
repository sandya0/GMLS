package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.data.remote.Coordinates
import com.example.gmls.data.remote.LocationService
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.repository.DisasterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling map-related operations
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val disasterRepository: DisasterRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadDisasters()
        getCurrentLocation()
    }

    /**
     * Load all disasters for the map
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
            val disasters = _uiState.value.disasters

            val filtered = if (type == null) {
                disasters
            } else {
                disasters.filter { it.type == type }
            }

            _uiState.update {
                it.copy(
                    filteredDisasters = filtered,
                    selectedType = type
                )
            }
        }
    }

    /**
     * Get the user's current location
     */
    fun getCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true) }

            val location = locationService.getCurrentLocation()

            _uiState.update {
                it.copy(
                    currentLocation = location,
                    isLoadingLocation = false
                )
            }
        }
    }

    /**
     * Select a disaster on the map
     * @param disaster The selected disaster
     */
    fun selectDisaster(disaster: Disaster) {
        _uiState.update {
            it.copy(
                selectedDisaster = disaster
            )
        }
    }

    /**
     * Clear selected disaster
     */
    fun clearSelectedDisaster() {
        _uiState.update {
            it.copy(
                selectedDisaster = null
            )
        }
    }

    /**
     * Set the map's camera position
     * @param latitude Latitude
     * @param longitude Longitude
     * @param zoom Zoom level
     */
    fun setMapPosition(latitude: Double, longitude: Double, zoom: Float) {
        _uiState.update {
            it.copy(
                mapPosition = MapPosition(latitude, longitude, zoom)
            )
        }
    }
}

/**
 * Data class representing the UI state for map screens
 */
data class MapUiState(
    val disasters: List<Disaster> = emptyList(),
    val filteredDisasters: List<Disaster> = emptyList(),
    val selectedDisaster: Disaster? = null,
    val selectedType: DisasterType? = null,
    val currentLocation: Coordinates? = null,
    val mapPosition: MapPosition = MapPosition(-6.200000, 106.816666, 10f), // Default to Jakarta
    val isLoading: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val error: String? = null
)

/**
 * Data class representing the map camera position
 */
data class MapPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float
)