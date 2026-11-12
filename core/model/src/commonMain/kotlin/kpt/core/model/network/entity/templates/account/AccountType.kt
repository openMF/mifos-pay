/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.templates.account

import kotlinx.serialization.Serializable

/**
 * Type of an account (id, code and display value).
 *
 * @property id Unique id.
 * @property code Machine-readable code.
 * @property value Human-readable display value.
 */
@Serializable
data class AccountType(
    val id: Int? = null,
    val code: String? = null,
    val value: String? = null,
)
