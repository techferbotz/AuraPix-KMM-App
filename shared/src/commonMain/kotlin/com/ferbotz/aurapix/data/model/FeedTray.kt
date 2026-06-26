package com.ferbotz.aurapix.data.model

import com.ferbotz.aurapix.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.data.remote.dto.FeedTrayDto
import com.ferbotz.aurapix.data.remote.dto.TemplateSummaryDto
import kotlinx.serialization.json.Json

enum class TrayType { TEMPLATE, CATEGORY, UNKNOWN }

/**
 * A feed tray with its polymorphic `items` already decoded into typed lists.
 * The decoding is the repository's "processing" step — the ViewModel never touches JSON.
 */
data class FeedTray(
    val id: String,
    val title: String,
    val type: TrayType,
    val displayOrder: Int,
    val templates: List<TemplateSummaryDto>,
    val categories: List<CategorySummaryDto>,
)

/** Decodes a raw [FeedTrayDto] (items are `JsonElement`) into a typed [FeedTray]. */
fun FeedTrayDto.toFeedTray(json: Json): FeedTray = when (type.uppercase()) {
    "TEMPLATE" -> FeedTray(
        id = id,
        title = title,
        type = TrayType.TEMPLATE,
        displayOrder = displayOrder,
        templates = items.mapNotNull {
            runCatching { json.decodeFromJsonElement(TemplateSummaryDto.serializer(), it) }.getOrNull()
        },
        categories = emptyList(),
    )
    "CATEGORY" -> FeedTray(
        id = id,
        title = title,
        type = TrayType.CATEGORY,
        displayOrder = displayOrder,
        templates = emptyList(),
        categories = items.mapNotNull {
            runCatching { json.decodeFromJsonElement(CategorySummaryDto.serializer(), it) }.getOrNull()
        },
    )
    else -> FeedTray(id, title, TrayType.UNKNOWN, displayOrder, emptyList(), emptyList())
}
