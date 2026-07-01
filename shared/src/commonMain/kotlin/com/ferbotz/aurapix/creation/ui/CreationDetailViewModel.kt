package com.ferbotz.aurapix.creation.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState
import com.ferbotz.aurapix.creation.data.CreationsRepository
import com.ferbotz.aurapix.creation.data.dto.CreationDetailDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Loads a single creation for the result/detail screen. */
@OptIn(ExperimentalCoroutinesApi::class)
class CreationDetailViewModel(
    creationsRepository: CreationsRepository,
) : AuraViewModel() {

    private val creationId = MutableStateFlow<String?>(null)

    val state: StateFlow<UiState<CreationDetailDto>> =
        creationId
            .filterNotNull()
            .flatMapLatest { creationsRepository.getCreation(it) }
            .map { it.toUiState() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun load(id: String) {
        creationId.value = id
    }
}
