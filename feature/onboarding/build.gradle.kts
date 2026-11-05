/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // UserDataRepository (setAnalyticsConsent / setCrashReportingConsent / setFirstTimeState) —
            // the feature's only inbound seam. AppPrivacyConfig (the hosted policy URL) is core/model.
            implementation(projects.core.data)
            implementation(projects.core.model)
            // System back steps to the previous page (androidx.compose.ui.backhandler.BackHandler).
            implementation(libs.ui.backhandler)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(projects.coreBase.store)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.onboarding.generated.resources"
    }
}
