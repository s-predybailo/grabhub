package com.grabhub.domain

import kotlinx.serialization.Serializable

@Serializable
enum class SourceType {
    PRINTABLES,
    THINGIVERSE,
    MAKERWORLD,
    CREALITY_CLOUD,
}

@Serializable
data class ModelItem(
    val id: String,
    val sourceId: String,
    val title: String,
    val imageUrl: String?,
    val previewUrl: String? = null,
    val author: String?,
    val source: SourceType,
    val modelUrl: String,
    val likes: Int? = null,
    val downloads: Int? = null,
    val tags: List<String>? = null,
    val isFree: Boolean? = null,
    val price: Double? = null,
    val license: String? = null,
)

@Serializable
data class SearchQuery(
    val text: String,
    val page: Int = 1,
    val pageSize: Int = 20,
    val filters: SearchFilters = SearchFilters(),
    val feedType: FeedType? = null,
)

@Serializable
data class SearchPage(
    val items: List<ModelItem>,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean,
)

@Serializable
data class SearchResult(
    val items: List<ModelItem>,
    val errors: List<ProviderError> = emptyList(),
)

@Serializable
data class ProviderError(
    val source: SourceType,
    val message: String,
)
