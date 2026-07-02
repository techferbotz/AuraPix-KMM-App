package com.ferbotz.aurapix.billing.data

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitGetProducts
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Your RevenueCat **public** SDK key. TODO: replace with the real key from the RC dashboard, and
 * make sure the products in the dashboard match the `productId`s in the monetization config JSON.
 */
const val REVENUECAT_API_KEY: String = "TODO_REPLACE_revenuecat_public_sdk_key"

/** Raised when the user dismisses the native purchase sheet. */
class PurchaseCancelledException : Exception("Purchase cancelled")

/**
 * Wraps the RevenueCat KMP SDK for purchases. Subscription/credit state is read from OUR backend
 * (UserManager) for now — after a successful purchase the RevenueCat webhook credits the backend,
 * so the caller re-reads `/profile` (`UserManager.refresh`). Future: reconcile RC customer info
 * against the backend when they look out of sync.
 *
 * Android-functional now. iOS compiles but its runtime needs the native StoreKit setup (SPM).
 */
class PaymentManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

    /**
     * Buys [productId] via the store. Success means the store transaction completed; the backend is
     * credited asynchronously by RevenueCat's webhook, so the caller should then `UserManager.refresh()`.
     */
    suspend fun purchase(productId: String): Result<Unit> {
        if (!Purchases.isConfigured) return Result.failure(IllegalStateException("Payments not configured"))
        return try {
            val product = Purchases.sharedInstance.awaitGetProducts(listOf(productId)).firstOrNull()
                ?: return Result.failure(IllegalStateException("Product '$productId' not found"))
            Purchases.sharedInstance.awaitPurchase(product)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: PurchasesTransactionException) {
            if (e.userCancelled) Result.failure(PurchaseCancelledException()) else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
