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
 * Represents the standard error response format from Mifos/Fineract server.
 *
 * Example server response:
 * ```json
 * {
 *   "developerMessage": "The resource with id 123 was not found.",
 *   "httpStatusCode": "404",
 *   "defaultUserMessage": "Account not found",
 *   "userMessageGlobalisationCode": "error.msg.account.not.found",
 *   "errors": [
 *     {
 *       "developerMessage": "Account ID is invalid",
 *       "defaultUserMessage": "The account does not exist",
 *       "userMessageGlobalisationCode": "error.msg.account.invalid",
 *       "parameterName": "accountId"
 *     }
 *   ]
 * }
 * ```
 *
 * @property developerMessage Technical message for developers.
 * @property httpStatusCode HTTP status code as a string.
 * @property defaultUserMessage Default message suitable for display.
 * @property userMessageGlobalisationCode Localization key of the user message.
 * @property errors Detailed error entries.
 */
@Serializable
data class FineractError(
    val developerMessage: String? = null,
    val httpStatusCode: String? = null,
    val defaultUserMessage: String? = null,
    val userMessageGlobalisationCode: String? = null,
    val errors: List<FineractErrorDetail> = emptyList(),
) {
    /**
     * Returns the most appropriate user-facing error message.
     * Priority: First error's defaultUserMessage > root defaultUserMessage > developerMessage > fallback
     */
    fun getUserMessage(fallback: String = "An error occurred"): String {
        return errors.firstOrNull()?.defaultUserMessage
            ?: defaultUserMessage
            ?: developerMessage
            ?: fallback
    }

    /**
     * Returns the HTTP status code as an integer, or null if not available.
     */
    fun getStatusCode(): Int? = httpStatusCode?.toIntOrNull()
}
