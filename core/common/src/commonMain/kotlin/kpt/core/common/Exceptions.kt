/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

/**
 * Exception thrown when an HTTP request fails with a specific status code.
 * Use this to differentiate between client errors (4xx) and server errors (5xx).
 *
 * @param statusCode The HTTP status code
 * @param userMessage The user-friendly message (parsed from server response if available)
 * @param developerMessage The technical message for debugging
 */
class HttpStatusException(
    val statusCode: Int,
    val userMessage: String,
    val developerMessage: String? = null,
) : Exception(userMessage) {
    /** True for a 4xx response: the request itself was rejected. */
    val isClientError: Boolean get() = statusCode in 400..499

    /** True for a 5xx response: the server failed to handle a valid request. */
    val isServerError: Boolean get() = statusCode in 500..599

    /** Whether retrying may succeed; only server errors are worth retrying. */
    val canRetry: Boolean get() = isServerError
}

/**
 * Exception thrown when a network operation fails due to connectivity issues.
 */
class NetworkException(message: String) : Exception(message)
