package com.ferbotz.aurapix.billing.ui

import com.ferbotz.aurapix.billing.data.PaymentManager
import com.ferbotz.aurapix.billing.data.PurchaseCancelledException
import com.ferbotz.aurapix.billing.data.RcPackage
import com.ferbotz.aurapix.core.config.MonetizationConfig
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.profile.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A purchasable plan: the live RevenueCat package (price + ids) enriched with gems from config. */
data class BillingPlan(
    val productId: String,
    val packageId: String,
    val priceLabel: String,
    val gems: Int,
    val isSubscription: Boolean,
    val highlighted: Boolean,
)

data class BillingUiState(
    val loading: Boolean = true,
    val plans: List<BillingPlan> = emptyList(),
    val error: String? = null,
    val purchasingProductId: String? = null,
    val purchaseComplete: Boolean = false,
)

/**
 * Backs the standalone Premium / Purchase Gems screens with the real RevenueCat flow: loads the
 * current offering (localized prices), joins each package with its gem count from
 * [MonetizationConfig], and runs purchase → backend verify. A verified purchase applies the fresh
 * balances to [UserManager] and flips [BillingUiState.purchaseComplete] so the screen can advance.
 */
class BillingViewModel(
    private val paymentManager: PaymentManager,
    private val userManager: UserManager,
    config: MonetizationConfig,
) : AuraViewModel() {

    private val offers = config.freeUserOffers + config.proUserOffers
    private val gemsByProduct = offers.associate { it.productId to it.gems }
    private val highlightByProduct = offers.associate { it.productId to it.highlighted }

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        scope.launch {
            paymentManager.getOfferings().fold(
                onSuccess = { packages ->
                    _state.value = _state.value.copy(loading = false, plans = packages.map { it.toPlan() }, error = null)
                },
                onFailure = {
                    _state.value = _state.value.copy(loading = false, error = "Couldn't load plans. Please try again.")
                },
            )
        }
    }

    /** Purchase the plan; a verified success applies the balances and flips [BillingUiState.purchaseComplete]. */
    fun purchase(plan: BillingPlan) {
        if (_state.value.purchasingProductId != null) return
        _state.value = _state.value.copy(purchasingProductId = plan.productId, error = null)
        scope.launch {
            paymentManager.purchaseAndVerify(plan.packageId).fold(
                onSuccess = { result ->
                    userManager.applyBilling(result.credits.totalCredits, result.subscription?.status ?: "NONE")
                    _state.value = _state.value.copy(purchasingProductId = null, purchaseComplete = true)
                },
                onFailure = { e ->
                    // Cancelling the native sheet isn't an error to surface.
                    val message = if (e is PurchaseCancelledException) null else "Purchase failed. Please try again."
                    _state.value = _state.value.copy(purchasingProductId = null, error = message)
                },
            )
        }
    }

    fun consumePurchaseComplete() {
        _state.value = _state.value.copy(purchaseComplete = false)
    }

    private fun RcPackage.toPlan() = BillingPlan(
        productId = productId,
        packageId = packageId,
        priceLabel = priceLabel,
        gems = gemsByProduct[productId] ?: 0,
        isSubscription = isSubscription,
        highlighted = highlightByProduct[productId] ?: false,
    )
}
