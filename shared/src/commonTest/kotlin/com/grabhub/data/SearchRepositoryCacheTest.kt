package com.grabhub.data

import com.grabhub.cache.GrabHubDatabase
import com.grabhub.cache.LocalCache
import com.grabhub.cache.createDefaultDatabaseDriverFactory
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType
import com.grabhub.engine.SearchEngine
import com.grabhub.providers.SearchProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchRepositoryCacheTest {

    @Test
    fun returnsCachedSearchResultOnSecondCall() = runTest {
        val provider = CountingProvider()
        val cache = LocalCache(GrabHubDatabase(createDefaultDatabaseDriverFactory().createDriver()))
        val repository = SearchRepository(SearchEngine(listOf(provider)), cache)

        repository.search(SearchQuery(text = "benchy"))
        repository.search(SearchQuery(text = "benchy"))

        assertEquals(1, provider.calls)
    }

    private class CountingProvider : SearchProvider {
        override val source = SourceType.PRINTABLES
        var calls = 0

        override suspend fun search(query: SearchQuery): com.grabhub.domain.SearchPage {
            calls++
            return com.grabhub.domain.SearchPage(
                items = listOf(
                    ModelItem(
                        id = "printables:1",
                        sourceId = "1",
                        title = "Benchy",
                        imageUrl = null,
                        author = "test",
                        source = source,
                        modelUrl = "https://example.com",
                    ),
                ),
                page = query.page,
                pageSize = query.pageSize,
                hasMore = false,
            )
        }
    }
}
