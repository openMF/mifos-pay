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

/**
 * Asks the OS for a specific system screen or document flow, and reports what came back.
 *
 * ## Scope — system intents ONLY
 * This interface used to also carry `launchUri` and the `share*` family, which conflated three
 * different acts. They now live on [kpt.core.base.platform.url.UrlLauncher] and
 * [kpt.core.base.platform.share.ShareManager]. What is left here is the case neither of those
 * covers: handing control to the OS and getting an [IntentResult] back.
 *
 * Every method returns an [IntentResult] rather than Unit, because a system flow can be cancelled
 * by the user and the caller usually has to react to that.
 */
@OptIn(ExperimentalIntentLauncherApi::class)
interface IntentManager {

    /** Open this app's entry in system settings — the target for a denied-permission rationale. */
    suspend fun openAppSettings(): IntentResult

    /** Ask the OS for a save location, returning the chosen document's uri. */
    suspend fun createDocument(fileName: String, mimeType: String = "*/*"): IntentResult
}
