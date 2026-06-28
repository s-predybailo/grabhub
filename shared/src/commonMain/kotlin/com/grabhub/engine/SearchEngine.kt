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
        val enabledSources = query.filters.enabledSources.toSet()
        val activeProviders = providers.filter { provider ->
            enabledSources.isEmpty() || provider.source in enabledSources
        }
        val deferredResults = activeProviders.map { provider ->
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
            items = SearchResultProcessor.process(allItems, query),
            errors = errors,
        )
    }

    private sealed interface ProviderOutcome {
        data class Success(val items: List<ModelItem>) : ProviderOutcome
        data class Failure(val source: SourceType, val cause: Throwable) : ProviderOutcome
    }
}
