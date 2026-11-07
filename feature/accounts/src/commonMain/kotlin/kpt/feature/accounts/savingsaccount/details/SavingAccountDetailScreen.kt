/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts.savingsaccount.details

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.model.account.Account
import kpt.core.model.savingsaccount.SavingAccountDetail
import kpt.core.model.savingsaccount.Status
import kpt.core.model.savingsaccount.Summary
import kpt.core.model.savingsaccount.toAccount
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkDivider
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.TransactionHistoryCard
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.accounts.SavingAccountStatus
import kpt.feature.accounts.color
import kpt.feature.accounts.generated.resources.Res
import kpt.feature.accounts.generated.resources.feature_accounts_detail_account_balance
import kpt.feature.accounts.generated.resources.feature_accounts_detail_account_details
import kpt.feature.accounts.generated.resources.feature_accounts_detail_account_summary
import kpt.feature.accounts.generated.resources.feature_accounts_detail_arrow
import kpt.feature.accounts.generated.resources.feature_accounts_detail_available_balance
import kpt.feature.accounts.generated.resources.feature_accounts_detail_interest_not_posted
import kpt.feature.accounts.generated.resources.feature_accounts_detail_product_name
import kpt.feature.accounts.generated.resources.feature_accounts_detail_total_deposits
import kpt.feature.accounts.generated.resources.feature_accounts_detail_total_interest_posted
import kpt.feature.accounts.generated.resources.feature_accounts_detail_total_overdraft
import kpt.feature.accounts.generated.resources.feature_accounts_detail_total_withdrawals
import kpt.feature.accounts.generated.resources.feature_accounts_detail_wallet_balance
import kpt.feature.accounts.generated.resources.feature_accounts_error_oops
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun SavingAccountDetailScreen(
    navigateBack: () -> Unit,
    onViewTransaction: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavingAccountDetailViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is SADEvent.NavigateBack -> navigateBack.invoke()

            is SADEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            is SADEvent.OnViewTransaction -> {
                onViewTransaction(event.clientId, event.accountId)
            }
        }
    }

    SavingAccountDetailScreen(
        state = state.viewState,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
@VisibleForTesting
internal fun SavingAccountDetailScreen(
    state: SADState.ViewState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (SADAction) -> Unit,
) {
    ForkScaffold(
        backPress = {
            onAction(SADAction.NavigateBack)
        },
        topBarTitle = stringResource(Res.string.feature_accounts_detail_account_details),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is SADState.ViewState.Loading -> ForkProgressIndicator()

                is SADState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
                        subTitle = state.message,
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is SADState.ViewState.Content -> {
                    SavingAccountDetailScreenContent(
                        state = state,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingAccountDetailScreenContent(
    state: SADState.ViewState.Content,
    modifier: Modifier = Modifier,
    onAction: (SADAction) -> Unit,
) {
    SavingAccountDetails(
        savingAccountDetail = state.data,
        modifier = modifier,
        onViewTransaction = { clientId, accountId ->
            onAction(SADAction.ViewTransaction(clientId, accountId))
        },
    )
}

@Composable
private fun SavingAccountDetails(
    savingAccountDetail: SavingAccountDetail,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onViewTransaction: (Long, Long) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        item {
            SavingAccountCard(
                account = savingAccountDetail.toAccount(),
                status = savingAccountDetail.status,
            )
        }

        item {
            SavingAccountSummaryCard(
                summary = savingAccountDetail.summary,
            )
        }

        item {
            TransactionHistoryCard(
                transactions = savingAccountDetail.transactions,
                onViewTransaction = onViewTransaction,
                showLeadingIcon = true,
                showViewAll = false,
            )
        }
    }
}

@Composable
private fun SavingAccountSummaryCard(
    summary: Summary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = stringResource(Res.string.feature_accounts_detail_account_summary),
                color = KptTheme.colorScheme.primary,
                fontWeight = FontWeight(500),
                modifier = Modifier.padding(KptTheme.spacing.sm),
            )

            ForkDivider()

            RowBlock {
                Text(text = stringResource(Res.string.feature_accounts_detail_account_balance))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.accountBalance,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            ForkDivider()

            RowBlock {
                Text(text = stringResource(Res.string.feature_accounts_detail_total_deposits))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.totalDeposits,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            ForkDivider()

            RowBlock {
                Text(text = stringResource(Res.string.feature_accounts_detail_total_withdrawals))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.totalWithdrawals,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            ForkDivider()

            RowBlock {
                Text(text = stringResource(Res.string.feature_accounts_detail_available_balance))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.availableBalance,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            ForkDivider()

            RowBlock {
                Text(text = stringResource(Res.string.feature_accounts_detail_total_interest_posted))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.totalInterestPosted,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            ForkDivider()

            RowBlock {
                Text(text = stringResource(Res.string.feature_accounts_detail_total_overdraft))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.totalOverdraftInterestDerived,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            ForkDivider()

            RowBlock {
                Text(text = stringResource(Res.string.feature_accounts_detail_interest_not_posted))
                Text(
                    text = "${summary.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = summary.interestNotPosted,
                        maximumFractionDigits = 2,
                    )}",
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private inline fun RowBlock(
    crossinline content: @Composable (RowScope.() -> Unit),
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
private fun SavingAccountCard(
    account: Account,
    status: Status,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        KptTheme.colorScheme.primary,
                        KptTheme.colorScheme.secondary,
                    ),
                ),
                shape = KptTheme.shapes.large,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.feature_accounts_detail_product_name),
                        fontWeight = FontWeight(300),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.surface,
                    )

                    Text(
                        text = account.name,
                        fontWeight = FontWeight(400),
                        color = KptTheme.colorScheme.surface,
                    )
                }

                SavingAccountStatusCard(status)
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.number,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.surface,
                    style = KptTheme.typography.headlineMedium,
                    letterSpacing = 0.50.sp,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.feature_accounts_detail_wallet_balance),
                        fontWeight = FontWeight(300),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.surface,
                    )

                    val accountBalance = "${account.currency.code} ${account.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = account.balance,
                        maximumFractionDigits = 2,
                    )}"

                    Text(
                        text = accountBalance,
                        color = KptTheme.colorScheme.surface,
                        style = KptTheme.typography.headlineLarge,
                    )
                }

                Icon(
                    modifier = Modifier
                        .graphicsLayer(rotationZ = 90f)
                        .padding(KptTheme.spacing.xs),
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = stringResource(Res.string.feature_accounts_detail_arrow),
                    tint = KptTheme.colorScheme.surface,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavingAccountStatusCard(
    status: Status,
    modifier: Modifier = Modifier,
) {
    val activeStatuses = SavingAccountStatus.entries.filter { it.isActive(status) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        modifier = modifier,
    ) {
        activeStatuses.forEach { statusEnum ->
            StatusChip(
                label = stringResource(statusEnum.labelRes),
                color = statusEnum.color,
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    SuggestionChip(
        onClick = { /* Handle click if needed */ },
        label = { Text(label) },
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = color,
        ),
        colors = SuggestionChipDefaults.suggestionChipColors(
            labelColor = color,
        ),
        modifier = Modifier,
    )
}
