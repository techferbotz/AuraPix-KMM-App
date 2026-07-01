package com.ferbotz.aurapix.creation.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState
import com.ferbotz.aurapix.creation.data.CreationsRepository
import com.ferbotz.aurapix.creation.data.dto.CreationDetailDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the generate → long-poll flow. Imperative (not the flatMapLatest/stateIn pattern) so
 * [retry] can re-run the last request and set Loading **synchronously**, avoiding a stale
 * terminal state being re-observed when the Processing screen re-enters composition.
 */
class GenerationViewModel(
    private val creationsRepository: CreationsRepository,
) : AuraViewModel() {

    private val _state = MutableStateFlow<UiState<CreationDetailDto>>(UiState.Idle)
    /** Idle → Loading (generating + polling) → Success(terminal creation) | Error. */
    val state: StateFlow<UiState<CreationDetailDto>> = _state.asStateFlow()

    private var lastRequest: Pair<String, List<ByteArray>>? = null
    private var job: Job? = null

    fun generate(templateId: String, images: List<ByteArray>) {
        lastRequest = templateId to images
        run(templateId, images)
    }

    fun retry() {
        lastRequest?.let { (templateId, images) -> run(templateId, images) }
    }

    fun reset() {
        job?.cancel()
        lastRequest = null
        _state.value = UiState.Idle
    }

    private fun run(templateId: String, images: List<ByteArray>) {
        job?.cancel()
        _state.value = UiState.Loading
        job = scope.launch {
            creationsRepository.generateAndPoll(templateId, images).collect { _state.value = it.toUiState() }
        }
    }
}
