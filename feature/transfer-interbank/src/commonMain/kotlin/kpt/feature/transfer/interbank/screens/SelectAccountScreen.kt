/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.interbank.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.fork.component.ForkCard
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTopBar
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.model.account.Account
import kpt.core.model.network.entity.templates.account.AccountType
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Status
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkProgressIndicator
import kpt.feature.transfer.interbank.generated.resources.Res
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_account
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_account_type
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_balance
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_choose_account_to_send
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_no_accounts
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_no_accounts_available
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_office
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_oops
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_select_account
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_select_your_account
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Step for choosing the account to transfer from. */
@Composable
fun SelectAccountScreen(
    accounts: List<Account>,
    isLoading: Boolean,
    error: String?,
    onAccountSelected: (Account) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        topBar = {
            ForkTopBar(
                topBarTitle = stringResource(Res.string.feature_send_interbank_select_account),
                backPress = onBackClick,
            )
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    ForkProgressIndicator()
                }
            }

            error != null -> {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_send_interbank_oops),
                    subTitle = error,
                    iconTint = KptTheme.colorScheme.error,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            accounts.isEmpty() -> {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_send_interbank_no_accounts),
                    subTitle = stringResource(Res.string.feature_send_interbank_no_accounts_available),
                    modifier = Modifier.padding(paddingValues),
                )
            }

            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(KptTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                ) {
                    item {
                        Text(
                            text = stringResource(Res.string.feature_send_interbank_select_your_account),
                            style = KptTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(Res.string.feature_send_interbank_choose_account_to_send),
                            style = KptTheme.typography.bodyMedium,
                            color = KptTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = KptTheme.spacing.xs),
                        )
                    }

                    items(accounts) { account ->
                        AccountSelectionCard(
                            modifier = Modifier.fillMaxWidth(),
                            account = account,
                            onClick = { onAccountSelected(account) },
                        )
                    }

                    item {
                        Box(modifier = Modifier.padding(vertical = KptTheme.spacing.md))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSelectionCard(
    account: Account,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkCard(
        colors = CardDefaults.cardColors(KptTheme.colorScheme.background),
        shape = KptTheme.shapes.medium,
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(KptTheme.spacing.xs),
    ) {
        val accountBalance = CurrencyFormatter.format(
            balance = account.balance,
            currencyCode = account.currency.code,
            maximumFractionDigits = null,
        )
        ListItem(
            headlineContent = {
                Text(
                    text = account.clientName,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            Res.string.feature_send_interbank_balance,
                            accountBalance,
                        ),
                        style = KptTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(
                            Res.string.feature_send_interbank_account,
                            account.name,
                            account.number,
                        ),
                        style = KptTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(
                            Res.string.feature_send_interbank_office,
                            account.officeName ?: "",
                        ),
                        style = KptTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(
                            Res.string.feature_send_interbank_account_type,
                            account.accountType?.value ?: "",
                        ),
                        style = KptTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(KptTheme.spacing.xs))
                }
            },
            leadingContent = {
                AvatarBox(
                    icon = ForkIcons.Bank,
                    backgroundColor = KptTheme.colorScheme.primaryContainer,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

/** Compose preview of [SelectAccountScreen] with accounts. */
@Preview
@Composable
fun SelectAccountScreenPreview() {
    ForkTheme {
        val mockAccounts = listOf(
            Account(
                id = 1L,
                name = "WALLET",
                clientName = "ALEJANDRO ESCUTIA",
                number = "00000002",
                balance = 5000.50,
                productId = 1L,
                officeName = "SAN JUAN OZOLOTEPEC",
                accountType = kpt.core.model.savingsaccount.AccountType(
                    id = 1L,
                    code = "savingsAccountType.savings",
                    value = "Savings",
                ),
                currency = Currency(
                    code = "USD",
                    name = "US Dollar",
                    displaySymbol = "$",
                    displayLabel = "US Dollar ($)",
                    decimalPlaces = 2,
                    inMultiplesOf = 10,
                    nameCode = "USD",
                ),
                status = Status(
                    id = 300,
                    code = "savingsAccountStatusType.active",
                    value = "Active",
                    submittedAndPendingApproval = false,
                    approved = true,
                    rejected = false,
                    withdrawnByApplicant = false,
                    active = true,
                    closed = false,
                    prematureClosed = false,
                    transferInProgress = false,
                    transferOnHold = false,
                    matured = false,
                ),
            ),
            Account(
                id = 2L,
                name = "WALLET",
                number = "00000003",
                clientName = "JUAN PEREZ",
                balance = 3200.75,
                productId = 1L,
                officeName = "SAN JUAN OZOLOTEPEC",
                accountType = kpt.core.model.savingsaccount.AccountType(
                    id = 1L,
                    code = "savingsAccountType.savings",
                    value = "Savings",
                ),
                currency = Currency(
                    code = "USD",
                    name = "US Dollar",
                    displaySymbol = "$",
                    displayLabel = "US Dollar ($)",
                    decimalPlaces = 2,
                    inMultiplesOf = 10,
                    nameCode = "USD",
                ),
                status = Status(
                    id = 300,
                    code = "savingsAccountStatusType.active",
                    value = "Active",
                    submittedAndPendingApproval = false,
                    approved = true,
                    rejected = false,
                    withdrawnByApplicant = false,
                    active = true,
                    closed = false,
                    prematureClosed = false,
                    transferInProgress = false,
                    transferOnHold = false,
                    matured = false,
                ),
            ),
        )

        SelectAccountScreen(
            accounts = mockAccounts,
            isLoading = false,
            error = null,
            onAccountSelected = {},
            onBackClick = {},
        )
    }
}

/** Compose preview of [SelectAccountScreen] while loading. */
@Preview
@Composable
fun SelectAccountScreenLoadingPreview() {
    ForkTheme {
        SelectAccountScreen(
            accounts = emptyList(),
            isLoading = true,
            error = null,
            onAccountSelected = {},
            onBackClick = {},
        )
    }
}

/** Compose preview of [SelectAccountScreen] in the error state. */
@Preview
@Composable
fun SelectAccountScreenErrorPreview() {
    ForkTheme {
        SelectAccountScreen(
            accounts = emptyList(),
            isLoading = false,
            error = "Failed to load accounts. Please try again.",
            onAccountSelected = {},
            onBackClick = {},
        )
    }
}
