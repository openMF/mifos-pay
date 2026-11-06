/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.client

import kotlinx.serialization.Serializable
import kpt.core.model.network.entity.loanAccount.LoanAccountResponseDto
import kpt.core.model.network.entity.shareAccount.ShareAccountResponseDto
import kpt.core.model.savingsaccount.SavingAccountEntity
@Serializable
data class ClientAccountsEntity(
    var savingsAccounts: List<SavingAccountEntity> = emptyList(),
    val loanAccounts: List<LoanAccountResponseDto> = emptyList(),
    val shareAccounts: List<ShareAccountResponseDto> = emptyList(),
    val groupLoanIndividualMonitoringAccounts: List<String?> = emptyList(),
    val guarantorAccounts: List<GuarantorAccountEntity> = emptyList(),
) {

    fun withSavingsAccounts(savingsAccounts: List<SavingAccountEntity>): ClientAccountsEntity {
        this.savingsAccounts = savingsAccounts
        return this
    }

    val recurringSavingsAccounts: List<SavingAccountEntity?>
        get() = getSavingsAccounts(true)
    val nonRecurringSavingsAccounts: List<SavingAccountEntity?>
        get() = getSavingsAccounts(false)

    private fun getSavingsAccounts(wantRecurring: Boolean): List<SavingAccountEntity?> {
        val result: MutableList<SavingAccountEntity?> = ArrayList()
        for (account in savingsAccounts) {
            if (account.depositType?.isRecurring == wantRecurring) {
                result.add(account)
            }
        }
        return result
    }
}
