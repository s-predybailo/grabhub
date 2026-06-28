package com.grabhub.cache

import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchFilters
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SearchResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalCache(
    database: GrabHubDatabase,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
) {
    private val queries = database.searchCacheQueries
    private val clock = { currentTimeMillis() }

    suspend fun getSearchResult(cacheKey: String): SearchResult? {
        purgeExpired()
        val row = queries.getSearchCache(cacheKey).executeAsOneOrNull() ?: return null
        if (isExpired(row.created_at)) return null
        return json.decodeFromString<SearchResult>(row.payload)
    }

    suspend fun putSearchResult(cacheKey: String, result: SearchResult) {
        purgeExpired()
        queries.upsertSearchCache(
            cache_key = cacheKey,
            payload = json.encodeToString(result),
            created_at = clock(),
        )
        result.items.forEach { putModelItem(it) }
    }

    suspend fun getModelItem(id: String): ModelItem? {
        purgeExpired()
        val row = queries.getModelCache(id).executeAsOneOrNull() ?: return null
        if (isExpired(row.created_at)) return null
        return json.decodeFromString<ModelItem>(row.payload)
    }

    fun putModelItem(item: ModelItem) {
        queries.upsertModelCache(
            id = item.id,
            payload = json.encodeToString(item),
            created_at = clock(),
        )
    }

    fun getFavorites(): List<ModelItem> =
        queries.getAllFavorites().executeAsList().map { row ->
            json.decodeFromString<ModelItem>(row.payload)
        }

    fun isFavorite(id: String): Boolean =
        queries.isFavorite(id).executeAsOne() > 0

    fun addFavorite(item: ModelItem) {
        putModelItem(item)
        queries.upsertFavorite(
            id = item.id,
            payload = json.encodeToString(item),
            added_at = clock(),
        )
    }

    fun removeFavorite(id: String) {
        queries.deleteFavorite(id)
    }

    fun getSearchHistory(limit: Long = HISTORY_LIMIT): List<SearchHistoryEntry> =
        queries.getSearchHistory(limit).executeAsList().map { row ->
            SearchHistoryEntry(
                query = row.query,
                filters = json.decodeFromString<SearchFilters>(row.filters_payload),
                searchedAt = row.searched_at,
            )
        }

    fun addSearchHistory(query: SearchQuery) {
        queries.upsertSearchHistory(
            query = query.text.trim(),
            filters_payload = json.encodeToString(query.filters),
            searched_at = clock(),
        )
    }

    fun removeSearchHistory(query: String) {
        queries.deleteSearchHistory(query)
    }

    fun clearSearchHistory() {
        queries.clearSearchHistory()
    }

    private fun purgeExpired() {
        val threshold = clock() - ttlMillis
        queries.deleteExpiredSearchCache(threshold)
        queries.deleteExpiredModelCache(threshold)
    }

    private fun isExpired(createdAt: Long): Boolean = clock() - createdAt > ttlMillis

    companion object {
        const val DEFAULT_TTL_MILLIS = 15 * 60 * 1000L
        const val HISTORY_LIMIT = 50L

        fun searchCacheKey(query: SearchQuery, json: Json = Json { ignoreUnknownKeys = true }): String =
            "search:${query.text.trim().lowercase()}:${query.page}:${query.pageSize}:${json.encodeToString(query.filters)}"
    }
}

data class SearchHistoryEntry(
    val query: String,
    val filters: SearchFilters,
    val searchedAt: Long,
)

internal expect fun currentTimeMillis(): Long
