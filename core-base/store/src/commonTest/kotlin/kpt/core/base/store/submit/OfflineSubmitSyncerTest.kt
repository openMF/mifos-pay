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

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineSubmitSyncerTest {

    private val formKey = "test_form"

    private val onlineWifi: NetworkStatus = NetworkStatus.Available(
        NetworkInfo(type = NetworkType.WiFi, isMetered = false),
    )
    private val captivePortalWifi: NetworkStatus = NetworkStatus.CaptivePortal(
        NetworkInfo(type = NetworkType.WiFi, isMetered = false),
    )

    // ─── T1: comes online → retries all PENDING ───────────────────────────────

    @Test
    fun `coming online retries all PENDING entries`() = runTest(UnconfinedTestDispatcher()) {
        val outbox = FakeSubmitOutbox<String>()
        outbox.save(formKey, "payload1")
        outbox.save("form2", "payload2")

        val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
        var submitCount = 0

        val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
            outbox = outbox,
            networkStatusFlow = networkStatus,
            submitBlock = { submitCount++ },
        )
        syncer.start()

        networkStatus.value = onlineWifi

        assertEquals(2, submitCount)
        assertTrue(outbox.getAllPending().isEmpty(), "All entries should be SUBMITTED after sync")
    }

    // ─── T2: edge-triggered — no duplicate retries ───────────────────────────

    @Test
    fun `emitting online twice only retries once per batch`() = runTest(UnconfinedTestDispatcher()) {
        val outbox = FakeSubmitOutbox<String>()
        outbox.save(formKey, "payload")

        val networkStatus = MutableStateFlow<NetworkStatus>(onlineWifi)
        var submitCount = 0

        val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
            outbox = outbox,
            networkStatusFlow = networkStatus,
            submitBlock = { submitCount++ },
        )
        syncer.start()

        // Re-emitting online with no state change must not re-trigger (distinctUntilChanged)
        networkStatus.value = onlineWifi

        assertEquals(1, submitCount, "distinctUntilChanged must prevent duplicate retry batch")
    }

    // ─── T3: retry success → entry marked SUBMITTED ───────────────────────────

    @Test
    fun `successful retry marks entry as SUBMITTED`() = runTest(UnconfinedTestDispatcher()) {
        val outbox = FakeSubmitOutbox<String>()
        outbox.save(formKey, "payload")

        val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
        val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
            outbox = outbox,
            networkStatusFlow = networkStatus,
            submitBlock = { /* success */ },
        )
        syncer.start()
        networkStatus.value = onlineWifi

        val entry = outbox.entries.first()
        assertEquals(SubmitOutboxStatus.SUBMITTED, entry.status)
    }

    // ─── T4: individual failure → FAILED, others continue ────────────────────

    @Test
    fun `one failing entry is marked FAILED and does not abort remaining entries`() =
        runTest(UnconfinedTestDispatcher()) {
            val outbox = FakeSubmitOutbox<String>()
            outbox.save("fail_form", "bad_payload")
            outbox.save("ok_form", "good_payload")

            val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
            val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
                outbox = outbox,
                networkStatusFlow = networkStatus,
                submitBlock = { payload ->
                    if (payload == "bad_payload") throw RuntimeException("server error")
                },
            )
            syncer.start()
            networkStatus.value = onlineWifi
            // A persistent failure is retried up to the cap (backoff 1s, 2s) before it is FAILED.
            testScheduler.advanceTimeBy(60_000) // past every backoff; backgroundScope work is not 'idle'-tracked

            val entries = outbox.entries
            val failEntry = entries.first { it.formKey == "fail_form" }
            val okEntry = entries.first { it.formKey == "ok_form" }
            assertEquals(SubmitOutboxStatus.FAILED, failEntry.status)
            assertEquals(SubmitOutboxStatus.SUBMITTED, okEntry.status)
        }

    // ─── T5: RETRYING entry is excluded from getAllPending → not double-synced ─

    @Test
    fun `entries already in RETRYING status are not picked up by syncer`() = runTest(UnconfinedTestDispatcher()) {
        val outbox = FakeSubmitOutbox<String>()
        val id = outbox.save(formKey, "payload")
        outbox.markRetrying(id) // simulate UI already claiming this entry

        val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
        var submitCount = 0
        val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
            outbox = outbox,
            networkStatusFlow = networkStatus,
            submitBlock = { submitCount++ },
        )
        syncer.start()
        networkStatus.value = onlineWifi

        assertEquals(0, submitCount, "RETRYING entries must be skipped by syncer")
    }

    // ─── T6: CaptivePortal status gated by RetryOnNetworkStatus policy ────────

    @Test
    fun `captive portal status does not retry under OnlineOnly policy`() =
        runTest(UnconfinedTestDispatcher()) {
            val outbox = FakeSubmitOutbox<String>()
            outbox.save(formKey, "payload")

            val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
            var submitCount = 0
            val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
                outbox = outbox,
                networkStatusFlow = networkStatus,
                submitBlock = { submitCount++ },
                retryOnStatus = RetryOnNetworkStatus.OnlineOnly,
            )
            syncer.start()
            networkStatus.value = captivePortalWifi

            assertEquals(
                0,
                submitCount,
                "OnlineOnly must NOT retry on captive portal (assumes portal not signed in)",
            )
        }

    @Test
    fun `captive portal status retries under OnlineOrCaptivePortal policy`() =
        runTest(UnconfinedTestDispatcher()) {
            val outbox = FakeSubmitOutbox<String>()
            outbox.save(formKey, "payload")

            val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
            var submitCount = 0
            val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
                outbox = outbox,
                networkStatusFlow = networkStatus,
                submitBlock = { submitCount++ },
                retryOnStatus = RetryOnNetworkStatus.OnlineOrCaptivePortal,
            )
            syncer.start()
            networkStatus.value = captivePortalWifi

            assertEquals(
                1,
                submitCount,
                "OnlineOrCaptivePortal must retry on captive portal (assumes portal signed in)",
            )
        }

    // ─── RetryPolicy is APPLIED (v14+) ────────────────────────────────────────
    //
    // Before this, OfflineSubmitSyncer accepted a RetryPolicy and ignored it — every PENDING entry
    // fired once per reconnect with no backoff and no attempt cap, so a caller passing
    // `RetryPolicy(maxAttempts = 5)` got none of it. These lock the wiring.

    @Test
    fun `entry at maxAttempts is failed permanently and never re-submitted`() =
        runTest(UnconfinedTestDispatcher()) {
            val outbox = FakeSubmitOutbox<String>()
            val id = outbox.save(formKey, "payload")
            // Simulate three prior failed cycles — the default policy allows 3 attempts.
            outbox.seedAttemptCount(id, 3)

            val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
            var submitCount = 0

            backgroundScope.offlineSubmitSyncer<String, Unit>(
                outbox = outbox,
                networkStatusFlow = networkStatus,
                submitBlock = { submitCount++ },
                retryPolicy = RetryPolicy(maxAttempts = 3),
            ).start()

            networkStatus.value = onlineWifi

            assertEquals(0, submitCount, "an entry past its attempt cap must NOT be re-submitted")
            assertTrue(outbox.getAllPending().isEmpty(), "it must be terminally FAILED, not left PENDING")
        }

    @Test
    fun `backoff delays the retry by the policy interval`() = runTest(UnconfinedTestDispatcher()) {
        val outbox = FakeSubmitOutbox<String>()
        val id = outbox.save(formKey, "payload")
        // One prior attempt → the next retry waits delayFor(1) == initialDelayMs (jitter disabled).
        outbox.seedAttemptCount(id, 1)

        val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
        var submitCount = 0

        backgroundScope.offlineSubmitSyncer<String, Unit>(
            outbox = outbox,
            networkStatusFlow = networkStatus,
            submitBlock = { submitCount++ },
            retryPolicy = RetryPolicy(maxAttempts = 3, initialDelayMs = 5_000L, jitterFraction = 0.0),
        ).start()

        val before = testScheduler.currentTime
        networkStatus.value = onlineWifi

        // The backoff `delay` suspends the collector, so the retry has NOT run yet — that suspension
        // is the behaviour under test. Before the fix there was no delay and the submit landed
        // synchronously here.
        assertEquals(0, submitCount, "retry must not fire before the backoff elapses")

        testScheduler.advanceTimeBy(6_000L)
        testScheduler.runCurrent()
        assertEquals(1, submitCount, "the entry is still within its attempt cap, so it retries")
        assertTrue(
            testScheduler.currentTime - before >= 5_000L,
            "retry must wait the policy backoff; virtual time advanced only " +
                "${testScheduler.currentTime - before}ms",
        )
    }

    // ─── retry within the cap ─────────────────────────────────────────────────

    @Test
    fun `a transient failure is retried within the cap and then lands`() = runTest(UnconfinedTestDispatcher()) {
        // Before: the first exception was final — FAILED, never picked up again — so RetryPolicy's
        // attempts were never used (an HTTP/2 stream reset at app start, device 2026-10-26).
        val outbox = FakeSubmitOutbox<String>()
        outbox.save(formKey, "payload")
        val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
        var calls = 0
        val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
            outbox = outbox,
            networkStatusFlow = networkStatus,
            submitBlock = { if (++calls == 1) throw RuntimeException("stream was reset: CANCEL") },
        )
        syncer.start()
        networkStatus.value = onlineWifi
        testScheduler.advanceTimeBy(60_000) // past every backoff; backgroundScope work is not 'idle'-tracked

        assertEquals(2, calls)
        assertEquals(SubmitOutboxStatus.SUBMITTED, outbox.entries.single().status)
    }

    @Test
    fun `a persistent failure goes FAILED once its attempts are spent`() = runTest(UnconfinedTestDispatcher()) {
        val outbox = FakeSubmitOutbox<String>()
        outbox.save(formKey, "payload")
        val networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
        var calls = 0
        val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
            outbox = outbox,
            networkStatusFlow = networkStatus,
            submitBlock = {
                calls++
                throw RuntimeException("rejected")
            },
            retryPolicy = RetryPolicy(maxAttempts = 3),
        )
        syncer.start()
        networkStatus.value = onlineWifi
        testScheduler.advanceTimeBy(60_000) // past every backoff; backgroundScope work is not 'idle'-tracked

        assertEquals(3, calls, "exactly maxAttempts tries, no more")
        assertEquals(SubmitOutboxStatus.FAILED, outbox.entries.single().status)
    }

    @Test
    fun `a draft that becomes pending while online is sent without waiting for a reconnect`() =
        runTest(UnconfinedTestDispatcher()) {
            // Sync & Drafts' Retry requeues a FAILED row while online; before, nothing sent it
            // until the connection next dropped and came back.
            val outbox = FakeSubmitOutbox<String>()
            val networkStatus = MutableStateFlow<NetworkStatus>(onlineWifi)
            var calls = 0
            val syncer = backgroundScope.offlineSubmitSyncer<String, Unit>(
                outbox = outbox,
                networkStatusFlow = networkStatus,
                submitBlock = { calls++ },
            )
            syncer.start()

            outbox.save(formKey, "kept while online")
            testScheduler.advanceTimeBy(60_000)

            assertEquals(1, calls)
            assertEquals(SubmitOutboxStatus.SUBMITTED, outbox.entries.single().status)
        }

    @Test
    fun `stopping the syncer mid-attempt puts the entry back to PENDING, not FAILED`() =
        runTest(UnconfinedTestDispatcher()) {
            val outbox = FakeSubmitOutbox<String>()
            outbox.save(formKey, "payload")
            val networkStatus = MutableStateFlow<NetworkStatus>(onlineWifi)
            val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
            val job = backgroundScope.offlineSubmitSyncer<String, Unit>(
                outbox = outbox,
                networkStatusFlow = networkStatus,
                submitBlock = { gate.await() },
            ).start()

            job.cancel()

            assertEquals(SubmitOutboxStatus.PENDING, outbox.entries.single().status)
        }
}
