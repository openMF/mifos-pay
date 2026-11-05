/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the counters behind the automatic review prompt.
 *
 * Every value here is a DATE DIFFERENCE, which is why the store takes an injectable clock: with the
 * wall clock the only reachable assertion is the zero-days case, and zero days is precisely the case
 * the thresholds never care about.
 */
class AppReviewPromptStoreTest {

    private val day = 86_400_000L
    private val settings = MapSettings()
    private var now = 1_700_000_000_000L

    private fun store() = SettingsAppReviewPromptStore(settings) { now }

    @Test
    fun countsLaunchesAcrossInstances() {
        // A new instance per launch is the real shape — Koin resolves one per process start, and the
        // count has to come off disk rather than memory or it resets every launch and never climbs.
        repeat(3) { store().recordLaunch() }

        assertEquals(4, store().recordLaunch().launchCount)
    }

    @Test
    fun firstLaunchStampsInstallDateRatherThanReadingAsEpochZero() {
        // Without the stamp, an unset Long reads 0 and daysSinceInstall becomes ~19_000 — so a fresh
        // install clears min_days_since_install on launch one, which is the opposite of the intent.
        assertEquals(0, store().recordLaunch().daysSinceInstall)

        now += 5 * day
        assertEquals(5, store().recordLaunch().daysSinceInstall)
    }

    @Test
    fun neverPromptedReportsNullNotZero() {
        // null means "never asked" (eligible); 0 would mean "asked today" (in cooldown). Collapsing
        // the two would make the FIRST prompt impossible rather than the repeat one.
        assertNull(store().recordLaunch().daysSinceLastPrompt)
    }

    @Test
    fun cooldownIsMeasuredFromTheLastPrompt() {
        store().recordLaunch()
        store().recordPromptShown()

        assertEquals(0, store().recordLaunch().daysSinceLastPrompt)

        now += 90 * day
        assertEquals(90, store().recordLaunch().daysSinceLastPrompt)
    }

    @Test
    fun backwardClockCannotManufactureEligibility() {
        // A device clock moved backwards would otherwise yield a NEGATIVE age, and negatives clear
        // every `>=` threshold at once — turning a timezone glitch into an immediate prompt.
        store().recordLaunch()
        store().recordPromptShown()

        now -= 30 * day

        val state = store().recordLaunch()
        assertEquals(0, state.daysSinceInstall)
        assertEquals(0, state.daysSinceLastPrompt)
        assertTrue(state.launchCount >= 2)
    }
}
