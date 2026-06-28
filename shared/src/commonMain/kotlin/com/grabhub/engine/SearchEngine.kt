package com.grabhub.engine

import com.grabhub.domain.ModelItem
import com.grabhub.domain.ProviderError
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SearchResult
import com.grabhub.domain.SourceType
import com.grabhub.providers.SearchProvider
import com.grabhub.providers.thingiverse.ThingiverseNotConfiguredException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SearchEngine(
    private val providers: List<SearchProvider>,
) {

    suspend fun search(query: SearchQuery): SearchResult = coroutineScope {
        val deferredResults = providers.map { provider ->
            async {
                runCatching { provider.search(query) }
                    .fold(
                        onSuccess = { ProviderOutcome.Success(it.items) },
                        onFailure = { ProviderOutcome.Failure(provider.source, it) },
                    )
            }
        }

        val outcomes = deferredResults.awaitAll()
        val errors = outcomes.filterIsInstance<ProviderOutcome.Failure>()
            .map { outcome ->
                ProviderError(
                    source = outcome.source,
                    message = when (val cause = outcome.cause) {
                        is ThingiverseNotConfiguredException -> cause.message ?: "Thingiverse not configured"
                        else -> cause.message ?: "Unknown error"
                    },
                )
            }

        val allItems = outcomes.filterIsInstance<ProviderOutcome.Success>()
            .flatMap { it.items }

        SearchResult(
            items = deduplicateAndSort(allItems, query.text),
            errors = errors,
        )
    }

    private fun deduplicateAndSort(items: List<ModelItem>, query: String): List<ModelItem> {
        val normalizedQuery = query.trim().lowercase()
        val seen = mutableSetOf<String>()

        return items
            .sortedByDescending { scoreItem(it, normalizedQuery) }
            .filter { item ->
                val key = "${item.title.lowercase()}|${item.author?.lowercase() ?: ""}"
                seen.add(key)
            }
    }

    private fun scoreItem(item: ModelItem, query: String): Int {
        val title = item.title.lowercase()
        var score = 0
        if (title == query) score += 100
        if (title.contains(query)) score += 50
        score += (item.likes ?: 0) / 100
        score += (item.downloads ?: 0) / 1000
        return score
    }

    private sealed interface ProviderOutcome {
        data class Success(val items: List<ModelItem>) : ProviderOutcome
        data class Failure(val source: SourceType, val cause: Throwable) : ProviderOutcome
    }
}
