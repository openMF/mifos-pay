/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.update

/**
 * In-app update check, with real behaviour on every target.
 *
 * ## Why this is one commonMain contract now
 * This used to be a per-target pair: a Play Core implementation in `androidMain` and a twin in
 * `nonAndroidMain` whose two methods were empty bodies. iOS, desktop and web therefore had no
 * update path at all, and no caller could tell — the calls compiled and silently did nothing.
 *
 * [AppUpdateManagerImpl] now delegates to the toolkit's `AppUpdate` engine, which ships real
 * `actual`s for 11 targets, so one implementation is honest on all of them. A target the engine
 * genuinely cannot serve says so through [UpdateOutcome.NotSupported] rather than by doing nothing.
 *
 * ## Why these suspend
 * The check is a network call on every target. The former signature was fire-and-forget, which is
 * exactly what let a no-op twin pass for a working implementation.
 */
interface AppUpdateManager {

    /** Check for an update and, if one is available, start the flow. */
    suspend fun checkForAppUpdate(): UpdateOutcome

    /**
     * Re-check after the app returns to the foreground, so an update the user backgrounded
     * mid-flow is offered again. Call from the host's resume hook.
     */
    suspend fun checkForResumeUpdateState(): UpdateOutcome

    /** Whether this target can perform an in-app update at all. */
    fun isSupported(): Boolean
}

/** What an update check concluded. Flattened from the engine's richer result type. */
sealed interface UpdateOutcome {

    /** No update available — the user is current. */
    data object UpToDate : UpdateOutcome

    /** An update was available and the flow was started. */
    data object UpdateStarted : UpdateOutcome

    /** The user dismissed the update flow. */
    data object Cancelled : UpdateOutcome

    /**
     * This target has no in-app update mechanism; [reason] says why.
     *
     * @property reason why this target has no in-app update mechanism. A diagnostic, not user-facing copy.
     */
    data class NotSupported(val reason: String) : UpdateOutcome

    /**
     * The check or the flow failed.
     *
     * @property message what failed, for logging — not user-facing copy.
     */
    data class Failed(val message: String) : UpdateOutcome
}
