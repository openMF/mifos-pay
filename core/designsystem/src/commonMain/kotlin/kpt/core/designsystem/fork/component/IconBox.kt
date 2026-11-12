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

import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.fork.ForkTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Outlined icon button showing [icon], used for small tappable icon actions. */
@Composable
fun IconBox(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier,
        shape = KptTheme.shapes.medium,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = icon.name,
        )
    }
}

@Composable
@Preview
private fun IconBoxPreview(
    modifier: Modifier = Modifier,
) {
    ForkTheme {
        Surface {
            IconBox(
                icon = ForkIcons.ArrowBack2,
                modifier = modifier,
                onClick = {},
            )
        }
    }
}
