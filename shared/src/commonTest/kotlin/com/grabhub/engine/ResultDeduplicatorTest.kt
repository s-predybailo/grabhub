package com.grabhub.engine

import com.grabhub.domain.ModelItem
import com.grabhub.domain.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultDeduplicatorTest {

    @Test
    fun removesDuplicateTitlesFromDifferentSources() {
        val items = listOf(
            model("printables:1", "Benchy", SourceType.PRINTABLES, likes = 10),
            model("makerworld:2", "Benchy", SourceType.MAKERWORLD, likes = 100),
            model("creality:3", "Dragon", SourceType.CREALITY_CLOUD),
        )

        val result = ResultDeduplicator.deduplicateAndSort(items, "benchy")

        assertEquals(2, result.size)
        assertEquals("makerworld:2", result.first().id)
    }

    @Test
    fun normalizesTitlesBeforeDedup() {
        val items = listOf(
            model("printables:1", "3D Benchy!", SourceType.PRINTABLES),
            model("makerworld:2", "3d benchy", SourceType.MAKERWORLD, likes = 5),
        )

        val result = ResultDeduplicator.deduplicateAndSort(items, "benchy")

        assertEquals(1, result.size)
    }

    private fun model(
        id: String,
        title: String,
        source: SourceType,
        likes: Int? = null,
    ) = ModelItem(
        id = id,
        sourceId = id.substringAfter(':'),
        title = title,
        imageUrl = null,
        author = "maker",
        source = source,
        modelUrl = "https://example.com/$id",
        likes = likes,
    )
}
