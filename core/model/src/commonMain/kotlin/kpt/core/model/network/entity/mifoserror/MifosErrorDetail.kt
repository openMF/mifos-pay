/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.network.entity.mifoserror

import kotlinx.serialization.Serializable

/**
 * Represents a specific error detail in the Mifos/Fineract error response.
 * This is typically used for validation errors where multiple fields may have issues.
 *
 * @property developerMessage Technical message for developers.
 * @property defaultUserMessage Default message suitable for display.
 * @property userMessageGlobalisationCode Localization key of the user message.
 * @property parameterName Name of the offending request parameter.
 */
@Serializable
data class MifosErrorDetail(
    val developerMessage: String? = null,
    val defaultUserMessage: String? = null,
    val userMessageGlobalisationCode: String? = null,
    val parameterName: String? = null,
)
