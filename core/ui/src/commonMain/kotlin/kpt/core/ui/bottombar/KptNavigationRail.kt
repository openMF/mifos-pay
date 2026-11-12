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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kpt.core.ui.navigation.NavigationItem

/**
 * The side rail used instead of a bottom bar on wide windows — same tab source, different surface.
 */
@Composable
fun KptNavigationRail(
    navigationItems: List<NavigationItem>,
    selectedItem: NavigationItem?,
    onClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Show each item's name beside its icon. Threaded from the same flag the bottom bar reads, so
     * labels do not disappear when the window widens and the bar becomes a rail — the rail IS the
     * bar on a large screen.
     */
    showLabels: Boolean = false,
    windowInsets: WindowInsets = NavigationRailDefaults.windowInsets,
) {
    Surface(
        color = Color.White,
        contentColor = Color.Unspecified,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .windowInsetsPadding(insets = windowInsets)
                .widthIn(min = 80.dp)
                .padding(vertical = 4.dp)
                .selectableGroup()
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            navigationItems.forEach { navigationItem ->
                KptNavigationRailItem(
                    contentDescriptionRes = navigationItem.contentDescriptionRes,
                    selectedIconRes = navigationItem.selectedIcon,
                    unselectedIconRes = navigationItem.icon,
                    isSelected = navigationItem == selectedItem,
                    onClick = { onClick(navigationItem) },
                    labelRes = navigationItem.labelRes.takeIf { showLabels },
                    modifier = Modifier.testTag(tag = navigationItem.testTag),
                )
            }
        }
    }
}
