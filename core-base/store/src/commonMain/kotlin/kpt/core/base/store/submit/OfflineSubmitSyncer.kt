/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.submit

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kpt.core.base.observability.CrashReporter
import kpt.core.base.observability.CrashSeverity

/**
 * Retries all PENDING outbox entries whenever the device regains connectivity.
 *
 * Wire this once in the app's DI scope (e.g. `AppViewModel`) to ensure that drafts saved
 * during offline submissions are automatically retried when the network comes back.
 *
 * ```kotlin
 * val syncer = coroutineScope.offlineSubmitSyncer(
 *     outbox             = roomSubmitOutbox,
 *     networkStatusFlow  = networkMonitor.networkStatus,
 *     submitBlock        = { payload -> repository.submitLoanApplication(payload) },
 *     retryOnStatus      = RetryOnNetworkStatus.OnlineOnly,   // default
 * )
 * syncer.start()
 * ```
 *
 * Pass `retryOnStatus = RetryOnNetworkStatus.OnlineOrCaptivePortal` for apps whose API is
 * reachable behind a captive portal (e.g. corporate WiFi after sign-in) — retries will
 * resume immediately after portal auth instead of waiting for the portal to clear.
 *
 * @param P Serializable payload type that the outbox holds.
 * @param R Result type returned by the server on success (ignored — fire-and-forget).
 *
 * **Optional crash reporter:** Pass a [CrashReporter] (typically Koin-injected) and the syncer
 * will record retry failures as non-fatal exceptions — useful in production where a single
 * persistent retry failure can indicate a server-side regression. When null (the default),
 * failures are silently logged to the outbox only. The optional shape preserves backwards
 * compatibility — existing call-sites that don't pass a reporter remain unchanged.
 */
class OfflineSubmitSyncer<P, R>(
    private val scope: CoroutineScope,
    private val outbox: SubmitOutbox<P>,
    private val networkStatusFlow: Flow<NetworkStatus>,
    private val submitBlock: suspend (P) -> R,
    private val retryOnStatus: RetryOnNetworkStatus = RetryOnNetworkStatus.OnlineOnly,
    private val crashReporter: CrashReporter? = null,
    /**
     * Per-attempt retry-backoff policy — APPLIED (v14+). Each entry carries a persisted
     * `attemptCount`; [retryAll] waits [RetryPolicy.delayFor] before re-submitting and stops
     * retrying an entry once its attempts reach [RetryPolicy.maxAttempts], marking it permanently
     * FAILED. Persisting the count means the cap survives process death — a crash-looping payload
     * cannot retry forever across restarts. Defaults to [RetryPolicy] defaults (3 attempts,
     * 1s/2s/4s backoff, ±25% jitter).
     */
    private val retryPolicy: RetryPolicy = RetryPolicy(),
) {

    /**
     * Begin watching [networkStatusFlow]. The status is mapped through [retryOnStatus] to
     * a `Boolean` "should retry now?" signal, then edge-triggered so retries fire only when
     * transitioning into a retry-eligible state.
     *
     * Individual entry failures are logged to the outbox as FAILED; they do not abort the
     * batch — remaining entries continue to be retried.
     */
    fun start(): Job = scope.launch {
        val online = networkStatusFlow
            .map { status -> retryOnStatus.shouldRetry(status) }
            .distinctUntilChanged()
            .stateIn(this)
        // Two triggers: coming online, and new PENDING entries while online (a Retry in Sync &
        // Drafts, a draft kept a moment ago). `conflate` runs sweeps one at a time — triggers that
        // arrive during a sweep fold into one more — so a sweep never races itself.
        merge(
            online.filter { it }.map { },
            outbox.observePendingIds().distinctUntilChanged().filter { it.isNotEmpty() }.map { },
        )
            .conflate()
            .collect { if (online.value) sweepUntilSettled() }
    }

    /**
     * Sweep, and sweep again while any entry was put back for another attempt.
     *
     * The edge-trigger fires ONCE per reconnect, so before this a failed attempt was final: the
     * entry went FAILED on the first exception — a transient one included (an HTTP/2 stream reset
     * while the app was starting, device 2026-10-26) — and [retryPolicy] never got to retry it.
     * Each later round still waits its entry's backoff first, so this cannot spin.
     */
    private suspend fun sweepUntilSettled() {
        while (retryAll()) Unit
    }

    /** @return true when an entry was requeued and the sweep should run again. */
    private suspend fun retryAll(): Boolean {
        var requeued = false
        val pending = outbox.getAllPending()
        for (entry in pending) {
            // Cap: an entry that has already burned its attempts is terminally FAILED, not retried
            // again on the next reconnect. attemptCount is persisted, so this holds across restarts.
            if (entry.attemptCount >= retryPolicy.maxAttempts) {
                outbox.markFailed(
                    entry.id,
                    "retry limit reached (${entry.attemptCount}/${retryPolicy.maxAttempts})",
                )
                continue
            }
            // Backoff BEFORE the attempt: attemptCount is the number already made, so
            // delayFor(0) == 0 leaves the first attempt immediate and later ones spaced 1s/2s/4s
            // (±25% jitter) so reconnecting clients don't thundering-herd a recovering server.
            val backoffMs = retryPolicy.delayFor(entry.attemptCount)
            if (backoffMs > 0L) delay(backoffMs)
            outbox.markRetrying(entry.id)
            try {
                submitBlock(entry.payload)
                outbox.markSubmitted(entry.id)
            } catch (e: CancellationException) {
                // Cancelled = the connection went (collectLatest) or the scope ended — not the
                // payload's fault. Back to PENDING so the next reconnect sends it; FAILED here
                // stranded it for good.
                withContext(NonCancellable) { outbox.requeue(entry.id, null) }
                throw e
            } catch (e: Exception) {
                // attemptCount is the attempts made BEFORE this one (markRetrying bumped the row).
                if (entry.attemptCount + 1 < retryPolicy.maxAttempts) {
                    outbox.requeue(entry.id, e.message)
                    requeued = true
                } else {
                    outbox.markFailed(entry.id, e.message)
                }
                // Surface retry-failure to the fork's crash-reporter (if wired) so production
                // installs can spot recurring failures without polling the outbox table.
                crashReporter?.recordException(
                    throwable = e,
                    message = "OfflineSubmitSyncer.retryAll: entry=${entry.id} " +
                        "failed during connectivity-triggered retry",
                )
            }
        }
        return requeued
    }
}

/**
 * Creates an [OfflineSubmitSyncer] bound to this [CoroutineScope].
 *
 * ```kotlin
 * val syncer = viewModelScope.offlineSubmitSyncer(
 *     outbox             = roomSubmitOutbox,
 *     networkStatusFlow  = networkMonitor.networkStatus,
 *     submitBlock        = { payload -> api.submit(payload) },
 * )
 * syncer.start()
 * ```
 */
fun <P, R> CoroutineScope.offlineSubmitSyncer(
    outbox: SubmitOutbox<P>,
    networkStatusFlow: Flow<NetworkStatus>,
    submitBlock: suspend (P) -> R,
    retryOnStatus: RetryOnNetworkStatus = RetryOnNetworkStatus.OnlineOnly,
    crashReporter: CrashReporter? = null,
    retryPolicy: RetryPolicy = RetryPolicy(),
): OfflineSubmitSyncer<P, R> = OfflineSubmitSyncer(
    scope = this,
    outbox = outbox,
    networkStatusFlow = networkStatusFlow,
    submitBlock = submitBlock,
    retryOnStatus = retryOnStatus,
    crashReporter = crashReporter,
    retryPolicy = retryPolicy,
)

/**
 * Re-exported for callers that want to set retry-classification severity in their own code.
 * Currently informational only — [OfflineSubmitSyncer] records exceptions at the default
 * severity (no [CrashSeverity] argument needed). Re-exposing the type avoids forcing every
 * call-site to depend on `core-base:observability` directly.
 */
typealias OfflineSubmitSyncerCrashSeverity = CrashSeverity
