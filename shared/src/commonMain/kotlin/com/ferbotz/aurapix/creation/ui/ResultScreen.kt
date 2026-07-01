package com.ferbotz.aurapix.creation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.NetworkImage
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/** Generated result viewer with a side action rail and a download CTA. */
@Composable
fun ResultScreen(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onRetry: () -> Unit = {},
    onDownload: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        NetworkImage(
            imageUrl,
            "Result",
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .aspectRatio(3f / 4f),
            shape = AuraShapes.large,
        )

        // Top bar
        Row(
            Modifier.fillMaxWidth().safeContentPadding().padding(horizontal = 16.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack, glass = true, tint = Color.White)
            AuraIconButton(Icons.Rounded.Share, "Share", onShare, glass = true, tint = Color.White)
        }

        // Right action rail
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            RailAction(Icons.Rounded.Favorite, "1.2k", onClick = {})
            RailAction(Icons.Rounded.Share, "Share", onClick = onShare)
            RailAction(Icons.Rounded.Refresh, "Retry", onClick = onRetry)
        }

        // Bottom download
        PrimaryButton(
            "Download",
            onDownload,
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().safeContentPadding().padding(16.dp),
            leadingIcon = Icons.Rounded.Download,
        )
    }
}

@Composable
private fun RailAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AuraIconButton(icon, label, onClick, glass = true, tint = Color.White)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AuraTheme.colors.onImage,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview
@Composable
private fun ResultScreenPreview() {
    AuraPixTheme { ResultScreen() }
}
