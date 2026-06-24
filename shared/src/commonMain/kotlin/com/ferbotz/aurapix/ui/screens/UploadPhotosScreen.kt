package com.ferbotz.aurapix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AuraIconButton
import com.ferbotz.aurapix.ui.components.AuraTopBar
import com.ferbotz.aurapix.ui.components.CreditsBadge
import com.ferbotz.aurapix.ui.components.GlassCard
import com.ferbotz.aurapix.ui.components.NetworkImage
import com.ferbotz.aurapix.ui.components.PrimaryButton
import com.ferbotz.aurapix.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.ui.theme.AuraShapes
import com.ferbotz.aurapix.ui.theme.AuraTheme

private const val REQUIRED_PHOTOS = 3

/** Collects training photos for the AI model with example guidance and a gated CTA. */
@Composable
fun UploadPhotosScreen(
    modifier: Modifier = Modifier,
    credits: Int = 50,
    onBack: () -> Unit = {},
    onTrain: () -> Unit = {},
) {
    var uploaded by remember { mutableIntStateOf(1) }
    val ready = uploaded >= REQUIRED_PHOTOS

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Upload Photos",
                navigationIcon = { AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack) },
                actions = { CreditsBadge(credits) },
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
                PrimaryButton(
                    text = if (ready) "Train AI Model" else "Upload ${REQUIRED_PHOTOS - uploaded} more photo${if (REQUIRED_PHOTOS - uploaded == 1) "" else "s"}",
                    onClick = onTrain,
                    enabled = ready,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "Add at least $REQUIRED_PHOTOS clear, front-facing photos to train your model.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // 2x2 photo grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (rowStart in 0 until 4 step 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (index in rowStart until rowStart + 2) {
                            PhotoSlot(
                                index = index,
                                uploaded = uploaded,
                                modifier = Modifier.weight(1f),
                                onAdd = { if (index == uploaded) uploaded++ },
                                onRemove = { if (uploaded > 0) uploaded-- },
                            )
                        }
                    }
                }
            }

            Text("Pro Tips", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            TipCard(
                icon = Icons.Rounded.CheckCircle,
                iconTint = AuraTheme.colors.success,
                title = "Good Example",
                description = "Bright lighting, clear face, varied angles.",
            )
            TipCard(
                icon = Icons.Rounded.Block,
                iconTint = MaterialTheme.colorScheme.error,
                title = "Avoid",
                description = "Blurry shots, sunglasses, group photos or filters.",
            )
        }
    }
}

@Composable
private fun PhotoSlot(
    index: Int,
    uploaded: Int,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    val shape = AuraShapes.medium
    Box(modifier.aspectRatio(1f).clip(shape), contentAlignment = Alignment.Center) {
        when {
            index < uploaded -> {
                NetworkImage(null, "Photo ${index + 1}", Modifier.fillMaxSize(), shape = shape)
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(28.dp).clip(CircleShape)
                        .background(AuraTheme.colors.scrim).clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Close, "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            index == uploaded -> Box(
                Modifier.fillMaxSize().clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.5.dp, MaterialTheme.colorScheme.primaryContainer, shape)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.AddAPhoto, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Text("Add Photo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 6.dp))
                }
            }
            else -> Box(
                Modifier.fillMaxSize().clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun TipCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String,
) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview
@Composable
private fun UploadPhotosScreenPreview() {
    AuraPixTheme { UploadPhotosScreen() }
}
