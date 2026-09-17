package com.ferbotz.aurapix.core.config

import kotlinx.coroutines.flow.StateFlow

/**
 * The app's remote config (`GET /config`, BE-005).
 *
 * [snapshot] **always** has a value, resolved in this order: the compiled-in defaults → the last
 * good document cached on this device → whatever the backend returned during this process.
 * Consumers read it synchronously; nothing waits on the network, so config can never be the
 * reason the app doesn't open, and no caller has to handle a "not loaded yet" state — that state
 * does not exist.
 */
interface RemoteConfigRepository {

    /** The current document plus where it came from. Never null; starts from cache or defaults. */
    val snapshot: StateFlow<RemoteConfigSnapshot>

    /** Shorthand for the current document. */
    val config: RemoteConfig get() = snapshot.value.config

    /**
     * `GET /config`. On success the document is applied to [snapshot] and cached for the next
     * session; on failure the current snapshot is kept and the error returned. Call on every cold
     * start. Never throws.
     */
    suspend fun refresh(): Result<RemoteConfigSnapshot>

    /**
     * [refresh] only when the current snapshot is older than its `ttlSeconds` (or was never
     * fetched); otherwise returns it as is. Call when the app returns to the foreground.
     */
    suspend fun refreshIfStale(): Result<RemoteConfigSnapshot>
}
