package com.ferbotz.aurapix.profile.data

import com.ferbotz.aurapix.core.data.remote.ApiError

/**
 * How `DELETE /delete-account` ended (API.md §4.2a), in the terms the app acts on. The server
 * side is all-or-nothing, so anything short of [Deleted] deleted nothing — except a network
 * error, after which nobody knows until the user retries.
 */
sealed interface AccountDeletion {

    /**
     * 200 — or 401 `UNAUTHORIZED`, which means the account was already gone: a repeated call, or
     * a retry after a response that got lost. The contract says to treat the two the same.
     */
    data object Deleted : AccountDeletion

    /** 401 `INVALID_TOKEN`: the token was rejected and nothing was deleted. Sign in, then retry. */
    data object SignInRequired : AccountDeletion

    /**
     * Anything else. A 500 deleted nothing; after a network error the outcome is unknown. Retrying
     * is right either way — if the first call did land, the retry answers `UNAUTHORIZED`.
     */
    data class Failed(val error: ApiError) : AccountDeletion
}
