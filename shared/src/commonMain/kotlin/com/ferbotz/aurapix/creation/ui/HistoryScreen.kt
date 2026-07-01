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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
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
import com.ferbotz.aurapix.core.ui.components.AuraBottomBar
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.Avatar
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.NetworkImage
import com.ferbotz.aurapix.core.ui.components.SecondaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/** Past creations: a 2-column gallery. */
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    credits: Int = 50,
    items: List<HistoryItem> = sampleHistory,
    onItemClick: (HistoryItem) -> Unit = {},
    onRefresh: () -> Unit = {},
    selectedTab: AuraTab = AuraTab.MyCreations,
    onSelectTab: (AuraTab) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "AuraPix",
                navigationIcon = { Avatar(size = 36.dp, border = true) },
                actions = { CreditsBadge(credits) },
            )
        },
        bottomBar = { AuraBottomBar(selected = selectedTab, onSelect = onSelectTab) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
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
            item {
                SecondaryButton("Load more", onClick = onRefresh, modifier = Modifier.fillMaxWidth(), leadingIcon = Icons.Rounded.Refresh)
            }
        }
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
