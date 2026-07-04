package com.ferbotz.aurapix.feed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraBottomBar
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.Avatar
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.SectionHeader
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.userMessage

/** Main feed: a hero carousel for the first template tray, then the remaining trays as rows. */
@Composable
fun HomeFeedScreen(
    modifier: Modifier = Modifier,
    credits: Int = 0,
    avatarUrl: String? = null,
    feedState: UiState<List<FeedSection>> = UiState.Success(sampleSections),
    onTemplateClick: (TemplateItem) -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    onSeeAll: (FeedSection) -> Unit = {},
    onRetry: () -> Unit = {},
    selectedTab: AuraTab = AuraTab.Feed,
    onSelectTab: (AuraTab) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
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
        bottomBar = { AuraBottomBar(selected = selectedTab, onSelect = onSelectTab) },
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
                            onSeeAll = onSeeAll,
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
    onSeeAll: (FeedSection) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        itemsIndexed(sections, key = { _, section -> section.id }) { index, section ->
            // The first tray leads with the hero carousel when it's templates; everything else is a row.
            if (index == 0 && section.kind == FeedSectionKind.TEMPLATES) {
                HeroCarousel(section.templates, onTemplateClick)
            } else {
                FeedRowSection(section, onTemplateClick, onCategoryClick, onSeeAll)
            }
        }
    }
}

@Composable
private fun FeedRowSection(
    section: FeedSection,
    onTemplateClick: (TemplateItem) -> Unit,
    onCategoryClick: (CategoryItem) -> Unit,
    onSeeAll: (FeedSection) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            section.title,
            modifier = Modifier.padding(horizontal = 16.dp),
            action = { SeeAllAction { onSeeAll(section) } },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (section.kind) {
                FeedSectionKind.TEMPLATES ->
                    items(section.templates, key = { it.id }) { template ->
                        TemplateTile(template, Modifier.width(150.dp).height(200.dp)) { onTemplateClick(template) }
                    }

                FeedSectionKind.CATEGORIES ->
                    items(section.categories, key = { it.id }) { category ->
                        BannerCategoryCard(category, Modifier.width(220.dp).height(120.dp)) { onCategoryClick(category) }
                    }
            }
        }
    }
}

private val sampleSections = listOf(
    FeedSection(
        id = "trending",
        title = "Trending Templates",
        kind = FeedSectionKind.TEMPLATES,
        templates = listOf(
            TemplateItem(name = "Studio Pro", id = "1", trending = true, description = "Editorial studio portraits in one tap"),
            TemplateItem(name = "Anime Hero", id = "2", description = "Turn your selfie into an anime lead"),
            TemplateItem(name = "Cyberpunk", id = "3", description = "Neon-lit future city vibes"),
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
    FeedSection(
        id = "popular",
        title = "Popular Templates",
        kind = FeedSectionKind.TEMPLATES,
        templates = listOf(
            TemplateItem(name = "Neon City", id = "4"),
            TemplateItem(name = "Vintage", id = "5"),
            TemplateItem(name = "Retro", id = "6"),
        ),
    ),
)

@Preview
@Composable
private fun HomeFeedScreenPreview() {
    AuraPixTheme { HomeFeedScreen() }
}
