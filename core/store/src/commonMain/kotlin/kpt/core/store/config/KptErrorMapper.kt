/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.store.generated.resources.Res
import kpt.core.store.generated.resources.error_category_auth
import kpt.core.store.generated.resources.error_category_client
import kpt.core.store.generated.resources.error_category_generic
import kpt.core.store.generated.resources.error_category_network
import kpt.core.store.generated.resources.error_category_quota
import kpt.core.store.generated.resources.error_category_ratelimit
import kpt.core.store.generated.resources.error_category_server
import kpt.core.store.generated.resources.error_category_timeout_connect
import kpt.core.store.generated.resources.error_category_timeout_read
import org.jetbrains.compose.resources.stringResource

/**
 * Application-level error → user-facing message mapper.
 *
 * ONE source of user-facing copy: [rememberAppErrorMessageFor], which resolves every
 * [ErrorCategory] through `stringResource(...)`. Wired into [appScreenStateDefaults] for the
 * [ScreenStateError.messageFor] hook.
 *
 * There used to be a second, non-Composable `mapErrorToUserMessage` carrying its own hardcoded
 * English copy. It was not merely a duplicate — it was FINER-GRAINED than the localized path, which
 * collapsed Timeout onto network copy and QuotaExceeded onto rate-limit copy. So the same error
 * produced different words depending on the call site, and the more accurate wording was the one
 * no locale could ever see. Those distinctions are now resource strings, and the non-Composable
 * entry point is [errorCategoryToken] — a stable diagnostic token, never user-facing, so it cannot
 * be rendered by mistake.
 *
 * TEMPLATE-OWNED — a sync full-copies this file, so a fork receives every improvement to the
 * framework branches below. Fork-specific errors go in [ForkErrorMapper], which implements
 * [ErrorMessageOverrides] and is consulted FIRST; this file falls through to the categorised copy
 * when it returns null. Do not add fork branches here — they would be overwritten on the next sync.
 */
fun errorCategoryToken(error: Throwable): String = when (val cat = categorize(error)) {
    ErrorCategory.Network -> "network"
    ErrorCategory.Timeout.Connect -> "timeout_connect"
    ErrorCategory.Timeout.Read -> "timeout_read"
    ErrorCategory.Auth -> "auth"
    ErrorCategory.RateLimit -> "rate_limit"
    ErrorCategory.QuotaExceeded -> "quota_exceeded"
    is ErrorCategory.Server -> "server_${cat.httpCode}"
    is ErrorCategory.ClientError -> "client_${cat.httpCode}"
    ErrorCategory.Generic -> "generic"
}

/**
 * The fork's error-copy extension point, declared HERE so it is TEMPLATE-owned and full-copied by
 * every sync. [ForkErrorMapper] implements it and is the only fork-owned half.
 *
 * It stays in this file on purpose: customization-surface.yaml lists this path as owner:template,
 * but the catch-all glob covering the rest of `core/store` is owner:fork — so the same interface
 * in a sibling file would
 * be fork-owned, never synced, and a new hook could never reach an existing fork. That is the
 * migration FS-4 of scripts/product-health/checks/store-fork-seam-wiring.sh exists to block.
 *
 * The default returns null — "I do not handle this" — so the framework's categorised copy above
 * stays reachable. That default is the reason this is an interface rather than a top-level function
 * the fork's file must declare: when the template adds a NEW hook here, it ships with a default body
 * and every existing fork keeps compiling. A top-level function cannot do that — the template's call
 * site would reference a function that does not exist in the fork-owned file, and the fork would
 * break on the very sync that was supposed to hand it the improvement.
 */
interface ErrorMessageOverrides {
    /** Fork copy for [error], or null to fall through to the framework's categorised message. */
    fun message(error: Throwable): String? = null
}

/**
 * Composable factory that resolves per-category copy via `stringResource(...)`, returning
 * a pure-Kotlin `(Throwable) -> String` lambda safe to pass through to
 * [kpt.core.base.ui.screen.ScreenStateError.messageFor] (which is invoked from inside
 * composition where these strings are already memoised).
 *
 * Reuses [categorize] to bucket the error, then picks the localized string.
 */
@Composable
fun rememberAppErrorMessageFor(): (Throwable) -> String {
    val networkCopy = stringResource(Res.string.error_category_network)
    val connectTimeoutCopy = stringResource(Res.string.error_category_timeout_connect)
    val readTimeoutCopy = stringResource(Res.string.error_category_timeout_read)
    val authCopy = stringResource(Res.string.error_category_auth)
    val rateLimitCopy = stringResource(Res.string.error_category_ratelimit)
    val quotaCopy = stringResource(Res.string.error_category_quota)
    val serverCopy = stringResource(Res.string.error_category_server)
    val clientCopy = stringResource(Res.string.error_category_client)
    val genericCopy = stringResource(Res.string.error_category_generic)
    val projectCopy = ForkErrorMapper::message
    return remember(
        networkCopy,
        connectTimeoutCopy,
        readTimeoutCopy,
        authCopy,
        rateLimitCopy,
        quotaCopy,
        serverCopy,
        clientCopy,
        genericCopy,
    ) {
        { error: Throwable ->
            projectCopy(error) ?: when (categorize(error)) {
                ErrorCategory.Network -> networkCopy
                ErrorCategory.Timeout.Connect -> connectTimeoutCopy
                ErrorCategory.Timeout.Read -> readTimeoutCopy
                ErrorCategory.Auth -> authCopy
                ErrorCategory.RateLimit -> rateLimitCopy
                ErrorCategory.QuotaExceeded -> quotaCopy
                is ErrorCategory.Server -> serverCopy
                // Never `error.message`: a transport exception's message is a diagnostic, not user
                // copy — Ktor builds it from the whole exchange (request URL with host, tenant and
                // query, then the raw response body), so echoing it shows users server JSON and can
                // grow tall enough to push the retry button off screen. Copy that an exception
                // genuinely carries for users belongs in ForkErrorMapper, consulted first above.
                is ErrorCategory.ClientError -> clientCopy
                ErrorCategory.Generic -> genericCopy
            }
        }
    }
}
