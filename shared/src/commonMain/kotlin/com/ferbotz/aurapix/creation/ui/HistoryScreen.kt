package com.ferbotz.aurapix.creation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.AuraTabScaffold
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.Avatar
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.NetworkImage
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/** Past creations: a 2-column gallery, with loading + empty states. */
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    credits: Int = 50,
    avatarUrl: String? = null,
    items: List<HistoryItem> = sampleHistory,
    loading: Boolean = false,
    onItemClick: (HistoryItem) -> Unit = {},
    selectedTab: AuraTab = AuraTab.MyCreations,
    onSelectTab: (AuraTab) -> Unit = {},
) {
    AuraTabScaffold(
        selectedTab = selectedTab,
        onSelectTab = onSelectTab,
        modifier = modifier,
        topBar = {
            AuraTopBar(
                navigationIcon = {
                    Text("AuraPix", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                },
                actions = {
                    CreditsBadge(credits)
                    Avatar(imageUrl = avatarUrl, size = 36.dp, border = true)
                },
            )
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(top = pad.calculateTopPadding())) {
            when {
                loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                )

                items.isEmpty() -> EmptyCreations(
                    onCreate = { onSelectTab(AuraTab.Feed) },
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = pad.calculateBottomPadding()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(items.chunked(2)) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { historyItem ->
                                HistoryCard(historyItem, Modifier.weight(1f), onClick = { onItemClick(historyItem) })
                            }
                            if (row.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCreations(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
        }
        Text("No creations yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "Your generated images will show up here. Pick a template to make your first one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PrimaryButton("Start creating", onCreate, Modifier.width(220.dp))
    }
}

@Composable
private fun HistoryCard(item: HistoryItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.aspectRatio(0.85f).clip(AuraShapes.medium).clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart,
    ) {
        NetworkImage(item.imageUrl, item.title, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, AuraTheme.colors.scrim))
            )
        )
        StatusBadge(
            item.category,
            Modifier.align(Alignment.TopStart).padding(8.dp),
            containerColor = AuraTheme.colors.badge,
            contentColor = Color.White,
        )
        Text(
            item.title,
            style = MaterialTheme.typography.titleSmall,
            color = AuraTheme.colors.onImage,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview
@Composable
private fun HistoryScreenPreview() {
    AuraPixTheme { HistoryScreen() }
}

@Preview
@Composable
private fun HistoryScreenEmptyPreview() {
    AuraPixTheme { HistoryScreen(items = emptyList()) }
}
