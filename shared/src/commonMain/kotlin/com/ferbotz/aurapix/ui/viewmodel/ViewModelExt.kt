package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.ApiError

fun Throwable.toApiError(): ApiError =
    if (this is ApiError) this else ApiError.NetworkError(this)
