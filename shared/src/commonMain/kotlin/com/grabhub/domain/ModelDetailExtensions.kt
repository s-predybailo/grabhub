package com.grabhub.domain

import com.grabhub.providers.mergeImageUrls

fun ModelItem.bestThumbnailUrl(): String? =
    imageUrl?.takeIf { it.isNotBlank() } ?: previewUrl?.takeIf { it.isNotBlank() }

fun ModelDetail.carouselImages(): List<String> = mergeImageUrls(
    listOfNotNull(item.imageUrl, item.previewUrl),
    images,
)
