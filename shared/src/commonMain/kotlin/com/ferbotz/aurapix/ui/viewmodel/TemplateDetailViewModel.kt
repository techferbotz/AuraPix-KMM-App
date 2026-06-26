package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.dto.TemplateDetailDto
import com.ferbotz.aurapix.data.repository.TemplatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TemplateDetailViewModel(
    private val templatesRepository: TemplatesRepository,
) : AuraViewModel() {

    private val _templateState = MutableStateFlow<UiState<TemplateDetailDto>>(UiState.Loading)
    val templateState: StateFlow<UiState<TemplateDetailDto>> = _templateState.asStateFlow()

    fun load(templateId: String) {
        scope.launch {
            _templateState.value = UiState.Loading
            templatesRepository.getTemplate(templateId).fold(
                onSuccess = { _templateState.value = UiState.Success(it) },
                onFailure = { _templateState.value = UiState.Error(it.toApiError()) },
            )
        }
    }
}
