package com.grabhub.providers.creality

import com.grabhub.domain.ModelDetail
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchPage
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType
import com.grabhub.providers.DetailProvider
import com.grabhub.providers.SearchProvider
import com.grabhub.providers.mergeImageUrls
import com.grabhub.providers.normalizeImageUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CrealityCloudProvider(
    private val httpClient: HttpClient,
) : SearchProvider, DetailProvider {

    override val source: SourceType = SourceType.CREALITY_CLOUD

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun search(query: SearchQuery): SearchPage {
        val responseText = postCreality(
            path = "/api/cxy/smart_search/v1/model",
            body = SearchRequest(
                page = query.page,
                pageSize = query.pageSize,
                keyword = query.text,
            ),
        )

        val response = json.decodeFromString<CrealitySearchResponse>(responseText)
        val items = response.result?.list.orEmpty()

        return SearchPage(
            items = items.map { it.toModelItem() },
            page = query.page,
            pageSize = query.pageSize,
            hasMore = items.size >= query.pageSize,
        )
    }

    override suspend fun getDetail(sourceId: String): ModelDetail? {
        val fileListText = postCreality(
            path = "/api/cxy/v3/model/fileList",
            body = FileListRequest(modelId = sourceId),
        )
        val fileList = json.decodeFromString<CrealityFileListResponse>(fileListText)
        val files = fileList.result?.list.orEmpty()
        if (files.isEmpty()) return null

        val firstFile = files.first()
        val images = files.flatMap { file ->
            listOfNotNull(
                normalizeImageUrl(file.coverUrl),
                normalizeImageUrl(file.cover?.url),
                normalizeImageUrl(file.cover?.originUrl),
            )
        }.distinct()
        val item = ModelItem(
            id = "creality:$sourceId",
            sourceId = sourceId,
            title = firstFile.fileName ?: firstFile.modelGroupName ?: "Untitled",
            imageUrl = images.firstOrNull(),
            previewUrl = images.firstOrNull(),
            author = null,
            source = SourceType.CREALITY_CLOUD,
            modelUrl = "${CrealityApiHeaders.WEB_BASE_URL}/model-detail/$sourceId",
            likes = null,
            downloads = firstFile.gcodeCount,
            tags = listOfNotNull(firstFile.modelGroupName?.takeIf { it.isNotBlank() }),
            isFree = !firstFile.isPay,
            price = firstFile.price.takeIf { it > 0.0 },
        )

        return ModelDetail(
            item = item,
            images = images,
            fileCount = files.size,
        )
    }

    private suspend fun postCreality(path: String, body: Any): String {
        val headers = CrealityApiHeaders.build()
        return httpClient.post("${CrealityApiHeaders.BASE_URL}$path") {
            contentType(ContentType.Application.Json)
            headers {
                headers.forEach { (key, value) -> append(key, value) }
            }
            setBody(body)
        }.bodyAsText()
    }

    private fun CrealityModelGroup.toModelItem(): ModelItem {
        val imageUrl = normalizeImageUrl(covers?.firstOrNull()?.url)
        val images = covers.orEmpty().mapNotNull { normalizeImageUrl(it.url) }
        val originImages = covers.orEmpty().mapNotNull { normalizeImageUrl(it.originUrl) }
        val allImages = mergeImageUrls(images, originImages)

        return ModelItem(
            id = "creality:$id",
            sourceId = id,
            title = groupName ?: "Untitled",
            imageUrl = imageUrl,
            previewUrl = imageUrl,
            author = userName,
            source = SourceType.CREALITY_CLOUD,
            modelUrl = "${CrealityApiHeaders.WEB_BASE_URL}/model-detail/$id",
            likes = likeCount,
            downloads = collectionCount,
            tags = listOfNotNull(categoryName?.takeIf { it.isNotBlank() }),
            isFree = isPay != true,
            price = price?.takeIf { it > 0.0 },
        ).let { item ->
            if (allImages.size > 1) item.copy(previewUrl = allImages.getOrNull(1)) else item
        }
    }

    @Serializable
    private data class SearchRequest(
        val page: Int,
        val pageSize: Int,
        val keyword: String,
    )

    @Serializable
    private data class FileListRequest(
        val cursor: String = "",
        val limit: Int = 20,
        val modelId: String,
    )

    @Serializable
    private data class CrealitySearchResponse(
        val result: CrealitySearchResult? = null,
    )

    @Serializable
    private data class CrealitySearchResult(
        val list: List<CrealityModelGroup>? = null,
    )

    @Serializable
    private data class CrealityModelGroup(
        val id: String,
        val groupName: String? = null,
        val categoryName: String? = null,
        val likeCount: Int? = null,
        val collectionCount: Int? = null,
        val commentCount: Int? = null,
        val modelCount: Int? = null,
        val isPay: Boolean? = null,
        val price: Double? = null,
        val userName: String? = null,
        val covers: List<CrealityCover>? = null,
    )

    @Serializable
    private data class CrealityCover(
        val url: String? = null,
        val originUrl: String? = null,
    )

    @Serializable
    private data class CrealityFileListResponse(
        val result: CrealityFileListResult? = null,
    )

    @Serializable
    private data class CrealityFileListResult(
        val list: List<CrealityFile>? = null,
    )

    @Serializable
    private data class CrealityFile(
        val fileName: String? = null,
        val modelGroupName: String? = null,
        val coverUrl: String? = null,
        val cover: CrealityCover? = null,
        val gcodeCount: Int? = null,
        val isPay: Boolean = false,
        val price: Double = 0.0,
    )
}
