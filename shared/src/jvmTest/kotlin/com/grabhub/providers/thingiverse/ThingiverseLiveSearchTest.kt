package com.grabhub.providers.thingiverse

import com.grabhub.domain.SearchFilters
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType
import com.grabhub.network.createHttpClient
import kotlinx.coroutines.test.runTest
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertTrue

class ThingiverseLiveSearchTest {

    @Test
    fun searchBoxReturnsResults() = runTest {
        val token = readToken()
        if (token.isBlank()) {
            println("Skipping live test: THINGIVERSE_ACCESS_TOKEN not set")
            return@runTest
        }

        val provider = ThingiverseProvider(createHttpClient(), token)
        val page = provider.search(
            SearchQuery(
                text = "box",
                filters = SearchFilters(enabledSources = listOf(SourceType.THINGIVERSE)),
            ),
        )

        println("Thingiverse hits: ${page.items.size}, first=${page.items.firstOrNull()?.title}")
        assertTrue(page.items.isNotEmpty(), "Expected Thingiverse results for query 'box'")
    }

    private fun readToken(): String {
        System.getenv("THINGIVERSE_ACCESS_TOKEN")?.takeIf { it.isNotBlank() }?.let { return it }
        val file = java.io.File("local.properties")
        if (!file.exists()) return ""
        return Properties().apply { file.inputStream().use(::load) }
            .getProperty("THINGIVERSE_ACCESS_TOKEN")
            .orEmpty()
    }
}
