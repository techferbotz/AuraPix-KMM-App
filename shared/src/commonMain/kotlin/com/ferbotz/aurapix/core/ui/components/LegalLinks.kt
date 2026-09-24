package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.ferbotz.aurapix.core.config.LocalRemoteConfig
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/*
 * The Terms & Conditions and Privacy Policy links every House of Apps app shows in the same places
 * (API.md §4.17b, HOA protocol 12): on sign-in as a consent line, and beside every purchase button.
 *
 * Both composables resolve the URLs from `/config` → `links.*` and open them themselves. They
 * deliberately take no click callbacks: the sign-in screens used to, with no-op defaults, and no
 * caller ever passed them — so the consent line drew two links that did nothing.
 */

/** "By continuing you agree to our Terms & Conditions and Privacy Policy." — for sign-in surfaces. */
@Composable
fun LegalConsentLine(modifier: Modifier = Modifier) {
    val link = legalLinkAnnotations()
    Text(
        text = buildAnnotatedString {
            append("By continuing you agree to our ")
            withLink(link.terms) { append("Terms & Conditions") }
            append(" and ")
            withLink(link.privacy) { append("Privacy Policy") }
            append(".")
        },
        style = MaterialTheme.typography.labelSmall,
        color = AuraTheme.colors.muted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * "Terms & Conditions · Privacy Policy" — for purchase surfaces. Apple requires both beside the
 * purchase button of anything that sells an auto-renewing subscription, and reviewers look there.
 */
@Composable
fun LegalLinksLine(modifier: Modifier = Modifier) {
    val link = legalLinkAnnotations()
    Text(
        text = buildAnnotatedString {
            withLink(link.terms) { append("Terms & Conditions") }
            append("  ·  ")
            withLink(link.privacy) { append("Privacy Policy") }
        },
        style = MaterialTheme.typography.labelMedium,
        color = AuraTheme.colors.muted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

private class LegalLinkAnnotations(val terms: LinkAnnotation, val privacy: LinkAnnotation)

/** Tinted `secondary` and underlined so the links read as links against the muted copy. */
@Composable
private fun legalLinkAnnotations(): LegalLinkAnnotations {
    val links = LocalRemoteConfig.current.links
    val browser = rememberInAppBrowser()
    val styles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.secondary,
            textDecoration = TextDecoration.Underline,
        ),
    )
    return LegalLinkAnnotations(
        terms = LinkAnnotation.Clickable("terms", styles) { browser.open(links.terms) },
        privacy = LinkAnnotation.Clickable("privacy", styles) { browser.open(links.privacyPolicy) },
    )
}
