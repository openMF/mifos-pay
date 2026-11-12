/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.intent

import com.mobilebytelabs.kmptoolkit.intentlauncher.ExperimentalIntentLauncherApi
import com.mobilebytelabs.kmptoolkit.intentlauncher.IntentResult
import com.mobilebytelabs.kmptoolkit.intentlauncher.SystemIntents

/**
 * The one [IntentManager], for every target.
 *
 * `cmp-intent-launcher` carries the per-target `actual`s, so there is no source-set split here.
 */
@OptIn(ExperimentalIntentLauncherApi::class)
class IntentManagerImpl : IntentManager {

    override suspend fun openAppSettings(): IntentResult = SystemIntents.openAppSettings()

    override suspend fun createDocument(fileName: String, mimeType: String): IntentResult =
        SystemIntents.createDocument(fileName, mimeType)
}
