package com.ferbotz.aurapix.template.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.OverlineLabel
import com.ferbotz.aurapix.core.ui.components.PrimaryButton
import com.ferbotz.aurapix.core.ui.components.plainTextClipEntry
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme
import com.ferbotz.aurapix.core.ui.theme.AuraShapes
import com.ferbotz.aurapix.core.ui.theme.AuraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * "Get prompt": the template's prompt, ready to reuse in another AI app. One button copies the
 * whole prompt verbatim ([TemplatePromptUi.text]). The steps cover the part people miss when they
 * move a prompt elsewhere: attaching their own photos, in the template's slot order.
 *
 * The sheet opens fully expanded, because a half-open sheet would push the Copy button below the
 * fold. The button stays pinned and the prompt and steps scroll above it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptBottomSheet(
    templateTitle: String,
    prompt: TemplatePromptUi,
    slots: List<TemplateSlotUi>,
    /** Whether generating in AuraPix is on, so the footnote can point back to it. */
    canGenerateHere: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        PromptSheetContent(
            templateTitle = templateTitle,
            prompt = prompt,
            slots = slots,
            canGenerateHere = canGenerateHere,
        )
    }
}

@Composable
private fun PromptSheetContent(
    templateTitle: String,
    prompt: TemplatePromptUi,
    slots: List<TemplateSlotUi>,
    canGenerateHere: Boolean,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    // The button's own "Copied" state is the confirmation. iOS has no system one, and on Android
    // 13+ it sits alongside the OS clipboard preview without doubling it the way a toast would.
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2_000)
            copied = false
        }
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
        PromptSheetHeader(templateTitle)

        // `fill = false` lets this shrink to fit, so on a short screen the prompt and steps scroll
        // while the header and the Copy button stay put.
        Column(
            Modifier.weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PromptCard(prompt)
            HowToUse(slots)
            ResultsNote(canGenerateHere)
        }

        PrimaryButton(
            text = if (copied) "Copied" else "Copy prompt",
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(plainTextClipEntry("Template prompt", prompt.text))
                    copied = true
                }
            },
            // Announce the label change, since the tap itself gives no other feedback on iOS.
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            leadingIcon = if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
        )
    }
}

@Composable
private fun PromptSheetHeader(templateTitle: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.Notes,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Use this prompt anywhere",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                templateTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The full prompt in a height-capped, scrollable card, so a long prompt can't push the steps off
 * the sheet. Long-press selects part of it; the Copy button always takes the whole prompt.
 */
@Composable
private fun PromptCard(prompt: TemplatePromptUi) {
    val scroll = rememberScrollState()
    val shape = AuraShapes.medium
    val fill = MaterialTheme.colorScheme.surfaceContainer
    Box(
        Modifier.fillMaxWidth()
            .heightIn(max = 280.dp)
            .clip(shape)
            .background(fill)
            .border(1.dp, AuraTheme.colors.glassBorder, shape),
    ) {
        SelectionContainer {
            Column(Modifier.verticalScroll(scroll).padding(16.dp)) {
                Text(prompt.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                prompt.negative?.let { negative ->
                    OverlineLabel("Negative prompt", Modifier.padding(top = 16.dp, bottom = 6.dp))
                    Text(negative, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        // Fade the cut edge so a long prompt reads as "scroll for more" rather than as truncated.
        if (scroll.canScrollForward) {
            Box(
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Brush.verticalGradient(listOf(fill.copy(alpha = 0f), fill))),
            )
        }
    }
}

private data class PromptStep(val title: String, val detail: String? = null)

/** The steps for another app. The photo step follows the template's slots, in slot order. */
@Composable
private fun HowToUse(slots: List<TemplateSlotUi>) {
    val steps = buildList {
        add(PromptStep("Copy the prompt"))
        add(PromptStep("Open Gemini, ChatGPT or another AI image app"))
        when (slots.size) {
            0 -> Unit
            1 -> add(PromptStep("Attach your photo", slots[0].description.ifBlank { slots[0].title }))
            else -> add(
                PromptStep(
                    "Attach your ${slots.size} photos in this order",
                    slots.mapIndexed { index, slot -> "${index + 1}. ${slot.title}" }.joinToString("  ·  "),
                )
            )
        }
        add(PromptStep("Paste the prompt and send"))
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OverlineLabel("How to use it")
        steps.forEachIndexed { index, step -> StepRow(index + 1, step) }
    }
}

@Composable
private fun StepRow(number: Int, step: PromptStep) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$number",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        // 2dp down so the first line centres on the 24dp number badge.
        Column(Modifier.weight(1f).padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(step.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            step.detail?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ResultsNote(canGenerateHere: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            Icons.Rounded.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            if (canGenerateHere) {
                "Results vary between AI apps. For the look shown here, generate it in AuraPix."
            } else {
                "Results vary between AI apps."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun PromptSheetContentPreview() {
    AuraPixTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow).padding(top = 24.dp)) {
            PromptSheetContent(
                templateTitle = "Cinematic Noir",
                prompt = templatePromptOf(
                    "A moody black-and-white portrait of the uploaded person, lit by a single hard key " +
                        "light through venetian blinds. Keep their face, age and features exactly. " +
                        "Negative Prompt: colour, cartoon, blur, extra people, watermark."
                )!!,
                slots = listOf(
                    TemplateSlotUi("Selfie", "A clear, front-facing photo."),
                    TemplateSlotUi("Side profile", "A photo from the side."),
                ),
                canGenerateHere = true,
            )
        }
    }
}
