package com.grabhub.providers.thingiverse

import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchPage
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType
import com.grabhub.providers.SearchProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ThingiverseProvider(
    private val httpClient: HttpClient,
    private val accessToken: String,
) : SearchProvider {

    override val source: SourceType = SourceType.THINGIVERSE

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun search(query: SearchQuery): SearchPage {
        if (accessToken.isBlank()) {
            throw ThingiverseNotConfiguredException(
                "Thingiverse access token is not configured. Register at https://www.thingiverse.com/developers",
            )
        }

        val responseText = httpClient.get("$API_BASE/search/$query.text") {
            parameter("access_token", accessToken)
            parameter("page", query.page)
            parameter("per_page", query.pageSize)
        }.bodyAsText()

        val response = json.decodeFromString<ThingiverseSearchResponse>(responseText)
        val hits = response.hits.orEmpty()

        return SearchPage(
            items = hits.map { it.toModelItem() },
            page = query.page,
            pageSize = query.pageSize,
            hasMore = hits.size >= query.pageSize,
        )
    }

    private fun ThingiverseHit.toModelItem(): ModelItem {
        val thingId = id?.toString() ?: "unknown"
        val modelUrl = publicUrl ?: "https://www.thingiverse.com/thing:$thingId"

        return ModelItem(
            id = "thingiverse:$thingId",
            sourceId = thingId,
            title = name ?: "Untitled",
            imageUrl = thumbnail,
            previewUrl = thumbnail,
            author = creator?.name ?: creator?.publicName,
            source = SourceType.THINGIVERSE,
            modelUrl = modelUrl,
            likes = likeCount,
            downloads = null,
            tags = tags,
            isFree = true,
            price = null,
        )
    }

    @Serializable
    private data class ThingiverseSearchResponse(
        val hits: List<ThingiverseHit>? = null,
        val total: Int? = null,
    )

    @Serializable
    private data class ThingiverseHit(
        val id: Long? = null,
        val name: String? = null,
        val thumbnail: String? = null,
        @SerialName("public_url")
        val publicUrl: String? = null,
        @SerialName("like_count")
        val likeCount: Int? = null,
        val tags: List<String>? = null,
        val creator: ThingiverseCreator? = null,
    )

    @Serializable
    private data class ThingiverseCreator(
        val name: String? = null,
        @SerialName("public_name")
        val publicName: String? = null,
    )

    companion object {
        private const val API_BASE = "https://api.thingiverse.com"
    }
}

class ThingiverseNotConfiguredException(message: String) : Exception(message)
