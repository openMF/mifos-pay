/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.biller

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a user-defined biller — the Room mirror of the
 * `kpt.core.model.autopay.Biller` domain model.
 *
 * Stored in the `wallet_autopay_billers` table. Each row is a locally-authored
 * biller definition; there is NO server counterpart today (GOAL D12 —
 * OFFLINE_LOCAL_ONLY archetype, the same shape as `wallet_autopay_bills`).
 * Writes are DAO-direct (via `notifyingWrite("wallet_autopay_billers")` inside
 * `BillerOfflineRepositoryImpl`), and the Store5 read side is
 * `createOfflineStore` (no fetcher).
 *
 * Migrated FROM `core/datastore/BillerDataSource` (multiplatform-settings-backed).
 * The old settings-based store held billers as a serialized JSON blob under the
 * `"billers"` key. Existing installs on that path have no rows in
 * `wallet_autopay_billers` on first launch after this migration lands; a
 * follow-up data-copy hook is scheduled in the Phase-5 follow-up (mirrors the
 * matching `wallet_autopay_bills` migration note).
 *
 * ## Column mapping
 *
 * The domain `Biller.id` is nullable-String in the domain, but the local store
 * always generates one on save; we PK on [id] as `NOT NULL TEXT`. Nullable-domain
 * billers without an id are impossible to persist (the repository generates a
 * stable id at write time).
 *
 * The [BillerCategory][kpt.core.model.autopay.BillerCategory] enum
 * persists as its `name` (TEXT) — mirrors [`BillEntity.status`][kpt.core.database.wallet.bill.BillEntity]'s
 * enum-as-string convention so the schema stays self-documenting without a
 * type-converter class.
 *
 * @property id Stable biller id — PK.
 * @property name Biller display name.
 * @property accountNumber Biller account number.
 * @property contactNumber Biller contact number.
 * @property email Optional biller email.
 * @property category Enum `.name` — persisted as TEXT.
 * @property address Optional biller address.
 * @property isActive Domain `isActive` flag.
 * @property createdAt / [updatedAt] Epoch-millis timestamps set by the mapper.
 */
@DbEntity
@Entity(
    tableName = "wallet_autopay_billers",
    indices = [
        Index(value = ["category"]),
        Index(value = ["accountNumber"]),
        Index(value = ["name"]),
    ],
)
data class BillerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val accountNumber: String,
    val contactNumber: String,
    val email: String?,
    val category: String,
    val address: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
