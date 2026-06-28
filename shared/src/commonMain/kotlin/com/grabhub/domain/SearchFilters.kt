package com.grabhub.domain

import kotlinx.serialization.Serializable

@Serializable
enum class PriceFilter {
    ALL,
    FREE_ONLY,
    PAID_ONLY,
}

@Serializable
enum class SortOrder {
    RELEVANCE,
    POPULARITY,
}

@Serializable
enum class LicenseFilter {
    ALL,
    COMMERCIAL_OK,
    NON_COMMERCIAL,
}

@Serializable
data class SearchFilters(
    val priceFilter: PriceFilter = PriceFilter.ALL,
    val sortOrder: SortOrder = SortOrder.RELEVANCE,
    val licenseFilter: LicenseFilter = LicenseFilter.ALL,
    val enabledSources: List<SourceType> = SourceType.entries,
)
