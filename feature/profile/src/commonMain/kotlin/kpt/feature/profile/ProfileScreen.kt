/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kpt.core.ui.scaffold.KptScaffold

/**
 * The profile tab shell — a framework-owned [KptScaffold] that carries ZERO demo/content imports and
 * simply renders whatever opaque [profileBody] the caller supplies. The fork owns that body via
 * `cmp-navigation`'s `ForkBackboneRegistry.profileBody` (default: [kpt.feature.profile.demo.ProfileDemoBody]),
 * mirroring the `home` shell/seam split — so a template sync full-copies this shell while the fork's
 * profile inner content survives. (WS01 base-feature seam, epic AC7.)
 */
@Composable
internal fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileBody: @Composable () -> Unit = {},
) {
    KptScaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.Profile.SCREEN),
    ) {
        profileBody()
    }
}
