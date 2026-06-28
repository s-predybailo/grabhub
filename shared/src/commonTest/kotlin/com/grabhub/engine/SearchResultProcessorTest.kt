package com.grabhub.engine

import com.grabhub.domain.LicenseFilter
import com.grabhub.domain.ModelItem
import com.grabhub.domain.PriceFilter
import com.grabhub.domain.SearchFilters
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SortOrder
import com.grabhub.domain.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchResultProcessorTest {

    @Test
    fun filtersFreeModelsOnly() {
        val items = listOf(
            model("1", isFree = true),
            model("2", isFree = false, price = 4.99),
            model("3", isFree = null),
        )
        val filters = SearchFilters(priceFilter = PriceFilter.FREE_ONLY)

        val result = SearchResultProcessor.applyFilters(items, filters)

        assertEquals(2, result.size)
        assertTrue(result.none { it.id == "printables:2" })
    }

    @Test
    fun sortsByPopularity() {
        val items = listOf(
            model("1", likes = 1, downloads = 10),
            model("2", likes = 100, downloads = 1000),
        )
        val filters = SearchFilters(sortOrder = SortOrder.POPULARITY)

        val result = SearchResultProcessor.process(
            items,
            SearchQuery(text = "test", filters = filters),
        )

        assertEquals("printables:2", result.first().id)
    }

    @Test
    fun filtersNonCommercialLicense() {
        val items = listOf(
            model("1", license = "CC BY-NC-SA"),
            model("2", license = "CC BY"),
        )
        val filters = SearchFilters(licenseFilter = LicenseFilter.NON_COMMERCIAL)

        val result = SearchResultProcessor.applyFilters(items, filters)

        assertEquals(1, result.size)
        assertEquals("printables:1", result.first().id)
    }

    private fun model(
        id: String,
        isFree: Boolean? = true,
        price: Double? = null,
        likes: Int? = null,
        downloads: Int? = null,
        license: String? = null,
    ) = ModelItem(
        id = "printables:$id",
        sourceId = id,
        title = "Model $id",
        imageUrl = null,
        author = "author",
        source = SourceType.PRINTABLES,
        modelUrl = "https://example.com/$id",
        isFree = isFree,
        price = price,
        likes = likes,
        downloads = downloads,
        license = license,
    )
}
