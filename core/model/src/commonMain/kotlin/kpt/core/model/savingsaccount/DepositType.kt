/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savingsaccount

import kotlinx.serialization.Serializable

/**
 * Deposit account type (savings, fixed or recurring) as reported by the server.
 *
 * @property id Deposit type id used by the server.
 * @property code Deposit type code used by the server.
 * @property value Human-readable deposit type label.
 */
@Serializable
data class DepositType(
    val id: Int,
    val code: String,
    val value: String,
) {
    /** Whether this is a recurring deposit. */
    val isRecurring: Boolean
        get() = ServerTypes.RECURRING.id == id

    /** API endpoint used for accounts of this type. */
    val endpoint: String
        get() = ServerTypes.fromId(
            id,
        ).endpoint

    /** The matching [ServerTypes] entry. */
    val serverType: ServerTypes
        get() = ServerTypes.fromId(
            id,
        )

    /**
     * Known Fineract deposit account types with their codes and API endpoints.
     *
     * @property id Deposit type id used by the server.
     * @property code Deposit type code used by the server.
     * @property endpoint API endpoint used for accounts of this type.
     */
    enum class ServerTypes(val id: Int, val code: String, val endpoint: String) {
        /** Regular savings deposit account. */
        SAVINGS(id = 100, code = "depositAccountType.savingsDeposit", endpoint = "savingsaccounts"),

        /** Fixed deposit account. */
        FIXED(id = 200, code = "depositAccountType.fixedDeposit", endpoint = "savingsaccounts"),

        /** Recurring deposit account. */
        RECURRING(
            id = 300,
            code = "depositAccountType.recurringDeposit",
            endpoint = "recurringdepositaccounts",
        ),
        ;

        /** Lookup helpers for [ServerTypes]. */
        companion object {
            /** Returns the [ServerTypes] entry whose id matches [id], or [SAVINGS] if none does. */
            fun fromId(id: Int): ServerTypes {
                for (type in entries) {
                    if (type.id == id) {
                        return type
                    }
                }
                return ServerTypes.SAVINGS
            }
        }
    }
}
