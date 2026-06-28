package com.grabhub.domain

fun ModelItem.bestThumbnailUrl(): String? =
    imageUrl?.takeIf { it.isNotBlank() } ?: previewUrl?.takeIf { it.isNotBlank() }

fun ModelDetail.carouselImages(): List<String> = mergeImageUrls(
    listOfNotNull(item.imageUrl, item.previewUrl),
    images,
)

private fun mergeImageUrls(primary: List<String?>, extra: List<String>): List<String> =
    (primary + extra)
        .mapNotNull { url ->
            val trimmed = url?.trim().orEmpty()
            when {
                trimmed.isBlank() -> null
                trimmed.startsWith("//") -> "https:$trimmed"
                else -> trimmed
            }
        }
        .distinct()
