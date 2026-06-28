package com.grabhub.providers

fun normalizeImageUrl(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val trimmed = raw.trim()
    return when {
        trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("http://") -> trimmed
        trimmed.startsWith("//") -> "https:$trimmed"
        else -> trimmed
    }
}

fun printablesMediaUrl(filePath: String?): String? {
    val normalized = normalizeImageUrl(filePath) ?: return null
    if (normalized.startsWith("http")) return normalized
    return "https://media.printables.com/${normalized.removePrefix("/")}"
}

fun mergeImageUrls(vararg groups: List<String?>): List<String> =
    groups.flatMap { it.filterNotNull() }
        .mapNotNull(::normalizeImageUrl)
        .distinct()

fun mergeImageUrls(primary: String?, vararg extras: String?): List<String> =
    mergeImageUrls(listOfNotNull(primary, *extras))
