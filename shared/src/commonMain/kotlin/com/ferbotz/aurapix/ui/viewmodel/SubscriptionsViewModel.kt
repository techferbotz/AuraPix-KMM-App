package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.PurchaseDto
import com.ferbotz.aurapix.data.remote.dto.SubscriptionDetailDto
import com.ferbotz.aurapix.data.repository.SubscriptionsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionsViewModel(
    subscriptionsRepository: SubscriptionsRepository,
) : AuraViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val subscriptionState: StateFlow<UiState<SubscriptionDetailDto>> =
        refreshTrigger
            .flatMapLatest { subscriptionsRepository.getSubscription() }
            .map { it.toUiState() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val purchasesState: StateFlow<UiState<PagedResponse<PurchaseDto>>> =
        refreshTrigger
            .flatMapLatest { subscriptionsRepository.getPurchases() }
            .map { it.toUiState() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun refresh() {
        refreshTrigger.value += 1
    }
}
