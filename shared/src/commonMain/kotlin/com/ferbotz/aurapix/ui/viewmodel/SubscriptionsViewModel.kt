package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.dto.PagedResponse
import com.ferbotz.aurapix.data.remote.dto.PurchaseDto
import com.ferbotz.aurapix.data.remote.dto.SubscriptionDetailDto
import com.ferbotz.aurapix.data.repository.SubscriptionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionsViewModel(
    private val subscriptionsRepository: SubscriptionsRepository,
) : AuraViewModel() {

    private val _subscriptionState = MutableStateFlow<UiState<SubscriptionDetailDto>>(UiState.Loading)
    val subscriptionState: StateFlow<UiState<SubscriptionDetailDto>> = _subscriptionState.asStateFlow()

    private val _purchasesState = MutableStateFlow<UiState<PagedResponse<PurchaseDto>>>(UiState.Loading)
    val purchasesState: StateFlow<UiState<PagedResponse<PurchaseDto>>> = _purchasesState.asStateFlow()

    init {
        load()
    }

    fun load() {
        scope.launch {
            subscriptionsRepository.getSubscription().fold(
                onSuccess = { _subscriptionState.value = UiState.Success(it) },
                onFailure = { _subscriptionState.value = UiState.Error(it.toApiError()) },
            )
        }
        scope.launch {
            subscriptionsRepository.getPurchases().fold(
                onSuccess = { _purchasesState.value = UiState.Success(it) },
                onFailure = { _purchasesState.value = UiState.Error(it.toApiError()) },
            )
        }
    }
}
