/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.repositoryImpl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.data.repository.UserVerificationRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** Validity window for a verification token. */
private val VERIFICATION_EXPIRY: Duration = 30.seconds

/**
 * [UserVerificationRepository] implementation backed by
 * [TimeSource.Monotonic], held in-process for the lifetime of this singleton.
 *
 * A wall-clock-based implementation (e.g. `Clock.System.now()` + persisted
 * epoch-millis) is vulnerable to a **clock-rewind attack**: an attacker who
 * just entered the passcode can record the verification, background the app,
 * roll the device clock backward via Settings → Date & time, then return
 * later and have the elapsed-time check still succeed — extending their
 * verification window far past the intended 30 s. Monotonic clocks cannot
 * be moved by the user, so this vector is closed.
 *
 * **Trade-off:** the mark is held in a `private var`, not persisted, so a
 * process restart between [recordVerification] and [consumeVerification]
 * invalidates the token and the user must re-verify. For a 30 s window
 * that's acceptable defense-in-depth — and in practice every consumer
 * (settings disable-biometrics, intra-bank transfer auth gate) round-trips
 * through navigation in the same process.
 */
@RepositoryBinding(binds = UserVerificationRepository::class)
class UserVerificationRepositoryImpl : UserVerificationRepository {

    private var mark: TimeSource.Monotonic.ValueTimeMark? = null

    override fun recordVerification() {
        mark = TimeSource.Monotonic.markNow()
    }

    override fun consumeVerification(): Boolean {
        val captured = mark
        mark = null
        return captured != null && captured.elapsedNow() <= VERIFICATION_EXPIRY
    }
}
