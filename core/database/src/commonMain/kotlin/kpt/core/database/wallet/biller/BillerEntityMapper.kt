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

import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
/**
 * Entity ↔ domain mapper for the `wallet_autopay_billers` table.
 *
 * The [BillerCategory] enum round-trips via `valueOf` (persisted as its `.name`).
 * Unknown category names (from an older install with a since-renamed value) fall
 * back to [BillerCategory.OTHER] so a bad row never crashes the observer —
 * mirrors the defensive fallback in [`BillEntity.toDomain`][kpt.core.database.wallet.bill.toDomain].
 */

/** Convert a persisted row back into the domain [Biller]. */
fun BillerEntity.toDomain(): Biller = Biller(
    id = id,
    name = name,
    accountNumber = accountNumber,
    contactNumber = contactNumber,
    email = email,
    category = runCatching { BillerCategory.valueOf(category) }
        .getOrDefault(BillerCategory.OTHER),
    address = address,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/**
 * Convert a domain [Biller] into a persistable row.
 *
 * @param generatedIdFallback stable id to use when [Biller.id] is null — passed
 *   by the repository (which owns id-generation policy). Callers with a
 *   non-null [Biller.id] never touch the fallback.
 */
fun Biller.toEntity(generatedIdFallback: String): BillerEntity = BillerEntity(
    id = id ?: generatedIdFallback,
    name = name,
    accountNumber = accountNumber,
    contactNumber = contactNumber,
    email = email,
    category = category.name,
    address = address,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
