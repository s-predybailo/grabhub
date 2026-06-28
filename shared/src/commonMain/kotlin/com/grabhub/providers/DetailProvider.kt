package com.grabhub.providers

import com.grabhub.domain.ModelDetail
import com.grabhub.domain.SourceType

interface DetailProvider {
    val source: SourceType

    suspend fun getDetail(sourceId: String): ModelDetail?
}
