package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.remote.ApiError

/** Maps an [ApiError] to a short, user-facing message for error states. */
fun ApiError.userMessage(): String = when (this) {
    ApiError.Unauthorized, ApiError.InvalidToken -> "Your session expired. Please sign in again."
    is ApiError.InvalidRequest -> message
    ApiError.TemplateNotFound -> "This template isn't available."
    ApiError.TemplateDisabled -> "This template is temporarily unavailable."
    ApiError.CategoryNotFound -> "This category isn't available."
    ApiError.CreationNotFound -> "We couldn't find that creation."
    ApiError.InvalidImageCount -> "Wrong number of photos for this template."
    ApiError.InsufficientCredits -> "You're out of credits."
    ApiError.GenerationFailed -> "Generation failed. Please try again."
    ApiError.InternalError -> "Something went wrong. Please try again."
    is ApiError.NetworkError -> "No connection. Check your network and retry."
}
