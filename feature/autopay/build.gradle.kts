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

            implementation(projects.core.common)
            implementation(projects.core.ui)
            // kpt.core.model.network.entity.Page (needed by AutoPayHistoryViewModel) comes
            // transitively via core/data's api(core.model) re-export — features never depend on
            // core/network directly.
            // Template idiom: SubmitHandler (core-base/store) for one-shot writes.
            implementation(projects.coreBase.store)
        }
    }
}

compose.resources {
    packageOfResClass = "kpt.feature.autopay.generated.resources"
}
