/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.bottombar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kpt.core.ui.navigation.NavigationItem

/**
 * App-level bottom navigation bar, driven by the generated tab registry.
 *
 * Tabs register by `@FeatureTab` annotation — never by editing a list here, which would make adding
 * a feature a two-place change and silently drop it if one were missed.
 */
@Composable
fun KptBottomBar(
    navigationItems: List<NavigationItem>,
    selectedItem: NavigationItem?,
    onClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    /** Show each tab's name under its icon. See `KptNavigationBarItem.labelRes`. */
    showLabels: Boolean = false,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
) {
    BottomAppBar(
        windowInsets = windowInsets,
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
    ) {
        navigationItems.forEach { navigationItem ->
            KptNavigationBarItem(
                contentDescriptionRes = navigationItem.contentDescriptionRes,
                selectedIcon = navigationItem.selectedIcon,
                unselectedIcon = navigationItem.icon,
                isSelected = selectedItem == navigationItem,
                onClick = { onClick(navigationItem) },
                labelRes = navigationItem.labelRes.takeIf { showLabels },
                modifier = Modifier.testTag(tag = navigationItem.testTag),
            )
        }
    }
}
