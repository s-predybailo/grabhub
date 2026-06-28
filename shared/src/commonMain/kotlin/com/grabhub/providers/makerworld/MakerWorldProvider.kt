package com.grabhub.providers.makerworld

import com.grabhub.domain.FeedType
import com.grabhub.domain.ModelDetail
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchPage
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType
import com.grabhub.providers.DetailProvider
import com.grabhub.providers.SearchProvider
import com.grabhub.providers.mergeImageUrls
import com.grabhub.providers.normalizeImageUrl
import com.grabhub.util.formatModelDescription
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MakerWorldProvider(
    private val httpClient: HttpClient,
) : SearchProvider, DetailProvider {

    override val source: SourceType = SourceType.MAKERWORLD

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun search(query: SearchQuery): SearchPage {
        val offset = (query.page - 1) * query.pageSize
        val responseText = httpClient.get("$API_BASE/v1/search-service/select/design2") {
            parameter("keyword", browseKeyword(query))
            parameter("limit", query.pageSize)
            parameter("offset", offset)
        }.bodyAsText()

        val response = json.decodeFromString<MakerWorldSearchResponse>(responseText)
        val hits = response.hits.orEmpty()

        return SearchPage(
            items = hits.map { it.toModelItem() },
            page = query.page,
            pageSize = query.pageSize,
            hasMore = hits.size >= query.pageSize,
        )
    }

    override suspend fun getDetail(sourceId: String): ModelDetail? {
        val responseText = httpClient.get("$API_BASE/v1/design-service/design/$sourceId").bodyAsText()
        val design = json.decodeFromString<MakerWorldDesignDetail>(responseText)
        if (design.id == null) return null

        val item = design.toModelItem()
        val images = collectDesignImages(design)

        return ModelDetail(
            item = item,
            description = formatModelDescription(design.summary),
            images = images,
            license = design.license,
            fileCount = design.designExtension?.modelFiles?.size,
            commentCount = design.commentCount,
            makeCount = design.printCount,
            viewCount = design.readCount?.takeIf { it > 0 },
        )
    }

    private fun collectDesignImages(design: MakerWorldDesignDetail): List<String> {
        val fromDesign = buildList {
            add(design.coverUrl)
            design.designExtension?.designPictures.orEmpty().forEach { picture ->
                add(picture.url)
            }
        }
        return mergeImageUrls(fromDesign)
    }

    private fun MakerWorldHit.toModelItem(): ModelItem {
        val designId = id?.toString() ?: "unknown"
        val slugPart = slug?.let { "-$it" }.orEmpty()
        val modelUrl = "https://makerworld.com/en/models/$designId$slugPart"
        val coverUrl = normalizeImageUrl(this.cover)
        val preview = designExtension?.designPictures
            ?.firstOrNull { normalizeImageUrl(it.url) != coverUrl }
            ?.url
            ?.let(::normalizeImageUrl)
            ?: coverUrl

        return ModelItem(
            id = "makerworld:$designId",
            sourceId = designId,
            title = title ?: "Untitled",
            imageUrl = coverUrl,
            previewUrl = preview,
            author = designCreator?.name,
            source = SourceType.MAKERWORLD,
            modelUrl = modelUrl,
            likes = likeCount,
            downloads = downloadCount,
            tags = tags,
            isFree = !isPointRedeemable,
            price = null,
            license = license,
        )
    }

    private fun MakerWorldDesignDetail.toModelItem(): ModelItem {
        val designId = id?.toString() ?: "unknown"
        val slugPart = slug?.let { "-$it" }.orEmpty()
        val modelUrl = "https://makerworld.com/en/models/$designId$slugPart"
        val cover = normalizeImageUrl(coverUrl)
        val preview = designExtension?.designPictures
            ?.firstOrNull { normalizeImageUrl(it.url) != cover }
            ?.url
            ?.let(::normalizeImageUrl)
            ?: cover

        return ModelItem(
            id = "makerworld:$designId",
            sourceId = designId,
            title = title ?: "Untitled",
            imageUrl = cover,
            previewUrl = preview,
            author = designCreator?.name,
            source = SourceType.MAKERWORLD,
            modelUrl = modelUrl,
            likes = likeCount,
            downloads = downloadCount,
            tags = tags,
            isFree = !isPointRedeemable,
            price = null,
            license = license,
        )
    }

    @Serializable
    private data class MakerWorldSearchResponse(
        val total: Int? = null,
        val hits: List<MakerWorldHit>? = null,
    )

    @Serializable
    private data class MakerWorldHit(
        val id: Long? = null,
        val title: String? = null,
        val slug: String? = null,
        val cover: String? = null,
        @SerialName("likeCount")
        val likeCount: Int? = null,
        @SerialName("downloadCount")
        val downloadCount: Int? = null,
        val tags: List<String>? = null,
        val license: String? = null,
        @SerialName("is_point_redeemable")
        val isPointRedeemable: Boolean = false,
        val titleTranslated: String? = null,
        val designCreator: MakerWorldCreator? = null,
        val designExtension: MakerWorldDesignExtension? = null,
    )

    @Serializable
    private data class MakerWorldDesignDetail(
        val id: Long? = null,
        val title: String? = null,
        val slug: String? = null,
        @SerialName("coverUrl")
        val coverUrl: String? = null,
        val summary: String? = null,
        @SerialName("likeCount")
        val likeCount: Int? = null,
        @SerialName("downloadCount")
        val downloadCount: Int? = null,
        @SerialName("commentCount")
        val commentCount: Int? = null,
        @SerialName("printCount")
        val printCount: Int? = null,
        @SerialName("readCount")
        val readCount: Int? = null,
        val tags: List<String>? = null,
        val license: String? = null,
        @SerialName("is_point_redeemable")
        val isPointRedeemable: Boolean = false,
        val designCreator: MakerWorldCreator? = null,
        val designExtension: MakerWorldDesignExtension? = null,
        val instances: List<MakerWorldInstance>? = null,
    )

    @Serializable
    private data class MakerWorldInstance(
        val cover: String? = null,
        val pictures: List<MakerWorldPicture>? = null,
    )

    @Serializable
    private data class MakerWorldCreator(
        val name: String? = null,
        val handle: String? = null,
    )

    @Serializable
    private data class MakerWorldDesignExtension(
        @SerialName("design_pictures")
        val designPictures: List<MakerWorldPicture>? = null,
        @SerialName("model_files")
        val modelFiles: List<MakerWorldModelFile>? = null,
    )

    @Serializable
    private data class MakerWorldPicture(
        val name: String? = null,
        val url: String? = null,
    )

    @Serializable
    private data class MakerWorldModelFile(
        val name: String? = null,
    )

    companion object {
        private const val API_BASE = "https://api.bambulab.com"

        private fun browseKeyword(query: SearchQuery): String =
            if (query.feedType != null) "" else query.text
    }
}
