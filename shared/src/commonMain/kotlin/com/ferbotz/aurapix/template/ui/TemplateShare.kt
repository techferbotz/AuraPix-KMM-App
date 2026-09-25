package com.ferbotz.aurapix.template.ui

/**
 * What goes out when a template is shared: its picture with a short caption, so a chat shows a
 * titled image rather than a bare link. The link opens the website, which hands off to the app
 * when it's installed (see `DeepLinks`).
 */

private const val TEMPLATE_LINK_BASE = "https://aurapix.ferbotz.com/template/"

/** Web link to a template. */
fun templateShareUrl(templateId: String): String = TEMPLATE_LINK_BASE + templateId

/** The caption: the template's title, its one-line pitch, and the link. Blank parts are left out. */
fun TemplateDetailUi.shareText(): String = listOfNotNull(
    title.takeIf { it.isNotBlank() }?.trim(),
    shortDescription.takeIf { it.isNotBlank() }?.trim(),
    "Try it on AuraPix: ${templateShareUrl(id)}",
).joinToString("\n")

/** The picture to attach: the full hero, else its smaller copy, else the first example. */
val TemplateDetailUi.shareImageUrl: String?
    get() = thumbnailUrl ?: thumbnailCompressedUrl ?: previewImageUrls.firstOrNull()
