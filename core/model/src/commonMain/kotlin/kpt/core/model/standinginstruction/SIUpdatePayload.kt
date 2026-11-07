/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.standinginstruction

import kotlinx.serialization.Serializable
import kpt.core.common.Parcelable
import kpt.core.common.Parcelize
/**
 * Request body for updating an existing standing instruction.
 *
 * @property priority Priority code of the instruction.
 * @property status Status code of the instruction.
 * @property locale Locale used to parse numbers and dates.
 * @property validFrom Date from which the instruction is valid.
 * @property validTill Date until which the instruction is valid.
 * @property dateFormat Format used for date values.
 */
@Serializable
@Parcelize
data class SIUpdatePayload(
    val priority: Long,
    val status: Long,
    val locale: String,
    val validFrom: String,
    val validTill: String,
    val dateFormat: String,
) : Parcelable
