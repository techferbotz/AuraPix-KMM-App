package com.ferbotz.aurapix.feed.ui

/** Presentation model for a template card in the feed. */
data class TemplateItem(
    val name: String,
    val premium: Boolean = false,
    val id: String = "",
    /** Card-sized copy. What every tile draws. */
    val thumbnailUrl: String? = null,
    /**
     * Larger copy, for the hero carousel only. Deliberately not drawn by the tiles: at roughly
     * 139 KB against 32 KB for the card copy, a grid that reached for this would undo the whole
     * point of BE-001.
     */
    val displayUrl: String? = null,
    val trending: Boolean = false,
    /** One-line tagline shown on the hero carousel card. */
    val description: String? = null,
)
