/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.freshness

import kpt.core.base.store.error.ErrorCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the [ErrorCategory.toShortMessage] / [ErrorCategory.toLongMessage] contracts —
 * the per-variant strings shown in `FreshnessIndicator` tooltips and
 * `RefreshStateChip` labels.
 *
 * `toShortMessage()` is constrained to ≤ 20 chars so the chip + indicator render
 * cleanly even on narrow phone widths.
 */
class ErrorCategoryMessagesTest {

    // === toShortMessage — per-variant assertions ===

    @Test
    fun `Network short = No network`() {
        assertEquals("No network", ErrorCategory.Network.toShortMessage())
    }

    @Test
    fun `Auth short = Auth error`() {
        assertEquals("Auth error", ErrorCategory.Auth.toShortMessage())
    }

    @Test
    fun `RateLimit short = Rate limited`() {
        assertEquals("Rate limited", ErrorCategory.RateLimit.toShortMessage())
    }

    @Test
    fun `ClientError 404 short = Request error`() {
        assertEquals("Request error", ErrorCategory.ClientError(404).toShortMessage())
    }

    @Test
    fun `Server 503 short = Server error`() {
        assertEquals("Server error", ErrorCategory.Server(503).toShortMessage())
    }

    @Test
    fun `Server null short = Server error`() {
        assertEquals("Server error", ErrorCategory.Server(null).toShortMessage())
    }

    @Test
    fun `QuotaExceeded short = Quota exceeded`() {
        assertEquals("Quota exceeded", ErrorCategory.QuotaExceeded.toShortMessage())
    }

    @Test
    fun `Timeout Connect short = Timed out`() {
        assertEquals("Timed out", ErrorCategory.Timeout.Connect.toShortMessage())
    }

    @Test
    fun `Timeout Read short = Timed out`() {
        assertEquals("Timed out", ErrorCategory.Timeout.Read.toShortMessage())
    }

    @Test
    fun `Generic short = Sync failed`() {
        assertEquals("Sync failed", ErrorCategory.Generic.toShortMessage())
    }

    // === Length invariant ===

    @Test
    fun `every variant short message is at most 20 chars`() {
        val variants: List<ErrorCategory> = listOf(
            ErrorCategory.Network,
            ErrorCategory.Auth,
            ErrorCategory.RateLimit,
            ErrorCategory.ClientError(400),
            ErrorCategory.Server(500),
            ErrorCategory.Server(null),
            ErrorCategory.QuotaExceeded,
            ErrorCategory.Timeout.Connect,
            ErrorCategory.Timeout.Read,
            ErrorCategory.Generic,
        )
        variants.forEach { cat ->
            val msg = cat.toShortMessage()
            assertTrue(
                msg.length <= MAX_SHORT_LEN,
                "toShortMessage() for $cat is '$msg' (length=${msg.length}), exceeds $MAX_SHORT_LEN",
            )
        }
    }

    @Test
    fun `every variant long message is non-empty`() {
        val variants: List<ErrorCategory> = listOf(
            ErrorCategory.Network,
            ErrorCategory.Auth,
            ErrorCategory.RateLimit,
            ErrorCategory.ClientError(400),
            ErrorCategory.Server(500),
            ErrorCategory.QuotaExceeded,
            ErrorCategory.Timeout.Connect,
            ErrorCategory.Timeout.Read,
            ErrorCategory.Generic,
        )
        variants.forEach { cat ->
            assertTrue(cat.toLongMessage().isNotBlank(), "toLongMessage() for $cat is blank")
        }
    }

    private companion object {
        const val MAX_SHORT_LEN = 20
    }
}
