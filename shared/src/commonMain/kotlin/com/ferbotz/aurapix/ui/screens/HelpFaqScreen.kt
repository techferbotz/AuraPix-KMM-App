package com.ferbotz.aurapix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.ui.components.AuraIconButton
import com.ferbotz.aurapix.ui.components.AuraSearchField
import com.ferbotz.aurapix.ui.components.AuraTopBar
import com.ferbotz.aurapix.ui.components.GlassCard
import com.ferbotz.aurapix.ui.components.PrimaryButton
import com.ferbotz.aurapix.ui.theme.AuraPixTheme

/** Help center: search, category shortcuts, an FAQ accordion and a support CTA. */
@Composable
fun HelpFaqScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onContactSupport: () -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableIntStateOf(0) }
    val categories = listOf(
        "Account" to Icons.Rounded.Person,
        "Billing" to Icons.Rounded.CreditCard,
        "Gen AI" to Icons.Rounded.AutoAwesome,
        "Privacy" to Icons.Rounded.Lock,
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AuraTopBar(
                title = "Help & FAQ",
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
            AuraSearchField(query, { query = it }, placeholder = "Search for help")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (label, icon) ->
                            CategoryCard(label, icon, Modifier.weight(1f))
                        }
                    }
                }
            }

            Text("Frequently asked", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            sampleFaqs.forEachIndexed { index, faq ->
                FaqAccordion(
                    faq = faq,
                    expanded = expanded == index,
                    onToggle = { expanded = if (expanded == index) -1 else index },
                )
            }

            GlassCard(glow = true, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.SupportAgent, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Text(
                    "Still need help?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    "Our support team typically replies within a few hours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PrimaryButton("Contact Support", onContactSupport, Modifier.fillMaxWidth().padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun CategoryCard(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun FaqAccordion(faq: FaqItem, expanded: Boolean, onToggle: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(faq.question, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(expanded) {
            Text(
                faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Preview
@Composable
private fun HelpFaqScreenPreview() {
    AuraPixTheme { HelpFaqScreen() }
}
