package com.grabhub.data

import com.grabhub.cache.LocalCache
import com.grabhub.domain.ModelItem

class FavoritesRepository(
    private val cache: LocalCache,
) {
    fun getFavorites(): List<ModelItem> = cache.getFavorites()

    fun isFavorite(modelId: String): Boolean = cache.isFavorite(modelId)

    fun toggleFavorite(item: ModelItem): Boolean {
        return if (cache.isFavorite(item.id)) {
            cache.removeFavorite(item.id)
            false
        } else {
            cache.addFavorite(item)
            true
        }
    }

    fun removeFavorite(modelId: String) {
        cache.removeFavorite(modelId)
    }
}
