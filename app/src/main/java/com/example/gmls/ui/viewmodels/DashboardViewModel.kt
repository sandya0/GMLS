package com.example.gmls.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.repository.DisasterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale

data class DashboardState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val selectedDisasterType: DisasterType? = null,
    val showSuccessMessage: Boolean = false,
    val error: String? = null,
    val searchResults: List<Disaster> = emptyList(),
    val isSearching: Boolean = false,
    val recentSearches: List<String> = emptyList()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val disasterRepository: DisasterRepository
) : ViewModel() {
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _searchQueryFlow = MutableStateFlow("")

    init {
        // Set up debounced search
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(300) // Wait for 300ms of inactivity before searching
                .distinctUntilChanged() // Only search if the query actually changed
                .filter { it.length >= 2 } // Only search if query is at least 2 characters
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    /**
     * Refresh the dashboard data - consolidated method
     */
    fun refreshData() {
        viewModelScope.launch {
            _dashboardState.update { it.copy(isRefreshing = true, error = null) }
            try {
                val result = disasterRepository.getAllDisasters()
                result.fold(
                    onSuccess = { disasters ->
                        // If we're currently searching, update search results
                        if (_dashboardState.value.searchQuery.isNotEmpty()) {
                            performSearch(_dashboardState.value.searchQuery)
                        }
                        // Clear any existing error state
                        _dashboardState.update { it.copy(error = null) }
                    },
                    onFailure = { error ->
                        _dashboardState.update { it.copy(
                            error = error.message ?: "Gagal menyegarkan data bencana"
                        ) }
                    }
                )
            } catch (e: Exception) {
                _dashboardState.update { it.copy(
                    error = e.message ?: "Terjadi kesalahan yang tidak terduga"
                ) }
            } finally {
                // Ensure we always reset the refreshing state
                _dashboardState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _dashboardState.value = _dashboardState.value.copy(searchQuery = query)
        _searchQueryFlow.value = query
        if (query.isEmpty()) {
            _dashboardState.update { it.copy(
                searchResults = emptyList(),
                isSearching = false,
                error = null
            ) }
        }
    }

    fun selectDisasterType(type: DisasterType?) {
        _dashboardState.value = _dashboardState.value.copy(selectedDisasterType = type)
    }

    fun showSuccessMessage() {
        _dashboardState.value = _dashboardState.value.copy(showSuccessMessage = true)
    }

    fun clearSuccessMessage() {
        _dashboardState.value = _dashboardState.value.copy(showSuccessMessage = false)
    }

    fun setLoading(loading: Boolean) {
        _dashboardState.value = _dashboardState.value.copy(isLoading = loading)
    }

    fun setError(error: String?) {
        _dashboardState.value = _dashboardState.value.copy(error = error)
    }

    private suspend fun performSearch(query: String) {
        _dashboardState.update { it.copy(isSearching = true) }
        try {
            val results = searchDisastersLocally(query)
            _dashboardState.update { it.copy(
                searchResults = results,
                isSearching = false,
                error = null
            ) }
            
            // Add to recent searches if we got results
            if (results.isNotEmpty()) {
                addToRecentSearches(query)
            }
        } catch (e: Exception) {
            _dashboardState.update { it.copy(
                searchResults = emptyList(),
                isSearching = false,
                error = e.message
            ) }
        }
    }

    private suspend fun searchDisastersLocally(query: String): List<Disaster> {
        val normalizedQuery = query.trim().lowercase(Locale.getDefault())
        return try {
            disasterRepository.getAllDisasters().fold(
                onSuccess = { disasters ->
                    disasters.filter { disaster ->
                        disaster.title?.lowercase(Locale.getDefault())?.contains(normalizedQuery) == true ||
                        disaster.description?.lowercase(Locale.getDefault())?.contains(normalizedQuery) == true ||
                        disaster.location?.lowercase(Locale.getDefault())?.contains(normalizedQuery) == true ||
                        disaster.type?.name?.lowercase(Locale.getDefault())?.contains(normalizedQuery) == true
                    }
                },
                onFailure = { throw it }
            )
        } catch (e: Exception) {
                            throw Exception("Gagal mencari bencana: ${e.message}")
        }
    }

    private fun addToRecentSearches(query: String) {
        _dashboardState.update { currentState ->
            val newRecentSearches = (listOf(query) + currentState.recentSearches)
                .distinct()
                .take(5) // Keep only the 5 most recent searches
            currentState.copy(recentSearches = newRecentSearches)
        }
    }

    fun clearRecentSearches() {
        _dashboardState.update { it.copy(recentSearches = emptyList()) }
    }

    fun clearSearchError() {
        _dashboardState.update { it.copy(error = null) }
    }

    fun selectSuggestedSearch(suggestion: String) {
        updateSearchQuery(suggestion)
    }

    fun selectRecentSearch(search: String) {
        updateSearchQuery(search)
    }
} 
