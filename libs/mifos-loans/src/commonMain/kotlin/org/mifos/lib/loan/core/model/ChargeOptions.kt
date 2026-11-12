/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model of the charge options used in the loan application flow.
 *
 * @property id Unique identifier.
 * @property name Name.
 * @property active Whether this entry is active.
 * @property penalty Whether the charge is a penalty.
 * @property currency Currency of the amount.
 * @property amount Amount.
 * @property chargeTimeType Charge time type.
 * @property chargeAppliesTo Charge applies to.
 * @property chargeCalculationType Charge calculation type.
 * @property chargePaymentMode Charge payment mode.
 * @property taxGroup Tax group.
 */
@Serializable
data class ChargeOptions(

    val id: Int? = null,

    val name: String? = null,

    val active: Boolean? = null,

    val penalty: Boolean? = null,

    val currency: Currency? = null,

    val amount: Double? = null,

    val chargeTimeType: ChargeTimeType? = null,

    val chargeAppliesTo: ChargeAppliesTo? = null,

    val chargeCalculationType: ChargeCalculationType? = null,

    val chargePaymentMode: ChargePaymentMode? = null,

    val taxGroup: TaxGroup? = null,

)
