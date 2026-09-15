package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AmbientGlow
import com.ferbotz.aurapix.core.ui.components.AuraWordmark
import com.ferbotz.aurapix.core.ui.components.BrandLogo
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/**
 * Authentication entry: brand lockup over ambient violet, one decision on screen.
 *
 * The CTA block is pinned to the bottom (16dp gutter, 44dp above the nav inset) with the brand
 * floating in the space above it, so the eye lands on the mark and exits through the button.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    errorMessage: String? = null,
    onGoogleSignIn: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onTerms: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AmbientGlow(alpha = 0.24f, offsetY = 60.dp)

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            BrandLogo(size = 96.dp)
            Spacer(Modifier.height(18.dp))
            AuraWordmark()
            Spacer(Modifier.height(12.dp))
            Text(
                "Welcome to the future of portraits",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1.2f))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GoogleSignInButton(onClick = onGoogleSignIn, loading = loading)
            LoginErrorSlot(errorMessage)
            LegalFooter(onPrivacyPolicy = onPrivacyPolicy, onTerms = onTerms)
        }
    }
}

@Preview
@Composable
private fun LoginScreenDarkPreview() {
    AuraPixTheme(darkTheme = true) { LoginScreen() }
}

@Preview
@Composable
private fun LoginScreenLightPreview() {
    AuraPixTheme(darkTheme = false) { LoginScreen() }
}

@Preview
@Composable
private fun LoginScreenErrorPreview() {
    AuraPixTheme(darkTheme = true) {
        LoginScreen(errorMessage = "Sign-in was cancelled. Please try again.")
    }
}
