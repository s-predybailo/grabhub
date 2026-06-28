package com.grabhub.engine

import com.grabhub.domain.ModelItem

internal object ResultDeduplicator {

    fun deduplicateAndSort(items: List<ModelItem>, query: String): List<ModelItem> {
        val normalizedQuery = normalize(query)
        val seen = mutableSetOf<String>()

        return items
            .sortedWith(
                compareByDescending<ModelItem> { scoreItem(it, normalizedQuery) }
                    .thenByDescending { it.downloads ?: 0 }
                    .thenByDescending { it.likes ?: 0 },
            )
            .filter { item ->
                seen.add(dedupKey(item))
            }
    }

    private fun dedupKey(item: ModelItem): String {
        val title = normalize(item.title)
        val author = normalize(item.author.orEmpty())
        return "$title|$author"
    }

    private fun scoreItem(item: ModelItem, query: String): Int {
        val title = normalize(item.title)
        var score = 0
        if (title == query) score += 100
        if (query.isNotBlank() && title.contains(query)) score += 50
        score += (item.likes ?: 0) / 100
        score += (item.downloads ?: 0) / 1000
        if (item.imageUrl != null) score += 5
        if (!item.tags.isNullOrEmpty()) score += 3
        return score
    }

    private fun normalize(value: String): String =
        value.lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
}
