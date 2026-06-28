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

fun mergeImageUrls(vararg groups: List<String?>): List<String> {
    val seen = linkedSetOf<String>()
    val result = mutableListOf<String>()
    groups.flatMap { it.filterNotNull() }
        .mapNotNull(::normalizeImageUrl)
        .forEach { url ->
            val key = imageDedupKey(url)
            if (seen.add(key)) {
                result.add(url)
            }
        }
    return result
}

fun mergeImageUrls(primary: String?, vararg extras: String?): List<String> =
    mergeImageUrls(listOfNotNull(primary, *extras))

internal fun imageDedupKey(url: String): String {
    val thingiverseEmbedded = Regex("url=([^&]+)").find(url)?.groupValues?.getOrNull(1)
    if (thingiverseEmbedded != null) {
        return percentDecode(thingiverseEmbedded)
    }
    return url.substringBefore('#').substringBefore('?')
}

private fun percentDecode(value: String): String = buildString {
    var index = 0
    while (index < value.length) {
        when {
            value[index] == '%' && index + 2 < value.length -> {
                append(value.substring(index + 1, index + 3).toInt(16).toChar())
                index += 3
            }
            value[index] == '+' -> {
                append(' ')
                index += 1
            }
            else -> {
                append(value[index])
                index += 1
            }
        }
    }
}
