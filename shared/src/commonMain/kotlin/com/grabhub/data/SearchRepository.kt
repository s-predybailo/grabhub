package com.grabhub.data

import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SearchResult
import com.grabhub.engine.SearchEngine

class SearchRepository(
    private val searchEngine: SearchEngine,
) {
    suspend fun search(query: SearchQuery): SearchResult {
        require(query.text.isNotBlank()) { "Search query must not be blank" }
        require(query.page >= 1) { "Page must be >= 1" }
        require(query.pageSize in 1..50) { "Page size must be between 1 and 50" }
        return searchEngine.search(query)
    }
}
