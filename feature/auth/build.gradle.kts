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
    alias(libs.plugins.mokkery)
}

// namespace auto-derives from baseNamespace + module path via kmp.library.convention.
// buildConfig = true previously requested via `android { buildFeatures { buildConfig = true } }`
// dropped — the module's only BuildConfig-adjacent reference is `PlatformBuildConfig.isDebug`
// (a template.core.base.platform abstraction), which does NOT require the generated Android
// BuildConfig class. The Google Sign-In wiring in androidMain reads WEB_CLIENT_ID from
// resources / string overlays, not BuildConfig. Restore via
// `androidComponents { onVariants { variant -> variant.buildConfigFields.put(...) } }`
// (AGP-9 API on com.android.kotlin.multiplatform.library) if a future dep needs it.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.coreBase.ui)
            implementation(projects.coreBase.platform)
            implementation(projects.coreBase.store)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.jb.kotlin.stdlib)
            implementation(libs.kotlin.reflect)
        }

        androidMain.dependencies {
            // Credentials Manager
            implementation(libs.androidx.credentials)
            // optional - needed for credentials support from play services, for devices running
            // Android 13 and below.
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)

            implementation(libs.play.services.auth)
        }

        commonTest.dependencies {
            implementation(libs.turbine)
        }
    }
}

compose.resources {
    packageOfResClass = "kpt.feature.auth.generated.resources"
}
