/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.payments.generated.resources.Res
import kpt.feature.payments.generated.resources.baseline_content_copy
import kpt.feature.payments.generated.resources.feature_payments_mobile_number
import kpt.feature.payments.generated.resources.feature_payments_receive
import kpt.feature.payments.generated.resources.feature_payments_show_code
import kpt.feature.payments.generated.resources.feature_payments_vpa
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/** Screen for requesting money, showing the client details and a button to show the QR code. */
@Composable
fun RequestScreen(
    showQr: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransferViewModel = koinViewModel(),
) {
    val clipboard = LocalClipboardManager.current
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is TransferEvent.OnShowQR -> showQr.invoke()

            is TransferEvent.OnCopyTextToClipboard -> {
                clipboard.setText(AnnotatedString(event.text))
            }
        }
    }

    RequestScreenContent(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun RequestScreenContent(
    state: TransferState,
    onAction: (TransferAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            modifier = Modifier.padding(top = KptTheme.spacing.sm),
            text = stringResource(Res.string.feature_payments_receive),
            style = KptTheme.typography.titleMedium,
            color = KptTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(text = stringResource(Res.string.feature_payments_vpa))
                    Text(
                        text = state.externalId,
                        style = KptTheme.typography.bodyMedium,
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        onAction(TransferAction.ShowQR)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(),
                ) {
                    Icon(
                        imageVector = ForkIcons.QrCode,
                        contentDescription = stringResource(Res.string.feature_payments_show_code),
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = KptTheme.colorScheme.outlineVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(text = stringResource(Res.string.feature_payments_mobile_number))
                    Text(
                        text = state.mobileNo,
                        style = KptTheme.typography.bodyMedium,
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        onAction(TransferAction.CopyTextToClipboard(state.mobileNo))
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.baseline_content_copy),
                        contentDescription = "Copy Text",
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RequestScreenPreview() {
    ForkTheme {
        RequestScreenContent(
            state = TransferState(
                mobileNo = "iisque",
                externalId = "nonumes",
            ),
            onAction = {},
            modifier = Modifier,
        )
    }
}
