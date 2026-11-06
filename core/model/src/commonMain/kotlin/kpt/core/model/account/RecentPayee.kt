/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.account

import kotlinx.serialization.Serializable

/**
 * Represents a recently paid beneficiary/payee.
 * Derived from transaction history and transfer details.
 */
@Serializable
data class RecentPayee(
    val clientId: Long,
    val clientName: String,
    val accountId: Long,
    val accountNo: String,
    val officeId: Long,
    val officeName: String,
    val lastTransferDate: String,
    val lastAmount: Double,
    val currency: String,
) {
    /**
     * Returns initials for avatar display (e.g., "John Doe" -> "JD")
     */
    val initials: String
        get() = clientName
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifEmpty { clientName.take(2).uppercase() }
}
