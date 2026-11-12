/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.bill

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Persistent row for a user-defined bill — the Room mirror of the
 * `kpt.core.model.autopay.Bill` domain model.
 *
 * Stored in the `wallet_autopay_bills` table. Each row is a locally-authored
 * bill definition; there is NO server counterpart (GOAL D12 — OFFLINE_LOCAL_ONLY
 * archetype). Writes are DAO-direct (via `notifyingWrite("wallet_autopay_bills")`
 * inside `BillOfflineRepositoryImpl`), and the Store5 read side is
 * `createOfflineStore` (no fetcher).
 *
 * Migrated FROM `core/datastore/BillDataSource` (multiplatform-settings-backed).
 * The old settings-based store held bills as a serialized JSON blob under key
 * `"bills"`. Existing installs on that path have no rows in `wallet_autopay_bills`
 * on first launch after this migration lands; a follow-up data-copy hook is
 * scheduled in the pilot follow-up (Phase 5 tickets).
 *
 * ## Column mapping
 *
 * The domain `Bill.id` is nullable-String in the domain, but the local store
 * always generates one on save; we PK on `id` as `NOT NULL TEXT`. Nullable-domain
 * bills without an id are impossible to persist (the mapper generates a stable
 * id at write time).
 *
 * Enums ([`RecurrencePattern`][kpt.core.model.autopay.RecurrencePattern],
 * [`BillStatus`][kpt.core.model.autopay.BillStatus]) persist as their
 * `name` (TEXT) via the [`WalletTypeConverters`][kpt.core.database.wallet.converter.WalletTypeConverters]
 * `@ColumnTypeConverter` set.
 *
 * @property id Stable bill id — PK.
 * @property name Bill display name.
 * @property amount Bill amount in the given currency.
 * @property currency ISO-4217 currency code (defaults to `"USD"` at the domain layer).
 * @property dueDate Next-due epoch millis.
 * @property recurrencePattern Enum name — persisted via converter.
 * @property billerId Optional biller id.
 * @property billerName Optional biller name.
 * @property description Optional free-text note.
 * @property isActive Legacy active flag (retained for domain parity).
 * @property status Enum name — persisted via converter.
 * @property autoPayEnabled AutoPay wiring flag.
 * @property autoPayPaymentMethod / [autoPaySourceAccount] / [autoPayMaxAmount]
 *   AutoPay configuration fields.
 * @property createdAt / [updatedAt] Epoch-millis timestamps set by the mapper.
 * @property autoPaySourceAccount Account debited by AutoPay, if configured.
 * @property autoPayMaxAmount Maximum amount AutoPay may charge per payment, if configured.
 * @property updatedAt Epoch-millis timestamp of the last update.
 */
@DbEntity
@Entity(
    tableName = "wallet_autopay_bills",
    indices = [
        Index(value = ["status"]),
        Index(value = ["dueDate"]),
        Index(value = ["billerId"]),
    ],
)
data class BillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val currency: String,
    val dueDate: Long,
    val recurrencePattern: String,
    val billerId: String?,
    val billerName: String?,
    val description: String?,
    val isActive: Boolean,
    val status: String,
    val autoPayEnabled: Boolean,
    val autoPayPaymentMethod: String?,
    val autoPaySourceAccount: String?,
    val autoPayMaxAmount: Double?,
    val createdAt: Long,
    val updatedAt: Long,
)
