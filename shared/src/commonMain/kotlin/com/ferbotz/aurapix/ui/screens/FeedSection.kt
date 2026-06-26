package com.ferbotz.aurapix.ui.screens

/**
 * Presentation models for the home feed. The ViewModel maps data-layer `FeedTray`s into
 * these so the screen never touches DTOs or `JsonElement`s.
 */

enum class FeedSectionKind { TEMPLATES, CATEGORIES }

data class CategoryItem(
    val id: String,
    val name: String,
    val iconUrl: String? = null,
)

/** One horizontal carousel on the feed: either templates or categories, in display order. */
data class FeedSection(
    val id: String,
    val title: String,
    val kind: FeedSectionKind,
    val templates: List<TemplateItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
)
