/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.mapper

import org.mifos.lib.loan.core.model.ChargeOptions
import org.mifos.lib.loan.core.model.TaxGroup
import org.mifos.lib.loan.network.model.ChargeOptionsResponseDto
import org.mifos.lib.loan.network.model.TaxGroupResponseDto

/** Maps this charge options DTO to the [ChargeOptions] domain model. */
fun ChargeOptionsResponseDto.toModel(): ChargeOptions =
    ChargeOptions(
        id = id,
        name = name,
        active = active,
        penalty = penalty,
        currency = currency?.toModel(),
        amount = amount,
        chargeTimeType = chargeTimeType?.toChargeTimeType(),
        chargeAppliesTo = chargeAppliesTo?.toChargeAppliesTo(),
        chargeCalculationType = chargeCalculationType?.toChargeCalculationType(),
        chargePaymentMode = chargePaymentMode?.toChargePaymentMode(),
        taxGroup = taxGroup?.toModel(),
    )

/** Maps this tax group DTO to the [TaxGroup] domain model. */
fun TaxGroupResponseDto.toModel(): TaxGroup =
    TaxGroup(
        id = id,
        name = name,
    )
