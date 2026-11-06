/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.mapper.pocket

import kpt.core.model.enums.AccountType
import kpt.core.model.network.entity.pocket.PocketAccountDto
import kpt.core.model.network.entity.pocket.PocketResponseDto
import kpt.core.model.pocket.PocketAccount
fun PocketResponseDto.toDomainList(): List<PocketAccount> {
    val all = mutableListOf<PocketAccount>()

    loanAccounts.forEach { all.add(it.toDomain(AccountType.LOAN)) }
    savingsAccounts.forEach { all.add(it.toDomain(AccountType.SAVINGS)) }
    shareAccounts.forEach { all.add(it.toDomain(AccountType.SHARE)) }

    return all
}

private fun PocketAccountDto.toDomain(type: AccountType) = PocketAccount(
    id = this.id ?: 0L,
    pocketId = this.pocketId ?: 0L,
    accountId = this.accountId ?: 0L,
    accountType = type,
    accountNumber = this.accountNumber ?: "",
)

fun kpt.core.model.network.entity.loanAccount.LoanStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
    when {
        active == true -> kpt.core.model.pocket.AccountStatus.ACTIVE
        pendingApproval == true -> kpt.core.model.pocket.AccountStatus.PENDING
        waitingForDisbursal == true -> kpt.core.model.pocket.AccountStatus.APPROVED
        overpaid == true -> kpt.core.model.pocket.AccountStatus.OVERPAID
        closed == true ||
            closedObligationsMet == true ||
            closedWrittenOff == true ||
            closedRescheduled == true -> kpt.core.model.pocket.AccountStatus.CLOSED
        else -> kpt.core.model.pocket.AccountStatus.UNKNOWN
    }

fun kpt.core.model.savingsaccount.Status.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
    when {
        active == true -> kpt.core.model.pocket.AccountStatus.ACTIVE
        submittedAndPendingApproval == true -> kpt.core.model.pocket.AccountStatus.PENDING
        approved == true -> kpt.core.model.pocket.AccountStatus.APPROVED
        rejected == true -> kpt.core.model.pocket.AccountStatus.REJECTED
        withdrawnByApplicant == true -> kpt.core.model.pocket.AccountStatus.WITHDRAWN
        matured == true -> kpt.core.model.pocket.AccountStatus.MATURED
        closed == true || prematureClosed == true -> kpt.core.model.pocket.AccountStatus.CLOSED
        else -> kpt.core.model.pocket.AccountStatus.UNKNOWN
    }

fun kpt.core.model.network.entity.shareAccount.ShareStatusResponseDto.toAccountStatus(): kpt.core.model.pocket.AccountStatus =
    when {
        active == true -> kpt.core.model.pocket.AccountStatus.ACTIVE
        submittedAndPendingApproval == true -> kpt.core.model.pocket.AccountStatus.PENDING
        approved == true -> kpt.core.model.pocket.AccountStatus.APPROVED
        rejected == true -> kpt.core.model.pocket.AccountStatus.REJECTED
        closed == true -> kpt.core.model.pocket.AccountStatus.CLOSED
        else -> kpt.core.model.pocket.AccountStatus.UNKNOWN
    }
