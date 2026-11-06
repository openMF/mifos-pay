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

@Serializable
data class DepositType(
    val id: Int,
    val code: String,
    val value: String,
) {
    val isRecurring: Boolean
        get() = ServerTypes.RECURRING.id == id
    val endpoint: String
        get() = ServerTypes.fromId(
            id,
        ).endpoint
    val serverType: ServerTypes
        get() = ServerTypes.fromId(
            id,
        )

    enum class ServerTypes(val id: Int, val code: String, val endpoint: String) {
        SAVINGS(id = 100, code = "depositAccountType.savingsDeposit", endpoint = "savingsaccounts"),
        FIXED(id = 200, code = "depositAccountType.fixedDeposit", endpoint = "savingsaccounts"),
        RECURRING(
            id = 300,
            code = "depositAccountType.recurringDeposit",
            endpoint = "recurringdepositaccounts",
        ),
        ;

        companion object {
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
