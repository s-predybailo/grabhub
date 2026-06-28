package com.grabhub.providers.thingiverse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ThingiverseSearchParseTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun parsesRealSearchPayloadWithObjectTags() {
        val payload = """
            {
              "total": 10000,
              "hits": [
                {
                  "id": 1559232,
                  "name": "Venus Box",
                  "public_url": "https://www.thingiverse.com/thing:1559232",
                  "thumbnail": "https://cdn.thingiverse.com/assets/63/c6/cd/22/bd/venus1.jpg",
                  "like_count": 48764,
                  "tags": [
                    {"name": "box", "tag": "box", "url": "https://api.thingiverse.com/tags/box"}
                  ],
                  "creator": {"name": "Prot0typ1cal", "public_name": "Prot0typ1cal"}
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<ThingiverseSearchResponseForTest>(payload)
        assertEquals(1, response.hits.orEmpty().size)
        assertEquals("Venus Box", response.hits?.first()?.name)
        assertEquals("box", response.hits?.first()?.tags?.first()?.name)
    }
}

@Serializable
private data class ThingiverseSearchResponseForTest(
    val hits: List<ThingiverseHitForTest>? = null,
    val total: Int? = null,
)

@Serializable
private data class ThingiverseHitForTest(
    val id: Long? = null,
    val name: String? = null,
    val thumbnail: String? = null,
    @SerialName("like_count")
    val likeCount: Int? = null,
    val tags: List<ThingiverseTagForTest>? = null,
)

@Serializable
private data class ThingiverseTagForTest(
    val name: String? = null,
    val tag: String? = null,
)
