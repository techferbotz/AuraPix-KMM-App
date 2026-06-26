package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.remote.ApiError

/**
 * Presentation-layer state collected by screens. [Idle] is the pre-action state for
 * action-driven screens (login, generate); auto-loading screens start at [Loading].
 */
sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val error: ApiError) : UiState<Nothing>()
}

/** Maps a data-layer [DataState] into a presentation [UiState], transforming the payload. */
inline fun <T, R> DataState<T>.toUiState(transform: (T) -> R): UiState<R> = when (this) {
    is DataState.Loading -> UiState.Loading
    is DataState.Success -> UiState.Success(transform(data))
    is DataState.Error -> UiState.Error(error)
}

/** Maps a data-layer [DataState] into a presentation [UiState] with the same payload. */
fun <T> DataState<T>.toUiState(): UiState<T> = toUiState { it }
