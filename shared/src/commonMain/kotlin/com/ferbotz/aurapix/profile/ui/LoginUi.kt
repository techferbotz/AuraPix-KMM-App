package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.config.LocalRemoteConfig
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/**
 * Shared pieces of the two sign-in surfaces (full-screen [LoginScreen] and the modal
 * [LoginBottomSheet]) so the two never drift apart.
 */

/**
 * The Google mark as the design draws it: a light disc carrying a deep-violet "G", sized to sit
 * inside the 56dp CTA. Deliberately not `Icons.Rounded.AccountCircle` — that generic person glyph
 * doesn't say "Google", and the design's leading slot is a branded token.
 */
@Composable
fun GoogleGlyph(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .background(MaterialTheme.colorScheme.onPrimary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "G",
            style = MaterialTheme.typography.labelLarge,
            color = AuraTheme.colors.gradientStart,
        )
    }
}

/** The "Continue with Google" CTA, identical on both sign-in surfaces. */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    // Kill switch: hides sign-in everywhere at once. The auth route itself stays up.
    if (!LocalRemoteConfig.current.features.googleLogin) return
    PrimaryButton(
        text = "Continue with Google",
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = !loading,
        loading = loading,
        leadingContent = { GoogleGlyph() },
    )
}

/**
 * Inline sign-in error.
 *
 * Rendered inside a **fixed-height slot** so that showing or clearing an error never shifts the
 * legal text or the button — the design calls this out explicitly ("layout never jumps").
 */
@Composable
fun LoginErrorSlot(message: String?, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(30.dp), contentAlignment = Alignment.Center) {
        if (message != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Footer legal line with tappable Privacy Policy / Terms of Service links, tinted `secondary`
 * and underlined so they read as links against the muted body copy.
 */
@Composable
fun LegalFooter(
    onPrivacyPolicy: () -> Unit,
    onTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.secondary,
            textDecoration = TextDecoration.Underline,
        ),
    )
    Text(
        text = buildAnnotatedString {
            append("By continuing you agree to our ")
            withLink(LinkAnnotation.Clickable("privacy", linkStyles) { onPrivacyPolicy() }) {
                append("Privacy Policy")
            }
            append(" and ")
            withLink(LinkAnnotation.Clickable("terms", linkStyles) { onTerms() }) {
                append("Terms of Service")
            }
            append(".")
        },
        style = MaterialTheme.typography.labelSmall,
        color = AuraTheme.colors.muted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}
