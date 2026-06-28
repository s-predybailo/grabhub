package com.grabhub.android.ui.feed

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

data class FeedUiState(
    val feedType: FeedType = FeedType.DISCOVER,
    val isLoading: Boolean = true,
    val items: List<ModelItem> = emptyList(),
    val error: String? = null,
)

class FeedViewModel(
    private val searchRepository: SearchRepository,
    private val uiPreferences: UiPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun load(feedType: FeedType) {
        viewModelScope.launch {
            _uiState.update { it.copy(feedType = feedType, isLoading = true, error = null) }
            runCatching {
                searchRepository.loadFeed(
                    feedType = feedType,
                    filters = SearchFilters(
                        enabledSources = uiPreferences.getEnabledSources().toList(),
                    ),
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = result.items,
                        error = if (result.items.isEmpty()) "empty" else null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = emptyList(),
                        error = error.message ?: "failed",
                    )
                }
            }
        }
    }
}
