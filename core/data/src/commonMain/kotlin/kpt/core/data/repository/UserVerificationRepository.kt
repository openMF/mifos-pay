/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repository

/**
 * One-shot, time-bounded "user passed a passcode/biometric verification just
 * now" token. Backs the round-trip handshake between the internal passcode
 * gate and any caller that needs proof of recent authentication before
 * performing a sensitive action.
 *
 * Pattern:
 *  1. The internal passcode screen calls [recordVerification] from
 *     `ForkNavHost`'s `onAuthenticationSuccess` callback after the user
 *     successfully verifies.
 *  2. The calling feature (settings disable-biometrics, intra-bank transfer)
 *     observes the saved-state-handle round-trip boolean, then calls
 *     [consumeVerification] before the protected action. If the call returns
 *     `false`, the verification has expired (default 30 s window) and the
 *     user must re-verify.
 *
 * Single-use: [consumeVerification] both checks validity and clears the
 * token. A second call without an intervening [recordVerification] returns
 * `false`.
 */
interface UserVerificationRepository {
    /** Stamp "verified just now" with the current monotonic time. */
    fun recordVerification()

    /**
     * Returns `true` iff a [recordVerification] happened within the
     * implementation's validity window (currently 30 s) **and** has not been
     * consumed yet. Always clears the stored mark before returning.
     */
    fun consumeVerification(): Boolean
}
