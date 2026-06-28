package com.grabhub.data

import com.grabhub.cache.LocalCache
import com.grabhub.domain.ModelDetail
import com.grabhub.domain.SourceType
import com.grabhub.providers.DetailProvider

class DetailRepository(
    private val cache: LocalCache,
    private val detailProviders: List<DetailProvider>,
) {
    suspend fun getDetail(modelId: String): ModelDetail? {
        val cachedItem = cache.getModelItem(modelId)
        val source = parseSource(modelId) ?: return cachedItem?.let { ModelDetail(item = it) }
        val sourceId = modelId.substringAfter(':')

        val provider = detailProviders.firstOrNull { it.source == source }
        val remoteDetail = provider?.getDetail(sourceId)

        return remoteDetail
            ?: cachedItem?.let { ModelDetail(item = it) }
    }

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
