/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network response DTO for the loan product options, as returned by the Fineract loans API.
 *
 * @property id Unique identifier.
 * @property name Name.
 * @property includeInBorrowerCycle Whether the loan counts toward the borrower cycle.
 * @property useBorrowerCycle Whether borrower-cycle based limits are used.
 * @property linkedToFloatingInterestRates Whether the product is linked to floating interest rates.
 * @property floatingInterestRateCalculationAllowed Whether floating interest rate calculation is allowed.
 * @property allowvaliableInstallments Whether variable installments are allowed.
 * @property interestRecalculationEnabled Whether interest recalculation is enabled.
 * @property canDefineInstallmentAmount Whether the installment amount can be defined.
 * @property holdGuaranteeFunds Whether guarantee funds are held for the loan.
 * @property accountMovesOutOfNPAOnlyOnArrearsCompletion Whether NPA status ends only after arrears clear.
 */
@Serializable
data class LoanProductOptionsResponseDto(

    val id: Int? = null,

    val name: String? = null,

    val includeInBorrowerCycle: Boolean? = null,

    val useBorrowerCycle: Boolean? = null,

    @SerialName("isLinkedToFloatingInterestRates")
    val linkedToFloatingInterestRates: Boolean? = null,

    @SerialName("isFloatingInterestRateCalculationAllowed")
    val floatingInterestRateCalculationAllowed: Boolean? = null,

    val allowvaliableInstallments: Boolean? = null,

    @SerialName("isInterestRecalculationEnabled")
    val interestRecalculationEnabled: Boolean? = null,

    val canDefineInstallmentAmount: Boolean? = null,

    val holdGuaranteeFunds: Boolean? = null,

    val accountMovesOutOfNPAOnlyOnArrearsCompletion: Boolean? = null,

)
