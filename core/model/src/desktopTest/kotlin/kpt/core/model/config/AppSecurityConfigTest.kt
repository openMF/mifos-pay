/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.config

import kpt.core.model.user.UserData
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks the wiring, not the value: [AppSecurityConfig] is GENERATED from app-profile, so asserting
 * `false` would fail on every fork that allows capture — and invite "fixing" it by weakening the test.
 */
class AppSecurityConfigTest {

    @Test
    fun freshInstallScreenCaptureFollowsAppProfile() {
        assertEquals(AppSecurityConfig.SCREEN_CAPTURE_ALLOWED_BY_DEFAULT, UserData.DEFAULT.enableScreenCapture)
    }

    @Test
    fun freshInstallSignedInStateFollowsAppProfile() {
        // Wiring only — the value is the fork's (`security.signed_in_by_default`); a login app sets false.
        assertEquals(AppSecurityConfig.SIGNED_IN_BY_DEFAULT, UserData.DEFAULT.isAuthenticated)
    }

    @Test
    fun freshInstallOnboardingFollowsAppProfile() {
        // Wiring only — `security.first_run_onboarding`; true only in an app that wants feature/onboarding shown.
        assertEquals(AppSecurityConfig.FIRST_RUN_ONBOARDING, UserData.DEFAULT.firstTimeUser)
    }
}
