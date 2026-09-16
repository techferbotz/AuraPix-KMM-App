package com.ferbotz.aurapix.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/** Pill showing the user's remaining credits. Used in every top bar that needs it. */
@Composable
fun CreditsBadge(
    credits: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, AuraTheme.colors.glassBorder, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Rounded.Diamond,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text("$credits", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Circular icon button used for back/share/etc. Optional translucent "glass" styling. */
@Composable
fun AuraIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glass: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val shape = CircleShape
    val base = if (glass) {
        Modifier.background(AuraTheme.colors.glassSurface).border(1.dp, AuraTheme.colors.glassBorder, shape)
    } else {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
    }
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .then(base)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

/**
 * Standardized top app bar: a fixed 64dp row with an always-centered [title], an optional
 * leading slot (back arrow / avatar) and optional trailing [actions] (credits / icons).
 * Consumes the status-bar inset itself (the app is edge-to-edge), mirroring M3 TopAppBar.
 */
@Composable
fun AuraTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        navigationIcon?.let { Box(Modifier.align(Alignment.CenterStart)) { it() } }
        title?.let {
            Text(
                it,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (actions != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions,
            )
        }
    }
}

/** The three canonical destinations. (Mockups also showed mismatched Search/Create tabs;
 *  those are dropped here.) */
enum class AuraTab(val label: String, val icon: ImageVector) {
    Feed("Feed", Icons.Rounded.GridView),
    MyCreations("Creations", Icons.Rounded.PhotoLibrary),
    Profile("Profile", Icons.Rounded.Person),
}

/**
 * Bottom navigation for Feed / My Creations / Profile.
 *
 * A standard Material 3 [NavigationBar] — anchored, opaque, icon **and** label, with the usual
 * pill indicator behind the selected icon. The brand shows up only in the indicator's violet;
 * everything else is stock M3 behaviour, sizing and motion.
 *
 * (This replaces an earlier floating glass pill taken from the design canvas. The canvas specifies
 * a 3-tab icon-only floating bar; we deliberately diverge — a standard bar is more legible, labels
 * remove the guess-the-icon problem, and it doesn't need the content-fade hack that a bar floating
 * over a scrolling grid required.)
 */
@Composable
fun AuraBottomBar(
    selected: AuraTab,
    onSelect: (AuraTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        AuraTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(24.dp)) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

/**
 * Container for the three tab screens: a [Scaffold] with [AuraBottomBar] in the `bottomBar` slot,
 * so the bar is anchored and the content sits above it rather than scrolling underneath.
 *
 * [content] receives the scaffold's own [PaddingValues]. The bottom value already covers the nav
 * bar and the system inset — pass it to your scrollable's `contentPadding` so the last row clears
 * the bar.
 */
@Composable
fun AuraTabScaffold(
    selectedTab: AuraTab,
    onSelectTab: (AuraTab) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        bottomBar = { AuraBottomBar(selected = selectedTab, onSelect = onSelectTab) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        content(innerPadding)
    }
}
