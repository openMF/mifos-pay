/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation.conflict

import kotlinx.coroutines.flow.Flow

/**
 * Durable inbox of write conflicts surfaced to the user in Settings.
 *
 * When an optimistic mutation syncs and the server record diverges from the recorded local payload,
 * the gateway records a [ConflictEntry] here (server-wins is applied to the store immediately, but the
 * user's version is preserved). The Settings conflict screen lists pending entries; the user opens one,
 * navigates to the originating form pre-filled from [ConflictEntry.localPayloadJson], and resolves it
 * by retrying their version or accepting the server's.
 */
interface ConflictInbox {

    /** Record a conflict; returns the new conflict id. */
    suspend fun record(
        entity: String,
        key: String,
        localPayloadJson: String,
        serverPayloadJson: String,
        formRoute: String?,
    ): String

    /** Observe the pending (unresolved) conflicts, newest first — drives the Settings badge + list. */
    fun observePending(): Flow<List<ConflictEntry>>

    /** Resolve a recorded conflict; the entry is cleared once resolution is applied by the caller. */
    suspend fun resolve(conflictId: String, resolution: ConflictResolution)
}

/** How the user chose to settle a [ConflictEntry]. */
enum class ConflictResolution {
    /** Discard the local payload; keep the server record already ingested (server-wins default). */
    ACCEPT_SERVER,

    /** Re-submit the user's recorded local payload (the form re-opens pre-filled). */
    RETRY_LOCAL,
}

/**
 * A conflict the caller detected between its local payload and the server result — returned by a
 * command's `conflictOf` so the gateway can record it. The caller owns serialization (it has the
 * serializers), keeping the gateway free of any JSON dependency.
 */
data class ConflictReport(
    /** Domain entity the conflict is on (`loans`, `bills`) — scopes a conflict to its feature. */
    val entity: String,
    /** Identity of the specific record that conflicted. */
    val key: String,
    /** The value the device tried to write, serialised. Kept verbatim so the user's edit is never lost. */
    val localPayloadJson: String,
    /** The value the server already held, serialised — the other side of the conflict. */
    val serverPayloadJson: String,
    /** Route to the form that can resolve this, so the UI can navigate straight there. Null when none. */
    val formRoute: String?,
)

/** A single recorded write conflict awaiting user resolution. */
data class ConflictEntry(
    /** Inbox row id. */
    val id: String,
    /** Domain entity the conflict is on. */
    val entity: String,
    /** Identity of the record that conflicted. */
    val key: String,
    /** The value the device tried to write, serialised. */
    val localPayloadJson: String,
    /** The value the server already held, serialised. */
    val serverPayloadJson: String,
    /**
     * Route that reopens the form where this conflict can be resolved, or null when the conflict has no editable
     * surface. Null means the inbox can only offer keep-mine / keep-theirs.
     */
    val formRoute: String?,
    /** When the conflict was recorded, epoch millis — drives ordering and staleness in the inbox. */
    val recordedAtMs: Long,
)
