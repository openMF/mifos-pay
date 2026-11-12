/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.url

import com.mobilebytelabs.kmptoolkit.openurl.canOpen
import com.mobilebytelabs.kmptoolkit.openurl.openInApp
import com.mobilebytelabs.kmptoolkit.openurl.openInBrowser
import com.mobilebytelabs.kmptoolkit.openurl.openUrl

/**
 * The one [UrlLauncher], for every target.
 *
 * `cmp-open-url` carries the per-target `actual`s, so there is no source-set split here.
 *
 * These are plain imports, and safely so: [UrlLauncher]'s methods are `launch*` while the delegates
 * are `open*`/`canOpen`, so no member can shadow an import here. That naming is load-bearing — a
 * same-named delegation is self-recursion, not delegation; see the rationale on [UrlLauncher].
 */
class UrlLauncherImpl : UrlLauncher {
    override fun launch(url: String): Boolean = openUrl(url)
    override fun launchInBrowser(url: String): Boolean = openInBrowser(url)
    override fun launchInApp(url: String): Boolean = openInApp(url)
    override fun canLaunch(url: String): Boolean = canOpen(url)
}
