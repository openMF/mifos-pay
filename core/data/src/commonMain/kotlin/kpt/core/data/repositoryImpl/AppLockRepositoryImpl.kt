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

import com.russhwolf.settings.Settings
import kpt.core.data.repository.AppLockRepository
/** Multiplatform-Settings key for the [APP_LOCK_FLAG] boolean. */
const val APP_LOCK_FLAG = "org.mifospay.mifos.app_lock_flag"

/**
 * [AppLockRepository] implementation backed by `com.russhwolf.settings.Settings`
 * (single boolean entry under [APP_LOCK_FLAG]).
 *
 * `isAppLocked()` returns `null` when the key is absent (fresh install or
 * after [deleteLock]); the caller decides how to interpret that.
 * `MifosPayApp` treats `null` the same as "no re-auth needed" via
 * `isAppLocked()?.let { ... }` — the absence of a flag means there's no
 * established session to re-gate.
 */
class AppLockRepositoryImpl(
    private val settings: Settings,
) : AppLockRepository {
    override fun lockApp() {
        settings.putBoolean(APP_LOCK_FLAG, true)
    }

    override fun unlockApp() {
        settings.putBoolean(APP_LOCK_FLAG, false)
    }

    override fun deleteLock() {
        settings.remove(APP_LOCK_FLAG)
    }

    override fun isAppLocked(): Boolean? {
        return settings.getBooleanOrNull(APP_LOCK_FLAG)
    }
}
