package com.ferbotz.aurapix.profile.ui

import com.ferbotz.aurapix.billing.data.PaymentManager
import com.ferbotz.aurapix.billing.data.SubscriptionsRepository
import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.creation.data.CreationsRepository
import com.ferbotz.aurapix.profile.data.AccountDeletion
import com.ferbotz.aurapix.profile.data.UserManager
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Where the delete-account confirmation is. */
sealed interface DeleteAccountStep {
    /** Waiting for the user to tap Delete. */
    data object Idle : DeleteAccountStep

    /** The call is in flight; Delete is disabled so it can't be sent twice. */
    data object Deleting : DeleteAccountStep

    /** The account is gone and the session with it; the app carries on as a guest. */
    data object Deleted : DeleteAccountStep

    /** Nothing was deleted (or it can't be told); [message] says so, and Delete retries. */
    data class Failed(val message: String) : DeleteAccountStep
}

data class DeleteAccountUiState(
    val step: DeleteAccountStep = DeleteAccountStep.Idle,
    /**
     * Where to cancel the user's subscription, or null when they have none. Deleting the account
     * does not cancel a store subscription — the store keeps billing, and renewals after deletion
     * grant nothing (§4.2a) — so the screen warns for as long as this is set.
     */
    val manageSubscriptionUrl: String? = null,
)

/**
 * Settings → Delete Account. Deletes through [UserManager], which also drops the session; this
 * adds the rest of what the account owned on the device — the cached creations and the
 * RevenueCat identity — and reads the subscription the confirmation has to warn about.
 */
class DeleteAccountViewModel(
    private val userManager: UserManager,
    private val subscriptions: SubscriptionsRepository,
    private val creations: CreationsRepository,
    private val payments: PaymentManager,
) : AuraViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<DeleteAccountUiState> = _state.asStateFlow()

    init {
        loadSubscription()
    }

    fun delete() {
        val step = _state.value.step
        if (step == DeleteAccountStep.Deleting || step == DeleteAccountStep.Deleted) return
        _state.update { it.copy(step = DeleteAccountStep.Deleting) }
        scope.launch {
            // Once the request is out it may land even if this screen goes away, and the device
            // has to end up matching the server — so see it through, cancelled or not.
            val next = withContext(NonCancellable) {
                when (val outcome = userManager.deleteAccount()) {
                    AccountDeletion.Deleted -> {
                        creations.clearCache()
                        payments.onLoggedOut()
                        DeleteAccountStep.Deleted
                    }
                    // Signed out and nothing deleted; the screen asks the user to sign in again.
                    AccountDeletion.SignInRequired -> {
                        payments.onLoggedOut()
                        DeleteAccountStep.Idle
                    }
                    is AccountDeletion.Failed -> DeleteAccountStep.Failed(outcome.error.deletionMessage())
                }
            }
            _state.update { it.copy(step = next) }
        }
    }

    /** Signed back in on this screen: start over, and re-read the subscription for this session. */
    fun onSignedIn() {
        _state.value = initialState()
        loadSubscription()
    }

    /** Seeded from the cached session, so the warning is there from the first frame. */
    private fun initialState() = DeleteAccountUiState(
        manageSubscriptionUrl = if (userManager.current.isPremium) playSubscriptionsUrl(null) else null,
    )

    /** The fresh answer from `GET /subscriptions`; on failure the cached session's stands. */
    private fun loadSubscription() {
        scope.launch {
            subscriptions.getSubscription().collect { result ->
                if (result is DataState.Success) {
                    val active = result.data.status == "ACTIVE"
                    _state.update {
                        it.copy(manageSubscriptionUrl = if (active) playSubscriptionsUrl(result.data.productId) else null)
                    }
                }
            }
        }
    }
}

/**
 * Google Play's own subscription screen, the only place a Play subscription can be cancelled.
 * Play is the only store that sells one today, so this holds on iOS too, where it opens the web
 * page. [productId] is the bare id, as the backend always returns it (§4.15).
 */
internal fun playSubscriptionsUrl(productId: String?): String {
    val base = "https://play.google.com/store/account/subscriptions"
    return if (productId.isNullOrBlank()) base else "$base?sku=$productId&package=$ANDROID_PACKAGE"
}

private const val ANDROID_PACKAGE = "com.ferbotz.aurapix"

private fun ApiError.deletionMessage(): String = when (this) {
    // The outcome is unknown, but retrying settles it either way.
    is ApiError.NetworkError -> "Couldn't reach AuraPix. Check your connection and try again."
    else -> "Something went wrong, and nothing was deleted. Please try again."
}
