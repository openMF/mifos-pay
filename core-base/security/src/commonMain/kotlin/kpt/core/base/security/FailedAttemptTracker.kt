/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.security

import co.touchlab.kermit.Logger

/**
 * Tracks failed authentication attempts and triggers lockout or data wipe
 * when configured thresholds are exceeded.
 *
 * @param policy Security policy with attempt thresholds.
 * @param secureWiper Used to wipe data when wipe threshold is reached.
 * @param onLockout Callback invoked when lock threshold is reached.
 */
class FailedAttemptTracker(
    private val policy: SecurityPolicy,
    private val secureWiper: SecureWiper,
    private val onLockout: () -> Unit = {},
) {
    @kotlin.concurrent.Volatile
    private var failedCount: Int = 0

    /** Failed attempts so far in this run. */
    val currentFailedCount: Int get() = failedCount

    /** Whether the lockout threshold has been reached. Recoverable — distinct from the wipe threshold. */
    val isLockedOut: Boolean get() = failedCount >= policy.lockAfterFailedAttempts

    /**
     * Record a failed attempt. Returns the action taken.
     */
    fun recordFailure(): FailureAction {
        failedCount++
        Logger.d("FailedAttemptTracker") { "Failed attempt $failedCount/${policy.wipeAfterFailedAttempts}" }

        return when {
            failedCount >= policy.wipeAfterFailedAttempts -> {
                Logger.w("FailedAttemptTracker") { "Wipe threshold reached — wiping secure storage" }
                secureWiper.wipeSecureStorage()
                failedCount = 0
                FailureAction.DATA_WIPED
            }
            failedCount >= policy.lockAfterFailedAttempts -> {
                onLockout()
                FailureAction.LOCKED_OUT
            }
            else -> FailureAction.ATTEMPT_RECORDED
        }
    }

    /** Reset the counter after a successful authentication. */
    fun reset() {
        failedCount = 0
    }
}

/**
 * What the tracker did in response to a failed attempt — the caller's cue for what to show next.
 *
 * Escalates in order: the attempt was counted, the account locked, or the local data was wiped after
 * the final permitted attempt. [DATA_WIPED] is terminal and irreversible, so a caller must treat it
 * as a state change rather than an error message.
 */
enum class FailureAction {
    /** The attempt was counted; the user may try again. */
    ATTEMPT_RECORDED,

    /** The lockout threshold was reached. Recoverable. */
    LOCKED_OUT,

    /** The wipe threshold was reached and local data has been destroyed. Terminal and irreversible. */
    DATA_WIPED,
}
