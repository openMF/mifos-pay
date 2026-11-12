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

import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillStatus
import kpt.core.model.autopay.RecurrencePattern
/**
 * Entity ↔ domain mapper for the `wallet_autopay_bills` table.
 *
 * Enum names round-trip via `valueOf` (`RecurrencePattern`, `BillStatus`) — matches
 * the [`WalletTypeConverters`][kpt.core.database.wallet.converter.WalletTypeConverters]
 * convention (the enums are declared as `String` on the entity so the schema is
 * self-documenting; the converter class is registered on `AppDatabase` for future
 * enum-typed columns).
 *
 * Unknown enum names (from an older install with a since-renamed value) fall
 * back to `RecurrencePattern.NONE` / `BillStatus.ACTIVE` so a bad row never
 * crashes the observer.
 */

/** Convert a persisted row back into the domain [Bill]. */
fun BillEntity.toDomain(): Bill = Bill(
    id = id,
    name = name,
    amount = amount,
    currency = currency,
    dueDate = dueDate,
    recurrencePattern = runCatching { RecurrencePattern.valueOf(recurrencePattern) }
        .getOrDefault(RecurrencePattern.NONE),
    billerId = billerId,
    billerName = billerName,
    description = description,
    isActive = isActive,
    status = runCatching { BillStatus.valueOf(status) }
        .getOrDefault(BillStatus.ACTIVE),
    autoPayEnabled = autoPayEnabled,
    autoPayPaymentMethod = autoPayPaymentMethod,
    autoPaySourceAccount = autoPaySourceAccount,
    autoPayMaxAmount = autoPayMaxAmount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/**
 * Convert a domain [Bill] into a persistable row.
 *
 * @param generatedIdFallback stable id to use when [Bill.id] is null — passed
 *   by the repository (which owns id-generation policy). Callers with a
 *   non-null [Bill.id] never touch the fallback.
 */
fun Bill.toEntity(generatedIdFallback: String): BillEntity = BillEntity(
    id = id ?: generatedIdFallback,
    name = name,
    amount = amount,
    currency = currency,
    dueDate = dueDate,
    recurrencePattern = recurrencePattern.name,
    billerId = billerId,
    billerName = billerName,
    description = description,
    isActive = isActive,
    status = status.name,
    autoPayEnabled = autoPayEnabled,
    autoPayPaymentMethod = autoPayPaymentMethod,
    autoPaySourceAccount = autoPaySourceAccount,
    autoPayMaxAmount = autoPayMaxAmount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
