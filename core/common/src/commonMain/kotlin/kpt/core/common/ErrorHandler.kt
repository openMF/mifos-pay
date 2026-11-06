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
 * Interface for providing localized error messages.
 * Implement this interface in the UI module to provide string resources.
 */
interface ErrorMessageProvider {
    fun getHttpErrorTitle(statusCode: Int): String
    fun getHttpErrorMessage(statusCode: Int, fallbackMessage: String?): String
    fun getNetworkErrorTitle(): String
    fun getNetworkErrorMessage(): String
    fun getGenericErrorTitle(): String
    fun getGenericErrorMessage(fallbackMessage: String?): String
}

/**
 * A data class representing a user-facing error with title, message, and retry capability.
 *
 * This class is designed to be used directly in UI layers without any additional mapping.
 * The companion object provides factory methods to create appropriate error representations
 * from various exception types.
 *
 * Usage:
 * ```kotlin
 * // In ViewModel
 * val uiError = UiError.from(exception, errorMessageProvider)
 *
 * // In Composable
 * MifosDialogBox(
 *     showDialogState = error != null,
 *     title = error?.title ?: "",
 *     message = error?.message ?: "",
 *     confirmButtonText = if (error?.canRetry == true) "Retry" else "OK",
 *     dismissButtonText = if (error?.canRetry == true) "Cancel" else null,
 *     onConfirm = if (error?.canRetry == true) onRetry else onDismiss,
 *     onDismiss = onDismiss,
 * )
 * ```
 */
data class UiError(
    val title: String,
    val message: String,
    val canRetry: Boolean,
    val errorCode: Int? = null,
) {
    companion object {
        /**
         * Creates a [UiError] from any [Throwable] using the provided [ErrorMessageProvider].
         *
         * This method automatically handles different exception types:
         * - [HttpStatusException]: Uses user-friendly HTTP error messages
         * - [NetworkException]: Uses network-specific error messages
         * - Other exceptions: Uses generic error messages with the exception message
         */
        fun from(throwable: Throwable, provider: ErrorMessageProvider): UiError {
            return when (throwable) {
                is HttpStatusException -> UiError(
                    title = provider.getHttpErrorTitle(throwable.statusCode),
                    message = provider.getHttpErrorMessage(throwable.statusCode, throwable.message),
                    canRetry = throwable.canRetry,
                    errorCode = throwable.statusCode,
                )

                is NetworkException -> UiError(
                    title = provider.getNetworkErrorTitle(),
                    message = throwable.message ?: provider.getNetworkErrorMessage(),
                    canRetry = true,
                )

                else -> UiError(
                    title = provider.getGenericErrorTitle(),
                    message = provider.getGenericErrorMessage(throwable.message),
                    canRetry = false,
                )
            }
        }
    }
}

/**
 * Extension function to convert any [Throwable] to a [UiError].
 */
fun Throwable.toUiError(provider: ErrorMessageProvider): UiError = UiError.from(this, provider)
