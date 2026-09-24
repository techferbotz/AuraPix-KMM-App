package com.ferbotz.aurapix.template.ui

/**
 * Presentation models for the template detail screen. The ViewModel maps `TemplateDetailDto`
 * into these so the screen stays DTO-free. `version` is intentionally dropped. The prompt used to
 * be dropped as well, but it is now offered through the "Get prompt" sheet so people can reuse it
 * in other AI apps.
 */

/** One required upload, ordered by the API's displayOrder. */
data class TemplateSlotUi(
    val title: String,
    val description: String,
    val exampleImageUrl: String? = null,
)

/**
 * A template's prompt, split for display. [text] is what gets copied or shared, always verbatim.
 * [body] and [negative] exist only for layout: when the author ended the prompt with a
 * "Negative Prompt: …" section, it is shown under its own label rather than as one wall of text.
 */
data class TemplatePromptUi(
    val text: String,
    val body: String,
    val negative: String? = null,
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
    /** Null when the API sent no usable prompt. The "Get prompt" button is hidden then. */
    val prompt: TemplatePromptUi? = null,
)

/**
 * The last "Negative prompt:" label in a prompt. Authors write it inline, as a trailing section of
 * the same string. It is matched case-insensitively, and the plural form is accepted too.
 */
private val NegativePromptLabel = Regex("""\bnegative\s+prompts?\s*:""", RegexOption.IGNORE_CASE)

/**
 * Reads a raw API prompt into a [TemplatePromptUi], or null when there is nothing to show. The
 * negative section is split off only when both halves have content, so a label at the very start
 * or end of the prompt leaves it whole.
 */
internal fun templatePromptOf(raw: String?): TemplatePromptUi? {
    val text = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val label = NegativePromptLabel.findAll(text).lastOrNull()
        ?: return TemplatePromptUi(text = text, body = text)
    val body = text.substring(0, label.range.first).trim()
    val negative = text.substring(label.range.last + 1).trim()
    return if (body.isEmpty() || negative.isEmpty()) {
        TemplatePromptUi(text = text, body = text)
    } else {
        TemplatePromptUi(text = text, body = body, negative = negative)
    }
}
