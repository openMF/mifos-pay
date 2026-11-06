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
            implementation(projects.core.data)
            implementation(projects.coreBase.ui)
            implementation(projects.coreBase.datastore)
            // Fork addition: BiometricsSetupScreen reads
            // kpt.core.designsystem.{component,theme}.* (MifosDialogBox/MifosScaffold/MifosTheme).
            // kpt.core.model.client.Client and kpt.core.datastore.UserPreferencesRepository
            // come transitively via core/data's api(core.model)/api(core.datastore) re-export.
            implementation(projects.core.designsystem)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // BiometricsSetupScreen + MifosPasscode use NavigationBackHandler +
            // rememberNavigationEventState from AndroidX's KMP back-handling API.
            implementation(libs.androidx.navigationevent.compose)
            // Fork addition: BiometricErrorMessages/MifosPasscodeViewModel/MifosPasscode/
            // BiometricsSetupScreenViewmodel wrap the mifos-authenticator biometrics + passcode
            // libraries (platformAuthenticator, PasscodeManager, PasscodeScreen et al.).
            implementation(libs.mifos.authenticator.biometrics)
            implementation(libs.mifos.authenticator.passcode)
        }
    }
}
