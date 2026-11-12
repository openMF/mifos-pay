/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.combine

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.combine.internal.ScreenWithMutationStreamImpl
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the [ScreenWithMutationStream] seam — focuses on the combine semantics,
 * not on the underlying [SubmitHandler] (covered exhaustively in
 * `SubmitHandlerTest`) or read pipeline (covered in
 * `ScreenDataStreamIntegrationTest`).
 *
 * Uses [ScreenWithMutationStreamImpl] directly with caller-controlled read
 * + outbox flows so the fan-in can be observed in isolation.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ScreenWithMutationStreamTest {

    @Test
    fun emitsLoadingThenContent() = runTest {
        val readFlow = MutableStateFlow<ScreenState<String>>(ScreenState.Loading)
        val submitHandler = backgroundScope.submitHandler<String>()

        val stream = ScreenWithMutationStreamImpl(
            readStream = readFlow,
            submitHandler = submitHandler,
            submitBlock = { it },
            pendingCountFlow = flowOf(0),
            syncingFlow = flowOf(false),
            scope = backgroundScope,
            onRefresh = {},
        )

        stream.state.test {
            // Initial value before WhileSubscribed materialises the upstream combine —
            // stateIn's initialValue is CombinedState(Loading, Idle, 0, false).
            val first = awaitItem()
            assertIs<ScreenState.Loading>(first.read)
            assertIs<SubmitState.Idle>(first.mutation)
            assertEquals(0, first.outboxPending)
            assertTrue(!first.isSyncing)

            // Push Content into the read flow — Combined state should advance.
            readFlow.value = ScreenState.Content("hello")
            val withContent = awaitItem()
            val content = withContent.read
            assertIs<ScreenState.Content<String>>(content)
            assertEquals("hello", content.data)
            assertIs<SubmitState.Idle>(withContent.mutation)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun outboxPendingThreadsThrough() = runTest(UnconfinedTestDispatcher()) {
        val readFlow = MutableStateFlow<ScreenState<String>>(
            ScreenState.Content("data"),
        )
        val pendingFlow = MutableStateFlow(0)
        val syncingFlow = MutableStateFlow(false)
        val submitHandler = backgroundScope.submitHandler<String>()

        val stream = ScreenWithMutationStreamImpl(
            readStream = readFlow,
            submitHandler = submitHandler,
            submitBlock = { it },
            pendingCountFlow = pendingFlow,
            syncingFlow = syncingFlow,
            scope = backgroundScope,
            onRefresh = {},
        )

        stream.state.test {
            // Drain initial value + any combine-stabilisation emissions until we see
            // pendingCount=0 from the upstream pipeline (not just the stateIn seed).
            var current = awaitItem()
            while (current.read is ScreenState.Loading) {
                current = awaitItem()
            }
            assertEquals(0, current.outboxPending)
            assertTrue(!current.isSyncing)

            // Flip pending count to 2 and assert it threads through.
            pendingFlow.value = 2
            val withPending = awaitItem()
            assertEquals(2, withPending.outboxPending)

            // Flip syncing on and assert.
            syncingFlow.value = true
            val withSyncing = awaitItem()
            assertTrue(withSyncing.isSyncing)
            assertEquals(2, withSyncing.outboxPending)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
