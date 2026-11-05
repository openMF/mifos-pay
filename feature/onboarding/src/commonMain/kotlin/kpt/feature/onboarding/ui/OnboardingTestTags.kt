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

/**
 * Stable handles for the onboarding screen — the Preview, the Compose UI test and the Maestro flow
 * (idea-layer/screens/onboarding/maestro) all read these, never a hand-typed string.
 */
object OnboardingTestTags {
    /** The screen root. */
    const val SCREEN = "onboarding_pager"

    /** Skip, on the intro pages. */
    const val SKIP = "onboarding_skip"

    /** The intro page's title. */
    const val TITLE = "onboarding_title"

    /** The intro page's body. */
    const val BODY = "onboarding_body"

    /** The page dots. */
    const val PAGE_INDICATOR = "onboarding_page_indicator"

    /** Next, on the intro pages. */
    const val NEXT = "onboarding_next"

    /** The privacy page's title. */
    const val PRIVACY_TITLE = "onboarding_privacy_title"

    /** The "Share usage analytics" switch row. */
    const val ANALYTICS_SWITCH = "onboarding_analytics_switch"

    /** The "Send crash reports" switch row. */
    const val CRASH_REPORTS_SWITCH = "onboarding_crash_reports_switch"

    /**
     * "By using our service you accept our Terms & Conditions and Privacy Policy." — one localized sentence
     * whose two links are
     * inline `LinkAnnotation`s (so each locale keeps its own word order). Spans cannot carry their own tag, so
     * tests find the sentence by this tag and tap a link by its text.
     */
    const val AGREEMENT = "onboarding_agreement"

    /** "Set up your server" — shown only when the app passes a server-setup destination. */
    const val CONNECT_INSTANCE = "onboarding_connect_instance"

    /** "Allow and get started" — the primary, one-tap consent. */
    const val ALLOW_GET_STARTED = "onboarding_allow_get_started"

    /** "Continue without sharing" / "Continue with my choices". */
    const val CONTINUE_CHOICES = "onboarding_continue_choices"

    /** The save-failed message. */
    const val SAVE_FAILED = "onboarding_save_failed"
}
