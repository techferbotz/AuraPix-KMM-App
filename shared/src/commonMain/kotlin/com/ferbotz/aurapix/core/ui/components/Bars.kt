package com.ferbotz.aurapix.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import com.ferbotz.aurapix.core.ui.theme.redGlow

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
    MyCreations("My Creations", Icons.Rounded.PhotoLibrary),
    Profile("Profile", Icons.Rounded.Person),
}

/**
 * Floating bottom navigation used by Feed / My Creations / Profile. A translucent glass pill that
 * hovers above the system nav inset; the selected tab is marked by a glowing purple circle around
 * its icon. Icon-only — no labels. Replaces the flat Material NavigationBar.
 */
@Composable
fun AuraBottomBar(
    selected: AuraTab,
    onSelect: (AuraTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraTheme.colors.navSurface, CircleShape)
                .border(1.dp, AuraTheme.colors.glassBorder, CircleShape)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AuraTab.entries.forEach { tab ->
                NavItem(tab = tab, selected = selected == tab, onClick = { onSelect(tab) })
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: AuraTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val fg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "navItemFg",
    )
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(220),
        label = "navItemBg",
    )
    val shape = CircleShape
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .then(if (selected) Modifier.redGlow(shape, elevation = 12.dp) else Modifier)
            .clip(shape)
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(tab.icon, contentDescription = tab.label, tint = fg, modifier = Modifier.size(24.dp))
    }
}

/** The vertical space the floating [AuraBottomBar] occupies (above the system nav inset). */
private val BottomBarHeight = 96.dp

/**
 * Container for the three tab screens. Unlike a plain [Scaffold] with a `bottomBar`, the
 * [AuraBottomBar] floats *over* the content, so the feed/gallery scrolls through behind the
 * translucent bar (no opaque strip). The [content] gets a [PaddingValues] carrying the top-bar
 * height and a bottom inset that clears the floating bar — apply the bottom to your scrollable's
 * `contentPadding` (not as a hard bottom padding on the container) so the last items scroll past it.
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            bottom = navBottom + BottomBarHeight,
        )
        Box(Modifier.fillMaxSize()) {
            content(contentPadding)
            AuraBottomBar(
                selected = selectedTab,
                onSelect = onSelectTab,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
