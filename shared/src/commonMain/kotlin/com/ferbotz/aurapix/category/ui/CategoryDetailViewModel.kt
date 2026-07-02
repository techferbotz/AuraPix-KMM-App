package com.ferbotz.aurapix.category.ui

import com.ferbotz.aurapix.category.data.CategoriesRepository
import com.ferbotz.aurapix.core.data.remote.dto.TemplateSummaryDto
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** A template shown in the category grid (the screen never touches DTOs). */
data class CategoryTemplate(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val trending: Boolean = false,
)

/**
 * Backs [CategoryDetailScreen]: loads one category's paginated templates via
 * [CategoriesRepository.getCategoryTemplates] and maps them to [CategoryTemplate]s.
 * The category name comes in through the route, so no header fetch is needed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModel(
    private val categoriesRepository: CategoriesRepository,
    private val categoryId: String,
) : AuraViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val state: StateFlow<UiState<List<CategoryTemplate>>> =
        refreshTrigger
            .flatMapLatest {
                categoriesRepository.getCategoryTemplates(categoryId).map { dataState ->
                    dataState.toUiState { paged -> paged.items.map { it.toCategoryTemplate() } }
                }
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() {
        refreshTrigger.value += 1
    }
}

private fun TemplateSummaryDto.toCategoryTemplate(): CategoryTemplate = CategoryTemplate(
    id = id,
    name = title,
    thumbnailUrl = thumbnailImageUrl,
    trending = isTrending,
)
