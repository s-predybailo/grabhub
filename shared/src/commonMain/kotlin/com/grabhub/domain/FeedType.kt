package com.grabhub.domain

import kotlinx.serialization.Serializable

@Serializable
enum class FeedType {
    /** Home discover feed — popular models across sources. */
    DISCOVER,

    /** Daily trending — recent popular uploads. */
    TRENDING,

    /** Monthly / all-time popular models. */
    POPULAR,

    /** Latest uploads. */
    LATEST,
}
