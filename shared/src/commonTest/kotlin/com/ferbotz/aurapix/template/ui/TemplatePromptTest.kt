package com.ferbotz.aurapix.template.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The "Get prompt" sheet splits a trailing negative section off for display only. Whatever the
 * split does, what gets copied ([TemplatePromptUi.text]) must stay the author's prompt verbatim.
 */
class TemplatePromptTest {

    @Test
    fun missingOrBlankPromptHasNothingToShow() {
        assertNull(templatePromptOf(null))
        assertNull(templatePromptOf(""))
        assertNull(templatePromptOf("   \n "))
    }

    @Test
    fun promptWithoutNegativeSectionStaysWhole() {
        val prompt = templatePromptOf("  A portrait at golden hour.  ")!!
        assertEquals("A portrait at golden hour.", prompt.text)
        assertEquals(prompt.text, prompt.body)
        assertNull(prompt.negative)
    }

    /** The shape the API actually serves: the negative section inline at the end. */
    @Test
    fun trailingNegativePromptIsSplitOffButCopiedTextIsUnchanged() {
        val raw = "Create a mountain portrait. Keep the face. Negative Prompt: cartoon, blur, watermark."
        val prompt = templatePromptOf(raw)!!
        assertEquals(raw, prompt.text)
        assertEquals("Create a mountain portrait. Keep the face.", prompt.body)
        assertEquals("cartoon, blur, watermark.", prompt.negative)
    }

    @Test
    fun labelMatchesAnyCaseAndThePlural() {
        assertEquals("blur", templatePromptOf("A portrait.\nnegative prompt : blur")!!.negative)
        assertEquals("blur", templatePromptOf("A portrait. NEGATIVE PROMPTS: blur")!!.negative)
    }

    @Test
    fun theLastLabelWins() {
        val prompt = templatePromptOf("Avoid a negative prompt: tone. Negative prompt: blur")!!
        assertEquals("Avoid a negative prompt: tone.", prompt.body)
        assertEquals("blur", prompt.negative)
    }

    @Test
    fun labelWithNothingOnOneSideLeavesThePromptWhole() {
        val leading = templatePromptOf("Negative prompt: blur, noise")!!
        assertEquals(leading.text, leading.body)
        assertNull(leading.negative)

        val trailing = templatePromptOf("A portrait. Negative prompt:")!!
        assertEquals(trailing.text, trailing.body)
        assertNull(trailing.negative)
    }

    @Test
    fun labelInsideAWordIsNotASection() {
        val prompt = templatePromptOf("A nonnegative prompt: keep it bright")!!
        assertNull(prompt.negative)
    }
}
