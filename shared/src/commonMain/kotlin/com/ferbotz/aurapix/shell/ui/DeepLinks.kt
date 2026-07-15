package com.ferbotz.aurapix.shell.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Cross-platform holder for an incoming deep link. Each platform's entry point
 * ([MainActivity] on Android, `onOpenURL` on iOS) parses the opened URL into this
 * bus, and [AuraNavHost] observes it to navigate to the target screen.
 */
object DeepLinks {
    private val _pendingTemplateId = MutableStateFlow<String?>(null)
    val pendingTemplateId: StateFlow<String?> = _pendingTemplateId

    private const val TEMPLATE_MARKER = "aurapix.ferbotz.com/template/"

    /** Parse an incoming [url]; returns true if it was a recognized template link. */
    fun handleUrl(url: String): Boolean {
        val i = url.indexOf(TEMPLATE_MARKER)
        if (i < 0) return false
        val id = url.substring(i + TEMPLATE_MARKER.length)
            .substringBefore('?').substringBefore('#').substringBefore('/').trim()
        if (id.isEmpty()) return false
        _pendingTemplateId.value = id
        return true
    }

    /** Clear the pending link once it has been routed. */
    fun consume() { _pendingTemplateId.value = null }
}
