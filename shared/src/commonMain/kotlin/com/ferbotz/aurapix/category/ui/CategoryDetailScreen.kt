package com.ferbotz.aurapix.category.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import com.ferbotz.aurapix.core.ui.components.AuraIconButton
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.NetworkImage
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.StatusBadge
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.userMessage

/** Category detail: the category name in the top bar and its templates as a 2-column grid. */
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    state: UiState<List<CategoryTemplate>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTemplateClick: (CategoryTemplate) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = categoryName,
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
                    CategoryMessage(state.error.userMessage(), onRetry, Modifier.align(Alignment.Center))

                is UiState.Success ->
                    if (state.data.isEmpty()) {
                        CategoryMessage(
                            "No templates in this category yet.",
                            onRetry,
                            Modifier.align(Alignment.Center),
                        )
                    } else {
                        CategoryTemplateGrid(state.data, onTemplateClick)
                    }
            }
        }
    }
}

@Composable
private fun CategoryTemplateGrid(
    templates: List<CategoryTemplate>,
    onTemplateClick: (CategoryTemplate) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(templates.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { template ->
                    CategoryTemplateCard(template, Modifier.weight(1f).aspectRatio(0.72f)) { onTemplateClick(template) }
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryTemplateCard(
    template: CategoryTemplate,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.clip(AuraShapes.medium).clickable(onClick = onClick),
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
private fun CategoryMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
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

@Preview
@Composable
private fun CategoryDetailScreenPreview() {
    AuraPixTheme {
        CategoryDetailScreen(
            categoryName = "Anime",
            state = UiState.Success(
                listOf(
                    CategoryTemplate(id = "1", name = "Anime Hero", trending = true),
                    CategoryTemplate(id = "2", name = "Manga Ink"),
                    CategoryTemplate(id = "3", name = "Chibi Style"),
                )
            ),
            onBack = {},
        )
    }
}
