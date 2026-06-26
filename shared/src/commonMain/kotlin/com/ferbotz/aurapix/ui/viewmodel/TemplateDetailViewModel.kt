package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.dto.TemplateDetailDto
import com.ferbotz.aurapix.data.repository.TemplatesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateDetailViewModel(
    templatesRepository: TemplatesRepository,
) : AuraViewModel() {

    private val templateId = MutableStateFlow<String?>(null)

    val templateState: StateFlow<UiState<TemplateDetailDto>> =
        templateId
            .filterNotNull()
            .flatMapLatest { templatesRepository.getTemplate(it) }
            .map { it.toUiState() }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun load(id: String) {
        templateId.value = id
    }
}
