/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import mifos_pay.feature.send_money.generated.resources.Res
import mifos_pay.feature.send_money.generated.resources.feature_send_money_autopay
import mifos_pay.feature.send_money.generated.resources.feature_send_money_bank_transfer
import mifos_pay.feature.send_money.generated.resources.feature_send_money_choose_method
import mifos_pay.feature.send_money.generated.resources.feature_send_money_fineract_payments
import mifos_pay.feature.send_money.generated.resources.feature_send_money_merchants
import mifos_pay.feature.send_money.generated.resources.feature_send_money_more
import mifos_pay.feature.send_money.generated.resources.feature_send_money_pay_anyone
import mifos_pay.feature.send_money.generated.resources.feature_send_money_people
import mifos_pay.feature.send_money.generated.resources.feature_send_money_scan_qr_code
import mifos_pay.feature.send_money.generated.resources.feature_send_money_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun SendMoneyOptionsScreen(
    onBackClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onPayAnyoneClick: () -> Unit,
    onBankTransferClick: () -> Unit,
    onFineractPaymentsClick: () -> Unit,
    onAutoPayClick: () -> Unit,
    onQrCodeScanned: (String) -> Unit,
    onNavigateToPayeeDetails: (String) -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onUpiTransactionHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendMoneyOptionsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            SendMoneyOptionsEvent.NavigateBack -> {
                onBackClick.invoke()
            }
            SendMoneyOptionsEvent.NavigateToPayAnyone -> {
                onPayAnyoneClick.invoke()
            }
            SendMoneyOptionsEvent.NavigateToBankTransfer -> {
                onBankTransferClick.invoke()
            }
            SendMoneyOptionsEvent.NavigateToFineractPayments -> {
                onFineractPaymentsClick.invoke()
            }
            SendMoneyOptionsEvent.NavigateToAutoPay -> {
                onAutoPayClick.invoke()
            }
            is SendMoneyOptionsEvent.QrCodeScanned -> {
                onQrCodeScanned.invoke(event.data)
            }
            is SendMoneyOptionsEvent.NavigateToPayeeDetails -> {
                onNavigateToPayeeDetails.invoke(event.qrCodeData)
            }
        }
    }
    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                MifosTopBar(
                    topBarTitle = stringResource(Res.string.feature_send_money_title),
                    backPress = {
                        viewModel.trySendAction(SendMoneyOptionsAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = KptTheme.spacing.lg)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                SendMoneyBanner()

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                SendMoneyOptionsRow(
                    onScanQrClick = {
                        viewModel.trySendAction(SendMoneyOptionsAction.ScanQrClicked)
                    },
                    onPayAnyoneClick = {
                        viewModel.trySendAction(SendMoneyOptionsAction.PayAnyoneClicked)
                    },
                    onBankTransferClick = {
                        viewModel.trySendAction(SendMoneyOptionsAction.BankTransferClicked)
                    },
                    onFineractPaymentsClick = {
                        viewModel.trySendAction(SendMoneyOptionsAction.FineractPaymentsClicked)
                    },
                    onAutoPayClick = {
                        viewModel.trySendAction(SendMoneyOptionsAction.AutoPayClicked)
                    },
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                PeopleSection(
                    onPaymentHistoryClick = onPaymentHistoryClick,
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                MerchantsSection()

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                TransactionHistorySection(
                    onSeeAllClick = onUpiTransactionHistoryClick,
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))
            }
        }
    }
}

@Composable
private fun SendMoneyBanner(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.xl),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.feature_send_money_choose_method),
                style = KptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = KptTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SendMoneyOptionsRow(
    onScanQrClick: () -> Unit,
    onPayAnyoneClick: () -> Unit,
    onBankTransferClick: () -> Unit,
    onFineractPaymentsClick: () -> Unit,
    onAutoPayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            SendMoneyOptionButton(
                icon = MifosIcons.Scan,
                label = stringResource(Res.string.feature_send_money_scan_qr_code),
                onClick = onScanQrClick,
                modifier = Modifier.weight(1f),
            )

            SendMoneyOptionButton(
                icon = MifosIcons.Person,
                label = stringResource(Res.string.feature_send_money_pay_anyone),
                onClick = onPayAnyoneClick,
                modifier = Modifier.weight(1f),
            )

            SendMoneyOptionButton(
                icon = MifosIcons.Bank,
                label = stringResource(Res.string.feature_send_money_bank_transfer),
                onClick = onBankTransferClick,
                modifier = Modifier.weight(1f),
            )

            SendMoneyOptionButton(
                icon = MifosIcons.Payment,
                label = stringResource(Res.string.feature_send_money_fineract_payments),
                onClick = onFineractPaymentsClick,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            SendMoneyOptionButton(
                icon = MifosIcons.CalenderMonth,
                label = stringResource(Res.string.feature_send_money_autopay),
                onClick = onAutoPayClick,
                modifier = Modifier.weight(1f),
            )

            // Empty space for future icons (UPI Lite, Tap & Pay, etc.)
            Spacer(modifier = Modifier.weight(3f))
        }
    }
}

@Composable
private fun SendMoneyOptionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
        color = KptTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = KptTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(KptTheme.spacing.sm),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = KptTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = label,
                style = KptTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = KptTheme.colorScheme.onSurface,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun PeopleSection(
    onPaymentHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: This is a placeholder section. People functionality is not implemented yet.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.feature_send_money_people),
            style = KptTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                PersonItem(
                    name = "John Doe",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Jane Smith",
                    onClick = onPaymentHistoryClick,
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Mike Johnson",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Sarah Wilson",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                PersonItem(
                    name = "David Brown",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Lisa Davis",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Tom Miller",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = stringResource(Res.string.feature_send_money_more),
                    isMoreButton = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MerchantsSection(
    modifier: Modifier = Modifier,
) {
    // TODO: This is a placeholder section. Merchants functionality is not implemented yet.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.feature_send_money_merchants),
            style = KptTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                PersonItem(
                    name = "Coffee Shop",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Grocery Store",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Restaurant",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Gas Station",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                PersonItem(
                    name = "Pharmacy",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Bookstore",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = "Bakery",
                    modifier = Modifier.weight(1f),
                )
                PersonItem(
                    name = stringResource(Res.string.feature_send_money_more),
                    isMoreButton = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TransactionHistorySection(
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dummyTransactions = listOf(
        TransactionItem(
            payeeName = "Rahul Sharma",
            amount = 2500.0,
            date = "15 August",
            profileImageUrl = null,
        ),
        TransactionItem(
            payeeName = "Priya Patel",
            amount = 1800.0,
            date = "14 August",
            profileImageUrl = null,
        ),
        TransactionItem(
            payeeName = "Amit Kumar",
            amount = 3200.0,
            date = "13 August",
            profileImageUrl = null,
        ),
        TransactionItem(
            payeeName = "Neha Singh",
            amount = 950.0,
            date = "12 August",
            profileImageUrl = null,
        ),
        TransactionItem(
            payeeName = "Vikram Mehta",
            amount = 4100.0,
            date = "11 August",
            profileImageUrl = null,
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "UPI Transaction History",
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = KptTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.clickable { onSeeAllClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = "See All",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = MifosIcons.ChevronRight,
                    contentDescription = "See All",
                    modifier = Modifier.size(16.dp),
                    tint = KptTheme.colorScheme.primary,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            dummyTransactions.forEach { transaction ->
                TransactionItemRow(transaction = transaction)
            }
        }
    }
}

@Composable
private fun TransactionItemRow(
    transaction: TransactionItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = KptTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (transaction.profileImageUrl != null) {
                Text(
                    text = transaction.payeeName.take(1).uppercase(),
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Text(
                    text = transaction.payeeName.take(1).uppercase(),
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = transaction.payeeName.uppercase(),
                style = KptTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KptTheme.colorScheme.onSurface,
            )
            Text(
                text = transaction.date,
                style = KptTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = CurrencyFormatter.format(
                balance = transaction.amount,
                currencyCode = "INR",
                maximumFractionDigits = 2,
            ),
            style = KptTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
        )
    }
}

data class TransactionItem(
    val payeeName: String,
    val amount: Double,
    val date: String,
    val profileImageUrl: String?,
)

@Composable
private fun PersonItem(
    name: String,
    isMoreButton: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .clip(RoundedCornerShape(KptTheme.spacing.sm)),
        color = KptTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isMoreButton) {
                            KptTheme.colorScheme.secondaryContainer
                        } else {
                            KptTheme.colorScheme.primaryContainer
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isMoreButton) {
                    Icon(
                        imageVector = MifosIcons.Add,
                        contentDescription = name,
                        modifier = Modifier.size(24.dp),
                        tint = KptTheme.colorScheme.onSecondaryContainer,
                    )
                } else {
                    Text(
                        text = name.take(1).uppercase(),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Text(
                text = name,
                style = KptTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = KptTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
