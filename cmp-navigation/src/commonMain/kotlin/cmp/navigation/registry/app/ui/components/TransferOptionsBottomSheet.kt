/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmp.navigation.generated.resources.upi_and_autopay
import cmp.navigation.registry.app.TransferOptionsViewModel
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.theme.fork.MifosTheme
import mifos_pay.feature.payments.generated.resources.Res
import mifos_pay.feature.payments.generated.resources.feature_payments_inter_bank_transfer_description
import mifos_pay.feature.payments.generated.resources.feature_payments_inter_bank_transfer_title
import mifos_pay.feature.payments.generated.resources.feature_payments_intra_bank_transfer_description
import mifos_pay.feature.payments.generated.resources.feature_payments_intra_bank_transfer_title
import mifos_pay.feature.payments.generated.resources.feature_payments_transfer_options_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import cmp.navigation.generated.resources.Res as SharedRes

@Composable
fun TransferOptionsBottomSheet(
    onIntraBankTransferClick: () -> Unit,
    onInterBankTransferClick: () -> Unit,
    onUpiSendMoney: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    transferOptionsViewModel: TransferOptionsViewModel = koinViewModel(),
) {
    val state by transferOptionsViewModel.stateFlow.collectAsStateWithLifecycle()

    MifosBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.feature_payments_transfer_options_title),
                    modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
                    style = KptTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                // Intra-Bank Transfer Option
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.feature_payments_intra_bank_transfer_title),
                            style = KptTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = KptTheme.colorScheme.onSurface,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(Res.string.feature_payments_intra_bank_transfer_description),
                            style = KptTheme.typography.bodySmall,
                            color = KptTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onIntraBankTransferClick()
                        },
                )

                if (state.isInterTransferOptionEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
                        color = KptTheme.colorScheme.outlineVariant,
                    )
                    // Inter-Bank Transfer Option
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(Res.string.feature_payments_inter_bank_transfer_title),
                                style = KptTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = KptTheme.colorScheme.onSurface,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(Res.string.feature_payments_inter_bank_transfer_description),
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onInterBankTransferClick()
                            },
                    )
                }

                if (state.isUpiEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
                        color = KptTheme.colorScheme.outlineVariant,
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(SharedRes.string.upi_and_autopay),
                                style = KptTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = KptTheme.colorScheme.onSurface,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(SharedRes.string.upi_and_autopay),
                                style = KptTheme.typography.bodySmall,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpiSendMoney()
                            },
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun TransferOptionsBottomSheetPreview() {
    MifosTheme {
        TransferOptionsBottomSheet(
            onIntraBankTransferClick = {},
            onInterBankTransferClick = {},
            onUpiSendMoney = {},
            onDismiss = {},
        )
    }
}
