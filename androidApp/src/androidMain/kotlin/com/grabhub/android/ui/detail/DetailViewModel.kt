package com.grabhub.android.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grabhub.data.DetailRepository
import com.grabhub.domain.ModelDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val detail: ModelDetail? = null,
    val error: String? = null,
)

class DetailViewModel(
    private val detailRepository: DetailRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun load(modelId: String) {
        viewModelScope.launch {
            _uiState.update { DetailUiState(isLoading = true) }
            runCatching { detailRepository.getDetail(modelId) }
                .onSuccess { detail ->
                    _uiState.update {
                        DetailUiState(
                            isLoading = false,
                            detail = detail,
                            error = if (detail == null) "Model not found" else null,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        DetailUiState(
                            isLoading = false,
                            error = error.message ?: "Failed to load model",
                        )
                    }
                }
        }
    }
}
