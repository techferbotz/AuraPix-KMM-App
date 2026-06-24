package com.ferbotz.aurapix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AuraIconButton
import com.ferbotz.aurapix.ui.components.AuraListRow
import com.ferbotz.aurapix.ui.components.AuraSearchField
import com.ferbotz.aurapix.ui.components.AuraTopBar
import com.ferbotz.aurapix.ui.components.CategoryChip
import com.ferbotz.aurapix.ui.components.NetworkImage
import com.ferbotz.aurapix.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.ui.theme.AuraShapes

/** Browse/search styles: search field, recent searches and a popular templates list. */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onResultClick: (PopularItem) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Search",
                navigationIcon = { AuraIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack) },
            )
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
            AuraSearchField(query, { query = it }, placeholder = "Search styles & templates")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Recent searches", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleRecentSearches) { term ->
                        CategoryChip(term, selected = false, onClick = { query = term })
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Popular templates", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                samplePopular.forEach { popularItem ->
                    AuraListRow(
                        title = popularItem.name,
                        subtitle = "${popularItem.category} · ${popularItem.uses}",
                        leadingContent = {
                            NetworkImage(null, popularItem.name, Modifier.size(48.dp), shape = AuraShapes.small)
                        },
                        onClick = { onResultClick(popularItem) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchScreenPreview() {
    AuraPixTheme { SearchScreen() }
}
