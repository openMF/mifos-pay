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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for [RetryPolicy] — exponential backoff with jitter, pure data shape.
 *
 * All jitter behavior is verified via a deterministic `random` lambda so the
 * tests don't drift across runs. The default `kotlin.random.Random.nextDouble()`
 * path is exercised by integration / hand-testing rather than this unit suite.
 */
class RetryPolicyTest {

    // ─── T1: monotonic increase ──────────────────────────────────────────────

    @Test
    fun `delays increase monotonically with attempt number when jitter is zero`() {
        val policy = RetryPolicy(
            maxAttempts = 5,
            initialDelayMs = 1_000L,
            multiplier = 2.0,
            // zero jitter so the sequence is deterministic
            jitterFraction = 0.0,
            maxDelayMs = 60_000L,
        )

        val schedule = (1..5).map { policy.delayFor(it) }
        // 1000, 2000, 4000, 8000, 16000
        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L), schedule)

        // sanity-check monotonicity in the loop form too
        for (i in 1 until schedule.size) {
            assertTrue(
                schedule[i] >= schedule[i - 1],
                "expected delays to be monotonically non-decreasing — got $schedule",
            )
        }
    }

    // ─── T2: cap at maxDelayMs ───────────────────────────────────────────────

    @Test
    fun `delay is capped at maxDelayMs once exponential growth would exceed it`() {
        val policy = RetryPolicy(
            maxAttempts = 20,
            initialDelayMs = 1_000L,
            multiplier = 2.0,
            jitterFraction = 0.0,
            maxDelayMs = 5_000L,
        )

        // Attempt 1: 1000, 2: 2000, 3: 4000, 4: 8000 → cap to 5000, 5: cap to 5000…
        assertEquals(1_000L, policy.delayFor(1))
        assertEquals(2_000L, policy.delayFor(2))
        assertEquals(4_000L, policy.delayFor(3))
        assertEquals(5_000L, policy.delayFor(4))
        assertEquals(5_000L, policy.delayFor(10))
        assertEquals(5_000L, policy.delayFor(20))
    }

    // ─── T3: jitter bounds — deterministic via fixed `random` ────────────────

    @Test
    fun `jitter stays within plus-or-minus jitterFraction of base delay`() {
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelayMs = 1_000L,
            multiplier = 2.0,
            jitterFraction = 0.25,
            maxDelayMs = 60_000L,
        )

        // Attempt 2 base = 2000ms. Jitter range = ±500ms → [1500, 2500].
        val maxJitterRandom = policy.delayFor(attempt = 2, random = { 1.0 }) // (2*1-1)=+1 → +25%
        val minJitterRandom = policy.delayFor(attempt = 2, random = { 0.0 }) // (2*0-1)=-1 → -25%
        val midJitterRandom = policy.delayFor(attempt = 2, random = { 0.5 }) // (2*0.5-1)=0 → 0%

        assertEquals(2_500L, maxJitterRandom, "+jitter must hit +25% boundary")
        assertEquals(1_500L, minJitterRandom, "-jitter must hit -25% boundary")
        assertEquals(2_000L, midJitterRandom, "0-centred jitter must equal base")
    }

    // ─── T4: constructor validation ──────────────────────────────────────────

    @Test
    fun `constructor rejects invalid arguments`() {
        assertFailsWith<IllegalArgumentException> { RetryPolicy(maxAttempts = 0) }
        assertFailsWith<IllegalArgumentException> { RetryPolicy(maxAttempts = -1) }
        assertFailsWith<IllegalArgumentException> { RetryPolicy(initialDelayMs = 0L) }
        assertFailsWith<IllegalArgumentException> { RetryPolicy(initialDelayMs = -100L) }
        assertFailsWith<IllegalArgumentException> { RetryPolicy(multiplier = 0.99) }
        assertFailsWith<IllegalArgumentException> { RetryPolicy(jitterFraction = -0.1) }
        assertFailsWith<IllegalArgumentException> { RetryPolicy(jitterFraction = 1.5) }
    }

    // ─── T5: delayFor(0) returns 0 ───────────────────────────────────────────

    @Test
    fun `delayFor returns 0 for the initial attempt`() {
        val policy = RetryPolicy()
        assertEquals(0L, policy.delayFor(0))
        assertEquals(0L, policy.delayFor(-5), "negative attempt is treated as no-wait too")
    }

    // ─── T6: default exponential schedule snapshot ───────────────────────────

    @Test
    fun `default policy emits 1s 2s 4s schedule with zero jitter`() {
        // Defaults: maxAttempts=3, initial=1000, multiplier=2.0, jitterFraction=0.25, maxDelay=60_000.
        // Use the midpoint random (0.5) to zero out jitter and snapshot the deterministic shape.
        val policy = RetryPolicy()
        val midpointRandom: () -> Double = { 0.5 }

        assertEquals(1_000L, policy.delayFor(1, midpointRandom))
        assertEquals(2_000L, policy.delayFor(2, midpointRandom))
        assertEquals(4_000L, policy.delayFor(3, midpointRandom))
    }
}
