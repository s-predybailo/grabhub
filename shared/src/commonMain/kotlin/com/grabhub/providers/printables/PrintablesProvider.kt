package com.grabhub.providers.printables

import com.grabhub.domain.ModelDetail
import com.grabhub.domain.ModelItem
import com.grabhub.domain.SearchPage
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType
import com.grabhub.providers.DetailProvider
import com.grabhub.providers.printablesMediaUrl
import com.grabhub.providers.SearchProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PrintablesProvider(
    private val httpClient: HttpClient,
) : SearchProvider, DetailProvider {

    override val source: SourceType = SourceType.PRINTABLES

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun search(query: SearchQuery): SearchPage {
        val responseText = httpClient.post(GRAPHQL_URL) {
            contentType(ContentType.Application.Json)
            setBody(
                GraphQlRequest(
                    operationName = "SearchModels",
                    query = SEARCH_QUERY,
                    variables = SearchVariables(
                        query = query.text,
                        limit = query.pageSize,
                        ordering = "best_match",
                    ),
                ),
            )
        }.bodyAsText()

        val response = json.decodeFromString<SearchResponse>(responseText)
        val items = response.data?.result?.items.orEmpty()

        return SearchPage(
            items = items.map { it.toModelItem() },
            page = query.page,
            pageSize = query.pageSize,
            hasMore = items.size >= query.pageSize,
        )
    }

    override suspend fun getDetail(sourceId: String): ModelDetail? {
        val responseText = httpClient.post(GRAPHQL_URL) {
            contentType(ContentType.Application.Json)
            setBody(
                DetailGraphQlRequest(
                    operationName = "PrintDetail",
                    query = DETAIL_QUERY,
                    variables = DetailVariables(id = sourceId),
                ),
            )
        }.bodyAsText()

        val response = json.decodeFromString<DetailResponse>(responseText)
        val print = response.data?.print ?: return null

        val item = ModelItem(
            id = "printables:${print.id}",
            sourceId = print.id,
            title = print.name,
            imageUrl = printablesMediaUrl(print.image?.filePath),
            previewUrl = printablesMediaUrl(print.images?.getOrNull(1)?.filePath ?: print.image?.filePath),
            author = print.user?.publicUsername ?: print.user?.handle,
            source = SourceType.PRINTABLES,
            modelUrl = "https://www.printables.com/model/${print.id}-${print.slug}",
            likes = print.likesCount,
            downloads = print.downloadCount,
            tags = print.tags?.map { it.name },
            isFree = !print.premium && print.price == null,
            price = print.price,
        )

        val galleryImages = print.images.orEmpty()
            .mapNotNull { printablesMediaUrl(it.filePath) }

        return ModelDetail(
            item = item,
            description = print.description,
            images = galleryImages.ifEmpty { listOfNotNull(item.imageUrl) },
            license = print.license,
        )
    }

    private fun PrintItem.toModelItem(): ModelItem {
        val modelUrl = "https://www.printables.com/model/$id-$slug"
        val imageUrl = printablesMediaUrl(image?.filePath)
        val previewUrl = printablesMediaUrl(image?.filePath)

        return ModelItem(
            id = "printables:$id",
            sourceId = id,
            title = name,
            imageUrl = imageUrl,
            previewUrl = previewUrl,
            author = user?.publicUsername ?: user?.handle,
            source = SourceType.PRINTABLES,
            modelUrl = modelUrl,
            likes = likesCount,
            downloads = downloadCount,
            tags = tags?.map { it.name },
            isFree = !premium && price == null,
            price = price,
        )
    }

    @Serializable
    private data class GraphQlRequest(
        val operationName: String,
        val query: String,
        val variables: SearchVariables,
    )

    @Serializable
    private data class SearchVariables(
        val query: String,
        val limit: Int,
        val ordering: String,
    )

    @Serializable
    private data class SearchResponse(
        val data: SearchData? = null,
    )

    @Serializable
    private data class SearchData(
        val result: SearchResult? = null,
    )

    @Serializable
    private data class SearchResult(
        val items: List<PrintItem>? = null,
    )

    @Serializable
    private data class PrintItem(
        val id: String,
        val name: String,
        val slug: String,
        val likesCount: Int? = null,
        val downloadCount: Int? = null,
        val premium: Boolean = false,
        val price: Double? = null,
        val user: PrintUser? = null,
        val image: PrintImage? = null,
        val tags: List<PrintTag>? = null,
    )

    @Serializable
    private data class PrintUser(
        val publicUsername: String? = null,
        val handle: String? = null,
    )

    @Serializable
    private data class PrintImage(
        val filePath: String? = null,
    )

    @Serializable
    private data class PrintTag(
        val name: String,
    )

    @Serializable
    private data class DetailGraphQlRequest(
        val operationName: String,
        val query: String,
        val variables: DetailVariables,
    )

    @Serializable
    private data class DetailVariables(
        val id: String,
    )

    @Serializable
    private data class DetailResponse(
        val data: DetailData? = null,
    )

    @Serializable
    private data class DetailData(
        val print: PrintDetail? = null,
    )

    @Serializable
    private data class PrintDetail(
        val id: String,
        val name: String,
        val slug: String,
        val description: String? = null,
        val likesCount: Int? = null,
        val downloadCount: Int? = null,
        val premium: Boolean = false,
        val price: Double? = null,
        val license: String? = null,
        val user: PrintUser? = null,
        val image: PrintImage? = null,
        val images: List<PrintImage>? = null,
        val tags: List<PrintTag>? = null,
    )

    companion object {
        private const val GRAPHQL_URL = "https://api.printables.com/graphql/"
        private const val MEDIA_BASE = "https://media.printables.com"

        private const val SEARCH_QUERY = """
            query SearchModels(${'$'}query: String!, ${'$'}limit: Int, ${'$'}ordering: SearchChoicesEnum) {
              result: searchPrints2(query: ${'$'}query, printType: print, limit: ${'$'}limit, ordering: ${'$'}ordering) {
                items {
                  id
                  name
                  slug
                  likesCount
                  downloadCount
                  premium
                  price
                  user { publicUsername handle }
                  image { filePath }
                  tags { name }
                }
              }
            }
        """

        private const val DETAIL_QUERY = """
            query PrintDetail(${'$'}id: ID!) {
              print(id: ${'$'}id) {
                id
                name
                slug
                description
                likesCount
                downloadCount
                premium
                price
                license
                user { publicUsername handle }
                image { filePath }
                images { filePath }
                tags { name }
              }
            }
        """
    }
}
