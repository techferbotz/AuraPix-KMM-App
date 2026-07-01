package com.ferbotz.aurapix.feed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import com.ferbotz.aurapix.core.ui.components.AuraBottomBar
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.Avatar
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.NetworkImage
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.SectionHeader
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.userMessage

/** Main feed: renders the API's trays as ordered horizontal carousels, with loading/error/empty states. */
@Composable
fun HomeFeedScreen(
    modifier: Modifier = Modifier,
    credits: Int = 0,
    feedState: UiState<List<FeedSection>> = UiState.Success(sampleSections),
    onCreate: () -> Unit = {},
    onTemplateClick: (TemplateItem) -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    onRetry: () -> Unit = {},
    selectedTab: AuraTab = AuraTab.Feed,
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
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (feedState) {
                is UiState.Loading, UiState.Idle ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )

                is UiState.Error ->
                    FeedMessage(
                        message = feedState.error.userMessage(),
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )

                is UiState.Success ->
                    if (feedState.data.isEmpty()) {
                        FeedMessage(
                            message = "Nothing here yet. Pull to refresh.",
                            onRetry = onRetry,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        FeedContent(
                            sections = feedState.data,
                            onTemplateClick = onTemplateClick,
                            onCategoryClick = onCategoryClick,
                        )
                    }
            }
        }
    }
}

@Composable
private fun FeedContent(
    sections: List<FeedSection>,
    onTemplateClick: (TemplateItem) -> Unit,
    onCategoryClick: (CategoryItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(sections, key = { it.id }) { section ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(section.title, modifier = Modifier.padding(horizontal = 16.dp))
                when (section.kind) {
                    FeedSectionKind.TEMPLATES -> LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(section.templates, key = { it.id }) { template ->
                            TemplateCard(template, onClick = { onTemplateClick(template) })
                        }
                    }

                    FeedSectionKind.CATEGORIES -> LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(section.categories, key = { it.id }) { category ->
                            CategoryCard(category, onClick = { onCategoryClick(category) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(template: TemplateItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .clip(AuraShapes.medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart,
    ) {
        NetworkImage(template.thumbnailUrl, template.name, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, AuraTheme.colors.scrim))
            )
        )
        if (template.trending) {
            StatusBadge("Trending", Modifier.align(Alignment.TopEnd).padding(8.dp))
        }
        Text(
            template.name,
            style = MaterialTheme.typography.titleSmall,
            color = AuraTheme.colors.onImage,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun CategoryCard(category: CategoryItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(90.dp)
            .clip(AuraShapes.medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomStart,
    ) {
        NetworkImage(category.iconUrl, category.name, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, AuraTheme.colors.scrim))
            )
        )
        Text(
            category.name,
            style = MaterialTheme.typography.titleSmall,
            color = AuraTheme.colors.onImage,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
private fun FeedMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PrimaryButton("Retry", onClick = onRetry, modifier = Modifier.width(160.dp))
    }
}

private val sampleSections = listOf(
    FeedSection(
        id = "trending",
        title = "Trending Templates",
        kind = FeedSectionKind.TEMPLATES,
        templates = listOf(
            TemplateItem(name = "Studio Pro", id = "1", trending = true),
            TemplateItem(name = "Anime Hero", id = "2"),
            TemplateItem(name = "Cyberpunk", id = "3"),
        ),
    ),
    FeedSection(
        id = "featured-categories",
        title = "Featured Categories",
        kind = FeedSectionKind.CATEGORIES,
        categories = listOf(
            CategoryItem(id = "a", name = "Anime"),
            CategoryItem(id = "b", name = "Portrait"),
            CategoryItem(id = "c", name = "Wedding"),
        ),
    ),
)

@Preview
@Composable
private fun HomeFeedScreenPreview() {
    AuraPixTheme { HomeFeedScreen() }
}
