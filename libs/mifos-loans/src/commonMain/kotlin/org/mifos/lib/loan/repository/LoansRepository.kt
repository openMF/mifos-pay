/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.repository

import kotlinx.coroutines.flow.Flow
import kpt.core.common.ScreenState
import org.mifos.lib.loan.core.model.LoanState
import org.mifos.lib.loan.core.model.LoanTemplate
import org.mifos.lib.loan.core.model.LoansPayload
/**
 * Facade injected by the wizard's data-consuming ViewModels ([org.mifos.lib.loan.ui.selectLoanType.SelectLoanTypeViewModel],
 * [org.mifos.lib.loan.ui.loanProductDetails.LoanProductDetailsViewModel],
 * [org.mifos.lib.loan.ui.loanApply.LoanApplyViewModel], [org.mifos.lib.loan.ui.confirmDetails.ConfirmDetailsViewModel])
 * instead of [org.mifos.lib.loan.core.LoanProvider] directly.
 *
 * Each call takes the `providerId` chosen on the Select Loan Provider screen and resolves the
 * concrete [org.mifos.lib.loan.core.LoanProvider] adapter to delegate to via
 * [org.mifos.lib.loan.providers.LoanProviderRegistry], so the same wizard behaves differently per
 * provider once distinct adapters exist for each.
 */
interface LoansRepository {
    /** Loads the loan template for [clientId] via the adapter for [providerId]. */
    fun getLoanTemplate(providerId: String, clientId: Long): Flow<ScreenState<LoanTemplate>>

    /** Loads the loan template for [clientId] and [productId] via the adapter for [providerId]. */
    fun getLoanTemplateByProduct(
        providerId: String,
        clientId: Long,
        productId: Long,
    ): Flow<ScreenState<LoanTemplate>>

    /**
     * Submits a loan application, throwing on failure. Returns [Unit] because the caller derives
     * its own user-facing success message from a [org.jetbrains.compose.resources.StringResource].
     */
    suspend fun submitLoanApplication(
        providerId: String,
        loanState: LoanState,
        payload: LoansPayload,
        loanId: Long,
    )
}
