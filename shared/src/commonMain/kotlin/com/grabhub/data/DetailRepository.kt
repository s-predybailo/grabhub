package com.grabhub.data

import com.grabhub.cache.LocalCache
import com.grabhub.domain.ModelDetail
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SourceType
import com.grabhub.providers.DetailProvider
import com.grabhub.providers.mergeImageUrls

class DetailRepository(
    private val cache: LocalCache,
    private val detailProviders: List<DetailProvider>,
) {
    suspend fun getDetail(modelId: String): ModelDetail? {
        val cachedItem = cache.getModelItem(modelId)
        val source = parseSource(modelId) ?: return cachedItem?.toCachedDetail()
        val sourceId = modelId.substringAfter(':')

        val provider = detailProviders.firstOrNull { it.source == source }
        val remoteDetail = provider?.getDetail(sourceId)

        return when {
            remoteDetail != null -> remoteDetail.enrichWithCache(cachedItem)
            cachedItem != null -> cachedItem.toCachedDetail()
            else -> null
        }
    }

    private fun ModelDetail.enrichWithCache(cached: ModelItem?): ModelDetail {
        if (cached == null) return this
        return copy(
            images = mergeImageUrls(
                images,
                listOfNotNull(cached.imageUrl, cached.previewUrl),
            ),
        )
    }

    private fun ModelItem.toCachedDetail(): ModelDetail =
        ModelDetail(
            item = this,
            images = listOfNotNull(imageUrl, previewUrl).distinct(),
        )

    private fun parseSource(modelId: String): SourceType? {
        val prefix = modelId.substringBefore(':', missingDelimiterValue = "")
        return when (prefix) {
            "printables" -> SourceType.PRINTABLES
            "thingiverse" -> SourceType.THINGIVERSE
            "makerworld" -> SourceType.MAKERWORLD
            "creality" -> SourceType.CREALITY_CLOUD
            else -> null
        }
    }
}
