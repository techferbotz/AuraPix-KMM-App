package com.ferbotz.aurapix.billing.ui

import co.touchlab.kermit.Logger
import com.ferbotz.aurapix.billing.data.PaymentManager
import com.ferbotz.aurapix.billing.data.PurchaseCancelledException
import com.ferbotz.aurapix.billing.data.RcPackage
import com.ferbotz.aurapix.core.config.Store
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.profile.data.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A purchasable plan: the catalogue entry from `/config` joined with the live store package.
 *
 * The split is deliberate — [product] is everything we *say* about the plan and comes from the
 * config document, so copy, gems, ordering and perks change without a release; [priceLabel] is
 * what the store will actually charge, localized, and can only come from the SDK.
 */
data class BillingPlan(
    val product: Store.Product,
    val packageId: String,
    val priceLabel: String,
) {
    val productId: String get() = product.productId
    val gems: Int get() = product.gems
    val highlighted: Boolean get() = product.highlighted
    val isSubscription: Boolean get() = product.kind == Store.Kind.SUBSCRIPTION
}

private val billingLogger: Logger = Logger.withTag("AuraPix-Billing")

data class BillingUiState(
    val loading: Boolean = true,
    val plans: List<BillingPlan> = emptyList(),
    val error: String? = null,
    val purchasingProductId: String? = null,
    val purchaseComplete: Boolean = false,
)

/**
 * Backs the Premium and Buy-gems screens: loads the current store offering, joins each package
 * with its catalogue entry, and runs purchase → backend verify. A verified purchase applies the
 * fresh balances to [UserManager] and flips [BillingUiState.purchaseComplete].
 *
 * A package the catalogue doesn't describe is **dropped**, not drawn with blanks — see [Store].
 */
class BillingViewModel(
    private val paymentManager: PaymentManager,
    private val userManager: UserManager,
    private val store: Store,
    private val kind: Store.Kind,
    private val isPremium: Boolean = false,
) : AuraViewModel() {

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    /** The plan the screen should start on, once the offering has loaded. */
    val defaultPlan: BillingPlan?
        get() = _state.value.plans.let { plans ->
            store.defaultFor(plans.map { it.product })?.let { d -> plans.firstOrNull { it.productId == d.productId } }
        }

    init { load() }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        scope.launch {
            paymentManager.getOfferings().fold(
                onSuccess = { packages ->
                    _state.value = _state.value.copy(loading = false, plans = join(packages), error = null)
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

    /** Catalogue order, catalogue copy, store price. */
    private fun join(packages: List<RcPackage>): List<BillingPlan> {
        // Google Play reports a SUBSCRIPTION's store id as "<productId>:<basePlanId>", while
        // one-time products carry the bare id. The catalogue names the product, not the base
        // plan, so index both spellings rather than making every entry know its base plan.
        // (If two base plans of one subscription were ever offered at once, the later wins —
        // the catalogue would need an explicit basePlanId to choose between them.)
        val byProductId = buildMap {
            packages.forEach { pkg ->
                put(pkg.productId, pkg)
                put(pkg.productId.substringBefore(':'), pkg)
            }
        }
        val catalogue = store.productsFor(kind, isPremium)
        val plans = catalogue.mapNotNull { product ->
            val pkg = byProductId[product.productId] ?: return@mapNotNull null
            BillingPlan(
                product = product,
                packageId = pkg.packageId,
                priceLabel = pkg.priceLabel.ifBlank { product.fallbackPriceLabel.orEmpty() },
            )
        }
        billingLogger.i { "Store offering for $kind: ${packages.map { it.productId }}" }
        if (plans.size != catalogue.size) {
            // Silently showing fewer plans than the catalogue lists is how an empty Premium
            // screen happens; say which ids didn't line up.
            val missed = catalogue.map { it.productId } - plans.map { it.productId }.toSet()
            billingLogger.w { "No store package for $missed; offering has ${byProductId.keys}" }
        }
        return plans
    }
}
