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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.onboarding.generated.resources.Res
import kpt.feature.onboarding.generated.resources.feature_onboarding_analytics_body
import kpt.feature.onboarding.generated.resources.feature_onboarding_analytics_label
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// Every page, plus the privacy page in each consent state — each preview is a Roborazzi golden
// (feature/onboarding/src/desktopTest/resources/screenshots/onboarding/<name>.png). `internal`, not private:
// CommonComposablePreviewScanner only sees internal/public previews.

@Composable
private fun Preview(state: OnboardingState) {
    KptTheme {
        OnboardingScreenContent(
            state = state,
            onAction = {},
            onOpenServerConfig = null,
            onOpenPrivacyPolicy = {},
            onOpenTerms = {},
        )
    }
}

@Preview
@Composable
internal fun OnboardingOverviewPreview() = Preview(OnboardingState(page = OnboardingPage.Overview))

@Preview
@Composable
internal fun OnboardingTrackPreview() = Preview(OnboardingState(page = OnboardingPage.Track))

@Preview
@Composable
internal fun OnboardingOfflinePreview() = Preview(OnboardingState(page = OnboardingPage.Offline))

/** The page a first run actually lands on: both switches OFF, "Continue without sharing". */
@Preview
@Composable
internal fun OnboardingPrivacyDefaultPreview() = Preview(OnboardingState(page = OnboardingPage.Privacy))

/** One switch on: the secondary button reads "Continue with my choices". */
@Preview
@Composable
internal fun OnboardingPrivacyOneChosenPreview() =
    Preview(OnboardingState(page = OnboardingPage.Privacy, crashReports = true))

@Preview
@Composable
internal fun OnboardingPrivacySaveFailedPreview() =
    Preview(OnboardingState(page = OnboardingPage.Privacy, saveFailed = true))

// The building blocks on their own, so a regression in one part shows in a golden of its own.

/** Parts drawn outside the screen's own Surface would otherwise render on a transparent background. */
@Composable
private fun PartsFrame(content: @Composable ColumnScope.() -> Unit) {
    Surface { Column(content = content) }
}

@Preview
@Composable
internal fun OnboardingIntroPartsPreview() {
    KptTheme {
        PartsFrame {
            OnboardingTopBar(page = OnboardingPage.Overview, onSkip = {})
            PageIllustration(page = OnboardingPage.Overview)
            IntroPage(page = OnboardingPage.Overview)
            PageActions(state = OnboardingState(page = OnboardingPage.Overview), onAction = {})
        }
    }
}

@Preview
@Composable
internal fun OnboardingPageBodyPreview() {
    KptTheme {
        PageBody(
            state = OnboardingState(page = OnboardingPage.Offline),
            onAction = {},
            onOpenServerConfig = null,
            onOpenPrivacyPolicy = {},
            onOpenTerms = {},
        )
    }
}

@Preview
@Composable
internal fun OnboardingPrivacyPagePreview() {
    KptTheme {
        PrivacyPage(
            state = OnboardingState(page = OnboardingPage.Privacy, analytics = true),
            onAction = {},
            onOpenServerConfig = null,
            onOpenPrivacyPolicy = {},
            onOpenTerms = {},
        )
    }
}

@Preview
@Composable
internal fun OnboardingConsentPartsPreview() {
    KptTheme {
        PartsFrame {
            PageIndicator(page = OnboardingPage.Privacy)
            OnboardingConsentRow(
                title = stringResource(Res.string.feature_onboarding_analytics_label),
                description = stringResource(Res.string.feature_onboarding_analytics_body),
                checked = true,
                onCheckedChange = {},
            )
            NoPersonalDataNote()
            AgreementText(onOpenPrivacyPolicy = {}, onOpenTerms = {})
        }
    }
}
