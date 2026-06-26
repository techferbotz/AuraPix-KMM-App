package com.ferbotz.aurapix.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AuraBottomBar
import com.ferbotz.aurapix.ui.components.AuraTab
import com.ferbotz.aurapix.ui.components.AuraTopBar
import com.ferbotz.aurapix.ui.components.Avatar
import com.ferbotz.aurapix.ui.components.CategoryChip
import com.ferbotz.aurapix.ui.components.CreditsBadge
import com.ferbotz.aurapix.ui.components.NetworkImage
import com.ferbotz.aurapix.ui.components.SectionHeader
import com.ferbotz.aurapix.ui.components.StatusBadge
import com.ferbotz.aurapix.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.ui.theme.AuraShapes
import com.ferbotz.aurapix.ui.theme.AuraTheme

/** Main feed: featured carousel, category filters and a 2-column template grid. */
@Composable
fun HomeFeedScreen(
    modifier: Modifier = Modifier,
    credits: Int = 50,
    categories: List<String> = sampleCategories,
    templates: List<TemplateItem> = sampleTemplates,
    onCreate: () -> Unit = {},
    onTemplateClick: (TemplateItem) -> Unit = {},
    selectedTab: AuraTab = AuraTab.Feed,
    onSelectTab: (AuraTab) -> Unit = {},
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreate,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Create")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sampleFeatured) { (title, subtitle) ->
                        FeaturedCard(title, subtitle)
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories.size) { index ->
                        CategoryChip(
                            text = categories[index],
                            selected = selectedCategory == index,
                            onClick = { selectedCategory = index },
                        )
                    }
                }
            }
            item { SectionHeader("Templates") }
            items(templates.chunked(2)) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { template ->
                        TemplateCard(template, Modifier.weight(1f), onClick = { onTemplateClick(template) })
                    }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FeaturedCard(title: String, subtitle: String) {
    Box(
        modifier = Modifier.width(300.dp).height(180.dp).clip(AuraShapes.large),
        contentAlignment = Alignment.BottomStart,
    ) {
        NetworkImage(null, title, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, AuraTheme.colors.scrim))
            )
        )
        Column(Modifier.padding(16.dp)) {
            StatusBadge("Featured")
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = AuraTheme.colors.onImage,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AuraTheme.colors.onImage.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun TemplateCard(template: TemplateItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(0.8f)
            .clip(AuraShapes.medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart,
    ) {
        NetworkImage(template.thumbnailUrl, template.name, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, AuraTheme.colors.scrim))
            )
        )
        if (template.premium) {
            StatusBadge("Premium", Modifier.align(Alignment.TopEnd).padding(8.dp))
        }
        Text(
            template.name,
            style = MaterialTheme.typography.titleSmall,
            color = AuraTheme.colors.onImage,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview
@Composable
private fun HomeFeedScreenPreview() {
    AuraPixTheme { HomeFeedScreen() }
}
