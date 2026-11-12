/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.onboarding.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.user.UserDataRepository

/**
 * First-run onboarding: three intro pages and the privacy page.
 *
 * The page is STATE and the screen renders a `when` over it: a pager kept in sync both ways can disagree mid-swipe.
 *
 * Consent is held in memory and written only by a finishing action, never by flipping a switch:
 * - [OnboardingAction.AllowAll] is the one-tap affirmative consent — both ON. A pre-ticked switch, or "turn it off
 *   later in Settings", is not valid consent under GDPR/ePrivacy, UK GDPR/PECR or India's DPDP Act; a tap on
 *   "Allow" is.
 * - [OnboardingAction.ContinueWithChoices] saves the switches exactly as shown (both OFF unless turned on).
 *
 * Either one writes the two choices FIRST and `setFirstTimeState(false)` LAST: RootNavViewModel moves on as soon
 * as first run is marked done, so consent must already be stored when it does. `PrivacyConsentApplier` then
 * applies the stored choice on every platform — onboarding has no Firebase dependency of its own.
 */
class OnboardingViewModel(
    private val userDataRepository: UserDataRepository,
) : BaseViewModel<OnboardingState, Nothing, OnboardingAction>(OnboardingState()) {

    override fun handleAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.Next -> updateState { copy(page = page.next()) }
            OnboardingAction.Previous -> updateState { copy(page = page.previous()) }
            // Skip lands on the privacy page, never past it: the intro is optional, the choice is not.
            OnboardingAction.Skip -> updateState { copy(page = OnboardingPage.Privacy) }
            is OnboardingAction.SetAnalytics -> updateState { copy(analytics = action.granted) }
            is OnboardingAction.SetCrashReports -> updateState { copy(crashReports = action.granted) }
            OnboardingAction.AllowAll -> {
                updateState { copy(analytics = true, crashReports = true) }
                finish()
            }
            OnboardingAction.ContinueWithChoices -> finish()
            OnboardingAction.DismissSaveError -> updateState { copy(saveFailed = false) }
        }
    }

    private fun finish() {
        if (state.saving) return
        val analytics = state.analytics
        val crashReports = state.crashReports
        updateState { copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val saved = runCatching {
                userDataRepository.setAnalyticsConsent(analytics)
                userDataRepository.setCrashReportingConsent(crashReports)
                userDataRepository.setFirstTimeState(false)
            }
            // On failure the user stays on the privacy page; first run is NOT marked done, so a partial
            // write is never treated as a finished onboarding.
            updateState { copy(saving = false, saveFailed = saved.isFailure) }
        }
    }
}

/** The four onboarding pages, in order. */
enum class OnboardingPage {
    /** What the app is for. A fork renames these to its own pages. */
    Overview,

    /** Keeping track of what is due. */
    Track,

    /** Working offline and syncing later. */
    Offline,

    /** The consent choices — always the last page. */
    Privacy,
    ;

    /** The next page; the privacy page is the last. */
    fun next(): OnboardingPage = entries[(ordinal + 1).coerceAtMost(entries.lastIndex)]

    /** The previous page; the first page has none. */
    fun previous(): OnboardingPage = entries[(ordinal - 1).coerceAtLeast(0)]
}

/** What the onboarding screen renders. */
data class OnboardingState(
    /** The page on screen. */
    val page: OnboardingPage = OnboardingPage.Overview,
    /** "Share usage analytics" — OFF until the user turns it on or taps Allow. */
    val analytics: Boolean = false,
    /** "Send crash reports" — OFF until the user turns it on or taps Allow. */
    val crashReports: Boolean = false,
    /** A finishing write is in flight; both finishing buttons are disabled. */
    val saving: Boolean = false,
    /** The last finishing write failed; the privacy page shows the error. */
    val saveFailed: Boolean = false,
) {
    /** Whether either switch is on — picks the secondary button's label. */
    val anyShared: Boolean get() = analytics || crashReports
}

/** Intents accepted by [OnboardingViewModel]. */
sealed interface OnboardingAction {
    /** Move to the next page. */
    data object Next : OnboardingAction

    /** Move to the previous page. */
    data object Previous : OnboardingAction

    /** Jump to the privacy page. */
    data object Skip : OnboardingAction

    /**
     * Turn usage analytics on or off (in memory until a finishing action).
     *
     * @property granted the switch value
     */
    data class SetAnalytics(val granted: Boolean) : OnboardingAction

    /**
     * Turn crash reports on or off (in memory until a finishing action).
     *
     * @property granted the switch value
     */
    data class SetCrashReports(val granted: Boolean) : OnboardingAction

    /** "Allow and get started" — consent to both, save, finish onboarding. */
    data object AllowAll : OnboardingAction

    /** "Continue without sharing" / "Continue with my choices" — save the switches as shown, finish. */
    data object ContinueWithChoices : OnboardingAction

    /** Hide the save-failed message. */
    data object DismissSaveError : OnboardingAction
}
