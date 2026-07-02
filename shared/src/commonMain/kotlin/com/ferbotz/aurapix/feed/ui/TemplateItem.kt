package com.ferbotz.aurapix.feed.ui

/** Presentation model for a template card in the feed. */
data class TemplateItem(
    val name: String,
    val premium: Boolean = false,
    val id: String = "",
    val thumbnailUrl: String? = null,
    val trending: Boolean = false,
    /** One-line tagline shown on the hero carousel card. */
    val description: String? = null,
)
