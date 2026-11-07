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
 * App-wide lock flag, persisted across process restarts.
 *
 * Set/cleared by [kpt.feature.passcode.MifosPasscodeViewModel]:
 *  - locked on `OnStart` if the passcode screen is in `Enter` step,
 *  - unlocked on `PasscodeResult.Verified` (passcode or biometric success),
 *  - deleted on `PasscodeResult.Forgotten` / logout.
 *
 * Read by `ForkAppViewModel.isAppLocked()` (nullable `Boolean?`) for the
 * 15-second background-resume re-auth gate in `MifosPayApp`. `null` means
 * "no flag written yet" — fresh install or post-logout — and is treated
 * as "no session to gate".
 */
interface AppLockRepository {
    /** Mark the app as locked. Persisted. */
    fun lockApp()

    /** Mark the app as unlocked. Persisted. */
    fun unlockApp()

    /** Clear the flag entirely (logout / forgot-passcode). */
    fun deleteLock()

    /**
     * Snapshot read of the lock flag.
     *
     * @return `true` if explicitly locked (set via [lockApp]), `false` if
     *         explicitly unlocked (set via [unlockApp]), or `null` if no flag
     *         has been written or it was cleared via [deleteLock] (fresh
     *         install / post-logout). Callers that want to treat
     *         "absent" as a specific boolean must do so themselves —
     *         this interface no longer assumes a default.
     */
    fun isAppLocked(): Boolean?
}
