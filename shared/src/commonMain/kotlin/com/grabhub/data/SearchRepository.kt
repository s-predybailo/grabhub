package com.grabhub.data

import com.grabhub.cache.LocalCache
import com.grabhub.domain.FeedType
import com.grabhub.domain.SearchFilters
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SearchResult
import com.grabhub.engine.SearchEngine

class SearchRepository(
    private val searchEngine: SearchEngine,
    private val cache: LocalCache,
    private val historyRepository: HistoryRepository,
) {
    suspend fun search(query: SearchQuery): SearchResult {
        val isFeed = query.feedType != null
        require(isFeed || query.text.isNotBlank()) { "Search query must not be blank" }
        require(query.page >= 1) { "Page must be >= 1" }
        require(query.pageSize in 1..50) { "Page size must be between 1 and 50" }

        val cacheKey = LocalCache.searchCacheKey(query)
        cache.getSearchResult(cacheKey)?.let { cached ->
            if (!isFeed) {
                historyRepository.addSearch(query)
            }
            return cached
        }

        val result = searchEngine.search(query)
        if (shouldCache(result)) {
            cache.putSearchResult(cacheKey, result)
        }
        if (!isFeed) {
            historyRepository.addSearch(query)
        }
        return result
    }

    suspend fun loadFeed(
        feedType: FeedType,
        filters: SearchFilters = SearchFilters(),
        pageSize: Int = 20,
    ): SearchResult = search(
        SearchQuery(
            text = "",
            pageSize = pageSize,
            filters = filters,
            feedType = feedType,
        ),
    )

    private fun shouldCache(result: SearchResult): Boolean {
        // Avoid caching transient provider failures (e.g. parse errors) as empty results.
        if (result.items.isNotEmpty()) return true
        return result.errors.isEmpty()
    }
}
