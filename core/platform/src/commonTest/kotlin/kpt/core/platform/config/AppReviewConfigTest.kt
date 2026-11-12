/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.platform.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Locks [AppReviewConfig.shouldPromptForReview].
 *
 * The thresholds themselves are GENERATED from app-profile, so asserting a specific verdict against
 * the shipped values would fail on any fork that retunes them — and "fix" it by weakening the test.
 * These assert the RELATIONSHIPS instead, which hold for every threshold set.
 */
class AppReviewConfigTest {

    @Test
    fun neverPromptsBeforeTheLaunchThreshold() {
        // One launch short. Everything else maximally satisfied, so only MIN_LAUNCHES can decide.
        assertFalse(
            AppReviewConfig.shouldPromptForReview(
                launchCount = AppReviewConfig.MIN_LAUNCHES - 1,
                daysSinceInstall = Int.MAX_VALUE,
                daysSinceLastPrompt = null,
            ),
        )
    }

    @Test
    fun neverPromptsBeforeTheInstallAgeThreshold() {
        assertFalse(
            AppReviewConfig.shouldPromptForReview(
                launchCount = Int.MAX_VALUE,
                daysSinceInstall = AppReviewConfig.MIN_DAYS_SINCE_INSTALL - 1,
                daysSinceLastPrompt = null,
            ),
        )
    }

    @Test
    fun neverRePromptsInsideTheCooldown() {
        // The regression this guards: `daysSinceLastPrompt == null` means NEVER PROMPTED, which is
        // eligible. A null-coalescing-to-zero reading would invert that into "prompted today",
        // silently making the first prompt impossible rather than the repeat prompt.
        assertFalse(
            AppReviewConfig.shouldPromptForReview(
                launchCount = Int.MAX_VALUE,
                daysSinceInstall = Int.MAX_VALUE,
                daysSinceLastPrompt = AppReviewConfig.COOLDOWN_DAYS - 1,
            ),
        )
        assertTrue(
            AppReviewConfig.shouldPromptForReview(
                launchCount = Int.MAX_VALUE,
                daysSinceInstall = Int.MAX_VALUE,
                daysSinceLastPrompt = null,
            ) == AppReviewConfig.ENABLED,
        )
    }

    @Test
    fun thresholdsAreInclusive() {
        // Exactly-at-threshold must pass, or every threshold is effectively one higher than the
        // number a fork wrote in app.yaml.
        assertEquals(
            AppReviewConfig.ENABLED,
            AppReviewConfig.shouldPromptForReview(
                launchCount = AppReviewConfig.MIN_LAUNCHES,
                daysSinceInstall = AppReviewConfig.MIN_DAYS_SINCE_INSTALL,
                daysSinceLastPrompt = AppReviewConfig.COOLDOWN_DAYS,
            ),
        )
    }

    @Test
    fun disabledConfigNeverPrompts() {
        if (AppReviewConfig.ENABLED) return
        assertFalse(AppReviewConfig.shouldPromptForReview(Int.MAX_VALUE, Int.MAX_VALUE, null))
    }

    @Test
    fun storeListingCarriesEveryGeneratedId() {
        // Guards a copy/paste transposition in the generated block: four same-typed String fields in
        // a row is exactly the shape where appStoreId lands in microsoftStoreProductId unnoticed.
        val listing = AppReviewConfig.storeListing
        assertEquals(AppReviewConfig.PLAY_STORE_PACKAGE, listing.playStorePackage)
        assertEquals(AppReviewConfig.APP_STORE_ID, listing.appStoreId)
        assertEquals(AppReviewConfig.MICROSOFT_STORE_PRODUCT_ID, listing.microsoftStoreProductId)
        assertEquals(AppReviewConfig.WEB_URL, listing.webUrl)
    }
}
