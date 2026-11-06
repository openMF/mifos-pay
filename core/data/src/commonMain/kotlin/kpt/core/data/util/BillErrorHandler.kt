/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Utility class for handling bill-related errors
 */
object BillErrorHandler {

    /**
     * Handles network errors and converts them to user-friendly messages
     */
    fun handleNetworkError(throwable: Throwable): String {
        return when (throwable) {
            is TimeoutCancellationException -> "Request timed out. Please try again."
            is ClientRequestException -> {
                when (throwable.response.status) {
                    HttpStatusCode.BadRequest -> "Invalid request. Please check your input and try again."
                    HttpStatusCode.Unauthorized -> "Authentication failed. Please log in again."
                    HttpStatusCode.Forbidden -> "Access denied. You don't have permission to perform this action."
                    HttpStatusCode.NotFound -> "Bill not found. It may have been deleted or moved."
                    HttpStatusCode.Conflict -> "Bill already exists with the same name and biller."
                    HttpStatusCode.UnprocessableEntity -> "Invalid bill data. Please check your input and try again."
                    else -> "An error occurred. Please try again."
                }
            }
            is ServerResponseException -> {
                when (throwable.response.status) {
                    HttpStatusCode.InternalServerError -> "Server error. Please try again later."
                    HttpStatusCode.BadGateway -> "Bad gateway. Please try again later."
                    HttpStatusCode.ServiceUnavailable -> "Service temporarily unavailable. Please try again later."
                    else -> "Server error. Please try again later."
                }
            }
            else -> "An unexpected error occurred. Please try again."
        }
    }

    /**
     * Handles validation errors and converts them to user-friendly messages
     */
    fun handleValidationError(validationResult: kpt.core.model.autopay.BillValidationResult): String {
        return validationResult.nameError
            ?: validationResult.amountError
            ?: validationResult.dueDateError
            ?: validationResult.recurrencePatternError
            ?: validationResult.billerError
            ?: "Please check your input and try again."
    }

    /**
     * Creates a user-friendly error message for specific bill operations
     */
    fun createOperationErrorMessage(operation: String, error: String): String {
        return when (operation.lowercase()) {
            "create" -> "Failed to create bill: $error"
            "update" -> "Failed to update bill: $error"
            "delete" -> "Failed to delete bill: $error"
            "fetch" -> "Failed to load bills: $error"
            "validate" -> "Bill validation failed: $error"
            else -> "Operation failed: $error"
        }
    }

    /**
     * Checks if an error is retryable
     */
    fun isRetryableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is TimeoutCancellationException -> true
            is ClientRequestException -> false
            is ServerResponseException -> {
                when (throwable.response.status) {
                    HttpStatusCode.InternalServerError,
                    HttpStatusCode.BadGateway,
                    HttpStatusCode.ServiceUnavailable,
                    HttpStatusCode.GatewayTimeout,
                    -> true
                    else -> false
                }
            }
            else -> false
        }
    }

    /**
     * Gets retry delay in milliseconds based on error type
     */
    fun getRetryDelay(throwable: Throwable): Long {
        return when (throwable) {
            is TimeoutCancellationException -> 2000L
            is ServerResponseException -> {
                when (throwable.response.status) {
                    HttpStatusCode.InternalServerError,
                    HttpStatusCode.BadGateway,
                    HttpStatusCode.ServiceUnavailable,
                    HttpStatusCode.GatewayTimeout,
                    -> 3000L
                    else -> 1000L
                }
            }
            else -> 1000L
        }
    }
}
