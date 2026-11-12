/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.infra.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A recorded write conflict awaiting user resolution — the durable backing for the framework
 * `ConflictInbox` surfaced in Settings. Purely additive framework table (`framework_write_conflicts`).
 *
 * @property resolved 0 = pending (shown in the inbox), 1 = resolved (kept for audit, hidden from the list).
 */
@Entity(tableName = "framework_write_conflicts")
data class ConflictEntity(
    /** Row id, assigned by Room. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Domain entity the conflict is on (`loans`, `bills`). */
    val entity: String,
    /** Identity of the record that conflicted. */
    val entityKey: String,
    /** The value the device tried to write, serialised — kept verbatim so the user's edit survives. */
    val localPayloadJson: String,
    /** The value the server already held, serialised. */
    val serverPayloadJson: String,
    /** Route to the form that can resolve this, or null when there is none. */
    val formRoute: String?,
    /** When the conflict was recorded, epoch millis. */
    val recordedAtMs: Long,
    val resolved: Int = 0,
)
