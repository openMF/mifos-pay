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
import kpt.core.common.DateAsStringSerializer
/**
 * Account transfer attached to a savings transaction.
 *
 * @property id Unique identifier.
 * @property reversed Whether the transfer has been reversed.
 * @property currency Currency of the transfer.
 * @property transferAmount Amount transferred.
 * @property transferDate Date of the transfer.
 * @property transferDescription Description of the transfer.
 */
@Serializable
data class Transfer(
    val id: Long? = null,
    val reversed: Boolean? = null,
    val currency: Currency? = null,
    val transferAmount: Double? = null,
    @Serializable(with = DateAsStringSerializer::class)
    val transferDate: String? = null,
    val transferDescription: String? = null,
)
