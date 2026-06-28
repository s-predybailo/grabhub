package com.grabhub.providers

import com.grabhub.domain.SearchPage
import com.grabhub.domain.SearchQuery
import com.grabhub.domain.SourceType

interface SearchProvider {
    val source: SourceType

    suspend fun search(query: SearchQuery): SearchPage
}
