package com.ferbotz.aurapix.creation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.NetworkImage
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes

/** Generated result viewer: template title + download on top, share CTA at the bottom. */
@Composable
fun ResultScreen(
    modifier: Modifier = Modifier,
    title: String = "",
    imageUrl: String? = null,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
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

        // Top bar: back · template title · download
        Box(
            Modifier.fillMaxWidth().safeContentPadding().padding(horizontal = 16.dp).align(Alignment.TopCenter),
        ) {
            AuraIconButton(
                Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack,
                modifier = Modifier.align(Alignment.CenterStart), glass = true, tint = Color.White,
            )
            if (title.isNotBlank()) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 56.dp),
                )
            }
            AuraIconButton(
                Icons.Rounded.Download, "Download", onDownload,
                modifier = Modifier.align(Alignment.CenterEnd), glass = true, tint = Color.White,
            )
        }

        // Bottom share CTA
        PrimaryButton(
            "Share",
            onShare,
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().safeContentPadding().padding(16.dp),
            leadingIcon = Icons.Rounded.Share,
        )
    }
}

@Preview
@Composable
private fun ResultScreenPreview() {
    AuraPixTheme { ResultScreen(title = "Studio Pro") }
}
