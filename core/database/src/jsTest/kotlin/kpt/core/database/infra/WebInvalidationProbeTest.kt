/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.infra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kpt.core.base.database.infra.entity.DraftEntity
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Does Room's own `InvalidationTracker` re-emit a DAO `Flow` after writes on the single-threaded
 * web event loop? That question decides whether the invalidation bridge
 * (`core-base/database/.../invalidation`) is load-bearing, so it is answered by measurement here
 * rather than asserted in prose.
 *
 * ## Why none of this uses `runTest` virtual time or turbine
 *
 * It MUST NOT. Under `runTest` the scheduler runs on virtual time, and `awaitItem()` waiting on a
 * value that can only arrive from a REAL web-worker round trip spins the single JS thread: the
 * browser stops answering Karma's pings and is dropped with
 * "Disconnected (0 times) reconnect failed before timeout of 2000ms (ping timeout)".
 *
 * That failure looks exactly like "Room lost the invalidation" and is not — it is the harness
 * blocking the very event loop the driver needs. It cost one wrong conclusion already (a bug was
 * attributed to Room that a real-time measurement then disproved), so every wait below is real
 * time on [Dispatchers.Default] and every collection is a plain `launch` + list.
 *
 * Only runnable where a browser `Worker` exists — `jsBrowserTest`/`wasmJsBrowserTest`, never the
 * node tasks (the module's build script excludes it there).
 */
class WebInvalidationProbeTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        startKoin { modules(testPlatformModule) }
        database = KoinPlatform.getKoin().get()
    }

    @AfterTest
    fun teardown() {
        runCatching { database.close() }
        stopKoin()
    }

    @Test
    fun roomReEmitsAfterASingleWriteOnWeb() = runTest {
        val dao = database.billReminderDao
        val seen = mutableListOf<Int>()
        val job = launch(Dispatchers.Default) { dao.count().collect { seen += it } }
        withContext(Dispatchers.Default) {
            settle()
            dao.upsert(bill("S1"))
            assertEquals(1, awaitLast(seen) { it == 1 }, "single write never re-emitted: $seen")
        }
        job.cancel()
    }

    /**
     * The scenario the bridge README says is broken. Measured on Room 3.1.0-alpha01 (js + wasmJs,
     * ChromeHeadless 153) it is NOT: the two signals coalesce into one emission of 2, which is
     * correct behaviour, not a lost refresh.
     *
     * Room gates `refreshAsync()` on `pendingRefresh.compareAndSet(false, true)` and drops the
     * second call, so a lost refresh IS structurally possible — this asserts that in practice the
     * surviving refresh still observes both committed writes. If a future Room release regresses
     * here, this test fails and the bridge becomes mandatory again rather than advisory.
     */
    @Test
    fun roomReEmitsAfterTwoRapidWritesOnWebWithoutTheBridge() = runTest {
        val dao = database.billReminderDao
        val seen = mutableListOf<Int>()
        val job = launch(Dispatchers.Default) { dao.count().collect { seen += it } }
        withContext(Dispatchers.Default) {
            settle()
            dao.upsert(bill("R1"))
            dao.upsert(bill("R2"))
            assertEquals(2, awaitLast(seen) { it == 2 }, "rapid writes never reached 2: $seen")
        }
        job.cancel()
    }

    /**
     * The scenario the README ACTUALLY argues is broken, and which the two-write cases do not
     * cover: a BURST of writes while the single event loop is also busy with other work (the
     * README's "Compose recomposition, StateFlow updates, navigation events" contention).
     *
     * If Room's gate loses a refresh anywhere, this is where — every write after the first races
     * a refresh that is already pending, and the loop is contended so each suspension yields to
     * something else. Asserting the FINAL count is observed is the honest check: intermediate
     * coalescing is correct, a stale terminal value is not.
     */
    @Test
    fun roomObservesFinalStateAfterAWriteBurstUnderLoopContention() = runTest {
        val dao = database.billReminderDao
        val seen = mutableListOf<Int>()
        val job = launch(Dispatchers.Default) { dao.count().collect { seen += it } }
        val noise = launch(Dispatchers.Default) {
            while (true) {
                delay(1)
            }
        }
        withContext(Dispatchers.Default) {
            settle()
            repeat(BURST) { dao.upsert(bill("W$it")) }
            assertEquals(
                BURST,
                awaitLast(seen) { it == BURST },
                "burst of $BURST writes never reached a terminal $BURST; observed=$seen",
            )
        }
        noise.cancel()
        job.cancel()
    }

    // ── real-time helpers ──────────────────────────────────────────────────────────────────
    /** Let the initial emission land before writing, so the first value is a known baseline. */
    private suspend fun settle() = delay(SETTLE_MS)

    /** Polls in REAL time; returns the matching value, or the last seen value on timeout. */
    private suspend fun awaitLast(seen: List<Int>, predicate: (Int) -> Boolean): Int? {
        var waited = 0L
        while (waited < AWAIT_TIMEOUT_MS) {
            seen.lastOrNull()?.let { if (predicate(it)) return it }
            delay(POLL_MS)
            waited += POLL_MS
        }
        return seen.lastOrNull()
    }

    private fun bill(id: String): BillReminderEntity = BillReminderEntity(
        id = id,
        name = "Probe $id",
        amount = 100.0,
        dueDay = 15,
        recurrence = Recurrence.MONTHLY,
        category = BillCategory.UTILITIES,
        enabled = true,
        reminderDaysBefore = 1,
        createdAtMs = 1_000L,
        updatedAtMs = 1_000L,
    )

    /**
     * The drafts-outbox path the bill-reminder probes above do NOT cover, on two axes at once: a
     * **filtered** query whose row must LEAVE the result set, driven by an **UPDATE** rather than an
     * insert. Everything above inserts rows into an unfiltered `count()`.
     *
     * `observeAllByFormKey` selects `status IN ('PENDING','RETRYING','FAILED')` and backs the
     * Sync & Drafts screen; `markSubmitted` is a bare `UPDATE … SET status = 'SUBMITTED'`. If Room's
     * tracker re-emitted only on insert, a submitted draft would sit in the picker forever. The
     * bridge used to force that refresh with `notifyingWrite(DRAFTS_TABLE)` — removed with the rest
     * of the bridge — so the behaviour is MEASURED here rather than inherited from the probes above.
     */
    @Test
    fun draftsFlowDropsARowOnWebWhenAnUpdateMovesItOutOfTheFilter() = runTest {
        val dao = database.draftDao
        val seen = mutableListOf<Int>()
        val job = launch(Dispatchers.Default) {
            dao.observeAllByFormKey(DRAFT_FORM_KEY).collect { seen += it.size }
        }
        withContext(Dispatchers.Default) {
            settle()
            val id = dao.insert(draft())
            assertEquals(1, awaitLast(seen) { it == 1 }, "draft insert never re-emitted: $seen")
            dao.markSubmitted(id, 2_000L)
            assertEquals(
                0,
                awaitLast(seen) { it == 0 },
                "UPDATE moving the row out of the filter never re-emitted; observed=$seen",
            )
        }
        job.cancel()
    }

    private fun draft(): DraftEntity = DraftEntity(
        formKey = DRAFT_FORM_KEY,
        payloadJson = "{}",
        status = "PENDING",
        createdAtMs = 1_000L,
        updatedAtMs = 1_000L,
    )

    private companion object {
        const val SETTLE_MS = 400L
        const val POLL_MS = 50L
        const val AWAIT_TIMEOUT_MS = 5_000L
        const val BURST = 20
        const val DRAFT_FORM_KEY = "probe-drafts"
    }
}
