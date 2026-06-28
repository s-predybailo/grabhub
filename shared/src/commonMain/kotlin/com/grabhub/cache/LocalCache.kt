package com.grabhub.cache

import com.grabhub.domain.ModelItem
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

    suspend fun putModelItem(item: ModelItem) {
        queries.upsertModelCache(
            id = item.id,
            payload = json.encodeToString(item),
            created_at = clock(),
        )
    }

    private fun purgeExpired() {
        val threshold = clock() - ttlMillis
        queries.deleteExpiredSearchCache(threshold)
        queries.deleteExpiredModelCache(threshold)
    }

    private fun isExpired(createdAt: Long): Boolean = clock() - createdAt > ttlMillis

    companion object {
        const val DEFAULT_TTL_MILLIS = 15 * 60 * 1000L

        fun searchCacheKey(query: String, page: Int, pageSize: Int): String =
            "search:${query.trim().lowercase()}:$page:$pageSize"
    }
}

internal expect fun currentTimeMillis(): Long
