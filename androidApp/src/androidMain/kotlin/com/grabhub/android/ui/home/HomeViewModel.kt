package com.grabhub.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grabhub.android.ui.preferences.UiPreferences
import com.grabhub.data.SearchRepository
import com.grabhub.domain.FeedType
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val feedItems: List<ModelItem> = emptyList(),
    val error: String? = null,
)

class HomeViewModel(
    private val searchRepository: SearchRepository,
    private val uiPreferences: UiPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                searchRepository.loadFeed(
                    feedType = FeedType.DISCOVER,
                    filters = SearchFilters(
                        enabledSources = uiPreferences.getEnabledSources().toList(),
                    ),
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        feedItems = result.items,
                        error = if (result.items.isEmpty()) "empty" else null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        feedItems = emptyList(),
                        error = error.message ?: "failed",
                    )
                }
            }
        }
    }
}
