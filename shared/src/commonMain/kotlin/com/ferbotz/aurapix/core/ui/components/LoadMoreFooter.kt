package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.base.PagedList
import com.ferbotz.aurapix.core.ui.base.userMessage

/**
 * The last row of a paged list, shown while [PagedList.hasMore].
 *
 * Being composed is the trigger: a lazy list only composes rows at (or just past) the edge of the
 * screen, so this asks for the next page as the user nears the end — and asks again after each
 * page lands, in case the list still doesn't reach past the screen. A failed page shows why, with
 * a Retry, rather than retrying on its own against a dead connection.
 */
@Composable
fun LoadMoreFooter(list: PagedList<*>, onLoadMore: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        val error = list.loadMoreError
        if (error == null) {
            LaunchedEffect(list.loads) { onLoadMore() }
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    error.userMessage(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                SecondaryButton("Retry", onLoadMore, Modifier.width(160.dp))
            }
        }
    }
}
