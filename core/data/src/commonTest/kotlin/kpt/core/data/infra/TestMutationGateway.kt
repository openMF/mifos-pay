/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.infra

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kpt.core.base.store.mutation.DefaultMutationGateway
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.conflict.ConflictResolution

/**
 * Test [MutationGateway] for repository tests — the real [DefaultMutationGateway] wired online with a
 * no-op conflict inbox. It exercises the production Optimistic-local write door
 * (`localMutation` → DAO write) exactly as the app does, so reactive-invalidation reads
 * re-emit after a gateway-routed write. No fake gateway: the repos are tested against the real seam.
 */
fun testMutationGateway(isOnline: Boolean = true): MutationGateway =
    DefaultMutationGateway(isOnline = { isOnline }, conflictInbox = NoopConflictInbox)

private object NoopConflictInbox : ConflictInbox {
    override suspend fun record(
        entity: String,
        key: String,
        localPayloadJson: String,
        serverPayloadJson: String,
        formRoute: String?,
    ): String = "noop"

    override fun observePending(): Flow<List<ConflictEntry>> = flowOf(emptyList())

    override suspend fun resolve(conflictId: String, resolution: ConflictResolution) = Unit
}
