/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins the two `app.yaml#security` parses `syncForkConfig` generates `AppSecurityConfig` from. They
 * have OPPOSITE missing-key rules, so copying one into the other is the regression this guards.
 */
class SecurityDefaultsTest {

    @Test
    fun signedInMissingKeyIsTrue() {
        // A fork whose app.yaml predates the key must stay signed in.
        assertEquals(true, signedInByDefault(mapOf<String, Any?>()))
        assertEquals(true, signedInByDefault(null))
        assertEquals(true, signedInByDefault(mapOf("signed_in_by_default" to null)))
    }

    @Test
    fun signedInExplicitValues() {
        assertEquals(false, signedInByDefault(mapOf("signed_in_by_default" to false)))
        assertEquals(false, signedInByDefault(mapOf("signed_in_by_default" to " FALSE ")))
        assertEquals(true, signedInByDefault(mapOf("signed_in_by_default" to true)))
        assertEquals(true, signedInByDefault(mapOf("signed_in_by_default" to "true")))
    }

    @Test
    fun screenCaptureMissingKeyIsFalse() {
        assertEquals(false, screenCaptureAllowedByDefault(mapOf<String, Any?>()))
        assertEquals(false, screenCaptureAllowedByDefault(null))
        assertEquals(false, screenCaptureAllowedByDefault(mapOf("screen_capture_allowed_by_default" to "yes")))
        assertEquals(true, screenCaptureAllowedByDefault(mapOf("screen_capture_allowed_by_default" to true)))
    }

    @Test
    fun firstRunOnboardingMissingKeyIsFalse() {
        // Onboarding is opt-in: an app.yaml without the key must not route a fresh install to it.
        assertEquals(false, firstRunOnboarding(mapOf<String, Any?>()))
        assertEquals(false, firstRunOnboarding(null))
        assertEquals(false, firstRunOnboarding(mapOf("first_run_onboarding" to "yes")))
        assertEquals(true, firstRunOnboarding(mapOf("first_run_onboarding" to true)))
        assertEquals(true, firstRunOnboarding(mapOf("first_run_onboarding" to " TRUE ")))
    }
}
