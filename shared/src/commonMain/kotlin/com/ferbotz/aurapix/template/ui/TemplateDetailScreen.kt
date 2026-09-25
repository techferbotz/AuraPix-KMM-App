package com.ferbotz.aurapix.template.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ferbotz.aurapix.core.media.rememberImagePicker
import com.ferbotz.aurapix.core.config.LocalRemoteConfig
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.userMessage
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.CategoryChip
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.NetworkImage
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SecondaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/**
 * Template detail: hero, categories, description, in-place photo upload and an Examples strip.
 * The sticky bar holds Generate and, under it, "Get prompt", which opens [PromptBottomSheet].
 */
@Composable
fun TemplateDetailScreen(
    modifier: Modifier = Modifier,
    state: UiState<TemplateDetailUi> = UiState.Success(sampleDetail),
    generationCost: Int = 10,
    /** A share is being prepared (its picture is downloading): the Share button spins meanwhile. */
    sharing: Boolean = false,
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onGenerate: (List<ByteArray>) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val detail = (state as? UiState.Success)?.data
    val slotCount = detail?.slots?.size ?: 0

    // One picked-image slot per template imageSlot; re-initialised when the template changes.
    val images = remember { mutableStateListOf<ByteArray?>() }
    LaunchedEffect(slotCount) {
        images.clear()
        repeat(slotCount) { images.add(null) }
    }
    var pendingIndex by remember { mutableStateOf(-1) }

    // Generation limits are mirrored from what the server enforces (BE-005), so check them here
    // and keep a doomed upload on the device instead of spending the round trip to be rejected.
    val limits = LocalRemoteConfig.current.generation
    var rejection by remember { mutableStateOf<String?>(null) }
    val picker = rememberImagePicker { bytes ->
        when {
            bytes.size > limits.maxImageBytes ->
                rejection = "That photo is too large. Please pick one under " +
                    "${limits.maxImageBytes / (1024 * 1024)} MB."

            pendingIndex in images.indices -> {
                rejection = null
                images[pendingIndex] = bytes
            }
        }
    }

    // Each action has its own kill switch, and a template with no prompt simply has no "Get prompt".
    val features = LocalRemoteConfig.current.features
    val prompt = detail?.prompt?.takeIf { features.promptSharing }
    var showPrompt by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (detail != null && (features.imageGeneration || prompt != null)) {
                Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).navigationBarsPadding().padding(16.dp)) {
                    rejection?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    if (features.imageGeneration) {
                        val filled = images.count { it != null }
                        // A template asking for more photos than the server accepts can never succeed —
                        // say so rather than letting the user fill every slot and be rejected.
                        val withinCap = slotCount <= limits.maxImagesPerRequest
                        val ready = slotCount > 0 && filled == slotCount && withinCap
                        val remaining = slotCount - filled
                        PrimaryButton(
                            text = when {
                                !withinCap -> "Unavailable on this version"
                                ready -> "Generate · $generationCost gems"
                                else -> "Add $remaining more photo${if (remaining == 1) "" else "s"}"
                            },
                            onClick = { onGenerate(images.filterNotNull()) },
                            enabled = ready,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Rounded.AutoAwesome,
                        )
                    }
                    if (prompt != null) {
                        // Stacked under Generate rather than beside it. Both stay full width, and
                        // Generate keeps the lead spot.
                        if (features.imageGeneration) Spacer(Modifier.height(12.dp))
                        SecondaryButton(
                            text = "Get prompt",
                            onClick = { showPrompt = true },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.AutoMirrored.Rounded.Notes,
                        )
                    }
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
                images = images,
                bottomInset = innerPadding.calculateBottomPadding(),
                sharing = sharing,
                onBack = onBack,
                onShare = onShare,
                onPickSlot = { index -> pendingIndex = index; picker.pick() },
                onClearSlot = { index -> if (index in images.indices) images[index] = null },
            )
        }
    }

    if (showPrompt && detail != null && prompt != null) {
        PromptBottomSheet(
            templateTitle = detail.title,
            prompt = prompt,
            slots = detail.slots,
            canGenerateHere = features.imageGeneration,
            onDismiss = { showPrompt = false },
        )
    }
}

@Composable
private fun DetailContent(
    detail: TemplateDetailUi,
    images: List<ByteArray?>,
    bottomInset: androidx.compose.ui.unit.Dp,
    sharing: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onPickSlot: (Int) -> Unit,
    onClearSlot: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomInset)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero
        Box(Modifier.fillMaxWidth().height(420.dp), contentAlignment = Alignment.BottomStart) {
            NetworkImage(
                detail.thumbnailUrl,
                detail.title,
                Modifier.fillMaxSize(),
                previewUrl = detail.thumbnailCompressedUrl,
            )
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
                AuraIconButton(Icons.Rounded.Share, "Share", onShare, glass = true, tint = Color.White, loading = sharing)
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

            // Upload — input images come BEFORE the examples strip.
            if (detail.slots.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Your photos (${detail.slots.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            detail.slots.forEachIndexed { index, slot ->
                                UploadSlot(
                                    slot = slot,
                                    image = images.getOrNull(index),
                                    onPick = { onPickSlot(index) },
                                    onClear = { onClearSlot(index) },
                                )
                            }
                        }
                    }
                }
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
        }
    }
}

/** A single required photo: tap the tile to pick/replace, or clear a chosen one. */
@Composable
private fun UploadSlot(
    slot: TemplateSlotUi,
    image: ByteArray?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(56.dp).clip(AuraShapes.small).clickable(onClick = onPick),
            contentAlignment = Alignment.Center,
        ) {
            if (image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = slot.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.5.dp, MaterialTheme.colorScheme.primaryContainer, AuraShapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.AddAPhoto, "Add photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(slot.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(slot.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (image != null) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
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
    prompt = templatePromptOf(
        "A moody black-and-white portrait of the uploaded person, lit by a single hard key light " +
            "through venetian blinds. Negative Prompt: colour, cartoon, blur, watermark."
    ),
)

@Preview
@Composable
private fun TemplateDetailScreenPreview() {
    AuraPixTheme { TemplateDetailScreen() }
}
