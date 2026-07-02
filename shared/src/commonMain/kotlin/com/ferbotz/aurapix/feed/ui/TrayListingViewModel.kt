package com.ferbotz.aurapix.feed.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState
import com.ferbotz.aurapix.feed.data.FeedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** A tray's "See all" payload — a tray is either templates or categories, never both. */
sealed interface TrayItems {
    data class Templates(val items: List<TemplateItem>) : TrayItems
    data class Categories(val items: List<CategoryItem>) : TrayItems
}

/**
 * Backs the "See all" listing for one feed tray. Picks the templates- or categories- endpoint
 * from the tray [kind] and maps the paged DTOs through the same [toTemplateItem]/[toCategoryItem]
 * mappers the feed uses, so cards look identical in both places.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TrayListingViewModel(
    private val feedRepository: FeedRepository,
    private val trayId: String,
    private val kind: FeedSectionKind,
) : AuraViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val state: StateFlow<UiState<TrayItems>> =
        refreshTrigger
            .flatMapLatest {
                val flow: Flow<UiState<TrayItems>> = when (kind) {
                    FeedSectionKind.TEMPLATES ->
                        feedRepository.getTrayTemplates(trayId).map { dataState ->
                            dataState.toUiState { paged -> TrayItems.Templates(paged.items.map { it.toTemplateItem() }) }
                        }

                    FeedSectionKind.CATEGORIES ->
                        feedRepository.getTrayCategories(trayId).map { dataState ->
                            dataState.toUiState { paged -> TrayItems.Categories(paged.items.map { it.toCategoryItem() }) }
                        }
                }
                flow
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() {
        refreshTrigger.value += 1
    }
}
