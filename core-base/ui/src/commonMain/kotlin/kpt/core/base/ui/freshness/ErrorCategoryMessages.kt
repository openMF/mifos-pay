/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.freshness

import kpt.core.base.store.error.ErrorCategory

/**
 * Short (≤ 20 chars) message per [ErrorCategory] variant — used by
 * `FreshnessIndicator` tooltip titles and `RefreshStateChip` labels.
 *
 * Generic-domain copy; forks can override `KptErrorMapper` in `core/store` to
 * surface app-specific messaging (which itself delegates here for the canonical
 * categorisation).
 */
fun ErrorCategory.toShortMessage(): String = when (this) {
    is ErrorCategory.Network -> "No network"
    is ErrorCategory.Auth -> "Auth error"
    is ErrorCategory.RateLimit -> "Rate limited"
    is ErrorCategory.ClientError -> "Request error"
    is ErrorCategory.Server -> "Server error"
    is ErrorCategory.QuotaExceeded -> "Quota exceeded"
    is ErrorCategory.Timeout.Connect -> "Timed out"
    is ErrorCategory.Timeout.Read -> "Timed out"
    is ErrorCategory.Generic -> "Sync failed"
}

/**
 * Longer descriptive message per [ErrorCategory] variant — used by
 * `FreshnessIndicator` `RichTooltip` body text. `KptErrorMapper.mapErrorToUserMessage`
 * delegates here so the message catalogue lives in one place.
 */
fun ErrorCategory.toLongMessage(): String = when (this) {
    is ErrorCategory.Network -> "Device is offline or no network connection."
    is ErrorCategory.Auth -> "Authentication failed. Please sign in again."
    is ErrorCategory.RateLimit -> "Too many requests. Please wait and retry."
    is ErrorCategory.ClientError -> "Request could not be processed (HTTP ${this.httpCode})."
    is ErrorCategory.Server -> "Server returned an error${this.httpCode?.let { " (HTTP $it)" } ?: ""}. Try again later."
    is ErrorCategory.QuotaExceeded -> "API quota exceeded. Data will refresh when the quota resets."
    is ErrorCategory.Timeout.Connect -> "Connection timed out. Check your network."
    is ErrorCategory.Timeout.Read -> "Request timed out reading data. Try again."
    is ErrorCategory.Generic -> "Sync failed for an unknown reason."
}
