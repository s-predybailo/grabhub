package com.grabhub.util

import com.grabhub.domain.ModelDetail
import com.grabhub.domain.SourceType

enum class ModelDetailSection {
    MODEL,
    COMMENTS,
    MAKES,
    FILES,
}

fun ModelDetail.externalUrl(section: ModelDetailSection = ModelDetailSection.MODEL): String {
    val base = item.modelUrl.trimEnd('/')
    return when (item.source) {
        SourceType.THINGIVERSE -> when (section) {
            ModelDetailSection.COMMENTS -> "$base/comments"
            ModelDetailSection.MAKES -> "$base/makes"
            ModelDetailSection.FILES -> "$base/files"
            ModelDetailSection.MODEL -> base
        }
        SourceType.PRINTABLES -> when (section) {
            ModelDetailSection.COMMENTS -> "$base#comments"
            ModelDetailSection.FILES -> "$base/files"
            ModelDetailSection.MAKES -> "$base#makes"
            ModelDetailSection.MODEL -> base
        }
        SourceType.MAKERWORLD -> when (section) {
            ModelDetailSection.COMMENTS -> "$base#comment"
            ModelDetailSection.MAKES -> "$base#make"
            ModelDetailSection.FILES -> "$base#download"
            ModelDetailSection.MODEL -> base
        }
        SourceType.CREALITY_CLOUD -> base
    }
}
