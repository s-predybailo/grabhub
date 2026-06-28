package com.grabhub.android.ui.favorites

import androidx.lifecycle.ViewModel
import com.grabhub.data.FavoritesRepository
import com.grabhub.domain.ModelItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _items = MutableStateFlow(favoritesRepository.getFavorites())
    val items: StateFlow<List<ModelItem>> = _items.asStateFlow()

    fun refresh() {
        _items.value = favoritesRepository.getFavorites()
    }

    fun remove(modelId: String) {
        favoritesRepository.removeFavorite(modelId)
        refresh()
    }
}
