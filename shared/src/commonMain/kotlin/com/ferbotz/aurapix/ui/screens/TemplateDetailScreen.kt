package com.ferbotz.aurapix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Share
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

/** Template detail: hero, description, required input slot, specs and a Generate CTA. */
@Composable
fun TemplateDetailScreen(
    modifier: Modifier = Modifier,
    title: String = "Cinematic Noir",
    onBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onGenerate: () -> Unit = {},
) {
    val styleTags = listOf("Cinematic lighting", "High detail", "Comic book aesthetic")
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
                PrimaryButton("Generate", onGenerate, Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.AutoAwesome)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero
            Box(Modifier.fillMaxWidth().height(420.dp), contentAlignment = Alignment.BottomStart) {
                NetworkImage(null, title, Modifier.fillMaxSize())
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
                    StatusBadge("Premium")
                    Text(
                        title,
                        style = MaterialTheme.typography.displaySmall,
                        color = AuraTheme.colors.onImage,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(styleTags) { tag -> CategoryChip(tag, selected = false, onClick = {}) }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "A moody, high-contrast portrait style inspired by classic film noir. Dramatic shadows and cinematic lighting turn any photo into a timeless scene.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Required input slot
                Box(
                    Modifier.fillMaxWidth().height(120.dp).clip(AuraShapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.5.dp, MaterialTheme.colorScheme.primaryContainer, AuraShapes.medium)
                        .clickable(onClick = {}),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Text("Upload 1 face image", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                GlassCard(Modifier.fillMaxWidth()) {
                    SpecRow("Aspect Ratio", "Vertical 9:16")
                    Box(Modifier.fillMaxWidth().height(1.dp).padding(vertical = 0.dp).background(AuraTheme.colors.glassBorder))
                    SpecRow("AI Engine", "Aura v2.4 Pro")
                }
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
private fun TemplateDetailScreenPreview() {
    AuraPixTheme { TemplateDetailScreen() }
}
