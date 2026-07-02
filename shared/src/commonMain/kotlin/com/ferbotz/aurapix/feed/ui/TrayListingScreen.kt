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
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.userMessage

/** A tray's paginated "See all" list, shown as a 2-column gallery of templates or categories. */
@Composable
fun TrayListingScreen(
    title: String,
    state: UiState<TrayItems>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTemplateClick: (TemplateItem) -> Unit = {},
    onCategoryClick: (CategoryItem) -> Unit = {},
    onRetry: () -> Unit = {},
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
                    TrayGrid(state.data, onTemplateClick, onCategoryClick)
            }
        }
    }
}

@Composable
private fun TrayGrid(
    content: TrayItems,
    onTemplateClick: (TemplateItem) -> Unit,
    onCategoryClick: (CategoryItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (content) {
            is TrayItems.Templates ->
                items(content.items.chunked(2)) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { template ->
                            TemplateTile(template, Modifier.weight(1f).aspectRatio(0.72f)) { onTemplateClick(template) }
                        }
                        if (row.size == 1) Box(Modifier.weight(1f))
                    }
                }

            is TrayItems.Categories ->
                items(content.items.chunked(2)) { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { category ->
                            BannerCategoryCard(category, Modifier.weight(1f).aspectRatio(1.5f)) { onCategoryClick(category) }
                        }
                        if (row.size == 1) Box(Modifier.weight(1f))
                    }
                }
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
                TrayItems.Templates(
                    listOf(
                        TemplateItem(name = "Studio Pro", id = "1", trending = true),
                        TemplateItem(name = "Anime Hero", id = "2"),
                        TemplateItem(name = "Cyberpunk", id = "3"),
                    )
                )
            ),
            onBack = {},
        )
    }
}
