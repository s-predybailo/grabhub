package com.grabhub.data

import com.grabhub.cache.GrabHubDatabase
import com.grabhub.cache.LocalCache
import com.grabhub.cache.createDefaultDatabaseDriverFactory
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoritesRepositoryTest {

    private val cache = LocalCache(GrabHubDatabase(createDefaultDatabaseDriverFactory().createDriver()))
    private val repository = FavoritesRepository(cache)

    private val item = ModelItem(
        id = "printables:42",
        sourceId = "42",
        title = "Benchy",
        imageUrl = null,
        author = "test",
        source = SourceType.PRINTABLES,
        modelUrl = "https://example.com",
    )

    @Test
    fun togglesFavoriteState() {
        assertFalse(repository.isFavorite(item.id))
        assertTrue(repository.toggleFavorite(item))
        assertTrue(repository.isFavorite(item.id))
        assertFalse(repository.toggleFavorite(item))
        assertFalse(repository.isFavorite(item.id))
    }

    @Test
    fun listsFavoritesInReverseInsertionOrder() {
        val second = item.copy(id = "printables:43", sourceId = "43", title = "Dragon")
        repository.toggleFavorite(item)
        repository.toggleFavorite(second)

        assertEquals(2, repository.getFavorites().size)
        assertEquals("Dragon", repository.getFavorites().first().title)
    }
}
