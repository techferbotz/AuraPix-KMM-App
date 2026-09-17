package com.ferbotz.aurapix.core.config

import co.touchlab.kermit.Logger
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.auraJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal val configLogger: Logger = Logger.withTag("AuraPix-Config")

@OptIn(ExperimentalTime::class)
internal fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * Cache-then-network remote config. The cached document is restored **synchronously in the
 * constructor** (one small preferences read), so the very first [snapshot] value is already the
 * last session's config rather than the defaults whenever one exists — which is what makes the
 * second launch honour yesterday's kill switch even before the network answers.
 *
 * Every failure path keeps the current snapshot: offline, 5xx, timeout and an unparseable body
 * are all invisible to the user.
 *
 * @param now injectable clock (epoch millis) so staleness is testable.
 */
class RemoteConfigRepositoryImpl(
    private val remote: RemoteConfigRemoteDataSource,
    private val prefs: AppPreferences,
    private val json: Json = auraJson,
    private val now: () -> Long = ::nowEpochMillis,
) : RemoteConfigRepository {

    private val _snapshot = MutableStateFlow(loadCached() ?: RemoteConfigSnapshot.Defaults)
    override val snapshot: StateFlow<RemoteConfigSnapshot> = _snapshot.asStateFlow()

    /** Serialises refreshes so two triggers (cold start + foreground) don't double-fetch. */
    private val refreshMutex = Mutex()

    override suspend fun refresh(): Result<RemoteConfigSnapshot> = refreshMutex.withLock {
        remote.getConfig()
            .map { dto ->
                val fetchedAt = now()
                val fresh = dto.toDomain(fetchedAt, RemoteConfigSnapshot.Source.NETWORK)
                // Cache what we understood, re-encoded, so a payload we couldn't read can never
                // poison the next start — it would have failed decoding above instead.
                runCatching { prefs.cacheRemoteConfig(json.encodeToString(dto), fetchedAt) }
                    .onFailure { configLogger.w(it) { "Could not write remote config cache" } }
                _snapshot.value = fresh
                if (fresh.schemaVersion > RemoteConfigSnapshot.SUPPORTED_SCHEMA_VERSION) {
                    configLogger.w {
                        "Config schema ${fresh.schemaVersion} is newer than supported " +
                            "${RemoteConfigSnapshot.SUPPORTED_SCHEMA_VERSION}; unknown keys ignored"
                    }
                }
                configLogger.i {
                    "Config applied: variant=${fresh.variant} revision=${fresh.revision} " +
                        "ttl=${fresh.ttlSeconds}s source=${fresh.source}"
                }
                fresh
            }
            .onFailure { e ->
                configLogger.w { "Config refresh failed ($e); keeping ${_snapshot.value.source} copy" }
            }
    }

    override suspend fun refreshIfStale(): Result<RemoteConfigSnapshot> {
        // A refresh already in flight will land shortly — don't queue a second one behind it.
        if (refreshMutex.isLocked) return Result.success(_snapshot.value)
        if (!_snapshot.value.isStale(now())) return Result.success(_snapshot.value)
        return refresh()
    }

    private fun loadCached(): RemoteConfigSnapshot? {
        val raw = prefs.remoteConfigJson ?: return null
        return runCatching {
            json.decodeFromString<RemoteConfigResponseDto>(raw)
                .toDomain(prefs.remoteConfigFetchedAt, RemoteConfigSnapshot.Source.CACHE)
        }.onFailure { e ->
            configLogger.w(e) { "Ignoring unreadable cached remote config" }
        }.getOrNull()
    }
}
