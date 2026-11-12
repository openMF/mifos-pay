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

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Durable row for a form payload that failed to reach the server.
 *
 * Written by [kpt.core.data.store.RoomSubmitOutbox] when a network error occurs during
 * form submission. The row survives process death so the user can resume or retry later.
 *
 * @param id          Auto-generated surrogate key.
 * @param formKey     Consumer-defined identifier that groups drafts by screen/form type
 *                    (e.g. `"loan_application"`, `"client_registration"`). One pending draft
 *                    per (formKey, uniqueKey) pair is the intended invariant — consumers are
 *                    responsible for enforcing uniqueness via [DraftDao.deleteByFormKey] or
 *                    [DraftDao.deleteByUniqueKey] before inserting.
 * @param uniqueKey   Optional sub-identifier within a form type. `null` means
 *                    "the canonical/singleton draft for this form" (e.g. settings save,
 *                    Personal Alert form). Non-null distinguishes one of many drafts under
 *                    the same form type (e.g. Portfolio holding row, alert-per-coin) —
 *                    enables N concurrent independent drafts under one `formKey`.
 * @param payloadJson Serialized form payload (JSON). The concrete type is opaque to the
 *                    framework; consumers encode/decode via kotlinx.serialization.
 * @param status      Lifecycle state — see [DraftStatus].
 * @param createdAtMs Epoch millis when the draft was first saved.
 * @param updatedAtMs Epoch millis of the most recent status transition.
 * @param errorMessage Last failure reason for display in the resume UI (nullable).
 * @param attemptCount Submit attempts made so far (0 = never attempted). Drives
 *   [kpt.core.base.store.submit.RetryPolicy] backoff + the maxAttempts cap in
 *   `OfflineSubmitSyncer`; persisted so the cap survives process death — otherwise a
 *   crash-looping payload would retry forever across restarts.
 */
@Entity(tableName = "framework_submit_drafts")
data class DraftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val formKey: String,
    val uniqueKey: String? = null,
    val payloadJson: String,
    val status: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val errorMessage: String? = null,
    @ColumnInfo(defaultValue = "0")
    val attemptCount: Int = 0,
)
