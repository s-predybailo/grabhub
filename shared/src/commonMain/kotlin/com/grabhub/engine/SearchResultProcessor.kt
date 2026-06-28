package com.grabhub.engine

import com.grabhub.domain.LicenseFilter
import com.grabhub.domain.ModelItem
import com.grabhub.domain.PriceFilter
import com.grabhub.domain.SearchFilters
import com.grabhub.domain.SortOrder

object SearchResultProcessor {

    fun process(items: List<ModelItem>, query: String, filters: SearchFilters): List<ModelItem> {
        val deduped = ResultDeduplicator.deduplicateAndSort(items, query, filters.sortOrder)
        return applyFilters(deduped, filters)
    }

    fun applyFilters(items: List<ModelItem>, filters: SearchFilters): List<ModelItem> =
        items.filter { item -> matchesPrice(item, filters.priceFilter) && matchesLicense(item, filters.licenseFilter) }

    private fun matchesPrice(item: ModelItem, filter: PriceFilter): Boolean = when (filter) {
        PriceFilter.ALL -> true
        PriceFilter.FREE_ONLY -> item.isFree != false
        PriceFilter.PAID_ONLY -> item.isFree == false || (item.price ?: 0.0) > 0.0
    }

    private fun matchesLicense(item: ModelItem, filter: LicenseFilter): Boolean {
        val license = item.license?.uppercase().orEmpty()
        return when (filter) {
            LicenseFilter.ALL -> true
            LicenseFilter.COMMERCIAL_OK -> {
                if (license.isBlank()) item.isFree != false else {
                    !license.contains("NC") && !license.contains("ND")
                }
            }
            LicenseFilter.NON_COMMERCIAL -> license.contains("NC")
        }
    }
}
