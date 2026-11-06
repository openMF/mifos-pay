/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.io.IOException

/**
 * Central mapper — Throwable / feature error handlers →
 * [ScreenState.Error] / [ScreenState.NoNetwork] / [ScreenState.Unauthenticated].
 *
 * Consolidates the exception-typing logic so that the repository read-side
 * has ONE seam to route every error through.
 *
 * Mirrors the template's error-routing intent:
 *  - `ClientRequestException` (401/403) → [ScreenState.Unauthenticated]
 *  - `ClientRequestException` (other 4xx) → [ScreenState.Error] with parsed [HttpStatusException]
 *  - `ServerResponseException` (5xx) → [ScreenState.Error] with parsed [HttpStatusException]
 *  - `IOException` / [NetworkException] → [ScreenState.NoNetwork]
 *  - Fork [HttpStatusException] 401/403 → [ScreenState.Unauthenticated]
 *  - Anything else → [ScreenState.Error]
 *
 * Phase-3 limitation: captive-portal detection defers to Phase 4 (needs
 * cmp-network-monitor); mapper always emits `isCaptivePortal = false`.
 */
object MifosErrorMapper {

    /** HTTP status codes that route to [ScreenState.Unauthenticated]. */
    private val UnauthenticatedStatuses: Set<Int> = setOf(401, 403)

    /**
     * Blocking (non-suspending) mapper — use when no response body is
     * available or the caller has already parsed a user-facing message.
     * For raw ktor exceptions in a `catch { }` on a `Flow`, prefer the
     * suspending [mapErrorBody] which will parse the response body via the
     * caller-supplied [ErrorBodyParser].
     */
    fun <T> mapError(
        throwable: Throwable,
        userMessage: String? = null,
    ): ScreenState<T> {
        // Fork-level status wrappers first — they preserve the parsed message.
        if (throwable is HttpStatusException) {
            return if (throwable.statusCode in UnauthenticatedStatuses) {
                ScreenState.Unauthenticated
            } else {
                ScreenState.Error(error = throwable, isNetworkError = false)
            }
        }
        if (throwable is NetworkException) {
            return ScreenState.NoNetwork(isCaptivePortal = false)
        }
        if (throwable is IOException) {
            return ScreenState.NoNetwork(isCaptivePortal = false)
        }

        // Raw ktor exceptions — no response-body parse (that's the suspend path).
        if (throwable is ClientRequestException) {
            val status = throwable.response.status.value
            return if (status in UnauthenticatedStatuses) {
                ScreenState.Unauthenticated
            } else {
                ScreenState.Error(
                    error = HttpStatusException(
                        statusCode = status,
                        userMessage = userMessage ?: (throwable.message ?: "Request failed"),
                        developerMessage = throwable.message,
                    ),
                    isNetworkError = false,
                )
            }
        }
        if (throwable is ServerResponseException) {
            val status = throwable.response.status.value
            return ScreenState.Error(
                error = HttpStatusException(
                    statusCode = status,
                    userMessage = userMessage ?: (throwable.message ?: "Server error"),
                    developerMessage = throwable.message,
                ),
                isNetworkError = false,
            )
        }

        return ScreenState.Error(error = throwable, isNetworkError = false)
    }

    /**
     * Suspending mapper — parses the ktor response body via [errorBodyParser]
     * to extract a user-facing message when possible. Use inside a
     * `.catch { MifosErrorMapper.mapErrorBody(it) }` Flow terminator when the
     * repository has a Mifos-error-body parser to route through.
     */
    suspend fun <T> mapErrorBody(
        throwable: Throwable,
        errorBodyParser: ErrorBodyParser = defaultErrorBodyParser,
    ): ScreenState<T> {
        return when (throwable) {
            is HttpStatusException, is NetworkException, is IOException -> mapError(throwable)
            is ClientRequestException -> {
                val status = throwable.response.status.value
                if (status in UnauthenticatedStatuses) {
                    ScreenState.Unauthenticated
                } else {
                    val body = readBodyOrEmpty(throwable)
                    val parsed = errorBodyParser(body, status)
                    ScreenState.Error(
                        error = HttpStatusException(
                            statusCode = status,
                            userMessage = parsed,
                            developerMessage = throwable.message,
                        ),
                        isNetworkError = false,
                    )
                }
            }
            is ServerResponseException -> {
                val status = throwable.response.status.value
                val body = readBodyOrEmpty(throwable)
                val parsed = errorBodyParser(body, status)
                ScreenState.Error(
                    error = HttpStatusException(
                        statusCode = status,
                        userMessage = parsed,
                        developerMessage = throwable.message,
                    ),
                    isNetworkError = false,
                )
            }
            else -> ScreenState.Error(error = throwable, isNetworkError = false)
        }
    }

    private suspend fun readBodyOrEmpty(e: ClientRequestException): String = try {
        e.response.bodyAsText()
    } catch (_: Exception) {
        ""
    }

    private suspend fun readBodyOrEmpty(e: ServerResponseException): String = try {
        e.response.bodyAsText()
    } catch (_: Exception) {
        ""
    }
}

/** Convert a raw [Throwable] to a [ScreenState] via [MifosErrorMapper]. */
fun <T> Throwable.toScreenState(userMessage: String? = null): ScreenState<T> =
    MifosErrorMapper.mapError(this, userMessage)
