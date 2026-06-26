package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.ApiError
import com.ferbotz.aurapix.data.remote.dto.CreationDetailDto
import com.ferbotz.aurapix.data.repository.CreationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenerationViewModel(
    private val creationsRepository: CreationsRepository,
) : AuraViewModel() {

    private val _generateState = MutableStateFlow<UiState<String>>(UiState.Loading)
    /** State for the upload/generate step. Success holds the creationId. */
    val generateState: StateFlow<UiState<String>> = _generateState.asStateFlow()

    private val _pollingState = MutableStateFlow<UiState<CreationDetailDto>>(UiState.Loading)
    /** State for the polling step. Success holds the completed/failed creation. */
    val pollingState: StateFlow<UiState<CreationDetailDto>> = _pollingState.asStateFlow()

    fun generate(templateId: String, images: List<ByteArray>) {
        scope.launch {
            _generateState.value = UiState.Loading
            creationsRepository.generate(templateId, images).fold(
                onSuccess = { response ->
                    _generateState.value = UiState.Success(response.creationId)
                    pollUntilDone(response.creationId)
                },
                onFailure = { _generateState.value = UiState.Error(it.toApiError()) },
            )
        }
    }

    fun pollUntilDone(creationId: String) {
        scope.launch {
            _pollingState.value = UiState.Loading
            while (true) {
                val result = creationsRepository.pollCreation(creationId)
                result.fold(
                    onSuccess = { detail ->
                        when (detail.status) {
                            "COMPLETED", "FAILED" -> {
                                _pollingState.value = UiState.Success(detail)
                                return@launch
                            }
                            else -> { /* still PROCESSING — long-poll returned early, loop */ }
                        }
                    },
                    onFailure = {
                        _pollingState.value = UiState.Error(it.toApiError())
                        return@launch
                    },
                )
            }
        }
    }

    fun reset() {
        _generateState.value = UiState.Loading
        _pollingState.value = UiState.Loading
    }
}
