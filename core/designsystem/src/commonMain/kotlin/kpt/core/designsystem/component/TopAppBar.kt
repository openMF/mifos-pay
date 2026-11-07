/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package kpt.core.designsystem.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Top app bar with a title and optional navigation icon and actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MifosTopAppBar(
    titleRes: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    navigationIconContentDescription: String? = null,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    colors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
    onNavigationClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = titleRes) },
        navigationIcon = {
            navigationIcon?.let {
                IconButton(onClick = onNavigationClick!!) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconContentDescription,
                        tint = KptTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        actions = {
            actionIcon?.let {
                IconButton(onClick = onActionClick!!) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionIconContentDescription,
                        tint = KptTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        colors = colors,
        modifier = modifier.testTag("mifosTopAppBar"),
    )
}

/** Top app bar with a title and a back navigation button when [onNavigationClick] is provided. */
@Composable
fun MifosNavigationTopAppBar(
    titleRes: String,
    onNavigationClick: (() -> Unit)?,
) {
    MifosTopAppBar(
        titleRes = titleRes,
        navigationIcon = MifosIcons.Back,
        navigationIconContentDescription = titleRes,
        colors =
        TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
        ),
        onNavigationClick = onNavigationClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun MifosTopAppBarPreview() {
    MifosTheme {
        MifosTopAppBar(
            titleRes = "Demo Preview",
            navigationIcon = MifosIcons.Search,
            navigationIconContentDescription = "Navigation icon",
            actionIcon = MifosIcons.MoreVert,
            actionIconContentDescription = "Action icon",
        )
    }
}
