package com.ferbotz.aurapix.core.ui.base

import com.ferbotz.aurapix.core.data.DataState
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.data.remote.dto.PagedResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PageLoaderTest {

    @Test
    fun pagesAppendUntilTheServerSaysThereAreNoMore() = runTest {
        val requested = mutableListOf<Int>()
        val loader = loader(requested, 1 to page(listOf("a", "b"), hasMore = true), 2 to page(listOf("c"), hasMore = false))

        loader.refresh(); advanceUntilIdle()
        assertEquals(listOf("a", "b"), loader.list().items)
        assertTrue(loader.list().hasMore)

        loader.loadMore(); advanceUntilIdle()
        assertEquals(listOf("a", "b", "c"), loader.list().items)
        assertFalse(loader.list().hasMore)

        loader.loadMore(); advanceUntilIdle()
        assertEquals(listOf(1, 2), requested, "no request past the last page")
    }

    /** A row added at the top between two requests pushes one row across the page boundary. */
    @Test
    fun aRowThatShiftsIntoTheNextPageIsNotShownTwice() = runTest {
        val loader = loader(mutableListOf(), 1 to page(listOf("a", "b"), hasMore = true), 2 to page(listOf("b", "c"), hasMore = false))

        loader.refresh(); advanceUntilIdle()
        loader.loadMore(); advanceUntilIdle()

        assertEquals(listOf("a", "b", "c"), loader.list().items)
    }

    @Test
    fun aFailedNextPageKeepsTheListAndCanBeRetried() = runTest {
        val pages = mutableMapOf(1 to page(listOf("a"), hasMore = true), 2 to failure())
        val loader = PageLoader<String, String>(this, { flowOf(DataState.Loading, pages.getValue(it)) }, key = { it }, map = { it })

        loader.refresh(); advanceUntilIdle()
        loader.loadMore(); advanceUntilIdle()
        assertEquals(listOf("a"), loader.list().items)
        assertIs<ApiError.NetworkError>(loader.list().loadMoreError)

        pages[2] = page(listOf("b"), hasMore = false)
        loader.loadMore(); advanceUntilIdle()
        assertEquals(listOf("a", "b"), loader.list().items)
        assertNull(loader.list().loadMoreError)
    }

    @Test
    fun aFailedFirstPageIsAScreenErrorAndRefreshStartsOver() = runTest {
        val pages = mutableMapOf(1 to failure())
        val loader = PageLoader<String, String>(this, { flowOf(DataState.Loading, pages.getValue(it)) }, key = { it }, map = { it })

        loader.refresh(); advanceUntilIdle()
        assertIs<UiState.Error>(loader.state.value)

        pages[1] = page(listOf("a"), hasMore = false)
        loader.refresh(); advanceUntilIdle()
        assertEquals(listOf("a"), loader.list().items)
    }

    @Test
    fun eachLoadedPageIsCounted() = runTest {
        val loader = loader(mutableListOf(), 1 to page(listOf("a"), hasMore = true), 2 to page(listOf("b"), hasMore = true))

        loader.refresh(); advanceUntilIdle()
        assertEquals(1, loader.list().loads)
        loader.loadMore(); advanceUntilIdle()
        assertEquals(2, loader.list().loads, "the footer re-asks on each change while it stays in view")
    }

    private fun TestScope.loader(
        requested: MutableList<Int>,
        vararg pages: Pair<Int, DataState<PagedResponse<String>>>,
    ): PageLoader<String, String> {
        val byNumber = pages.toMap()
        return PageLoader(
            scope = this,
            fetch = { page ->
                requested += page
                flowOf(DataState.Loading, byNumber.getValue(page))
            },
            key = { it },
            map = { it },
        )
    }

    private fun PageLoader<String, String>.list(): PagedList<String> =
        (state.value as UiState.Success).data

    private fun page(items: List<String>, hasMore: Boolean): DataState<PagedResponse<String>> =
        DataState.Success(PagedResponse(items, page = 0, limit = 20, hasMore = hasMore))

    private fun failure(): DataState<PagedResponse<String>> =
        DataState.Error(ApiError.NetworkError(IllegalStateException("offline")))
}
