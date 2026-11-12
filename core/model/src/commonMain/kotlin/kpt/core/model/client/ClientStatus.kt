/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable

/**
 * A coded status value of a client, such as status or legal form.
 *
 * @property id Id of the code value.
 * @property code Machine-readable code.
 * @property value Human-readable label.
 */
@Serializable
data class ClientStatus(
    val id: Int = 0,
    val code: String = "",
    val value: String = "",
)
