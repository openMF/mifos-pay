/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)

            // Fork addition: ProfileScreen/EditProfileViewModel etc. read org.mifospay.core.{common,
            // data,designsystem,ui}.* (ScreenState, ClientRepository, ForkScaffold/ForkIcons,
            // BaseViewModel) and kpt.core.base.{store,ui}.* (SubmitState/submitHandler, ScreenContent).
            implementation(projects.core.common)
            implementation(projects.core.data)
            implementation(projects.core.designsystem)
            implementation(projects.core.ui)
            implementation(projects.coreBase.store)
            implementation(projects.coreBase.ui)

            // Fork addition: ProfileImage renders the client's avatar via Coil3; EditProfileViewModel
            // picks + reads a new avatar file via FileKit.
            implementation(libs.coil.kt.compose)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.profile.generated.resources"
    }
}
