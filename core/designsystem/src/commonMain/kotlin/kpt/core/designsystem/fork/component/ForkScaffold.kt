/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.fork.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
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
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import kpt.core.base.designsystem.theme.KptTheme

/** Scaffold with a standard top bar (title and back button) wrapping the screen [content]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForkScaffold(
    backPress: () -> Unit,
    modifier: Modifier = Modifier,
    topBarTitle: String? = null,
    containerColor: Color = Color.Transparent,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: ForkPullToRefreshState = rememberForkPullToRefreshState(),
    snackbarHost: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {},
) {
    Scaffold(
        topBar = {
            if (topBarTitle != null) {
                ForkTopBar(
                    topBarTitle = topBarTitle,
                    backPress = backPress,
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
        content = { paddingValues ->
            val internalPullToRefreshState = rememberPullToRefreshState()
            Box(
                modifier = Modifier.pullToRefresh(
                    state = internalPullToRefreshState,
                    isRefreshing = pullToRefreshState.isRefreshing,
                    onRefresh = pullToRefreshState.onRefresh,
                    enabled = pullToRefreshState.isEnabled,
                ),
            ) {
                content(paddingValues)

                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .padding(paddingValues)
                        .align(Alignment.TopCenter),
                    isRefreshing = pullToRefreshState.isRefreshing,
                    state = internalPullToRefreshState,
                )
            }
        },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    )
}

/** Scaffold that takes caller-supplied top bar, bottom bar, snackbar and floating action button slots. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForkScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    pullToRefreshState: ForkPullToRefreshState = rememberForkPullToRefreshState(),
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = Color.Transparent,
    contentColor: Color = KptTheme.colorScheme.onBackground,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
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
            val internalPullToRefreshState = rememberPullToRefreshState()
            Box(
                modifier = Modifier.pullToRefresh(
                    state = internalPullToRefreshState,
                    isRefreshing = pullToRefreshState.isRefreshing,
                    onRefresh = pullToRefreshState.onRefresh,
                    enabled = pullToRefreshState.isEnabled,
                ),
            ) {
                content(paddingValues)

                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .padding(paddingValues)
                        .align(Alignment.TopCenter),
                    isRefreshing = pullToRefreshState.isRefreshing,
                    state = internalPullToRefreshState,
                )
            }
        },
    )
}

/** Scaffold with a persistent bottom sheet hosting [sheetContent]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForkBottomSheetScaffold(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    sheetShape: Shape = BottomSheetDefaults.ExpandedShape,
    sheetContainerColor: Color = BottomSheetDefaults.ContainerColor,
    sheetContentColor: Color = contentColorFor(sheetContainerColor),
    sheetTonalElevation: Dp = KptTheme.elevation.level0,
    sheetShadowElevation: Dp = BottomSheetDefaults.Elevation,
    sheetDragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    sheetSwipeEnabled: Boolean = true,
    topBar: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    containerColor: Color = KptTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (PaddingValues) -> Unit,
) {
    BottomSheetScaffold(
        sheetContent = sheetContent,
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetMaxWidth = sheetMaxWidth,
        sheetShape = sheetShape,
        sheetContainerColor = sheetContainerColor,
        sheetContentColor = sheetContentColor,
        sheetTonalElevation = sheetTonalElevation,
        sheetShadowElevation = sheetShadowElevation,
        sheetDragHandle = sheetDragHandle,
        sheetSwipeEnabled = sheetSwipeEnabled,
        topBar = topBar,
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        contentColor = contentColor,
        content = content,
    )
}

/**
 * Describes a floating action button for [ForkScaffold].
 *
 * @property onClick Invoked when the button is clicked.
 * @property contentColor Color of the button content.
 * @property content Composable content (icon/label) of the button.
 */
data class FloatingActionButtonContent(
    val onClick: (() -> Unit),
    val contentColor: Color,
    val content: (@Composable () -> Unit),
)

/**
 * State holder for pull-to-refresh behaviour of a scaffold.
 *
 * @property isEnabled Whether pull-to-refresh is enabled.
 * @property isRefreshing Whether a refresh is currently in progress.
 * @property onRefresh Invoked when the user triggers a refresh.
 */
data class ForkPullToRefreshState(
    val isEnabled: Boolean,
    val isRefreshing: Boolean,
    val onRefresh: () -> Unit,
)

/** Creates and remembers a [ForkPullToRefreshState]. */
@Composable
fun rememberForkPullToRefreshState(
    isEnabled: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = { },
): ForkPullToRefreshState = remember(isEnabled, isRefreshing, onRefresh) {
    ForkPullToRefreshState(
        isEnabled = isEnabled,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    )
}
