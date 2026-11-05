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

import com.mobilebytelabs.kmptoolkit.appupdate.AppUpdate
import com.mobilebytelabs.kmptoolkit.appupdate.AppUpdateConfig
import com.mobilebytelabs.kmptoolkit.appupdate.UpdateResult
import com.mobilebytelabs.kmptoolkit.appupdate.UpdateType

/**
 * The one [AppUpdateManager], for every target.
 *
 * There is deliberately no `androidMain` / `nonAndroidMain` split: `cmp-in-app-update` carries the
 * per-target `actual`s. It also takes no `Activity` — that was Play Core's requirement, not the
 * contract's — which is why this can be a Koin `single` and why `LocalManagerProvider` no longer
 * has to construct it per platform.
 *
 * [config] carries the store ids and per-platform enablement. A fork passes its own so those values
 * stay in app-profile rather than hardcoded here.
 */
class AppUpdateManagerImpl(
    private val config: AppUpdateConfig = AppUpdateConfig.Default,
    private val updateType: UpdateType = UpdateType.IMMEDIATE,
) : AppUpdateManager {

    override suspend fun checkForAppUpdate(): UpdateOutcome {
        val result = AppUpdate.checkForUpdate(config)
        if (result !is UpdateResult.Success) return result.toOutcome()
        // Start it here rather than returning "available" and trusting the caller: a check that
        // reports availability and never offers the update is the failure this class exists to end.
        return if (result.updateInfo.isAvailable) {
            AppUpdate.startUpdate(updateType, config).toOutcome()
        } else {
            UpdateOutcome.UpToDate
        }
    }

    /**
     * The engine resolves in-progress update state per target, so resuming is the same call as the
     * initial check — an already-running IMMEDIATE flow is re-surfaced rather than restarted.
     */
    override suspend fun checkForResumeUpdateState(): UpdateOutcome = checkForAppUpdate()

    override fun isSupported(): Boolean = AppUpdate.isSupported()
}

private fun UpdateResult.toOutcome(): UpdateOutcome = when (this) {
    is UpdateResult.Success ->
        if (updateInfo.isAvailable) UpdateOutcome.UpdateStarted else UpdateOutcome.UpToDate
    is UpdateResult.Cancelled -> UpdateOutcome.Cancelled
    is UpdateResult.NotSupported -> UpdateOutcome.NotSupported(reason)
    is UpdateResult.Error -> UpdateOutcome.Failed(message)
}
