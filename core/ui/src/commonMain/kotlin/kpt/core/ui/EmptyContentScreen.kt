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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.artwork
import kpt.core.ui.generated.resources.core_ui_money_in
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
@Composable
fun EmptyContentScreen(
    title: String,
    subTitle: String,
    imageContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(KptTheme.spacing.md)
                .fillMaxSize()
                .testTag("mifos:empty"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            imageContent()

            Spacer(modifier = Modifier.height(KptTheme.spacing.xxl))

            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = KptTheme.spacing.lg, end = KptTheme.spacing.lg),
                textAlign = TextAlign.Center,
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

            Text(
                text = subTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = KptTheme.spacing.lg, end = KptTheme.spacing.lg),
                textAlign = TextAlign.Center,
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun EmptyContentScreen(
    title: String,
    subTitle: String,
    btnText: String,
    btnIcon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageContent: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(KptTheme.spacing.md)
                .fillMaxSize()
                .testTag("mifos:empty"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            imageContent()

            Spacer(modifier = Modifier.height(KptTheme.spacing.xxl))

            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = KptTheme.spacing.lg, end = KptTheme.spacing.lg),
                textAlign = TextAlign.Center,
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

            Text(
                text = subTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = KptTheme.spacing.lg, end = KptTheme.spacing.lg),
                textAlign = TextAlign.Center,
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.xxl))

            MifosButton(
                text = {
                    Text(text = btnText)
                },
                leadingIcon = {
                    Icon(
                        imageVector = btnIcon,
                        contentDescription = "BtnIcon",
                    )
                },
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun EmptyContentScreen(
    title: String,
    subTitle: String,
    iconDrawable: DrawableResource,
    modifier: Modifier = Modifier,
    iconTint: Color = KptTheme.colorScheme.surfaceTint,
) {
    EmptyContentScreen(
        title = title,
        subTitle = subTitle,
        imageContent = {
            Image(
                modifier = Modifier.size(64.dp),
                painter = painterResource(iconDrawable),
                colorFilter = if (iconTint != Color.Unspecified) ColorFilter.tint(iconTint) else null,
                contentDescription = null,
            )
        },
        modifier = modifier,
    )
}

@Composable
fun EmptyContentScreen(
    title: String,
    subTitle: String,
    modifier: Modifier = Modifier,
    drawableResource: DrawableResource = Res.drawable.artwork,
    iconTint: Color = KptTheme.colorScheme.surfaceTint,
) {
    EmptyContentScreen(
        title = title,
        subTitle = subTitle,
        imageContent = {
            Image(
                modifier = Modifier.size(200.dp),
                painter = painterResource(drawableResource),
                colorFilter = if (iconTint != Color.Unspecified) ColorFilter.tint(iconTint) else null,
                contentDescription = null,
            )
        },
        modifier = modifier,
    )
}

@Composable
fun EmptyContentScreen(
    title: String,
    subTitle: String,
    btnText: String,
    btnIcon: ImageVector,
    modifier: Modifier = Modifier,
    icon: ImageVector = MifosIcons.Info,
    iconTint: Color = KptTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    EmptyContentScreen(
        title = title,
        subTitle = subTitle,
        imageContent = {
            Icon(
                modifier = Modifier.size(64.dp),
                imageVector = icon,
                tint = iconTint,
                contentDescription = null,
            )
        },
        btnText = btnText,
        btnIcon = btnIcon,
        onClick = onClick,
        modifier = modifier,
    )
}

@DevicePreviews
@Composable
fun EmptyContentScreenDrawableImagePreview() {
    MifosTheme {
        EmptyContentScreen(
            title = "No data found",
            subTitle = "Please check you connection or try again",
            iconDrawable = Res.drawable.core_ui_money_in,
            modifier = Modifier,
            iconTint = KptTheme.colorScheme.primary,
        )
    }
}

@DevicePreviews
@Composable
fun EmptyContentScreenImageVectorPreview() {
    MifosTheme {
        EmptyContentScreen(
            title = "No data found",
            subTitle = "Please check you connection or try again",
            modifier = Modifier,
            iconTint = KptTheme.colorScheme.primary,
        )
    }
}
