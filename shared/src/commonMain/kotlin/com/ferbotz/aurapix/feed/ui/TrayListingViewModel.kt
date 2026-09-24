package com.ferbotz.aurapix.feed.ui

import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.PageLoader
import com.ferbotz.aurapix.core.ui.base.PagedList
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.feed.data.FeedRepository
import kotlinx.coroutines.flow.StateFlow

/** One card in a tray's "See all" grid. A tray holds templates or categories, never both. */
sealed interface TrayEntry {
    val id: String

    data class Template(val item: TemplateItem) : TrayEntry {
        override val id: String get() = item.id
    }

    data class Category(val item: CategoryItem) : TrayEntry {
        override val id: String get() = item.id
    }
}

/**
 * Backs the "See all" listing for one feed tray, a page at a time (§4.5). Picks the templates- or
 * categories- endpoint from the tray [kind] and maps the DTOs through the same
 * [toTemplateItem]/[toCategoryItem] mappers the feed uses, so cards look identical in both places.
 */
class TrayListingViewModel(
    feedRepository: FeedRepository,
    trayId: String,
    kind: FeedSectionKind,
) : AuraViewModel() {

    private val pages: PageLoader<*, TrayEntry> = when (kind) {
        FeedSectionKind.TEMPLATES -> PageLoader(
            scope = scope,
            fetch = { page -> feedRepository.getTrayTemplates(trayId, page) },
            key = TrayEntry::id,
            map = { TrayEntry.Template(it.toTemplateItem()) },
        )
        FeedSectionKind.CATEGORIES -> PageLoader(
            scope = scope,
            fetch = { page -> feedRepository.getTrayCategories(trayId, page) },
            key = TrayEntry::id,
            map = { TrayEntry.Category(it.toCategoryItem()) },
        )
    }

    val state: StateFlow<UiState<PagedList<TrayEntry>>> = pages.state

    init {
        pages.refresh()
    }

    fun retry() = pages.refresh()

    fun loadMore() = pages.loadMore()
}
