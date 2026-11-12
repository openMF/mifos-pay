/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.conflict.ConflictReport
import kpt.core.base.store.mutation.conflict.ConflictResolution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeConflictInbox : ConflictInbox {
    val recorded = mutableListOf<ConflictReport>()
    override suspend fun record(
        entity: String,
        key: String,
        localPayloadJson: String,
        serverPayloadJson: String,
        formRoute: String?,
    ): String {
        recorded += ConflictReport(entity, key, localPayloadJson, serverPayloadJson, formRoute)
        return "c${recorded.size}"
    }
    override fun observePending(): Flow<List<ConflictEntry>> = flowOf(emptyList())
    override suspend fun resolve(conflictId: String, resolution: ConflictResolution) = Unit
}

/**
 * Tests the [DefaultMutationGateway] command-path decision logic (SP-1/T2) — the arm that carries the
 * policy + conflict + rollback semantics without needing a fake Store5 MutableStore.
 */
class DefaultMutationGatewayTest {

    @Test
    fun onlineRequired_offline_isBlocked_andRunsNoEndpoint() = runTest {
        var endpointRan = false
        val gw = DefaultMutationGateway(isOnline = { false }, conflictInbox = FakeConflictInbox())
        val spec = CommandSpec<Int, String>(payload = 1, endpoint = {
            endpointRan = true
            "ok"
        })
        val r = gw.command(spec, MutationPolicy.OnlineRequired)
        assertIs<MutationResult.Blocked>(r)
        assertEquals(BlockReason.OFFLINE, r.reason)
        assertFalse(endpointRan, "OnlineRequired offline must not touch the network")
    }

    @Test
    fun command_success_ingestsServerRecord() = runTest {
        var ingested: String? = null
        val gw = DefaultMutationGateway(isOnline = { true }, conflictInbox = FakeConflictInbox())
        val spec = CommandSpec<Int, String>(
            payload = 42,
            endpoint = { p -> "server:$p" },
            localApply = { r -> ingested = r },
        )
        val r = gw.command(spec, MutationPolicy.OnlineRequired)
        assertIs<MutationResult.Applied<String>>(r)
        assertEquals("server:42", r.value)
        assertTrue(r.synced)
        assertEquals("server:42", ingested, "the real server record must be ingested")
    }

    @Test
    fun command_conflict_isRecorded() = runTest {
        val inbox = FakeConflictInbox()
        val gw = DefaultMutationGateway(isOnline = { true }, conflictInbox = inbox)
        val spec = CommandSpec<Int, String>(
            payload = 1,
            endpoint = { "server-value" },
            conflictOf = { _, server -> ConflictReport("todo", "1", "\"local\"", "\"$server\"", "todo/edit/1") },
        )
        val r = gw.command(spec, MutationPolicy.Optimistic)
        val conflicted = assertIs<MutationResult.Conflicted<String>>(r)
        assertEquals("server-value", conflicted.server)
        assertEquals(1, inbox.recorded.size)
        assertEquals("todo/edit/1", inbox.recorded.single().formRoute)
    }

    @Test
    fun command_failure_rollsBack() = runTest {
        var rolledBack = false
        val gw = DefaultMutationGateway(isOnline = { true }, conflictInbox = FakeConflictInbox())
        val spec = CommandSpec<Int, String>(
            payload = 1,
            endpoint = { throw IllegalStateException("boom") },
            rollback = { rolledBack = true },
        )
        val r = gw.command(spec, MutationPolicy.Optimistic)
        val failed = assertIs<MutationResult.Failed>(r)
        assertTrue(failed.rolledBack)
        assertTrue(rolledBack)
    }
}
