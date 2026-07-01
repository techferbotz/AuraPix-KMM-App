package com.ferbotz.aurapix.template.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState

import com.ferbotz.aurapix.template.data.dto.TemplateDetailDto
import com.ferbotz.aurapix.template.data.TemplatesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateDetailViewModel(
    templatesRepository: TemplatesRepository,
) : AuraViewModel() {

    private val templateId = MutableStateFlow<String?>(null)
    private val refreshTrigger = MutableStateFlow(0)

    val templateState: StateFlow<UiState<TemplateDetailUi>> =
        combine(templateId.filterNotNull(), refreshTrigger) { id, _ -> id }
            .flatMapLatest { templatesRepository.getTemplate(it) }
            .map { state -> state.toUiState { it.toUi() } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun load(id: String) {
        templateId.value = id
    }

    fun retry() {
        refreshTrigger.value += 1
    }
}

private fun TemplateDetailDto.toUi() = TemplateDetailUi(
    id = id,
    title = title,
    thumbnailUrl = thumbnailImageUrl,
    description = longDescription?.takeIf { it.isNotBlank() } ?: shortDescription,
    trending = isTrending,
    categories = categories.map { it.name },
    previewImageUrls = previewImages.sortedBy { it.displayOrder }.map { it.imageUrl },
    slots = imageSlots.sortedBy { it.displayOrder }.map {
        TemplateSlotUi(title = it.title, description = it.description, exampleImageUrl = it.exampleImageUrl)
    },
)
