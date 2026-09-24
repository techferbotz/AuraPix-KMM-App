package com.ferbotz.aurapix.core.ui.base

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A list read page by page — API.md §2: `page` + `limit`, follow `hasMore`, never assume a total.
 *
 * [items] only grows while the user scrolls. A page that fails leaves them where they are and
 * sets [loadMoreError] instead, so a flaky connection never blanks a list someone is reading.
 */
data class PagedList<out T>(
    val items: List<T> = emptyList(),
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val loadMoreError: ApiError? = null,
    /** Pages loaded so far. The load-more footer re-asks on each one while it stays in view. */
    val loads: Int = 0,
)

/**
 * Reads one paginated endpoint into a [PagedList]. The first page decides the screen — Loading,
 * then Success or a full-screen Error — and each [loadMore] appends the next page for as long as
 * the last one said `hasMore`.
 *
 * Pages are offsets, so a row added at the top between two requests pushes the previous page's
 * last row into the next one; [key] drops that repeat rather than drawing it twice.
 */
class PageLoader<Dto, Item>(
    private val scope: CoroutineScope,
    private val fetch: (page: Int) -> Flow<DataState<PagedResponse<Dto>>>,
    private val key: (Item) -> Any,
    private val map: (Dto) -> Item,
) {
    private val _state = MutableStateFlow<UiState<PagedList<Item>>>(UiState.Loading)
    val state: StateFlow<UiState<PagedList<Item>>> = _state.asStateFlow()

    private var nextPage = 1
    private var job: Job? = null

    /** Starts again from page 1: the first load, and the full-screen Retry. */
    fun refresh() {
        job?.cancel()
        nextPage = 1
        _state.value = UiState.Loading
        job = scope.launch { load() }
    }

    /** Appends the next page. Does nothing while one is loading, or once there are no more. */
    fun loadMore() {
        val list = (_state.value as? UiState.Success)?.data ?: return
        if (!list.hasMore || list.loadingMore) return
        _state.value = UiState.Success(list.copy(loadingMore = true, loadMoreError = null))
        job = scope.launch { load() }
    }

    private suspend fun load() {
        val page = nextPage
        val result = fetch(page).first { it !is DataState.Loading }
        val shown = (_state.value as? UiState.Success)?.data
        _state.value = when (result) {
            is DataState.Success -> {
                nextPage = page + 1
                val before = if (page == 1) emptyList() else shown?.items.orEmpty()
                UiState.Success(
                    PagedList(
                        items = (before + result.data.items.map(map)).distinctBy(key),
                        hasMore = result.data.hasMore,
                        loads = (shown?.loads ?: 0) + 1,
                    ),
                )
            }
            is DataState.Error ->
                if (page == 1 || shown == null) UiState.Error(result.error)
                else UiState.Success(shown.copy(loadingMore = false, loadMoreError = result.error))
            DataState.Loading -> return
        }
    }
}
