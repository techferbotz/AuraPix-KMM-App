package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.ApiError

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: ApiError) : UiState<Nothing>()
}
