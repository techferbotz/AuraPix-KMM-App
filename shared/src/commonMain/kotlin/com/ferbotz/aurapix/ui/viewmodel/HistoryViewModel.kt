package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.local.CreationEntity
import com.ferbotz.aurapix.data.repository.CreationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val creationsRepository: CreationsRepository,
) : AuraViewModel() {

    val creationsFlow: Flow<List<CreationEntity>> = creationsRepository.observeCreations()

    private val _refreshState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val refreshState: StateFlow<UiState<Unit>> = _refreshState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _refreshState.value = UiState.Loading
            creationsRepository.refreshCreations().fold(
                onSuccess = { _refreshState.value = UiState.Success(Unit) },
                onFailure = { _refreshState.value = UiState.Error(it.toApiError()) },
            )
        }
    }
}
