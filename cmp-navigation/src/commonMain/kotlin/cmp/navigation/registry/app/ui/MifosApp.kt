/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.wallet_not_connected
import cmp.navigation.registry.app.navigation.MifosNavHost
import cmp.navigation.registry.app.utils.TopLevelDestination
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.data.util.NetworkMonitor
import kpt.core.data.util.TimeZoneMonitor
import kpt.core.designsystem.component.IconBox
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosNavigationBar
import kpt.core.designsystem.component.MifosNavigationBarItem
import kpt.core.designsystem.component.MifosNavigationRail
import kpt.core.designsystem.component.MifosNavigationRailItem
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.LocalGradientColors
import kpt.feature.mpay.qr.scan.navigation.navigateToScanQr
import kpt.feature.profile.navigation.navigateToEditProfile
import kpt.feature.settings.navigation.navigateToSettings
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MifosApp(
    networkMonitor: NetworkMonitor,
    timeZoneMonitor: TimeZoneMonitor,
    onClickLogout: () -> Unit,
    handleAppLocale: (locale: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosGradientBackground(
        gradientColors = LocalGradientColors.current,
    ) {
        val appState = rememberMifosAppState(
            networkMonitor = networkMonitor,
            timeZoneMonitor = timeZoneMonitor,
        )

        val snackbarHostState = remember { SnackbarHostState() }
        val destination = appState.currentTopLevelDestination

        val isOffline by appState.isOffline.collectAsStateWithLifecycle()

        // If user is not connected to the internet show a snack bar to inform them.
        val notConnectedMessage = stringResource(Res.string.wallet_not_connected)
        LaunchedEffect(isOffline) {
            if (isOffline) {
                snackbarHostState.showSnackbar(
                    message = notConnectedMessage,
                    duration = Indefinite,
                )
            }
        }

        Scaffold(
            modifier = Modifier,
            containerColor = Color.Transparent,
            contentColor = KptTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (appState.shouldShowBottomBar && destination != null) {
                    MifosBottomBar(
                        destinations = appState.topLevelDestinations,
                        destinationsWithUnreadResources = emptySet(),
                        onNavigateToDestination = appState::navigateToTopLevelDestination,
                        currentDestination = appState.currentDestination,
                        onScanQrClick = { appState.navController.navigateToScanQr() },
                        modifier = Modifier.testTag("NiaBottomBar"),
                    )
                }
            },
        ) { padding ->
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal,
                        ),
                    ),
            ) {
                if (appState.shouldShowNavRail && destination != null) {
                    MifosNavRail(
                        destinations = appState.topLevelDestinations,
                        destinationsWithUnreadResources = emptySet(),
                        onNavigateToDestination = appState::navigateToTopLevelDestination,
                        currentDestination = appState.currentDestination,
                        onScanQrClick = { appState.navController.navigateToScanQr() },
                        modifier = Modifier
                            .testTag("NiaNavRail")
                            .safeDrawingPadding(),
                    )
                }

                Column(Modifier.fillMaxSize()) {
                    // Show the top app bar on top level destinations.
                    if (destination != null) {
                        MifosAppBar(
                            title = stringResource(destination.titleText),
                            onClickLogout = onClickLogout,
                            onNavigateToFaq = {},
                            onNavigateToSettings = {
                                appState.navController.navigateToSettings()
                            },
                            onNavigateToEditProfile = {
                                appState.navController.navigateToEditProfile()
                            },
//                            onNavigateToNotification = {
//                                appState.navController.navigateToNotification()
//                            },
                            destination = destination,
                        )
                    }

                    MifosNavHost(
                        appState = appState,
                        handleAppLocale = handleAppLocale,
                        onClickLogout = onClickLogout,
                    )
                }
            }
        }
    }
}

// TODO Notification feature is not using self api
//  https://venus.mifos.community/fineract-provider/api/v1/notifications/?isRead=true
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MifosAppBar(
    title: String,
    onClickLogout: () -> Unit,
    onNavigateToFaq: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
//    onNavigateToNotification: () -> Unit,
    destination: TopLevelDestination?,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = title) },
        actions = {
            Box {
                when (destination) {
                    TopLevelDestination.HOME -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                        ) {
//                            IconBox(
//                                icon = MifosIcons.OutlinedNotifications,
//                                onClick = onNavigateToNotification,
//                            )

                            IconBox(
                                icon = MifosIcons.SettingsOutlined,
                                onClick = onNavigateToSettings,
                            )
                        }
                    }

//                    TopLevelDestination.PROFILE -> {
//                        IconBox(
//                            icon = MifosIcons.Edit2,
//                            onClick = onNavigateToEditProfile,
//                        )
//                    }

                    else -> {}
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier.testTag("mifosTopAppBar"),
    )
}

@Composable
private fun MifosNavRail(
    destinations: List<TopLevelDestination>,
    destinationsWithUnreadResources: Set<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    onScanQrClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosNavigationRail(
        modifier = modifier,
        header = {
            FloatingActionButton(
                onClick = onScanQrClick,
                containerColor = KptTheme.colorScheme.primary,
                contentColor = KptTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = MifosIcons.Scan,
                    contentDescription = "Scan QR Code",
                )
            }
        },
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            val hasUnread = destinationsWithUnreadResources.contains(destination)
            MifosNavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.unselectedIcon,
                        contentDescription = null,
                    )
                },
                modifier = if (hasUnread) Modifier.notificationDot() else Modifier,
                selectedIcon = {
                    Icon(
                        imageVector = destination.selectedIcon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.iconText)) },
            )
        }
    }
}

@Composable
private fun MifosBottomBar(
    destinations: List<TopLevelDestination>,
    destinationsWithUnreadResources: Set<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    onScanQrClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        MifosNavigationBar {
            // First 2 destinations on the left
            destinations.take(2).forEach { destination ->
                val hasUnread = destinationsWithUnreadResources.contains(destination)
                val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
                MifosNavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    modifier = if (hasUnread) Modifier.notificationDot() else Modifier,
                    selectedIcon = {
                        Icon(
                            imageVector = destination.selectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.iconText)) },
                )
            }

            // Spacer for center FAB
            Spacer(Modifier.weight(1f))

            // Last 2 destinations on the right
            destinations.drop(2).forEach { destination ->
                val hasUnread = destinationsWithUnreadResources.contains(destination)
                val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
                MifosNavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    modifier = if (hasUnread) Modifier.notificationDot() else Modifier,
                    selectedIcon = {
                        Icon(
                            imageVector = destination.selectedIcon,
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.iconText)) },
                )
            }
        }

        // Center QR Scan FAB - inside navigation bar
        FloatingActionButton(
            onClick = onScanQrClick,
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            containerColor = KptTheme.colorScheme.primary,
            contentColor = KptTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp,
            ),
        ) {
            Icon(
                imageVector = MifosIcons.Scan,
                contentDescription = "Scan QR Code",
            )
        }
    }
}

private fun Modifier.notificationDot(): Modifier =
    composed {
        val tertiaryColor = KptTheme.colorScheme.tertiary
        drawWithContent {
            drawContent()
            drawCircle(
                tertiaryColor,
                radius = 5.dp.toPx(),
                // This is based on the dimensions of the NavigationBar's "indicator pill";
                // however, its parameters are private, so we must depend on them implicitly
                // (NavigationBarTokens.ActiveIndicatorWidth = 64.dp)
                center =
                center +
                    Offset(
                        64.dp.toPx() * .45f,
                        32.dp.toPx() * -.45f - 6.dp.toPx(),
                    ),
            )
        }
    }

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
