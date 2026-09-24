package com.ferbotz.aurapix.creation.ui

import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.data.remote.asApiError
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.PagedList
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.creation.data.CreationsRepository
import com.ferbotz.aurapix.creation.data.local.CreationEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * My Creations, a page at a time (§4.11). The rows come from the cache — every page loaded so far,
 * shown at once and kept live — and the paging state wraps them: page 1 refreshes the cache when
 * the screen opens, and each [loadMore] adds the next page.
 */
class HistoryViewModel(
    private val creationsRepository: CreationsRepository,
) : AuraViewModel() {

    private data class Paging(
        val firstPageLoaded: Boolean = false,
        val firstPageError: ApiError? = null,
        val hasMore: Boolean = false,
        val loadingMore: Boolean = false,
        val loadMoreError: ApiError? = null,
        val loads: Int = 0,
    )

    private val paging = MutableStateFlow(Paging())
    private var nextPage = 1
    private var job: Job? = null

    val state: StateFlow<UiState<PagedList<HistoryItem>>> =
        combine(creationsRepository.observeCreations(), paging) { cached, p ->
            when {
                cached.isNotEmpty() || p.firstPageLoaded -> UiState.Success(
                    PagedList(
                        items = cached.map { it.toHistoryItem() },
                        hasMore = p.hasMore,
                        loadingMore = p.loadingMore,
                        loadMoreError = p.loadMoreError,
                        loads = p.loads,
                    ),
                )
                p.firstPageError != null -> UiState.Error(p.firstPageError)
                else -> UiState.Loading
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        job?.cancel()
        nextPage = 1
        paging.value = Paging()
        job = scope.launch { load() }
    }

    fun loadMore() {
        val p = paging.value
        if (!p.hasMore || p.loadingMore) return
        paging.value = p.copy(loadingMore = true, loadMoreError = null)
        job = scope.launch { load() }
    }

    private suspend fun load() {
        val page = nextPage
        creationsRepository.loadCreationsPage(page).fold(
            onSuccess = { hasMore ->
                nextPage = page + 1
                paging.update {
                    it.copy(
                        firstPageLoaded = true,
                        firstPageError = null,
                        hasMore = hasMore,
                        loadingMore = false,
                        loadMoreError = null,
                        loads = it.loads + 1,
                    )
                }
            },
            onFailure = { e ->
                val error = e.asApiError()
                paging.update {
                    // A first page that fails over a cached list leaves the list up, with the
                    // footer's Retry to try page 1 again.
                    if (page == 1) it.copy(firstPageError = error, hasMore = true, loadMoreError = error)
                    else it.copy(loadingMore = false, loadMoreError = error)
                }
            },
        )
    }
}

private fun CreationEntity.toHistoryItem() = HistoryItem(
    title = templateTitleSnapshot,
    category = status,
    id = id,
    imageUrl = generatedImageUrl,
    status = status,
)
