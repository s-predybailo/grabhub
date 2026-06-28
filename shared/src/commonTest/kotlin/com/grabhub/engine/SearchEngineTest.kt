package com.grabhub.engine

import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType
import com.grabhub.providers.SearchProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchEngineTest {

    @Test
    fun mergesResultsFromMultipleProviders() = runTest {
        val printables = FakeProvider(
            source = SourceType.PRINTABLES,
            items = listOf(
                model("printables:1", "Benchy", SourceType.PRINTABLES, likes = 100),
            ),
        )
        val thingiverse = FakeProvider(
            source = SourceType.THINGIVERSE,
            items = listOf(
                model("thingiverse:2", "Benchy Remix", SourceType.THINGIVERSE, likes = 50),
            ),
        )

        val engine = SearchEngine(listOf(printables, thingiverse))
        val result = engine.search(SearchQuery(text = "benchy"))

        assertEquals(2, result.items.size)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun continuesWhenOneProviderFails() = runTest {
        val printables = FakeProvider(
            source = SourceType.PRINTABLES,
            items = listOf(model("printables:1", "Benchy", SourceType.PRINTABLES)),
        )
        val failing = object : SearchProvider {
            override val source = SourceType.THINGIVERSE
            override suspend fun search(query: SearchQuery) = error("API down")
        }

        val engine = SearchEngine(listOf(printables, failing))
        val result = engine.search(SearchQuery(text = "benchy"))

        assertEquals(1, result.items.size)
        assertEquals(1, result.errors.size)
        assertEquals(SourceType.THINGIVERSE, result.errors.first().source)
    }

    @Test
    fun skipsDisabledProviders() = runTest {
        val printables = FakeProvider(
            source = SourceType.PRINTABLES,
            items = listOf(model("printables:1", "Benchy", SourceType.PRINTABLES)),
        )
        val thingiverse = FakeProvider(
            source = SourceType.THINGIVERSE,
            items = listOf(model("thingiverse:2", "Benchy Remix", SourceType.THINGIVERSE)),
        )

        val engine = SearchEngine(listOf(printables, thingiverse))
        val result = engine.search(
            SearchQuery(
                text = "benchy",
                filters = com.grabhub.domain.SearchFilters(
                    enabledSources = listOf(SourceType.PRINTABLES),
                ),
            ),
        )

        assertEquals(1, result.items.size)
        assertEquals(SourceType.PRINTABLES, result.items.first().source)
        assertTrue(result.errors.isEmpty())
    }

    private fun model(
        id: String,
        title: String,
        source: SourceType,
        likes: Int? = null,
    ) = ModelItem(
        id = id,
        sourceId = id.substringAfter(':'),
        title = title,
        imageUrl = null,
        author = "author",
        source = source,
        modelUrl = "https://example.com/$id",
        likes = likes,
    )

    private class FakeProvider(
        override val source: SourceType,
        private val items: List<ModelItem>,
    ) : SearchProvider {
        override suspend fun search(query: SearchQuery) = com.grabhub.domain.SearchPage(
            items = items,
            page = query.page,
            pageSize = query.pageSize,
            hasMore = false,
        )
    }
}
