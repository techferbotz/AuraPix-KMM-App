package com.ferbotz.aurapix.shell.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AmbientGlow
import com.ferbotz.aurapix.core.ui.components.AuraIndeterminateBar
import com.ferbotz.aurapix.core.ui.components.BrandLogo
import com.ferbotz.aurapix.core.ui.components.AuraWordmark
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/**
 * Launch screen: the knockout logo mark lit by the ambient glow, the wordmark, the tagline, and
 * an indeterminate gradient bar. Shows ~1.6s.
 *
 * Sits on `surfaceContainerLowest` (`#08060E`) rather than `background` — the darkest ground in
 * the app, so the glow has the most room to read. This is also the screen where the logo's light
 * plate was most visible; see [BrandLogo] for that fix.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentAlignment = Alignment.Center,
    ) {
        AmbientGlow()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BrandLogo(size = 96.dp)
            Spacer(Modifier.height(20.dp))
            AuraWordmark()
            Spacer(Modifier.height(8.dp))
            Text(
                "Transform Yourself with AI",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AuraIndeterminateBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
                .fillMaxWidth(0.5f),
        )
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    AuraPixTheme { SplashScreen() }
}
