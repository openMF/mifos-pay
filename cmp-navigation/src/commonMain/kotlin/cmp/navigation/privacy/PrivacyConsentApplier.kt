/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.privacy

import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.crashlytics.CrashReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kpt.core.data.user.UserDataRepository

/**
 * Applies the user's stored analytics and crash-reporting consent to the Firebase library — on start, and every
 * time either changes.
 *
 * core-base starts analytics with collection OFF, so nothing is sent before this runs; from then on the
 * stored choice (seeded by `app-profile/app.yaml#privacy`, changed in Settings → Privacy) is the only
 * thing that turns collection on, and withdrawing it turns collection off again immediately — GDPR
 * Art. 7(3): withdrawing consent must be as easy as giving it.
 *
 * Started once from `initKoin`, so it runs on every platform. Crash reporting goes through the library's
 * [CrashReporter] too: withdrawing consent stops Crashlytics and the GA4 crash mirror, and deletes reports
 * cached but not yet sent. Crashlytics persists its flag, so the stored choice is re-applied on every start.
 */
class PrivacyConsentApplier(
    private val userData: UserDataRepository,
    private val analytics: AnalyticsHelper,
    private val crashReporter: CrashReporter,
) {
    /** Begin following the stored consent; returns immediately. */
    fun start(scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)) {
        userData.observeAnalyticsConsent
            .distinctUntilChanged()
            .onEach { granted ->
                analytics.setCollectionEnabled(granted)
                // Google Consent Mode v2 (expected for EEA users): analytics storage follows the user; ad storage is
                // always denied — the template shows no ads, so there is nothing to consent to there.
                analytics.setConsent(analyticsStorage = granted, adStorage = false)
            }
            .launchIn(scope)
        userData.observeCrashReportingConsent
            .distinctUntilChanged()
            .onEach { crashReporter.setCollectionEnabled(it) }
            .launchIn(scope)
    }
}
