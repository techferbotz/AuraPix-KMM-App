package com.ferbotz.aurapix.creation.ui

import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.creation.data.dto.CreationDetailDto

/**
 * Why a generation ended on the failure screen. Each case is a different situation for the user
 * — above all in what a retry costs, since a gem is taken when a generation starts (§4.10).
 */
sealed interface GenerationFailure {

    /**
     * It ran, and failed: the creation came back `FAILED` (§4.12), maybe with a [reason]. It stays
     * in My Creations, and trying again means a new generation — paid for again.
     */
    data class Failed(val creationId: String, val reason: String?) : GenerationFailure

    /**
     * It started, but checking on it failed — usually the connection. It may still finish, and
     * checking again polls the same creation, which costs nothing.
     */
    data class Unconfirmed(val creationId: String, val error: ApiError) : GenerationFailure

    /** It never started: the request was refused, or never got through. */
    data class NotStarted(val error: ApiError) : GenerationFailure
}

/** A creation that came back `FAILED`, as the failure screen tells it. */
internal fun CreationDetailDto.asFailure(): GenerationFailure.Failed =
    GenerationFailure.Failed(id, failureReason?.trim()?.takeIf { it.isNotEmpty() })
