package com.ferbotz.aurapix.billing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.ferbotz.aurapix.billing.data.PurchaseCancelledException
import com.ferbotz.aurapix.billing.data.RcPackage
import com.ferbotz.aurapix.core.config.MonetizationConfig
import com.ferbotz.aurapix.core.di.DataModule
import kotlinx.coroutines.launch

/**
 * Self-contained paywall: fetches the RevenueCat offering, shows [PaywallBottomSheet], and on select
 * runs purchase → backend verify. Reports the fresh balances via [onPurchased] (credits +
 * subscription status) so the caller can update `UserManager` and resume its gated action.
 */
@Composable
fun PaywallHost(
    config: MonetizationConfig,
    onDismiss: () -> Unit,
    onPurchased: (totalCredits: Int, subscriptionStatus: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var packages by remember { mutableStateOf<List<RcPackage>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var purchasingProductId by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableStateOf(0) }

    LaunchedEffect(reloadTick) {
        loading = true
        error = null
        DataModule.paymentManager.getOfferings()
            .onSuccess { packages = it; loading = false }
            .onFailure { packages = emptyList(); error = "Couldn't load plans. Please try again."; loading = false }
    }

    // productId → gems, from the monetization config (RC has price/availability; config has gems).
    val gemsByProduct = remember(config) {
        (config.freeUserOffers + config.proUserOffers).associate { it.productId to it.gems }
    }

    PaywallBottomSheet(
        packages = packages,
        loading = loading,
        purchasingProductId = purchasingProductId,
        errorMessage = error,
        generationCostGems = config.generationCostGems,
        gemsForProduct = { gemsByProduct[it] },
        onRetry = { reloadTick++ },
        onDismiss = onDismiss,
        onSelect = { pkg ->
            purchasingProductId = pkg.productId
            error = null
            scope.launch {
                DataModule.paymentManager.purchaseAndVerify(pkg.packageId)
                    .onSuccess { result ->
                        purchasingProductId = null
                        onPurchased(result.credits.totalCredits, result.subscription?.status ?: "NONE")
                    }
                    .onFailure { e ->
                        purchasingProductId = null
                        if (e !is PurchaseCancelledException) error = "Purchase failed. Please try again."
                    }
            }
        },
    )
}
