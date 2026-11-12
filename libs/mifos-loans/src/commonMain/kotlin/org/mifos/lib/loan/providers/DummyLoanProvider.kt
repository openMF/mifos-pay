/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.providers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kpt.core.common.ScreenState
import org.mifos.lib.loan.core.LoanProvider
import org.mifos.lib.loan.core.model.AmortizationType
import org.mifos.lib.loan.core.model.Currency
import org.mifos.lib.loan.core.model.DaysInYearType
import org.mifos.lib.loan.core.model.InterestCalculationPeriodType
import org.mifos.lib.loan.core.model.InterestType
import org.mifos.lib.loan.core.model.LoanPurposeOptions
import org.mifos.lib.loan.core.model.LoanState
import org.mifos.lib.loan.core.model.LoanTemplate
import org.mifos.lib.loan.core.model.LoansPayload
import org.mifos.lib.loan.core.model.Product
import org.mifos.lib.loan.core.model.ProductOptions
/**
 * Dummy, no-network implementation of [LoanProvider].
 *
 * The product owner's requirement is to stand up a new "Select Loan Provider" step ahead of the
 * existing (Fineract-backed) wizard, without wiring any real per-provider backend yet ("this does
 * not have any apis for now let it be dummy for now"). Rather than let the rest of the wizard
 * (Select Loan Type, Loan Product Details, Loan Apply, Confirm Details) fail against a real
 * server, this class fabricates the same [LoanTemplate] shape the real
 * [org.mifos.lib.loan.providers.FineractLoanProvider] would have returned, so the remaining
 * screens keep working end-to-end with static data.
 *
 * A single shared instance of this class is currently bound in
 * [org.mifos.lib.loan.providers.LoanProviderRegistry] under all four of `"hdfc"`, `"sbi"`,
 * `"icici"`, `"axis"` — there is no real per-bank backend distinction yet. Once one exists, swap
 * the corresponding registry entry for a dedicated adapter without touching the others.
 */
class DummyLoanProvider : LoanProvider {

    override fun getLoanTemplate(clientId: Long): Flow<ScreenState<LoanTemplate>> = flow {
        delay(DUMMY_DELAY_MS)
        emit(ScreenState.Content(mockLoanTemplate))
    }

    override fun getLoanTemplateByProduct(
        clientId: Long,
        productId: Long,
    ): Flow<ScreenState<LoanTemplate>> = flow {
        delay(DUMMY_DELAY_MS)
        emit(ScreenState.Content(mockLoanTemplate))
    }

    override suspend fun submitLoanApplication(
        loanState: LoanState,
        payload: LoansPayload,
        loanId: Long,
    ) {
        delay(DUMMY_SUBMIT_DELAY_MS)
    }

    private companion object {
        const val DUMMY_DELAY_MS = 300L
        const val DUMMY_SUBMIT_DELAY_MS = 500L
    }
}

/**
 * A shared fabricated currency used by [mockLoanTemplate] and [mockProduct].
 */
private val mockCurrency = Currency(
    code = "USD",
    name = "US Dollar",
    decimalPlaces = 2.0,
    inMultiplesOf = 1,
    displaySymbol = "$",
    nameCode = "currency.USD",
    displayLabel = "US Dollar ($)",
)

/**
 * A shared fabricated product used by [mockLoanTemplate], populated with the fields read by
 * [org.mifos.lib.loan.ui.loanApply.LoanApplyViewModel] and
 * [org.mifos.lib.loan.ui.loanProductDetails.LoanProductDetailsViewModel] (principal bounds, interest
 * rate bounds, currency, name).
 */
private val mockProduct = Product(
    id = 1,
    name = "Personal Loan",
    shortName = "PL",
    currency = mockCurrency,
    principal = 10_000.0,
    minPrincipal = 1_000.0,
    maxPrincipal = 50_000.0,
    interestRatePerPeriod = 12.0,
    minInterestRatePerPeriod = 8.0,
    maxInterestRatePerPeriod = 24.0,
    annualInterestRate = 12.0,
    amortizationType = AmortizationType(id = 1, code = "equal.installments", value = "Equal installments"),
    interestType = InterestType(id = 0, code = "declining.balance", value = "Declining Balance"),
    interestCalculationPeriodType = InterestCalculationPeriodType(
        id = 1,
        code = "daily",
        value = "Daily",
    ),
    daysInYearType = DaysInYearType(id = 1, code = "actual", value = "Actual"),
)

/**
 * The single fabricated [LoanTemplate] returned by both [DummyLoanProvider.getLoanTemplate]
 * and [DummyLoanProvider.getLoanTemplateByProduct], populated with the fields actually read
 * by the wizard's three consuming ViewModels:
 *  - [org.mifos.lib.loan.ui.selectLoanType.SelectLoanTypeViewModel] reads [productOptions].
 *  - [org.mifos.lib.loan.ui.loanProductDetails.LoanProductDetailsViewModel] reads [product],
 *    [currency], and [loanProductName].
 *  - [org.mifos.lib.loan.ui.loanApply.LoanApplyViewModel] reads [product], [currency],
 *    [loanPurposeOptions], and [loanProductName].
 *
 * Everything else is left at its default (`null`/`emptyList()`).
 */
private val mockLoanTemplate = LoanTemplate(
    loanProductName = "Personal Loan",
    currency = mockCurrency,
    principal = 10_000.0,
    product = mockProduct,
    annualInterestRate = 12.0,
    productOptions = listOf(
        ProductOptions(id = 1, name = "Personal Loan"),
        ProductOptions(id = 2, name = "Business Loan"),
        ProductOptions(id = 3, name = "Home Loan"),
        ProductOptions(id = 4, name = "Education Loan"),
    ),
    loanPurposeOptions = listOf(
        LoanPurposeOptions(id = 1, name = "Agriculture"),
        LoanPurposeOptions(id = 2, name = "Business"),
        LoanPurposeOptions(id = 3, name = "Education"),
        LoanPurposeOptions(id = 4, name = "Housing"),
    ),
)
