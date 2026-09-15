package com.ferbotz.aurapix.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme

/**
 * The single search input used by Search and Help & FAQ.
 *
 * Built on [BasicTextField] rather than M3's `TextField`: the design specifies a 52dp box with a
 * 20dp radius, a hairline border and no underline, and M3's text field enforces a taller box and
 * its own internal padding. Focus swaps the hairline for a 1.5dp violet edge inside a soft ring —
 * the same "emitting" language as the CTA glow, scaled down.
 */
@Composable
fun AuraSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    onSearch: (() -> Unit)? = null,
) {
    val shape = AuraShapes.large
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        tween(180), label = "searchBorder",
    )
    val borderWidth by animateDpAsState(
        if (focused) 1.5.dp else 1.dp, tween(180), label = "searchBorderWidth",
    )
    val iconTint by animateColorAsState(
        if (focused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        tween(180), label = "searchIcon",
    )

    val selectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    )

    CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                // The focus ring sits outside the border, like the design's 3dp box-shadow.
                .then(
                    if (focused) {
                        Modifier.border(
                            4.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape,
                        )
                    } else {
                        Modifier
                    }
                )
                .height(52.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                .border(borderWidth, borderColor, shape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AuraTheme.colors.muted,
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        interactionSource = interaction,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (value.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear search",
                        tint = AuraTheme.colors.muted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onValueChange("") },
                    )
                }
            }
        }
    }
}

/** App switch with the brand violet active track. */
@Composable
fun AuraToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}
