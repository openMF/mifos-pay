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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_error_occurred
import kpt.core.ui.generated.resources.core_ui_retry
import kpt.core.ui.generated.resources.core_ui_try_again
import org.jetbrains.compose.resources.stringResource
/** Error state with title, subtitle and a retry button wired to [onClickRetry]. */
@Composable
fun ErrorScreenContent(
    modifier: Modifier = Modifier,
    title: String = stringResource(resource = Res.string.core_ui_error_occurred),
    subTitle: String = stringResource(resource = Res.string.core_ui_try_again),
    onClickRetry: () -> Unit = { },
) {
    Column(
        modifier = modifier
            .padding(KptTheme.spacing.md)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("mifos:empty"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
        )

        ForkButton(
            modifier = Modifier
                .width(150.dp)
                .padding(top = KptTheme.spacing.md),
            onClick = onClickRetry,
        ) {
            Text(text = stringResource(resource = Res.string.core_ui_retry))
        }
    }
}

/** Preview of [ErrorScreenContent]. */
@DevicePreviews
@Composable
fun ErrorContentScreenDrawableImagePreview() {
    ForkTheme {
        ErrorScreenContent(
            modifier = Modifier,
            title = "Error Occurred!",
            subTitle = "Please check your connection or try again",
            onClickRetry = { },
        )
    }
}
