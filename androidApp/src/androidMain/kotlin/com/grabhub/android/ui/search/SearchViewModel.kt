package com.grabhub.android.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grabhub.data.SearchRepository
import com.grabhub.domain.ModelItem
import com.grabhub.domain.ProviderError
import com.grabhub.domain.SearchQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<ModelItem> = emptyList(),
    val errors: List<ProviderError> = emptyList(),
    val hasSearched: Boolean = false,
    val fatalError: String? = null,
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errors = emptyList(), fatalError = null) }
            runCatching {
                searchRepository.search(SearchQuery(text = query))
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
