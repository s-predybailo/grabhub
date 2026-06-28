package com.grabhub.util

fun formatModelDescription(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return raw
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .replace(Regex("(?i)</p>"), "\n\n")
        .replace(Regex("(?i)</li>"), "\n")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("\r\n", "\n")
        .replace(Regex("[ \t]+"), " ")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
        .takeIf { it.isNotBlank() }
}

fun formatCount(value: Int?): String? {
    if (value == null) return null
    return when {
        value >= 1_000_000 -> formatDecimal(value / 1_000_000.0, suffix = "M")
        value >= 10_000 -> "${value / 1_000}k"
        value >= 1_000 -> formatDecimal(value / 1_000.0, suffix = "k")
        else -> value.toString()
    }
}

private fun formatDecimal(value: Double, suffix: String): String {
    val roundedTenths = kotlin.math.round(value * 10.0).toInt()
    val whole = roundedTenths / 10
    val fraction = roundedTenths % 10
    return if (fraction == 0) "${whole}$suffix" else "$whole.$fraction$suffix"
}
