package com.axiel7.yumehyou.core.model

data class TrackerMapping(
    val trackerType: TrackerType,
    val trackerMediaId: String,
    val trackerEntryId: String? = null,
    val isPrimary: Boolean = false,
    val url: String? = null,
)
