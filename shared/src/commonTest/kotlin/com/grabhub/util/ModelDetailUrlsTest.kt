package com.grabhub.util

import com.grabhub.domain.ModelDetail
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelDetailUrlsTest {

    @Test
    fun thingiverseSectionUrls() {
        val detail = detail(
            source = SourceType.THINGIVERSE,
            modelUrl = "https://www.thingiverse.com/thing:123",
        )
        assertEquals("https://www.thingiverse.com/thing:123/comments", detail.externalUrl(ModelDetailSection.COMMENTS))
        assertEquals("https://www.thingiverse.com/thing:123/makes", detail.externalUrl(ModelDetailSection.MAKES))
        assertEquals("https://www.thingiverse.com/thing:123/files", detail.externalUrl(ModelDetailSection.FILES))
    }

    @Test
    fun printablesSectionUrls() {
        val detail = detail(
            source = SourceType.PRINTABLES,
            modelUrl = "https://www.printables.com/model/1-box",
        )
        assertEquals("https://www.printables.com/model/1-box#comments", detail.externalUrl(ModelDetailSection.COMMENTS))
    }

    private fun detail(source: SourceType, modelUrl: String) = ModelDetail(
        item = ModelItem(
            id = "test:1",
            sourceId = "1",
            title = "Test",
            imageUrl = null,
            author = null,
            source = source,
            modelUrl = modelUrl,
        ),
    )
}
