package com.ferbotz.aurapix.template.ui

/**
 * Presentation models for the template detail screen. The ViewModel maps `TemplateDetailDto`
 * into these so the screen stays DTO-free. `prompt`/`version` are intentionally dropped
 * (the prompt must not be shown to users).
 */

/** One required upload, ordered by the API's displayOrder. */
data class TemplateSlotUi(
    val title: String,
    val description: String,
    val exampleImageUrl: String? = null,
)

data class TemplateDetailUi(
    val id: String,
    val title: String,
    /** Hero image, longest side 1280px — the largest size the API serves, not an original. */
    val thumbnailUrl: String? = null,
    /** Smaller copy of [thumbnailUrl], already cached by the feed card that linked here. */
    val thumbnailCompressedUrl: String? = null,
    val description: String,
    val trending: Boolean = false,
    val categories: List<String> = emptyList(),
    val previewImageUrls: List<String> = emptyList(),
    val slots: List<TemplateSlotUi> = emptyList(),
)
