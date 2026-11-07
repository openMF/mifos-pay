/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.twofactor

import kotlinx.serialization.Serializable

/**
 * A channel (for example SMS or email) over which a two-factor code can be delivered.
 *
 * @property name Name of the delivery method, such as SMS or email.
 * @property target Address the code is sent to, such as a phone number or email.
 */
@Serializable
data class DeliveryMethod(
    val name: String?,
    val target: String?,
)
