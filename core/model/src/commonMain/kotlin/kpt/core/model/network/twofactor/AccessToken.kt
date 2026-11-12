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
 * Two-factor access token with its validity window.
 *
 * @property token The access token string.
 * @property validFrom Start of the validity period.
 * @property validTo End of the validity period.
 */
@Serializable
data class AccessToken(
    val token: String? = null,
    val validFrom: Long? = null,
    val validTo: Long? = null,
)
