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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * One item in [KptBottomBar]: icon, label and selected state.
 */
@Composable
fun RowScope.KptNavigationBarItem(
    contentDescriptionRes: StringResource,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * The tab's NAME, rendered as the item's label and shown on every tab. Null keeps the
     * selection-PILL look below, which is what this component shipped with.
     *
     * `NavigationItem.labelRes` has always existed and every tab populates it, but nothing rendered
     * it: the `label` slot held a `Spacer` drawn as a rounded bar, so the field reached the UI only
     * as a content description and a bar of monochrome glyphs had nothing to read. Labelled tabs are
     * the Material 3 default and what most apps want; the pill stays the default here so no existing
     * fork's bar changes under it.
     */
    labelRes: StringResource? = null,
) {
    NavigationBarItem(
        icon = {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = stringResource(contentDescriptionRes),
                // Inherit LocalContentColor, which NavigationBarItem drives from the
                // selected/unselected icon colors set below (KptTheme.colorScheme.primary), so the
                // monochrome tab icons follow the theme in BOTH light and dark. `Color.Unspecified`
                // forced the vector's baked-in black, which vanished on the dark navigation bar and
                // made the selected/unselectedIconColor config below dead.
                tint = LocalContentColor.current,
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
                            shape = RoundedCornerShape(7.dp),
                        )
                        .animateContentSize(),
                )
            }
        },
        // `selected` drives the LABEL's colour and the indicator. It was hardcoded false, which the
        // pill variant can afford because it has no text to colour and a transparent indicator.
        // A labelled item needs it, or every label renders unselected-grey.
        selected = isSelected,
        // Labelled tabs all show their name — that is the point of labelling them. The pill variant
        // keeps the original selected-only behaviour.
        alwaysShowLabel = labelRes != null || isSelected,
        onClick = onClick,
        modifier = modifier,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = KptTheme.colorScheme.primary,
            unselectedIconColor = KptTheme.colorScheme.primary,
            selectedTextColor = KptTheme.colorScheme.primary,
            unselectedTextColor = KptTheme.colorScheme.secondary,
            // A labelled item marks selection with a tinted capsule behind the icon — the Material 3
            // affordance, and what carries selection once the pill is gone. The pill variant keeps
            // its transparent indicator, since the pill IS its selection marker.
            indicatorColor = if (labelRes != null) {
                KptTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                Color.Transparent
            },
        ),
    )
}
