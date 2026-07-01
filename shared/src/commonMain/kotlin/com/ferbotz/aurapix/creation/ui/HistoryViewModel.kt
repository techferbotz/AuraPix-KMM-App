package com.ferbotz.aurapix.creation.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState

import com.ferbotz.aurapix.creation.data.local.CreationEntity
import com.ferbotz.aurapix.creation.data.CreationsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    creationsRepository: CreationsRepository,
) : AuraViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val state: StateFlow<UiState<List<HistoryItem>>> =
        refreshTrigger
            .flatMapLatest { creationsRepository.observeCreations() }
            .map { dataState -> dataState.toUiState { list -> list.map { it.toHistoryItem() } } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun refresh() {
        refreshTrigger.value += 1
    }
}

private fun CreationEntity.toHistoryItem() = HistoryItem(
    title = templateTitleSnapshot,
    category = status,
    id = id,
    imageUrl = generatedImageUrl,
    status = status,
)
