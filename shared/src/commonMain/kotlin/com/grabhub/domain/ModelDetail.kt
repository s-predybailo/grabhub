package com.grabhub.domain

data class ModelDetail(
    val item: ModelItem,
    val description: String? = null,
    val images: List<String> = emptyList(),
    val license: String? = null,
    val fileCount: Int? = null,
)
