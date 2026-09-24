package com.ferbotz.aurapix.feed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.LoadMoreFooter
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.base.PagedList
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.userMessage

/**
 * A tray's paginated "See all" list, shown as a 2-column gallery of templates or categories. The
 * next page loads as the user nears the end of the list.
 */
@Composable
fun TrayListingScreen(
    title: String,
    state: UiState<PagedList<TrayEntry>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTemplateClick: (TemplateItem) -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    onRetry: () -> Unit = {},
    onLoadMore: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = title,
                navigationIcon = { AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack) },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (state) {
                is UiState.Loading, UiState.Idle ->
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )

                is UiState.Error ->
                    FeedMessage(state.error.userMessage(), onRetry, Modifier.align(Alignment.Center))

                is UiState.Success ->
                    TrayGrid(state.data, onTemplateClick, onCategoryClick, onLoadMore)
            }
        }
    }
}

@Composable
private fun TrayGrid(
    list: PagedList<TrayEntry>,
    onTemplateClick: (TemplateItem) -> Unit,
    onCategoryClick: (CategoryItem) -> Unit,
    onLoadMore: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(list.items.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { entry ->
                    when (entry) {
                        is TrayEntry.Template ->
                            TemplateTile(entry.item, Modifier.weight(1f).aspectRatio(0.72f)) { onTemplateClick(entry.item) }
                        is TrayEntry.Category ->
                            BannerCategoryCard(entry.item, Modifier.weight(1f).aspectRatio(1.5f)) { onCategoryClick(entry.item) }
                    }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
        if (list.hasMore) {
            item(key = "load-more") { LoadMoreFooter(list, onLoadMore) }
        }
    }
}

@Preview
@Composable
private fun TrayListingScreenPreview() {
    AuraPixTheme {
        TrayListingScreen(
            title = "Trending Templates",
            state = UiState.Success(
                PagedList(
                    items = listOf(
                        TemplateItem(name = "Studio Pro", id = "1", trending = true),
                        TemplateItem(name = "Anime Hero", id = "2"),
                        TemplateItem(name = "Cyberpunk", id = "3"),
                    ).map { TrayEntry.Template(it) },
                    hasMore = true,
                )
            ),
            onBack = {},
        )
    }
}
