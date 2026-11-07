/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.receipt

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.common.DateHelper
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.model.savingsaccount.TransferDetail
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.fork.utils.EventsEffect
import kpt.core.ui.fork.utils.ShareUtils
import kpt.feature.receipt.generated.resources.Res
import kpt.feature.receipt.generated.resources.feature_receipt_credited_by
import kpt.feature.receipt.generated.resources.feature_receipt_paid_to
import kpt.feature.receipt.generated.resources.feature_receipt_receipt
import kpt.feature.receipt.generated.resources.feature_receipt_reference_id
import kpt.feature.receipt.generated.resources.feature_receipt_share_receipt
import kpt.feature.receipt.generated.resources.feature_receipt_transaction_date
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun ReceiptScreenRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiptViewModel = koinViewModel(),
) {
    val receiptUiState by viewModel.receiptUiState.collectAsState()
    val scope = rememberCoroutineScope()

    EventsEffect(viewModel) { event ->
        when (event) {
            ReceiptEvent.OnNavigateBack -> onBackClick()
            ReceiptEvent.OnShareReceipt -> {
                val text = (receiptUiState as? ReceiptUiState.Success)?.let {
                    formatReceiptShareText(it.transferDetail)
                }
                if (text != null) {
                    scope.launch { ShareUtils.shareText(text) }
                }
            }
        }
    }

    ReceiptScreen(
        uiState = receiptUiState,
        onBackClick = { viewModel.trySendAction(ReceiptAction.NavigateBack) },
        onShareClick = { viewModel.trySendAction(ReceiptAction.ShareReceipt) },
        modifier = modifier,
    )
}

@Composable
@VisibleForTesting
internal fun ReceiptScreen(
    uiState: ReceiptUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit = {},
) {
    ForkScaffold(
        backPress = onBackClick,
        topBarTitle = stringResource(Res.string.feature_receipt_receipt),
        actions = {
            if (uiState is ReceiptUiState.Success) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = ForkIcons.OutlinedShare,
                        contentDescription = stringResource(Res.string.feature_receipt_share_receipt),
                    )
                }
            }
        },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
        ) {
            when (uiState) {
                is ReceiptUiState.Loading -> ForkProgressIndicator()

                is ReceiptUiState.Error -> {
                    EmptyContentScreen(
                        title = "Oops!",
                        subTitle = stringResource(uiState.messageRes),
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is ReceiptUiState.ErrorMessage -> {
                    EmptyContentScreen(
                        title = "Oops!",
                        subTitle = uiState.message,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is ReceiptUiState.Success -> {
                    ReceiptCard(detail = uiState.transferDetail)
                }
            }
        }
    }
}

/** Renders fully offline once [detail] is cached — same guarantee `TransactionDetailScreen` has. */
@Composable
private fun ReceiptCard(detail: TransferDetail, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.feature_receipt_reference_id),
                    style = KptTheme.typography.labelLarge,
                )
                Text(text = detail.id.toString())
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.feature_receipt_transaction_date),
                    style = KptTheme.typography.labelLarge,
                )
                Text(text = DateHelper.getDateAsString(detail.transferDate))
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            Text(
                text = stringResource(Res.string.feature_receipt_paid_to),
                style = KptTheme.typography.labelLarge,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KptTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                ListItem(
                    headlineContent = { Text(text = detail.toClient.displayName) },
                    supportingContent = { Text(text = detail.toAccount.accountNo) },
                    leadingContent = { AvatarBox(name = detail.toClient.displayName) },
                    trailingContent = {
                        Text(
                            text = CurrencyFormatter.format(
                                detail.transferAmount,
                                currencyCode = detail.currency.code,
                                maximumFractionDigits = null,
                            ),
                            style = KptTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }

            HorizontalDivider(modifier = Modifier.fillMaxWidth())

            Text(
                text = stringResource(Res.string.feature_receipt_credited_by),
                style = KptTheme.typography.labelLarge,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KptTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                ListItem(
                    headlineContent = { Text(text = detail.fromClient.displayName) },
                    supportingContent = { Text(text = detail.fromAccount.accountNo) },
                    leadingContent = { AvatarBox(name = detail.fromClient.displayName) },
                    trailingContent = {
                        Text(
                            text = CurrencyFormatter.format(
                                detail.transferAmount,
                                currencyCode = detail.currency.code,
                                maximumFractionDigits = null,
                            ),
                            style = KptTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

/** Plain-text share body (`ShareUtils.shareText`) — no image/PDF capture exists in this codebase. */
private fun formatReceiptShareText(detail: TransferDetail): String = buildString {
    appendLine("Mifos Pay — Receipt #${detail.id}")
    appendLine(DateHelper.getDateAsString(detail.transferDate))
    appendLine(
        CurrencyFormatter.format(
            detail.transferAmount,
            currencyCode = detail.currency.code,
            maximumFractionDigits = null,
        ),
    )
    appendLine("From: ${detail.fromClient.displayName} (${detail.fromAccount.accountNo})")
    append("To: ${detail.toClient.displayName} (${detail.toAccount.accountNo})")
}
