/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fork.util

import kotlinx.serialization.json.Json
import kpt.core.common.ErrorBodyParser
import kpt.core.model.network.entity.mifoserror.FineractError
/**
 * JSON instance configured for lenient parsing of error responses.
 * Ignores unknown keys since error responses may vary.
 */
private val errorJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Parses the Mifos/Fineract server error response body to extract a user-friendly message.
 *
 * This parser follows the mifos-mobile pattern for extracting error messages from
 * the standard Mifos/Fineract error response format.
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
 *       "parameterName": "accountId"
 *     }
 *   ]
 * }
 * ```
 *
 * @param responseBody The raw response body string from the server
 * @param statusCode The HTTP status code of the response
 * @return A user-friendly error message extracted from the response
 */
val parseFineractError: ErrorBodyParser = { responseBody, statusCode ->
    extractFineractErrorMessage(responseBody, statusCode)
}

/**
 * Extracts a user-friendly error message from a Mifos/Fineract error response.
 *
 * Priority order:
 * 1. First error's defaultUserMessage (for validation errors)
 * 2. Root level defaultUserMessage
 * 3. Root level developerMessage
 * 4. Generic fallback message based on status code
 *
 * @param responseBody The raw response body string
 * @param statusCode The HTTP status code
 * @return The extracted error message
 */
fun extractFineractErrorMessage(responseBody: String, statusCode: Int): String {
    if (responseBody.isBlank()) {
        return getGenericErrorMessage(statusCode)
    }

    return try {
        val fineractError = errorJson.decodeFromString<FineractError>(responseBody)
        fineractError.getUserMessage(fallback = getGenericErrorMessage(statusCode))
    } catch (e: Exception) {
        // If parsing fails, try to return a meaningful message
        // The response might not be JSON (e.g., HTML error page)
        responseBody.take(200).ifBlank { getGenericErrorMessage(statusCode) }
    }
}

/**
 * Returns a generic error message based on the HTTP status code.
 * Used as a fallback when no specific message can be extracted.
 */
private fun getGenericErrorMessage(statusCode: Int): String {
    return when (statusCode) {
        400 -> "Invalid request. Please check your input."
        401 -> "Authentication required. Please log in again."
        403 -> "Access denied. You don't have permission for this action."
        404 -> "The requested resource was not found."
        409 -> "A conflict occurred. Please try again."
        422 -> "Unable to process the request. Please check your input."
        in 500..599 -> "Server error. Please try again later."
        else -> "An error occurred. Please try again."
    }
}
