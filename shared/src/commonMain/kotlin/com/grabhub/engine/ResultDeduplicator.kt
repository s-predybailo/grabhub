package com.grabhub.engine

import com.grabhub.domain.ModelItem
import com.grabhub.domain.SortOrder

internal object ResultDeduplicator {

    fun deduplicateAndSort(items: List<ModelItem>, query: String, sortOrder: SortOrder = SortOrder.RELEVANCE): List<ModelItem> {
        val normalizedQuery = normalize(query)
        val seen = mutableSetOf<String>()

        val comparator = when (sortOrder) {
            SortOrder.RELEVANCE -> compareByDescending<ModelItem> { scoreItem(it, normalizedQuery) }
                .thenByDescending { it.downloads ?: 0 }
                .thenByDescending { it.likes ?: 0 }

            SortOrder.POPULARITY -> compareByDescending<ModelItem> { popularityScore(it) }
                .thenByDescending { it.downloads ?: 0 }
                .thenByDescending { it.likes ?: 0 }
        }

        return items
            .sortedWith(comparator)
            .filter { item ->
                seen.add(dedupKey(item))
            }
    }

    fun deduplicatePreserveOrder(items: List<ModelItem>): List<ModelItem> {
        val seen = mutableSetOf<String>()
        return items.filter { item ->
            seen.add(dedupKey(item))
        }
    }

    private fun popularityScore(item: ModelItem): Int =
        (item.downloads ?: 0) + (item.likes ?: 0) * 10

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
