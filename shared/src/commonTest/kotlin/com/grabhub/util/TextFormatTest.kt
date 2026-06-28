package com.grabhub.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextFormatTest {

    @Test
    fun stripsHtmlFromDescription() {
        val result = formatModelDescription("<p>Hello<br/>world</p>")
        assertTrue(result?.contains("Hello") == true)
        assertTrue(result?.contains("world") == true)
    }

    @Test
    fun formatsLargeCounts() {
        assertEquals("48k", formatCount(48764))
        assertEquals("1.4M", formatCount(1_354_738))
    }
}
