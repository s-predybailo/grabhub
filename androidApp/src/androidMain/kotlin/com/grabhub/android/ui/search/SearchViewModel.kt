package com.grabhub.android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grabhub.android.ui.preferences.UiPreferences
import com.grabhub.data.SearchRepository
import com.grabhub.domain.LicenseFilter
import com.grabhub.domain.ModelItem
import com.grabhub.domain.PriceFilter
import com.grabhub.domain.ProviderError
import com.grabhub.domain.SearchFilters
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SortOrder
import com.grabhub.domain.SourceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val isLoading: Boolean = false,
    val results: List<ModelItem> = emptyList(),
    val errors: List<ProviderError> = emptyList(),
    val hasSearched: Boolean = false,
    val fatalError: String? = null,
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val uiPreferences: UiPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUiState(
            filters = SearchUiState().filters.copy(
                enabledSources = uiPreferences.getEnabledSources().toList(),
            ),
        ),
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun setPriceFilter(filter: PriceFilter) {
        _uiState.update { it.copy(filters = it.filters.copy(priceFilter = filter)) }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(filters = it.filters.copy(sortOrder = order)) }
    }

    fun setLicenseFilter(filter: LicenseFilter) {
        _uiState.update { it.copy(filters = it.filters.copy(licenseFilter = filter)) }
    }

    fun toggleSource(source: SourceType) {
        _uiState.update { state ->
            val current = state.filters.enabledSources.toMutableSet()
            if (source in current) {
                if (current.size > 1) current.remove(source)
            } else {
                current.add(source)
            }
            uiPreferences.setEnabledSources(current)
            state.copy(filters = state.filters.copy(enabledSources = current.toList()))
        }
    }

    fun search(prefilledQuery: String? = null) {
        prefilledQuery?.let { _uiState.update { state -> state.copy(query = it) } }
        val state = _uiState.value
        val query = (prefilledQuery ?: state.query).trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errors = emptyList(), fatalError = null) }
            runCatching {
                searchRepository.search(
                    SearchQuery(
                        text = query,
                        filters = state.filters,
                    ),
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = result.items,
                        errors = result.errors,
                        hasSearched = true,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = emptyList(),
                        fatalError = error.message ?: "Search failed",
                        hasSearched = true,
                    )
                }
            }
        }
    }
}
