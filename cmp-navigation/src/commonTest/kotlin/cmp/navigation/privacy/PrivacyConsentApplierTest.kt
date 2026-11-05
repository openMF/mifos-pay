/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.privacy

import cmp.navigation.testing.FakeUserDataRepository
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.TestAnalyticsHelper
import io.github.mobilebytelabs.kmptoolkit.firebase.testing.FakeCrashReporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Consent drives collection both ways: given turns it on, withdrawn turns it off again. */
@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyConsentApplierTest {

    @Test
    fun collectionFollowsTheStoredConsentBothWays() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeUserDataRepository()
        val analytics = TestAnalyticsHelper()
        PrivacyConsentApplier(repo, analytics, FakeCrashReporter()).start(backgroundScope)

        // No consent stored -> collection off, Consent Mode denied (the helper starts `true`, so this proves
        // the applier actively switched it off rather than inheriting a default).
        assertFalse(analytics.collectionEnabled)
        assertEquals(false to false, analytics.consent)

        repo.setAnalyticsConsent(true)
        assertTrue(analytics.collectionEnabled)
        assertEquals(true to false, analytics.consent, "ad storage stays denied even with consent")

        repo.setAnalyticsConsent(false)
        assertFalse(analytics.collectionEnabled, "withdrawal must stop collection immediately")
    }

    @Test
    fun crashReportingFollowsItsOwnConsentIndependently() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeUserDataRepository()
        val crashes = FakeCrashReporter()
        PrivacyConsentApplier(repo, TestAnalyticsHelper(), crashes).start(backgroundScope)

        // Applied at start even when "off": Crashlytics persists the flag, so a stale "on" from an older build
        // must be overwritten, not assumed away.
        assertEquals(listOf(false), crashes.collectionChanges)

        repo.setCrashReportingConsent(true)
        repo.setAnalyticsConsent(true) // a different consent — must not touch crash reporting
        repo.setCrashReportingConsent(false)
        assertEquals(listOf(false, true, false), crashes.collectionChanges)
    }
}
