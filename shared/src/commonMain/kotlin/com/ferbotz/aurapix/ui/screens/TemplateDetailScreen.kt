package com.ferbotz.aurapix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AuraIconButton
import com.ferbotz.aurapix.ui.components.CategoryChip
import com.ferbotz.aurapix.ui.components.GlassCard
import com.ferbotz.aurapix.ui.components.NetworkImage
import com.ferbotz.aurapix.ui.components.PrimaryButton
import com.ferbotz.aurapix.ui.components.StatusBadge
import com.ferbotz.aurapix.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.ui.theme.AuraShapes
import com.ferbotz.aurapix.ui.theme.AuraTheme
import com.ferbotz.aurapix.ui.viewmodel.UiState
import com.ferbotz.aurapix.ui.viewmodel.userMessage

/** Template detail: hero, categories, description, example previews, required photos and a Generate CTA. */
@Composable
fun TemplateDetailScreen(
    modifier: Modifier = Modifier,
    state: UiState<TemplateDetailUi> = UiState.Success(sampleDetail),
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onGenerate: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (state is UiState.Success) {
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
                    val enabled = state.data.slots.isNotEmpty()
                    PrimaryButton(
                        text = if (enabled) "Generate (${state.data.slots.size} photo${if (state.data.slots.size == 1) "" else "s"})" else "Generate",
                        onClick = onGenerate,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Rounded.AutoAwesome,
                    )
                }
            }
        },
    ) { innerPadding ->
        when (state) {
            is UiState.Loading, UiState.Idle -> CenteredState(onBack) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            is UiState.Error -> CenteredState(onBack) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp),
                ) {
                    Text(
                        state.error.userMessage(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    PrimaryButton("Retry", onClick = onRetry, modifier = Modifier.width(160.dp))
                }
            }

            is UiState.Success -> DetailContent(
                detail = state.data,
                bottomInset = innerPadding.calculateBottomPadding(),
                onBack = onBack,
                onShare = onShare,
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: TemplateDetailUi,
    bottomInset: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomInset)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero
        Box(Modifier.fillMaxWidth().height(420.dp), contentAlignment = Alignment.BottomStart) {
            NetworkImage(detail.thumbnailUrl, detail.title, Modifier.fillMaxSize())
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, AuraTheme.colors.scrim))
                )
            )
            Row(
                Modifier.fillMaxWidth().safeContentPadding().padding(horizontal = 16.dp).align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack, glass = true, tint = Color.White)
                AuraIconButton(Icons.Rounded.Share, "Share", onShare, glass = true, tint = Color.White)
            }
            Column(Modifier.padding(16.dp)) {
                if (detail.trending) StatusBadge("Trending")
                Text(
                    detail.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = AuraTheme.colors.onImage,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (detail.categories.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(detail.categories) { name -> CategoryChip(name, selected = false, onClick = {}) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    detail.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (detail.previewImageUrls.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Examples", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(detail.previewImageUrls) { url ->
                            NetworkImage(
                                url,
                                "Example",
                                Modifier.width(140.dp).height(200.dp).clip(AuraShapes.medium),
                            )
                        }
                    }
                }
            }

            if (detail.slots.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "What you'll need (${detail.slots.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            detail.slots.forEach { slot -> SlotRow(slot) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotRow(slot: TemplateSlotUi) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkImage(
            slot.exampleImageUrl,
            slot.title,
            Modifier.size(48.dp).clip(AuraShapes.small),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(slot.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(slot.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Shared scaffolding for the loading/error states: a back button plus centered content. */
@Composable
private fun CenteredState(onBack: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Box(Modifier.safeContentPadding().padding(horizontal = 16.dp)) {
            AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
    }
}

private val sampleDetail = TemplateDetailUi(
    id = "1",
    title = "Cinematic Noir",
    description = "A moody, high-contrast portrait style inspired by classic film noir. Dramatic shadows and cinematic lighting turn any photo into a timeless scene.",
    trending = true,
    categories = listOf("Cinematic", "Portrait"),
    previewImageUrls = emptyList(),
    slots = listOf(
        TemplateSlotUi("Selfie", "A clear, front-facing photo."),
        TemplateSlotUi("Side profile", "A photo from the side."),
    ),
)

@Preview
@Composable
private fun TemplateDetailScreenPreview() {
    AuraPixTheme { TemplateDetailScreen() }
}
