package com.ferbotz.aurapix.template.ui

import com.ferbotz.aurapix.shell.ui.DeepLinks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * A shared template goes out as its picture plus a short caption, so chats show more than a bare
 * link. The link in the caption has to be one the app itself can open.
 */
class TemplateShareTest {

    private val detail = TemplateDetailUi(
        id = "8f6be922-7f0a-4c95-95f5-52f21956a098",
        title = "Mountain Base Camp",
        description = "Transform your selfie into an epic mountaineering portrait.",
        shortDescription = "Celebrate your greatest trekking achievement.",
        thumbnailUrl = "https://cdn.example/full.webp",
        thumbnailCompressedUrl = "https://cdn.example/compressed.webp",
        previewImageUrls = listOf("https://cdn.example/example-1.webp"),
    )

    @Test
    fun captionIsTitleThenPitchThenLink() {
        assertEquals(
            "Mountain Base Camp\n" +
                "Celebrate your greatest trekking achievement.\n" +
                "Try it on AuraPix: https://aurapix.ferbotz.com/template/8f6be922-7f0a-4c95-95f5-52f21956a098",
            detail.shareText(),
        )
    }

    @Test
    fun blankPitchIsLeftOutRatherThanLeavingAnEmptyLine() {
        assertEquals(
            "Mountain Base Camp\nTry it on AuraPix: https://aurapix.ferbotz.com/template/${detail.id}",
            detail.copy(shortDescription = "  ").shareText(),
        )
    }

    @Test
    fun pictureFallsBackFromHeroToItsSmallerCopyToTheFirstExample() {
        assertEquals("https://cdn.example/full.webp", detail.shareImageUrl)

        val noHero = detail.copy(thumbnailUrl = null)
        assertEquals("https://cdn.example/compressed.webp", noHero.shareImageUrl)

        val onlyExamples = noHero.copy(thumbnailCompressedUrl = null)
        assertEquals("https://cdn.example/example-1.webp", onlyExamples.shareImageUrl)

        assertNull(onlyExamples.copy(previewImageUrls = emptyList()).shareImageUrl)
    }

    @Test
    fun sharedLinkOpensTheSameTemplateInTheApp() {
        try {
            assertTrue(DeepLinks.handleUrl(templateShareUrl(detail.id)))
            assertEquals(detail.id, DeepLinks.pendingTemplateId.value)
        } finally {
            DeepLinks.consume()
        }
    }
}
