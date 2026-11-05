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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * One item in [KptNavigationRail].
 */
@Composable
fun ColumnScope.KptNavigationRailItem(
    contentDescriptionRes: StringResource,
    selectedIconRes: ImageVector,
    unselectedIconRes: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** The item's NAME. Null keeps the selection-pill look. See `KptNavigationBarItem.labelRes`. */
    labelRes: StringResource? = null,
) {
    NavigationRailItem(
        icon = {
            Icon(
                imageVector = if (isSelected) selectedIconRes else unselectedIconRes,
                contentDescription = stringResource(contentDescriptionRes),
            )
        },
        label = if (labelRes != null) {
            {
                Text(
                    text = stringResource(labelRes),
                    style = KptTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            {
                Spacer(
                    modifier = Modifier
                        .height(4.dp)
                        .width(10.dp)
                        .background(
                            color = KptTheme.colorScheme.primary,
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
            }
        },
        selected = isSelected,
        alwaysShowLabel = labelRes != null || isSelected,
        onClick = onClick,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = KptTheme.colorScheme.primary,
            unselectedIconColor = KptTheme.colorScheme.primary,
        ),
        modifier = modifier,
    )
}
