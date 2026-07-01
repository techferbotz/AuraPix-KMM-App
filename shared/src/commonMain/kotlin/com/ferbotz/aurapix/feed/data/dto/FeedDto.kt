package com.ferbotz.aurapix.feed.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FeedResponseDto(val trays: List<FeedTrayDto>)

@Serializable
data class FeedTrayDto(
    val id: String,
    val type: String,
    val title: String,
    val displayOrder: Int,
    val items: List<JsonElement>,
)
