/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user

import kotlinx.serialization.Serializable
import kpt.core.model.config.AppPrivacyConfig
import kpt.core.model.config.AppSecurityConfig

/**
 * Everything the app persists about the current user — theme, language, onboarding progress and the
 * lock state.
 *
 * One aggregate rather than a field-per-key store: it is read as a unit on almost every screen, and
 * splitting it would make a theme change and a lock change race each other. `DEFAULT` is what a
 * first run sees.
 */
@Serializable
data class UserData(
    /** Id of the signed-in user, or empty on a signed-out/demo run. */
    val activeUserId: String,
    /** Selected colour brand. */
    val themeBrand: ThemeBrand,
    /** Dark-mode preference. */
    val darkThemeConfig: DarkThemeConfig,
    /** Whether to derive the palette from the platform's dynamic colour (Android 12+); ignored where unsupported. */
    val useDynamicColor: Boolean,
    /** Selected app language, or `DEFAULT` to follow the system. */
    val appLanguage: LanguageConfig,
    /** Whether the onboarding flow still has to be shown. */
    val showOnboarding: Boolean,
    /** True until the first full run completes. Distinct from [showOnboarding], which the user can dismiss. */
    val firstTimeUser: Boolean,
    /** Whether a session is established. */
    val isAuthenticated: Boolean,
    /** Whether the app-lock is currently satisfied. Reset on every cold start, unlike [isPasscodeEnabled]. */
    val isUnlocked: Boolean,
    /** The app-lock passcode. Present only when [isPasscodeEnabled]. */
    val passcode: String,
    /**
     * Whether screenshots and screen recording are permitted; false hides the window from the recents thumbnail on the
     * platforms that support it.
     */
    val enableScreenCapture: Boolean,
    /** Whether passcode lock is turned on. */
    val isPasscodeEnabled: Boolean,
    /**
     * Whether biometric unlock is turned on. Independent of [isPasscodeEnabled] — biometrics still need a passcode
     * fallback.
     */
    val isBiometricsEnabled: Boolean,
    /**
     * The user's consent to share usage analytics (GDPR Art. 6(1)(a)). Defaulted, so a record saved by a build
     * that predates consent decodes to the generated starting value ([AppPrivacyConfig]) — off unless a fork
     * declares otherwise — rather than failing to load.
     */
    val analyticsConsent: Boolean = AppPrivacyConfig.ANALYTICS_ENABLED_BY_DEFAULT,
    /** The user's consent to send crash reports. Defaulted for the same reason as [analyticsConsent]. */
    val crashReportingConsent: Boolean = AppPrivacyConfig.CRASH_REPORTING_ENABLED_BY_DEFAULT,
) {
    /** Factory holder. */
    companion object {
        /**
         * What a first run sees: nothing locked, system theme and language. Signed in or not comes from
         * `app-profile/app.yaml#security.signed_in_by_default` (generated into [AppSecurityConfig]) —
         * `true` for the no-login demo, `false` for a fork whose app has a login.
         */
        val DEFAULT = UserData(
            activeUserId = "",
            passcode = "1234",
            themeBrand = ThemeBrand.DEFAULT,
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useDynamicColor = false,
            appLanguage = LanguageConfig.DEFAULT,
            isAuthenticated = AppSecurityConfig.SIGNED_IN_BY_DEFAULT,
            isUnlocked = true,
            isPasscodeEnabled = false,
            isBiometricsEnabled = false,
            showOnboarding = false,
            // app-profile/app.yaml#security.first_run_onboarding (generated) — true only in a fork that ships an
            // onboarding destination; RootNavViewModel shows it once, until setFirstTimeState(false).
            firstTimeUser = AppSecurityConfig.FIRST_RUN_ONBOARDING,
            // app-profile/app.yaml#security.screen_capture_allowed_by_default (generated) — a fork
            // decides; the user's later choice is persisted by core/datastore and wins.
            enableScreenCapture = AppSecurityConfig.SCREEN_CAPTURE_ALLOWED_BY_DEFAULT,
        )
    }
}
