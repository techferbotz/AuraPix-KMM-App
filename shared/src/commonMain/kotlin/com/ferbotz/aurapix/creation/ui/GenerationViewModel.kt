package com.ferbotz.aurapix.creation.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState

import com.ferbotz.aurapix.creation.data.dto.CreationDetailDto
import com.ferbotz.aurapix.creation.data.CreationsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class GenerationViewModel(
    creationsRepository: CreationsRepository,
) : AuraViewModel() {

    private data class Request(val templateId: String, val images: List<ByteArray>)

    private val request = MutableStateFlow<Request?>(null)

    /** Idle until [generate]; then Loading while generating + polling; Success holds the terminal creation. */
    val state: StateFlow<UiState<CreationDetailDto>> =
        request
            .filterNotNull()
            .flatMapLatest { creationsRepository.generateAndPoll(it.templateId, it.images) }
            .map { it.toUiState() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Idle)

    fun generate(templateId: String, images: List<ByteArray>) {
        request.value = Request(templateId, images)
    }

    fun reset() {
        request.value = null
    }
}
