package com.grabhub.data

import com.grabhub.cache.LocalCache
import com.grabhub.cache.SearchHistoryEntry
import com.grabhub.domain.SearchQuery

class HistoryRepository(
    private val cache: LocalCache,
) {
    fun getHistory(): List<SearchHistoryEntry> = cache.getSearchHistory()

    fun addSearch(query: SearchQuery) {
        cache.addSearchHistory(query)
    }

    fun removeEntry(query: String) {
        cache.removeSearchHistory(query)
    }

    fun clearAll() {
        cache.clearSearchHistory()
    }
}
