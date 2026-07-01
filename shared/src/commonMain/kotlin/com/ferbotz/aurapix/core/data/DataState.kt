package com.ferbotz.aurapix.core.data

import com.ferbotz.aurapix.core.data.remote.ApiError

/**
 * Data-layer signal emitted by repositories. The ViewModel maps this into a
 * presentation-layer `UiState`. Repositories emit [Loading] first, then [Success]
 * or [Error]; offline-first flows may emit [Success] (cached) then [Success] again
 * (refreshed) or keep [Success] and swallow the refresh [Error] when a cache exists.
 */
sealed class DataState<out T> {
    data object Loading : DataState<Nothing>()
    data class Success<out T>(val data: T) : DataState<T>()
    data class Error(val error: ApiError) : DataState<Nothing>()
}
