/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.KptTopAppBar
import kpt.core.base.designsystem.core.TopAppBarAction
import kpt.core.base.designsystem.theme.KptTheme

/**
 * App screen scaffold for a screen with a BACK affordance — the nav icon is always shown and
 * [onNavigationIconClick] is required.
 *
 * Wraps the top bar, pull-to-refresh, snackbar host and FAB so a feature screen declares content
 * and nothing else. Use this overload for a detail/child screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptScaffold(
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = KptTheme.colorScheme.background,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
    snackbarHost: @Composable () -> Unit = {},
    actions: List<TopAppBarAction> = emptyList(),
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
            }
        },
        floatingActionButton = {
            floatingActionButtonContent?.let { content ->
                FloatingActionButton(
                    onClick = content.onClick,
                    contentColor = content.contentColor,
                    content = content.content,
                )
            }
        },
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentWindowInsets = WindowInsets(0.dp),
        content = { paddingValues ->
            val internalPullToRefreshState = rememberPullToRefreshState()
            Box(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .consumeWindowInsets(paddingValues = paddingValues)
                    .imePadding()
                    .navigationBarsPadding(),
            ) {
                Box(
                    modifier = Modifier
                        .windowInsetsPadding(insets = contentWindowInsets)
                        .pullToRefresh(
                            state = internalPullToRefreshState,
                            isRefreshing = pullToRefreshState.isRefreshing,
                            onRefresh = pullToRefreshState.onRefresh,
                            enabled = pullToRefreshState.isEnabled,
                        ),
                ) {
                    content()

                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter),
                        isRefreshing = pullToRefreshState.isRefreshing,
                        state = internalPullToRefreshState,
                    )
                }
            }
        },
        modifier = modifier,
    )
}

/**
 * App screen scaffold whose back affordance is CONDITIONAL — pass [showNavigationIcon] when the same
 * screen is reachable both as a tab root and as a pushed destination.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptScaffold(
    showNavigationIcon: Boolean,
    modifier: Modifier = Modifier,
    onNavigationIconClick: () -> Unit = {},
    title: String? = null,
    containerColor: Color = KptTheme.colorScheme.background,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
    snackbarHost: @Composable () -> Unit = {},
    actions: List<TopAppBarAction> = emptyList(),
    content: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = {
            if (title != null) {
                KptTopAppBar(
                    title = title,
                    showNavigationIcon = showNavigationIcon,
                    onNavigationIconClick = onNavigationIconClick,
                    actions = actions,
                )
            }
        },
        floatingActionButton = {
            floatingActionButtonContent?.let { content ->
                FloatingActionButton(
                    onClick = content.onClick,
                    contentColor = content.contentColor,
                    content = content.content,
                )
            }
        },
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentWindowInsets = WindowInsets(0.dp),
        content = { paddingValues ->
            val internalPullToRefreshState = rememberPullToRefreshState()
            Box(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .consumeWindowInsets(paddingValues = paddingValues)
                    .imePadding()
                    .navigationBarsPadding(),
            ) {
                Box(
                    modifier = Modifier
                        .windowInsetsPadding(insets = contentWindowInsets)
                        .pullToRefresh(
                            state = internalPullToRefreshState,
                            isRefreshing = pullToRefreshState.isRefreshing,
                            onRefresh = pullToRefreshState.onRefresh,
                            enabled = pullToRefreshState.isEnabled,
                        ),
                ) {
                    content()

                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter),
                        isRefreshing = pullToRefreshState.isRefreshing,
                        state = internalPullToRefreshState,
                    )
                }
            }
        },
        modifier = modifier,
    )
}

/**
 * Fully slot-based scaffold: the caller supplies [topBar], [bottomBar] and the FAB composables.
 *
 * The escape hatch for a screen whose chrome does not fit the two title-driven overloads above;
 * prefer those, since they keep top-bar behaviour consistent across screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KptScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = KptTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box(modifier = Modifier.navigationBarsPadding()) {
                floatingActionButton()
            }
        },
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .consumeWindowInsets(paddingValues = paddingValues)
                    .imePadding()
                    .navigationBarsPadding(),
            ) {
                val internalPullToRefreshState = rememberPullToRefreshState()
                Box(
                    modifier = Modifier
                        .windowInsetsPadding(insets = contentWindowInsets)
                        .pullToRefresh(
                            state = internalPullToRefreshState,
                            isRefreshing = pullToRefreshState.isRefreshing,
                            onRefresh = pullToRefreshState.onRefresh,
                            enabled = pullToRefreshState.isEnabled,
                        ),
                ) {
                    content()

                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = pullToRefreshState.isRefreshing,
                        state = internalPullToRefreshState,
                    )
                }
            }
        },
    )
}

/**
 * Declarative FAB spec — icon, description and click — so a caller configures the FAB without
 * passing a composable slot.
 */
data class FloatingActionButtonContent(
    /** What the FAB does. */
    val onClick: (() -> Unit),
    /** Colour for the icon and any text inside. */
    val contentColor: Color,
    /** The FAB's content — usually an icon. */
    val content: (@Composable () -> Unit),
)
