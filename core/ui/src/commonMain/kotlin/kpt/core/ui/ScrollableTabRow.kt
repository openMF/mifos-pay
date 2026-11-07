/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosTab
import kpt.core.ui.utility.TabContent
/** Scrollable tab row linked to a pager, one tab per [TabContent]. */
@Suppress("MultipleEmitters")
@Composable
fun MifosScrollableTabRow(
    tabContents: List<TabContent>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    selectedContentColor: Color = KptTheme.colorScheme.primary,
    unselectedContentColor: Color = KptTheme.colorScheme.surfaceContainerLow,
    edgePadding: Dp = KptTheme.spacing.sm,
) {
    val scope = rememberCoroutineScope()

    ScrollableTabRow(
        modifier = modifier,
        containerColor = containerColor,
        selectedTabIndex = pagerState.currentPage,
        edgePadding = edgePadding,
        indicator = {},
        divider = {},
    ) {
        tabContents.forEachIndexed { index, currentTab ->
            MifosTab(
                text = currentTab.tabName,
                selected = pagerState.currentPage == index,
                selectedColor = selectedContentColor,
                unselectedColor = unselectedContentColor,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }

    HorizontalPager(
        state = pagerState,
    ) {
        tabContents[it].content.invoke()
    }
}
