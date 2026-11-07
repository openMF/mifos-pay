/*
 * Copyright 2024 Mifos Initiative
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
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Template idiom: ScreenDataStream (core-base/store) + ScreenContent
            // (core-base/ui) so the screen consumes the store's pre-decided
            // ScreenState directly instead of a fork-ScreenState fold.
            implementation(projects.coreBase.store)
            implementation(projects.coreBase.ui)
            // Fork addition: AddEditSIViewModel/SIDetailViewModel read kpt.core.common
            // (ScreenState/CurrencyFormatter/DateHelper/getSerialized/setSerialized).
            implementation(projects.core.common)
        }
    }
}

compose.resources {
    packageOfResClass = "kpt.feature.standing.instruction.generated.resources"
}
