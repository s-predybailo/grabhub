package com.grabhub.providers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageUrlsTest {

    @Test
    fun printablesMediaUrl_buildsAbsoluteUrl() {
        assertEquals(
            "https://media.printables.com/media/prints/1/a.png",
            printablesMediaUrl("media/prints/1/a.png"),
        )
    }

    @Test
    fun normalizeImageUrl_supportsProtocolRelativeUrls() {
        assertEquals(
            "https://cdn.example.com/a.jpg",
            normalizeImageUrl("//cdn.example.com/a.jpg"),
        )
    }

    @Test
    fun normalizeImageUrl_returnsNullForBlank() {
        assertNull(normalizeImageUrl("   "))
    }

    @Test
    fun mergeImageUrls_deduplicatesPreservingOrder() {
        assertEquals(
            listOf("https://a.test/1.jpg", "https://a.test/2.jpg"),
            mergeImageUrls(
                listOf("https://a.test/1.jpg", "https://a.test/1.jpg"),
                listOf("https://a.test/2.jpg"),
            ),
        )
    }
}
