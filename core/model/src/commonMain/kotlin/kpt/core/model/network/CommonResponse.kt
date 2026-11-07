/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network

import kotlinx.serialization.Serializable

/**
 * Minimal Fineract command response carrying only the id of the affected resource.
 *
 * @property resourceId Id of the created or affected resource.
 */
@Serializable
data class CommonResponse(
    val resourceId: Int,
)
