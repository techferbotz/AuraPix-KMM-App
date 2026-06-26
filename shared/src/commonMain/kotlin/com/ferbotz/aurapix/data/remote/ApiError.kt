package com.ferbotz.aurapix.data.remote

sealed class ApiError : Exception() {
    data object Unauthorized : ApiError()
    data object InvalidToken : ApiError()
    data class InvalidRequest(override val message: String) : ApiError()
    data object TemplateNotFound : ApiError()
    data object TemplateDisabled : ApiError()
    data object CategoryNotFound : ApiError()
    data object CreationNotFound : ApiError()
    data object InvalidImageCount : ApiError()
    data object InsufficientCredits : ApiError()
    data object GenerationFailed : ApiError()
    data object InternalError : ApiError()
    data class NetworkError(val error: Throwable) : ApiError()
}

fun String.toApiError(message: String): ApiError = when (this) {
    "UNAUTHORIZED"         -> ApiError.Unauthorized
    "INVALID_TOKEN"        -> ApiError.InvalidToken
    "INVALID_REQUEST"      -> ApiError.InvalidRequest(message)
    "TEMPLATE_NOT_FOUND"   -> ApiError.TemplateNotFound
    "TEMPLATE_DISABLED"    -> ApiError.TemplateDisabled
    "CATEGORY_NOT_FOUND"   -> ApiError.CategoryNotFound
    "CREATION_NOT_FOUND"   -> ApiError.CreationNotFound
    "INVALID_IMAGE_COUNT"  -> ApiError.InvalidImageCount
    "INSUFFICIENT_CREDITS" -> ApiError.InsufficientCredits
    "GENERATION_FAILED"    -> ApiError.GenerationFailed
    else                   -> ApiError.InternalError
}

/** Normalizes any throwable from a failed [Result] into an [ApiError]. */
fun Throwable.asApiError(): ApiError = this as? ApiError ?: ApiError.NetworkError(this)
