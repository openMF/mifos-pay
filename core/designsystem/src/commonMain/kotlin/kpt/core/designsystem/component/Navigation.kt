/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * Now in Android navigation bar item with icon and label content slots. Wraps Material 3
 * [NavigationBarItem].
 *
 * @param selected Whether this item is selected.
 * @param onClick The callback to be invoked when this item is selected.
 * @param icon The item icon content.
 * @param modifier Modifier to be applied to this item.
 * @param selectedIcon The item icon content when selected.
 * @param enabled controls the enabled state of this item. When `false`, this item will not be
 * clickable and will appear disabled to accessibility services.
 * @param label The item text label content.
 * @param alwaysShowLabel Whether to always show the label for this item. If false, the label will
 * only be shown when this item is selected.
 */
@Composable
fun RowScope.MifosNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    selectedIcon: @Composable () -> Unit = icon,
    label: @Composable (() -> Unit)? = null,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MifosNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = MifosNavigationDefaults.navigationContentColor(),
            selectedTextColor = MifosNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = MifosNavigationDefaults.navigationContentColor(),
            indicatorColor = MifosNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

/**
 * Now in Android navigation bar with content slot. Wraps Material 3 [NavigationBar].
 *
 * @param modifier Modifier to be applied to the navigation bar.
 * @param content Destinations inside the navigation bar. This should contain multiple
 * [NavigationBarItem]s.
 */
@Composable
fun MifosNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = KptTheme.colorScheme.surface,
        contentColor = MifosNavigationDefaults.navigationContentColor(),
        tonalElevation = KptTheme.elevation.level0,
        content = content,
    )
}

/**
 * Now in Android navigation rail item with icon and label content slots. Wraps Material 3
 * [NavigationRailItem].
 *
 * @param selected Whether this item is selected.
 * @param onClick The callback to be invoked when this item is selected.
 * @param icon The item icon content.
 * @param modifier Modifier to be applied to this item.
 * @param selectedIcon The item icon content when selected.
 * @param enabled controls the enabled state of this item. When `false`, this item will not be
 * clickable and will appear disabled to accessibility services.
 * @param label The item text label content.
 * @param alwaysShowLabel Whether to always show the label for this item. If false, the label will
 * only be shown when this item is selected.
 */
@Composable
fun MifosNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true,
    selectedIcon: @Composable () -> Unit = icon,
    label: @Composable (() -> Unit)? = null,
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = if (selected) selectedIcon else icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MifosNavigationDefaults.navigationSelectedItemColor(),
            unselectedIconColor = MifosNavigationDefaults.navigationContentColor(),
            selectedTextColor = MifosNavigationDefaults.navigationSelectedItemColor(),
            unselectedTextColor = MifosNavigationDefaults.navigationContentColor(),
            indicatorColor = MifosNavigationDefaults.navigationIndicatorColor(),
        ),
    )
}

/**
 * Now in Android navigation rail with header and content slots. Wraps Material 3 [NavigationRail].
 *
 * @param modifier Modifier to be applied to the navigation rail.
 * @param header Optional header that may hold a floating action button or a logo.
 * @param content Destinations inside the navigation rail. This should contain multiple
 * [NavigationRailItem]s.
 */
@Composable
fun MifosNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = KptTheme.colorScheme.surface,
        contentColor = MifosNavigationDefaults.navigationContentColor(),
        header = header,
        content = content,
    )
}

/** Preview of the Mifos bottom navigation bar. */
@Preview
@Composable
fun MifosNavigationBarPreview() {
    val items = listOf("Home", "Payments", "Finance", "Profile")
    val icons =
        listOf(
            MifosIcons.Home,
            MifosIcons.Payment,
            MifosIcons.Finance,
            MifosIcons.Profile,
        )
    val selectedIcons =
        listOf(
            MifosIcons.HomeBoarder,
            MifosIcons.Payment,
            MifosIcons.Finance,
            MifosIcons.ProfileBoarder,
        )

    MifosTheme {
        MifosNavigationBar {
            items.forEachIndexed { index, item ->
                MifosNavigationBarItem(
                    selected = index == 0,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                )
            }
        }
    }
}

/** Preview of the Mifos navigation rail. */
@Preview
@Composable
fun MifosNavigationRailPreview() {
    val items = listOf("Home", "Payments", "Finance", "Profile")
    val icons =
        listOf(
            MifosIcons.Home,
            MifosIcons.Payment,
            MifosIcons.Finance,
            MifosIcons.Profile,
        )
    val selectedIcons =
        listOf(
            MifosIcons.HomeBoarder,
            MifosIcons.Payment,
            MifosIcons.Finance,
            MifosIcons.ProfileBoarder,
        )

    MifosTheme {
        MifosNavigationRail {
            items.forEachIndexed { index, item ->
                MifosNavigationRailItem(
                    selected = index == 0,
                    onClick = { },
                    icon = {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                        )
                    },
                    selectedIcon = {
                        Icon(
                            imageVector = selectedIcons[index],
                            contentDescription = item,
                        )
                    },
                    label = { Text(item) },
                )
            }
        }
    }
}

/**
 * Now in Android navigation default values.
 */
object MifosNavigationDefaults {
    /** Color of unselected navigation item content. */
    @Composable
    fun navigationContentColor() = KptTheme.colorScheme.onSurfaceVariant

    /** Color of the selected navigation item content. */
    @Composable
    fun navigationSelectedItemColor() = KptTheme.colorScheme.onPrimaryContainer

    /** Color of the selected navigation item indicator. */
    @Composable
    fun navigationIndicatorColor() = KptTheme.colorScheme.primaryContainer
}
