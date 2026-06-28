package com.grabhub.domain

import com.grabhub.domain.SourceType
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelDetailExtensionsTest {

    @Test
    fun carouselImages_mergesItemAndGalleryWithoutDuplicates() {
        val detail = ModelDetail(
            item = ModelItem(
                id = "printables:1",
                sourceId = "1",
                title = "Box",
                imageUrl = "https://media.printables.com/a.png",
                previewUrl = "https://media.printables.com/b.png",
                author = null,
                source = SourceType.PRINTABLES,
                modelUrl = "https://example.com",
            ),
            images = listOf(
                "https://media.printables.com/b.png",
                "https://media.printables.com/c.png",
            ),
        )

        assertEquals(
            listOf(
                "https://media.printables.com/a.png",
                "https://media.printables.com/b.png",
                "https://media.printables.com/c.png",
            ),
            detail.carouselImages(),
        )
    }
}
