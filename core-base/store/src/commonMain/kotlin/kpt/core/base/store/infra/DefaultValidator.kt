/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra

import org.mobilenativefoundation.store.store5.Validator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

/**
 * A TTL-based [Validator] that marks cached data as stale after a given duration.
 *
 * Tracks when data was last fetched using [TimeSource.Monotonic] and considers
 * it invalid once the [ttl] has elapsed. Call [markFresh] when fresh data arrives.
 *
 * @param Output The cached data type.
 * @param ttl Maximum age before data is considered stale. Defaults to 30 minutes.
 */
class DefaultValidator<Output : Any>(
    private val ttl: Duration = 30.minutes,
) : Validator<Output> {

    private var lastFetchMark: TimeSource.Monotonic.ValueTimeMark? = null

    /**
     * Marks the current moment as the last fetch time. **Must be called from inside the
     * Fetcher block** after a successful network response, or the TTL timer never starts
     * and cached data is treated as always valid regardless of age.
     *
     * Typical Fetcher wiring:
     * ```kotlin
     * val validator = DefaultValidator.withTtl<MyData>(ttl = 15.minutes)
     * val store = StoreFactory.createStore(
     *     fetcher = Fetcher.of { key ->
     *         val data = api.fetch(key)
     *         validator.markFresh()   // ← required here, after successful network call
     *         data
     *     },
     *     sourceOfTruth = mySourceOfTruth,
     *     validator = validator,
     * )
     * ```
     */
    fun markFresh() {
        lastFetchMark = TimeSource.Monotonic.markNow()
    }

    override suspend fun isValid(item: Output): Boolean {
        // Before first fetch, trust cached data (avoids Store5 invalidation NPE).
        // TTL starts after the first successful network write calls markFresh().
        val mark = lastFetchMark ?: return true
        return mark.elapsedNow() < ttl
    }

    /** Validator presets. */
    companion object {

        /**
         * Creates a [Validator] that always considers data valid (no TTL).
         */
        fun <Output : Any> alwaysValid(): Validator<Output> {
            return Validator.by { true }
        }

        /**
         * Creates a TTL-based [Validator] with the given duration.
         *
         * @param ttl Maximum age before data is stale.
         */
        fun <Output : Any> withTtl(
            ttl: Duration = 30.minutes,
        ): DefaultValidator<Output> {
            return DefaultValidator(ttl = ttl)
        }
    }
}
