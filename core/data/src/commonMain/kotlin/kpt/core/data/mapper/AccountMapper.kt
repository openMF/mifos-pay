/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.mapper

import kpt.core.model.account.Account
import kpt.core.model.network.entity.client.ClientAccountsEntity
import kpt.core.model.savingsaccount.AccountType
import kpt.core.model.savingsaccount.SavingAccountEntity
import kpt.core.model.network.entity.templates.account.AccountType as NetworkAccountType
fun ClientAccountsEntity.toAccount(): List<Account> {
    return this.savingsAccounts.toAccount()
}

fun List<SavingAccountEntity>.toAccount(): List<Account> {
    return map {
        Account(
            name = it.productName,
            number = it.accountNo,
            id = it.id,
            externalId = it.externalId,
            balance = it.accountBalance,
            currency = it.currency,
            productId = it.productId,
            productName = it.productName,
            status = it.status,
            accountType = it.accountType,
        )
    }
}

fun NetworkAccountType.toModelAccountType(): AccountType {
    return AccountType(
        id = this.id?.toLong() ?: 0L,
        code = this.code ?: "",
        value = this.value ?: "",
    )
}
