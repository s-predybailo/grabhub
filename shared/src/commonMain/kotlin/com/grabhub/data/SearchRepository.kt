package com.grabhub.data

import com.grabhub.cache.LocalCache
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SearchResult
import com.grabhub.engine.SearchEngine

class SearchRepository(
    private val searchEngine: SearchEngine,
    private val cache: LocalCache,
) {
    suspend fun search(query: SearchQuery): SearchResult {
        require(query.text.isNotBlank()) { "Search query must not be blank" }
        require(query.page >= 1) { "Page must be >= 1" }
        require(query.pageSize in 1..50) { "Page size must be between 1 and 50" }

        val cacheKey = LocalCache.searchCacheKey(query.text, query.page, query.pageSize)
        cache.getSearchResult(cacheKey)?.let { return it }

        val result = searchEngine.search(query)
        cache.putSearchResult(cacheKey, result)
        return result
    }
}
