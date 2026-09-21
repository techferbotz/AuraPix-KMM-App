package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * The brand takes whatever vertical space is left over and the CTA block sits **below** it, in
 * the same column. The two used to be separate layers of a [Box] — brand centred, CTA pinned to
 * the bottom — so on a short screen the button rode up over the tagline. Laying them out in
 * sequence makes that impossible; if the ground gets tight the brand block scrolls instead.
 *
 * Insets are the caller's: this screen is hosted inside the tab scaffold, whose padding already
 * clears the nav bar and the system inset, so adding `navigationBarsPadding()` here would count
 * the bottom inset twice — which is how the button ended up floating so far above the bar.
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
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
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GoogleSignInButton(onClick = onGoogleSignIn, loading = loading)
                LoginErrorSlot(errorMessage)
                LegalFooter(onPrivacyPolicy = onPrivacyPolicy, onTerms = onTerms)
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenLightPreview() {
    AuraPixTheme(darkTheme = false) { LoginScreen() }
}

@Preview
@Composable
private fun LoginScreenDarkPreview() {
    AuraPixTheme(darkTheme = true) { LoginScreen() }
}

@Preview
@Composable
private fun LoginScreenErrorPreview() {
    AuraPixTheme(darkTheme = true) {
        LoginScreen(errorMessage = "Sign-in was cancelled. Please try again.")
    }
}
