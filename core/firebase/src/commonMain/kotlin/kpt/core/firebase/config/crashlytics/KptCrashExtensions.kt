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

import io.github.mobilebytelabs.kmptoolkit.firebase.crashlytics.CrashReporter

/**
 * CROSS-CUTTING [CrashReporter] breadcrumbs — TEMPLATE-OWNED, full-copied by every sync.
 *
 * The extension half of [KptCrashKeys]: it sets those keys and nothing else, so the vocabulary and
 * the calls that write it stay reviewable apart. Extensions rather than a wrapper class because the
 * callers already hold a [CrashReporter] — a navigation observer, a Ktor plugin, a sync worker.
 *
 * ## Feature breadcrumbs go in the feature's package
 * `kpt/core/firebase/<feature>/` holds them (see `loans/`), fork-owned in a fork. Nothing here
 * may name a feature, and no key set here may carry a value the user typed, an amount, or an
 * identifier that resolves to a person — a crash report travels further than an analytics event.
 */

/**
 * Record the screen the user is on, so a crash report opens with the right context.
 * Call from the navigation observer that already raises `trackScreenView`.
 */
fun CrashReporter.setCurrentScreen(screenName: String, previousScreen: String? = null) {
    setCustomKey(KptCrashKeys.CURRENT_SCREEN, screenName)
    previousScreen?.let { setCustomKey(KptCrashKeys.PREVIOUS_SCREEN, it) }
    log("screen -> $screenName")
}

/**
 * Connectivity at crash time. Offline-first apps behave differently offline, so a report without this
 * is ambiguous between "broken" and "correctly degraded".
 */
fun CrashReporter.setNetworkState(online: Boolean, captivePortal: Boolean = false) {
    val state = when {
        !online -> "offline"
        captivePortal -> "captive_portal"
        else -> "online"
    }
    setCustomKey(KptCrashKeys.NETWORK_STATE, state)
}

/**
 * Whether a sync was in flight, and how many writes were still queued. A crash during replay of a
 * 40-deep outbox is a different defect from a crash with an empty queue.
 */
fun CrashReporter.setSyncState(inFlight: Boolean, pendingWrites: Int) {
    setCustomKey(KptCrashKeys.SYNC_IN_FLIGHT, inFlight.toString())
    setCustomKey(KptCrashKeys.PENDING_WRITES, pendingWrites.toString())
}

/**
 * The last request the app made. [endpoint] must be the TEMPLATED path (`/loans/{id}`) — an
 * interpolated one would put a real id into the report.
 */
fun CrashReporter.setLastRequest(endpoint: String, statusCode: Int?) {
    setCustomKey(KptCrashKeys.LAST_ENDPOINT, endpoint)
    statusCode?.let { setCustomKey(KptCrashKeys.LAST_STATUS_CODE, it.toString()) }
}

/** Locale and theme — cheap to set, and they explain a surprising share of layout crashes. */
fun CrashReporter.setAppearance(locale: String, themeMode: String) {
    setCustomKey(KptCrashKeys.APP_LOCALE, locale)
    setCustomKey(KptCrashKeys.THEME_MODE, themeMode)
}

/**
 * A non-fatal that the app handled but should not have hit — a `Result.failure` surfaced to the user,
 * an unexpected empty state. Fatal crashes arrive on their own; these are the ones that stay invisible.
 */
fun CrashReporter.recordHandled(throwable: Throwable, context: String) {
    recordException(throwable, fatal = false, extraKeys = mapOf("handled_context" to context))
}
