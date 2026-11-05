/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.observability

/**
 * Fork-customization seam for crash + non-fatal-error reporting.
 *
 * The toolkit ships [ConsoleCrashReporter] which writes everything to stdout — sufficient
 * for local development but not for shipped builds. Forks override the
 * [kpt.core.base.observability.di.observabilityModule] binding in their app module
 * with a real implementation (Firebase Crashlytics, Sentry, Bugsnag, Datadog RUM, etc.).
 *
 * ## Why a separate seam from the cmp-firebase `AnalyticsHelper`?
 *
 * Crash reporting and analytics serve different audiences:
 * - **Analytics** is high-volume, sampled, event-stream (button clicks, screen views) routed
 *   to product owners.
 * - **Crash reporting** is low-volume, exception-detail-heavy, stack-trace-bearing data
 *   routed to engineering.
 *
 * Most production stacks (Firebase Analytics + Firebase Crashlytics, Mixpanel + Sentry)
 * keep them in separate SDKs with separate ingestion pipelines and different retention
 * policies. Modeling them as separate seams matches the wire reality and lets each fork
 * choose providers independently.
 *
 * ## Contract notes
 *
 * - All methods **must** be safe to call from any thread / coroutine. No suspending APIs —
 *   crash reporters typically queue + flush asynchronously.
 * - [setUser] accepts a nullable userId for sign-out flows; pass `null` to clear.
 * - [recordException] is for **caught** exceptions (e.g. retry-loop exhaustion); uncaught
 *   exceptions are reported by the platform-level handler the fork installs.
 * - [isConfigured] is `false` for the no-op default; UI / startup checks can decide whether
 *   to surface "send crash logs?" prompts based on this.
 *
 * @see ConsoleCrashReporter Default development-only implementation
 */
interface CrashReporter {

    /**
     * Record a caught exception with optional context message. Use for:
     * - Retry-loop exhaustion (e.g. [OfflineSubmitSyncer] failing after N attempts)
     * - Recovered errors that nonetheless deserve engineering visibility
     * - Logic violations that didn't crash but indicate a bug
     *
     * @param throwable The exception (including its stack trace)
     * @param message Optional human-readable label (e.g. `"OfflineSubmitSyncer.retryAll: gave up after 5 retries"`)
     */
    fun recordException(throwable: Throwable, message: String? = null)

    /**
     * Record a free-form message at a severity level.
     *
     * @param message Human-readable text to attach to the crash-reporter ring buffer
     *   (most providers persist a tail of these and attach to subsequent crashes for
     *   crash-context debugging)
     * @param level Severity classification — providers route Fatal+ to PagerDuty etc.
     */
    fun recordMessage(message: String, level: CrashSeverity = CrashSeverity.Info)

    /**
     * Set (or clear) the current user identifier for subsequent crash reports.
     *
     * Pass `null` on sign-out so crashes after sign-out are attributed to "anonymous". The
     * identifier passed here is provider-specific — typically a hashed user ID (Firebase
     * Crashlytics: `setUserId(String)`; Sentry: `Sentry.configureScope { it.user = User(id=...) }`).
     *
     * @param userId Opaque identifier, or `null` to clear
     */
    fun setUser(userId: String?)

    /**
     * `true` when the fork has wired a real crash reporter. `false` for the default
     * [ConsoleCrashReporter] (signals to startup checks that crash reporting is local-only).
     */
    val isConfigured: Boolean
}

/**
 * Severity vocabulary for [CrashReporter.recordMessage].
 *
 * Providers map these to their own log-level abstractions:
 * - Firebase Crashlytics: `setCustomKey` + log-level annotation
 * - Sentry: `SentryLevel.DEBUG/INFO/WARNING/ERROR/FATAL`
 * - Bugsnag: severity field on the event payload
 *
 * The toolkit reserves [Fatal] for events that **would** be crashes if the code path
 * didn't catch them (e.g. a guard at the root of a coroutine scope catching CancellationException).
 */
enum class CrashSeverity {
    /** Verbose detail, useful only while debugging. */
    Debug,

    /** Normal operational events. */
    Info,

    /** Something unexpected that the app recovered from. */
    Warning,

    /** A failure the user is likely to have noticed. */
    Error,

    /** A failure the app cannot continue past. */
    Fatal,
}
