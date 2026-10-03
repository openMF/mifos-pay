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
        commonTest.dependencies {
            implementation(libs.turbine)
        }

        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Template idiom: SubmitHandler (core-base/store) for one-shot writes.
            implementation(projects.coreBase.store)

            implementation(libs.filekit.core)
            implementation(libs.coil.kt.compose)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)
        }
    }
}