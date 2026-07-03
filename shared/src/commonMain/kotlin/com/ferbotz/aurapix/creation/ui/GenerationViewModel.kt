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
 * [retry] can re-run the request and set Loading **synchronously**, avoiding a stale terminal
 * state being re-observed when the Processing screen re-enters composition.
 *
 * Once `/generate` has produced a creation id, [retry] re-polls that SAME creation (`wait=true`)
 * rather than starting a new generation — a socket timeout or transient API failure must never
 * burn credits on a second image.
 */
class GenerationViewModel(
    private val creationsRepository: CreationsRepository,
) : AuraViewModel() {

    private val _state = MutableStateFlow<UiState<CreationDetailDto>>(UiState.Idle)
    /** Idle → Loading (generating + polling) → Success(terminal creation) | Error. */
    val state: StateFlow<UiState<CreationDetailDto>> = _state.asStateFlow()

    private var lastRequest: Pair<String, List<ByteArray>>? = null
    /** Set the instant `/generate` succeeds; retry re-polls this instead of regenerating. */
    private val creationId = MutableStateFlow<String?>(null)
    private var job: Job? = null

    fun generate(templateId: String, images: List<ByteArray>) {
        lastRequest = templateId to images
        creationId.value = null
        job?.cancel()
        _state.value = UiState.Loading
        job = scope.launch {
            creationsRepository.generateAndPoll(templateId, images) { creationId.value = it }
                .collect { _state.value = it.toUiState() }
        }
    }

    fun retry() {
        val id = creationId.value
        if (id == null) {
            // The POST /generate itself never succeeded — nothing to poll, so start over.
            lastRequest?.let { (templateId, images) -> generate(templateId, images) }
            return
        }
        // Generation already started; only the long-poll failed — re-poll the SAME creation.
        job?.cancel()
        _state.value = UiState.Loading
        job = scope.launch {
            creationsRepository.pollCreation(id).collect { _state.value = it.toUiState() }
        }
    }

    fun reset() {
        job?.cancel()
        lastRequest = null
        creationId.value = null
        _state.value = UiState.Idle
    }
}
