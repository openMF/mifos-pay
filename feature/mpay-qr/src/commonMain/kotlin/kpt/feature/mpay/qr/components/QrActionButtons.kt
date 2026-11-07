/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkOutlinedButton
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.feature.mpay.qr.generated.resources.Res
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_download_qr
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_share_qr
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * Row of outlined buttons for QR actions:
 * - "Share" with Share icon
 * - "Download" with Download icon
 * - Equal width (weight 1f each)
 */
@Composable
internal fun QrActionButtons(
    onShareClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        ForkOutlinedButton(
            onClick = onShareClick,
            modifier = Modifier.weight(1f),
            shape = KptTheme.shapes.medium,
        ) {
            Icon(
                imageVector = ForkIcons.Share,
                contentDescription = null,
                modifier = Modifier.size(KptTheme.spacing.md),
            )
            Spacer(modifier = Modifier.width(KptTheme.spacing.xs))
            Text(
                text = stringResource(Res.string.feature_mpay_qr_share_qr),
                style = KptTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        ForkOutlinedButton(
            onClick = onDownloadClick,
            modifier = Modifier.weight(1f),
            shape = KptTheme.shapes.medium,
        ) {
            Icon(
                imageVector = ForkIcons.Download,
                contentDescription = null,
                modifier = Modifier.size(KptTheme.spacing.md),
            )
            Spacer(modifier = Modifier.width(KptTheme.spacing.xs))
            Text(
                text = stringResource(Res.string.feature_mpay_qr_download_qr),
                style = KptTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun QrActionButtonsPreview() {
    KptMaterialTheme {
        QrActionButtons(
            onShareClick = {},
            onDownloadClick = {},
        )
    }
}
