/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import kpt.core.common.ErrorMessageProvider
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_error_msg_access_denied
import kpt.core.ui.generated.resources.core_ui_error_msg_bad_gateway
import kpt.core.ui.generated.resources.core_ui_error_msg_conflict
import kpt.core.ui.generated.resources.core_ui_error_msg_gateway_timeout
import kpt.core.ui.generated.resources.core_ui_error_msg_generic
import kpt.core.ui.generated.resources.core_ui_error_msg_invalid_request
import kpt.core.ui.generated.resources.core_ui_error_msg_not_found
import kpt.core.ui.generated.resources.core_ui_error_msg_request_timeout
import kpt.core.ui.generated.resources.core_ui_error_msg_server_error
import kpt.core.ui.generated.resources.core_ui_error_msg_service_unavailable
import kpt.core.ui.generated.resources.core_ui_error_msg_too_many_requests
import kpt.core.ui.generated.resources.core_ui_error_msg_unauthorized
import kpt.core.ui.generated.resources.core_ui_error_msg_validation_error
import kpt.core.ui.generated.resources.core_ui_error_title_access_denied
import kpt.core.ui.generated.resources.core_ui_error_title_bad_gateway
import kpt.core.ui.generated.resources.core_ui_error_title_conflict
import kpt.core.ui.generated.resources.core_ui_error_title_gateway_timeout
import kpt.core.ui.generated.resources.core_ui_error_title_invalid_request
import kpt.core.ui.generated.resources.core_ui_error_title_not_found
import kpt.core.ui.generated.resources.core_ui_error_title_request_error
import kpt.core.ui.generated.resources.core_ui_error_title_request_timeout
import kpt.core.ui.generated.resources.core_ui_error_title_server_error
import kpt.core.ui.generated.resources.core_ui_error_title_service_unavailable
import kpt.core.ui.generated.resources.core_ui_error_title_too_many_requests
import kpt.core.ui.generated.resources.core_ui_error_title_unauthorized
import kpt.core.ui.generated.resources.core_ui_error_title_validation_error
import org.jetbrains.compose.resources.getString
/**
 * Default implementation of [ErrorMessageProvider] that uses Compose resources.
 *
 * This implementation provides localized error messages for HTTP status codes
 * and network errors. It uses suspend functions to load string resources.
 *
 * Usage:
 * ```kotlin
 * val provider = ErrorMessageProviderImpl()
 * val uiError = UiError.from(exception, provider)
 * ```
 */
class ErrorMessageProviderImpl : ErrorMessageProvider {

    override fun getHttpErrorTitle(statusCode: Int): String {
        return getHttpErrorTitleSync(statusCode)
    }

    override fun getHttpErrorMessage(statusCode: Int, fallbackMessage: String?): String {
        return getHttpErrorMessageSync(statusCode, fallbackMessage)
    }

    override fun getNetworkErrorTitle(): String {
        return getNetworkErrorTitleSync()
    }

    override fun getNetworkErrorMessage(): String {
        return getNetworkErrorMessageSync()
    }

    override fun getGenericErrorTitle(): String {
        return getGenericErrorTitleSync()
    }

    override fun getGenericErrorMessage(fallbackMessage: String?): String {
        return fallbackMessage ?: getGenericErrorMessageSync()
    }

    companion object {
        // HTTP Error Titles - using hardcoded fallbacks for sync access
        private fun getHttpErrorTitleSync(statusCode: Int): String = when (statusCode) {
            400 -> "Invalid Request"
            401 -> "Unauthorized"
            403 -> "Access Denied"
            404 -> "Not Found"
            408 -> "Request Timeout"
            409 -> "Conflict"
            422 -> "Validation Error"
            429 -> "Too Many Requests"
            500 -> "Server Error"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Timeout"
            else -> if (statusCode in 400..499) "Request Error" else "Server Error"
        }

        // HTTP Error Messages
        private fun getHttpErrorMessageSync(statusCode: Int, fallbackMessage: String?): String = when (statusCode) {
            400 -> "The request was invalid. Please check your input and try again."
            401 -> "You are not authorized. Please log in again."
            403 -> "You don't have permission to perform this action."
            404 -> "The requested resource was not found."
            408 -> "The request timed out. Please try again."
            409 -> "There was a conflict with the current state. Please refresh and try again."
            422 -> "The provided data is invalid. Please check and try again."
            429 -> "Too many requests. Please wait a moment and try again."
            500 -> "An unexpected server error occurred. Please try again later."
            502 -> "The server received an invalid response. Please try again later."
            503 -> "The service is temporarily unavailable. Please try again later."
            504 -> "The server took too long to respond. Please try again."
            else -> fallbackMessage ?: "An error occurred. Please try again."
        }

        private fun getNetworkErrorTitleSync(): String = "Network Error"

        private fun getNetworkErrorMessageSync(): String =
            "Please check your internet connection and try again."

        private fun getGenericErrorTitleSync(): String = "Error"

        private fun getGenericErrorMessageSync(): String =
            "An unexpected error occurred."

        /**
         * Suspend function to get HTTP error title using Compose resources.
         * Use this when you need localized strings in a coroutine context.
         */
        suspend fun getHttpErrorTitleResource(statusCode: Int): String = when (statusCode) {
            400 -> getString(Res.string.core_ui_error_title_invalid_request)
            401 -> getString(Res.string.core_ui_error_title_unauthorized)
            403 -> getString(Res.string.core_ui_error_title_access_denied)
            404 -> getString(Res.string.core_ui_error_title_not_found)
            408 -> getString(Res.string.core_ui_error_title_request_timeout)
            409 -> getString(Res.string.core_ui_error_title_conflict)
            422 -> getString(Res.string.core_ui_error_title_validation_error)
            429 -> getString(Res.string.core_ui_error_title_too_many_requests)
            500 -> getString(Res.string.core_ui_error_title_server_error)
            502 -> getString(Res.string.core_ui_error_title_bad_gateway)
            503 -> getString(Res.string.core_ui_error_title_service_unavailable)
            504 -> getString(Res.string.core_ui_error_title_gateway_timeout)
            else -> if (statusCode in 400..499) {
                getString(Res.string.core_ui_error_title_request_error)
            } else {
                getString(Res.string.core_ui_error_title_server_error)
            }
        }

        /**
         * Suspend function to get HTTP error message using Compose resources.
         */
        suspend fun getHttpErrorMessageResource(statusCode: Int, fallbackMessage: String?): String = when (statusCode) {
            400 -> getString(Res.string.core_ui_error_msg_invalid_request)
            401 -> getString(Res.string.core_ui_error_msg_unauthorized)
            403 -> getString(Res.string.core_ui_error_msg_access_denied)
            404 -> getString(Res.string.core_ui_error_msg_not_found)
            408 -> getString(Res.string.core_ui_error_msg_request_timeout)
            409 -> getString(Res.string.core_ui_error_msg_conflict)
            422 -> getString(Res.string.core_ui_error_msg_validation_error)
            429 -> getString(Res.string.core_ui_error_msg_too_many_requests)
            500 -> getString(Res.string.core_ui_error_msg_server_error)
            502 -> getString(Res.string.core_ui_error_msg_bad_gateway)
            503 -> getString(Res.string.core_ui_error_msg_service_unavailable)
            504 -> getString(Res.string.core_ui_error_msg_gateway_timeout)
            else -> fallbackMessage ?: getString(Res.string.core_ui_error_msg_generic)
        }
    }
}

/**
 * Singleton instance of [ErrorMessageProviderImpl] for easy access.
 */
val DefaultErrorMessageProvider: ErrorMessageProvider = ErrorMessageProviderImpl()
