package com.ferbotz.aurapix.creation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.ui.components.AuraTopBar
import com.ferbotz.aurapix.core.ui.components.CircularProgressRing
import com.ferbotz.aurapix.core.ui.components.CreditsBadge
import com.ferbotz.aurapix.core.ui.components.GlassCard
import com.ferbotz.aurapix.core.ui.components.StageRow
import com.ferbotz.aurapix.core.ui.components.StageState
import com.ferbotz.aurapix.core.ui.theme.AuraPixTheme

/** Generation progress: big ring, staged checklist and a helpful tip. */
@Composable
fun ProcessingScreen(
    modifier: Modifier = Modifier,
    progress: Float = 0.45f,
    credits: Int = 50,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AuraTopBar(title = "AuraPix", actions = { CreditsBadge(credits) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            CircularProgressRing(
                progress = progress,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text("Generating", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                "Crafting your portrait",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StageRow(Icons.Rounded.CloudUpload, "Uploading photos", StageState.Done)
                StageRow(Icons.Rounded.Tune, "Preparing AI model", StageState.Active)
                StageRow(Icons.Rounded.AutoAwesome, "Generating image", StageState.Pending)
                StageRow(Icons.Rounded.Brush, "Finalizing artwork", StageState.Pending)
            }

            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.Lightbulb, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text(
                        "Keep the app open while we generate your portrait — it usually takes under a minute.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProcessingScreenPreview() {
    AuraPixTheme { ProcessingScreen() }
}
