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

import com.russhwolf.settings.Settings
import kotlin.time.Clock

/**
 * What the review policy needs to know at app open, derived from [AppReviewPromptStore].
 *
 * @property launchCount completed app launches, including this one.
 * @property daysSinceInstall whole days since first launch.
 * @property daysSinceLastPrompt whole days since the last prompt, or `null` if never prompted.
 */
data class AppReviewPromptState(
    val launchCount: Int,
    val daysSinceInstall: Int,
    val daysSinceLastPrompt: Int?,
)

/**
 * Tracks the three counters that gate the automatic review prompt.
 *
 * Deliberately NOT part of [UserPreferencesRepository]: this is DEVICE state, not user state. It
 * must survive sign-out — a user who declined a prompt yesterday should not be asked again today
 * because they logged out in between — whereas `clearUserData()` exists to discard per-user state.
 * Keeping it separate means a fork that later makes `clearUserData()` actually clear the blob
 * cannot silently reset everyone's cooldown.
 *
 * It stores COUNTERS only. Whether those counters justify a prompt is
 * `AppReviewConfig.shouldPromptForReview(...)`, whose thresholds are generated from app-profile —
 * so the policy lives with the config and this stays a dumb ledger.
 */
// datastore-scope: per-device — launch count, install date and last-prompt date gate a
// cooldown that must survive sign-out; a user who declined yesterday must not be asked again
// today merely because they logged out in between. clearUserData() deliberately does NOT
// reach these, which is why they are not fields on UserData.
/**
 * Persisted state behind the in-app review prompt — when it was last shown and whether the user
 * already responded.
 *
 * Durable by necessity: the platform review APIs are quota-limited and silently no-op once the
 * quota is spent, so re-prompting after every restart burns the allowance without showing anything.
 */
interface AppReviewPromptStore {

    /**
     * Record an app launch and return the resulting state.
     *
     * Called once per launch from the app shell. On first ever call it stamps the install date, so
     * `daysSinceInstall` is 0 rather than "since the epoch" — without the stamp every fresh install
     * would read as decades old and clear the install-age threshold immediately.
     */
    fun recordLaunch(): AppReviewPromptState

    /**
     * Stamp that a prompt was just requested, starting the cooldown.
     *
     * Recorded on REQUEST, not on a completed review, because neither Play nor StoreKit reports
     * whether the user actually reviewed. Treating "asked" as the cooldown trigger is the only
     * honest reading available, and it is the conservative one — the alternative re-asks.
     */
    fun recordPromptShown()
}

internal const val REVIEW_LAUNCH_COUNT_KEY = "app_review.launch_count"
internal const val REVIEW_FIRST_LAUNCH_KEY = "app_review.first_launch_epoch_ms"
internal const val REVIEW_LAST_PROMPT_KEY = "app_review.last_prompt_epoch_ms"

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * [Settings]-backed [AppReviewPromptStore], using the same plain store as user preferences.
 *
 * @param nowEpochMillis injectable clock. The codebase reads the wall clock directly elsewhere, but
 *   every value here is a DATE DIFFERENCE, so a test that cannot move time could only assert the
 *   zero-days case — which is the one case the thresholds never exercise.
 */
class SettingsAppReviewPromptStore(
    private val plainSettings: Settings,
    private val nowEpochMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : AppReviewPromptStore {

    override fun recordLaunch(): AppReviewPromptState {
        val now = nowEpochMillis()

        val launchCount = plainSettings.getInt(REVIEW_LAUNCH_COUNT_KEY, 0) + 1
        plainSettings.putInt(REVIEW_LAUNCH_COUNT_KEY, launchCount)

        val firstLaunch = plainSettings.getLong(REVIEW_FIRST_LAUNCH_KEY, 0L)
            .takeIf { it > 0L }
            ?: now.also { plainSettings.putLong(REVIEW_FIRST_LAUNCH_KEY, it) }

        val lastPrompt = plainSettings.getLong(REVIEW_LAST_PROMPT_KEY, 0L).takeIf { it > 0L }

        return AppReviewPromptState(
            launchCount = launchCount,
            daysSinceInstall = wholeDaysBetween(firstLaunch, now),
            daysSinceLastPrompt = lastPrompt?.let { wholeDaysBetween(it, now) },
        )
    }

    override fun recordPromptShown() {
        plainSettings.putLong(REVIEW_LAST_PROMPT_KEY, nowEpochMillis())
    }

    // Clamped at 0. A device whose clock moves BACKWARD (manual change, timezone-less RTC reset)
    // would otherwise produce a negative age that clears every `>=` threshold at once — turning a
    // clock adjustment into an immediate prompt.
    private fun wholeDaysBetween(fromEpochMillis: Long, toEpochMillis: Long): Int =
        ((toEpochMillis - fromEpochMillis) / MILLIS_PER_DAY).coerceAtLeast(0L).toInt()
}
