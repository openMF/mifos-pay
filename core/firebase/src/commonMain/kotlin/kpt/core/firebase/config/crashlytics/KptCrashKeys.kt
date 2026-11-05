/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.firebase.config.crashlytics

/**
 * CROSS-CUTTING crash context — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Mirrors its `config/analytics/` sibling: this file knows about NO feature. A feature's crash context
 * lives with its analytics in `kpt/core/firebase/<feature>/`, outside `config/` — fork-owned in a
 * fork; the shipped `loans/` is the template's showcase of that layout.
 *
 * ## What this is for
 * A stack trace says where a crash happened; it rarely says what the user was doing. These helpers
 * attach the breadcrumbs that make a report actionable — which screen, which sync, which request —
 * through the kmptoolkit [CrashReporter] contract (`recordException` / `log` / `setCustomKey`).
 *
 * ## The rule these helpers exist to enforce
 * A crash report leaves the device and is readable by anyone with console access, so **no key set
 * here carries a value the user typed, an amount, or an identifier that resolves to a person**.
 * Screen names, request paths and durations describe the app; account numbers describe the user.
 * Feature packages inherit the same rule — see `loans/` for how it is applied.
 */
object KptCrashKeys {
    /** Route the user is on — the single most useful key for reproducing a crash. */
    const val CURRENT_SCREEN = "current_screen"

    /** Route they came from, which distinguishes a bad destination from a bad transition. */
    const val PREVIOUS_SCREEN = "previous_screen"

    /** Groups every crash and breadcrumb from one app run. */
    const val SESSION_ID = "session_id"

    /** Connectivity at crash time; separates an offline path from a server fault. */
    const val NETWORK_STATE = "network_state"

    /** Whether a background sync was running — the usual source of a race. */
    const val SYNC_IN_FLIGHT = "sync_in_flight"

    /** Most recent request path. */
    const val LAST_ENDPOINT = "last_endpoint"

    /**
     * HTTP status of the last response. Paired with [LAST_ENDPOINT] it distinguishes a crash after a 401 from one
     * after a 500, which usually have different causes.
     */
    const val LAST_STATUS_CODE = "last_status_code"

    /** Active locale, for a crash that only reproduces under one translation or script direction. */
    const val APP_LOCALE = "app_locale"

    /** Light/dark, for a crash confined to one palette. */
    const val THEME_MODE = "theme_mode"

    /** How many queued offline mutations were outstanding. */
    const val PENDING_WRITES = "pending_writes"
}
