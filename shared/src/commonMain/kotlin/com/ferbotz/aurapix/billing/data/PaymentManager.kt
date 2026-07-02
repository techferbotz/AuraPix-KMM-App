package com.ferbotz.aurapix.billing.data

import com.ferbotz.aurapix.billing.data.dto.BillingResultDto
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.ProductType
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Your RevenueCat **public** SDK key. TODO: replace with your real (test) key from the RC dashboard,
 * and make sure the RC products/offerings match the `productId`s in the monetization config JSON.
 */
const val REVENUECAT_API_KEY: String = "TODO_REPLACE_revenuecat_public_sdk_key"

/** Raised when the user dismisses the native purchase sheet. */
class PurchaseCancelledException : Exception("Purchase cancelled")

/**
 * Wraps the RevenueCat KMP SDK: fetches offerings for the paywall, runs the store purchase, then
 * **verifies with our backend** (billing verify endpoints) so credits/subscription update immediately
 * instead of waiting for the webhook. Subscription/credit state is still owned by the backend
 * (UserManager); RC↔backend reconciliation (`/billing/sync`) is a future step.
 *
 * Android-functional now. iOS compiles but its runtime needs the native StoreKit setup (SPM).
 */
class PaymentManager(
    private val billingRemote: BillingRemoteDataSource,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentOffering: Offering? = null

    /** Configure once at app start (RevenueCat `appUserID` = our backend user id). Safe to re-call. */
    fun configure(appUserId: String?) {
        if (Purchases.isConfigured) return
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration(REVENUECAT_API_KEY) {
                if (appUserId != null) this.appUserId = appUserId
            },
        )
    }

    /** Align RevenueCat's identity with our backend user id after a login. Fire-and-forget. */
    fun identify(appUserId: String) {
        scope.launch { if (Purchases.isConfigured) runCatching { Purchases.sharedInstance.awaitLogIn(appUserId) } }
    }

    /** Reset RevenueCat's identity on logout. Fire-and-forget. */
    fun onLoggedOut() {
        scope.launch { if (Purchases.isConfigured) runCatching { Purchases.sharedInstance.awaitLogOut() } }
    }

    /** Fetch the current RevenueCat offering's packages to show in the paywall. */
    suspend fun getOfferings(): Result<List<RcPackage>> {
        if (!Purchases.isConfigured) return Result.failure(IllegalStateException("Payments not configured"))
        return try {
            val offering = Purchases.sharedInstance.awaitOfferings().current
            currentOffering = offering
            val packages = offering?.availablePackages.orEmpty().map { pkg ->
                RcPackage(
                    packageId = pkg.identifier,
                    productId = pkg.storeProduct.id,
                    title = pkg.storeProduct.title,
                    priceLabel = pkg.storeProduct.price.formatted,
                    isSubscription = pkg.storeProduct.type == ProductType.SUBS,
                )
            }
            Result.success(packages)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Purchases [packageId] via the store, then verifies it with the backend (§4.15/§4.16) so the
     * returned [BillingResultDto] carries the fresh credits/subscription — no webhook wait.
     */
    suspend fun purchaseAndVerify(packageId: String): Result<BillingResultDto> {
        val pkg = currentOffering?.availablePackages?.firstOrNull { it.identifier == packageId }
            ?: return Result.failure(IllegalStateException("Package '$packageId' not available"))
        return try {
            val txn = Purchases.sharedInstance.awaitPurchase(pkg).storeTransaction
            val productId = txn.productIds.firstOrNull() ?: pkg.storeProduct.id
            if (pkg.storeProduct.type == ProductType.SUBS) {
                billingRemote.verifySubscription(productId, txn.transactionId)
            } else {
                val transactionId = txn.transactionId
                    ?: return Result.failure(IllegalStateException("Missing transaction id"))
                billingRemote.verifyPurchase(transactionId, productId)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: PurchasesTransactionException) {
            if (e.userCancelled) Result.failure(PurchaseCancelledException()) else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
