package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.data.remote.dto.FeedTrayDto
import com.ferbotz.aurapix.data.remote.dto.TemplateSummaryDto
import com.ferbotz.aurapix.data.repository.CategoriesRepository
import com.ferbotz.aurapix.data.repository.FeedRepository
import com.ferbotz.aurapix.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeFeedViewModel(
    private val feedRepository: FeedRepository,
    private val categoriesRepository: CategoriesRepository,
    private val profileRepository: ProfileRepository,
) : AuraViewModel() {

    private val _traysState = MutableStateFlow<UiState<List<FeedTrayDto>>>(UiState.Loading)
    val traysState: StateFlow<UiState<List<FeedTrayDto>>> = _traysState.asStateFlow()

    private val _categoriesState = MutableStateFlow<UiState<List<CategorySummaryDto>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<CategorySummaryDto>>> = _categoriesState.asStateFlow()

    private val _credits = MutableStateFlow(profileRepository.getCachedCredits())
    val credits: StateFlow<Int> = _credits.asStateFlow()

    init {
        load()
    }

    fun load() {
        scope.launch {
            _traysState.value = UiState.Loading
            feedRepository.getFeed().fold(
                onSuccess = { _traysState.value = UiState.Success(it.trays) },
                onFailure = { _traysState.value = UiState.Error(it.toApiError()) },
            )
        }
        scope.launch {
            _categoriesState.value = UiState.Loading
            categoriesRepository.getCategories().fold(
                onSuccess = { _categoriesState.value = UiState.Success(it) },
                onFailure = { _categoriesState.value = UiState.Error(it.toApiError()) },
            )
        }
        scope.launch {
            profileRepository.getCredits().onSuccess {
                _credits.value = it.totalCredits
            }
        }
    }

    /** Extracts TemplateSummaryDto list from a TEMPLATE tray's JSON items. */
    fun templateItems(tray: FeedTrayDto): List<TemplateSummaryDto> =
        tray.items.mapNotNull { element ->
            try {
                com.ferbotz.aurapix.data.remote.auraJson.decodeFromJsonElement(
                    TemplateSummaryDto.serializer(), element
                )
            } catch (_: Exception) { null }
        }

    /** Extracts CategorySummaryDto list from a CATEGORY tray's JSON items. */
    fun categoryItems(tray: FeedTrayDto): List<CategorySummaryDto> =
        tray.items.mapNotNull { element ->
            try {
                com.ferbotz.aurapix.data.remote.auraJson.decodeFromJsonElement(
                    CategorySummaryDto.serializer(), element
                )
            } catch (_: Exception) { null }
        }
}
