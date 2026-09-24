package com.ferbotz.aurapix.category.ui

import com.ferbotz.aurapix.category.data.CategoriesRepository
import com.ferbotz.aurapix.core.data.remote.dto.TemplateSummaryDto
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.PageLoader
import com.ferbotz.aurapix.core.ui.base.PagedList
import com.ferbotz.aurapix.core.ui.base.UiState
import kotlinx.coroutines.flow.StateFlow

/** A template shown in the category grid (the screen never touches DTOs). */
data class CategoryTemplate(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val trending: Boolean = false,
)

/**
 * Backs [CategoryDetailScreen]: loads one category's templates a page at a time via
 * [CategoriesRepository.getCategoryTemplates] (§4.8) and maps them to [CategoryTemplate]s.
 * The category name comes in through the route, so no header fetch is needed.
 */
class CategoryDetailViewModel(
    categoriesRepository: CategoriesRepository,
    categoryId: String,
) : AuraViewModel() {

    private val pages = PageLoader(
        scope = scope,
        fetch = { page -> categoriesRepository.getCategoryTemplates(categoryId, page) },
        key = CategoryTemplate::id,
        map = { it.toCategoryTemplate() },
    )

    val state: StateFlow<UiState<PagedList<CategoryTemplate>>> = pages.state

    init {
        pages.refresh()
    }

    fun retry() = pages.refresh()

    fun loadMore() = pages.loadMore()
}

private fun TemplateSummaryDto.toCategoryTemplate(): CategoryTemplate = CategoryTemplate(
    id = id,
    name = title,
    thumbnailUrl = thumbnailImageUrl,
    trending = isTrending,
)
