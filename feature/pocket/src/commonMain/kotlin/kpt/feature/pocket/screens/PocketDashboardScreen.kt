/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pocket.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.freshness.FreshnessIndicator
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.MifosAccountCard
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.pocket.AccountStatus
import kpt.core.ui.utils.EventsEffect
import kpt.feature.pocket.generated.resources.Res
import kpt.feature.pocket.generated.resources.feature_pocket_dashboard_loan_accounts
import kpt.feature.pocket.generated.resources.feature_pocket_dashboard_manage
import kpt.feature.pocket.generated.resources.feature_pocket_dashboard_savings_accounts
import kpt.feature.pocket.generated.resources.feature_pocket_dashboard_share_accounts
import kpt.feature.pocket.generated.resources.feature_pocket_dashboard_title
import kpt.feature.pocket.generated.resources.feature_pocket_dashboard_total_balance
import kpt.feature.pocket.generated.resources.feature_pocket_empty_action
import kpt.feature.pocket.generated.resources.feature_pocket_empty_description
import kpt.feature.pocket.generated.resources.feature_pocket_empty_title
import kpt.feature.pocket.generated.resources.feature_pocket_unknown_account
import kpt.feature.pocket.generated.resources.feature_pocket_unknown_status
import kpt.feature.pocket.viewmodels.PocketBuckets
import kpt.feature.pocket.viewmodels.PocketDashboardAction
import kpt.feature.pocket.viewmodels.PocketDashboardEvent
import kpt.feature.pocket.viewmodels.PocketDashboardViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/**
 * Main entry point for the Pocket Dashboard.
 *
 * **Architecture Decisions:**
 * - **ScreenContent Wrapper**: Completely leverages the new KPT offline-first architecture (`ScreenContent`).
 * - **Empty States**: Specifically implements a custom empty state `EmptyPocketContent` that overrides
 *   the generic default empty screen, providing a tailored onboarding experience for pockets.
 *
 * **Component Structure:**
 * - `MifosAccountCard`: Used strictly as a dumb, reusable UI component representing a single account
 *   (Loan, Savings, or Share). It relies on the pre-formatted `accountType` and `accountStatus` strings
 *   passed down from the ViewModel.
 * - `PocketDashboardScreen`: Orchestrates the layout by reading the `PocketBuckets` and sequentially
 *   spawning sections (Savings, Loans, Shares) only if they are not empty.
 */
@Composable
internal fun PocketDashboardScreen(
    navigateBack: () -> Unit,
    navigateToManagePocket: () -> Unit,
    navigateToLoanAccountDetail: (Long) -> Unit,
    navigateToShareAccountDetail: (Long) -> Unit,
    navigateToSavingsAccountDetail: (Long) -> Unit,
    viewModel: PocketDashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val freshness by viewModel.freshness.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            PocketDashboardEvent.NavigateBack -> navigateBack.invoke()
            PocketDashboardEvent.ManagePocket -> navigateToManagePocket.invoke()
            is PocketDashboardEvent.NavigateToLoanDetail -> navigateToLoanAccountDetail(event.accountId)
            is PocketDashboardEvent.NavigateToSavingsDetail -> navigateToSavingsAccountDetail(event.accountId)
            is PocketDashboardEvent.NavigateToShareDetail -> navigateToShareAccountDetail(event.accountId)
        }
    }

    PocketDashboardContent(
        state = state,
        freshness = freshness,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        onRetry = viewModel::retry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PocketDashboardContent(
    state: ScreenState<PocketBuckets>,
    freshness: FreshnessSignal,
    onAction: (PocketDashboardAction) -> Unit,
    onRetry: () -> Unit,
) {
    val pullRefreshState = rememberPullToRefreshState()
    val isRefreshing = (state as? ScreenState.Content)?.freshnessSignal?.isRefreshing == true

    MifosScaffold(
        backPress = { onAction(PocketDashboardAction.NavigateBack) },
        topBarTitle = stringResource(Res.string.feature_pocket_dashboard_title),
        containerColor = KptTheme.colorScheme.background,
        actions = {
            FreshnessIndicator(
                signal = freshness,
                onRefresh = { onAction(PocketDashboardAction.Refresh) },
            )
            IconButton(onClick = { onAction(PocketDashboardAction.ManagePocket) }) {
                Icon(
                    imageVector = MifosIcons.Edit2,
                    contentDescription = stringResource(Res.string.feature_pocket_dashboard_manage),
                    tint = KptTheme.colorScheme.onSurface,
                )
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onAction(PocketDashboardAction.Refresh) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            ScreenContent(
                state = state,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
                refreshingIndicator = null,
                empty = {
                    EmptyPocketContent(
                        onLinkFirstAccount = { onAction(PocketDashboardAction.LinkFirstAccount) },
                    )
                },
            ) { buckets, _ ->
                PocketDashboardContent(buckets = buckets, onAction = onAction)
            }
        }
    }
}

@Composable
private fun PocketDashboardContent(
    buckets: PocketBuckets,
    onAction: (PocketDashboardAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(KptTheme.spacing.lg),
    ) {
        PocketDashboardCard(
            totalBalance = buckets.totalBalance,
            onManageClick = { onAction(PocketDashboardAction.ManagePocket) },
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.xl))

        if (buckets.savingsAccounts.isNotEmpty()) {
            PocketSectionHeader(title = Res.string.feature_pocket_dashboard_savings_accounts)
            Spacer(modifier = Modifier.height(KptTheme.spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md)) {
                buckets.savingsAccounts.forEach { account ->
                    MifosAccountCard(
                        accountId = account.accountId,
                        accountType = account.name
                            ?: stringResource(Res.string.feature_pocket_unknown_account),
                        accountNumber = account.accountNumber,
                        accountStatus = account.balanceOrStatus
                            ?: stringResource(Res.string.feature_pocket_unknown_status),
                        accountStatusColor = account.status.toColor(),
                        onAccountClick = {
                            onAction(PocketDashboardAction.NavigateToSavingsDetail(account.accountId))
                        },
                        icon = MifosIcons.PersonAccounts,
                    )
                }
            }
            Spacer(modifier = Modifier.height(KptTheme.spacing.xl))
        }

        if (buckets.loanAccounts.isNotEmpty()) {
            PocketSectionHeader(title = Res.string.feature_pocket_dashboard_loan_accounts)
            Spacer(modifier = Modifier.height(KptTheme.spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md)) {
                buckets.loanAccounts.forEach { account ->
                    MifosAccountCard(
                        accountId = account.accountId,
                        accountType = account.name
                            ?: stringResource(Res.string.feature_pocket_unknown_account),
                        accountNumber = account.accountNumber,
                        accountStatus = account.balanceOrStatus
                            ?: stringResource(Res.string.feature_pocket_unknown_status),
                        accountStatusColor = account.status.toColor(),
                        onAccountClick = {
                            onAction(PocketDashboardAction.NavigateToLoanDetail(account.accountId))
                        },
                        icon = MifosIcons.CoinMultiple,
                    )
                }
            }
            Spacer(modifier = Modifier.height(KptTheme.spacing.xl))
        }

        if (buckets.shareAccounts.isNotEmpty()) {
            PocketSectionHeader(title = Res.string.feature_pocket_dashboard_share_accounts)
            Spacer(modifier = Modifier.height(KptTheme.spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md)) {
                buckets.shareAccounts.forEach { account ->
                    MifosAccountCard(
                        accountId = account.accountId,
                        accountType = account.name
                            ?: stringResource(Res.string.feature_pocket_unknown_account),
                        accountNumber = account.accountNumber,
                        accountStatus = account.balanceOrStatus
                            ?: stringResource(Res.string.feature_pocket_unknown_status),
                        accountStatusColor = account.status.toColor(),
                        onAccountClick = {
                            onAction(PocketDashboardAction.NavigateToShareDetail(account.accountId))
                        },
                        icon = MifosIcons.CoinMultiple,
                    )
                }
            }
            Spacer(modifier = Modifier.height(KptTheme.spacing.xl))
        }
    }
}

@Composable
internal fun PocketSectionHeader(
    title: StringResource,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(title),
        modifier = modifier.fillMaxWidth(),
        style = KptTheme.typography.titleSmall,
        color = KptTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
internal fun PocketDashboardCard(
    totalBalance: String,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(KptTheme.shapes.large)
            .heightIn(min = 112.dp)
            .fillMaxWidth()
            .background(KptTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(KptTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.feature_pocket_dashboard_total_balance),
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )

                Text(
                    text = totalBalance,
                    style = KptTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.onPrimary,
                )
            }

            MifosButton(
                onClick = onManageClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.onPrimary,
                    contentColor = KptTheme.colorScheme.primary,
                ),
                content = {
                    Text(
                        text = stringResource(Res.string.feature_pocket_dashboard_manage),
                        style = KptTheme.typography.titleSmall,
                    )
                },
            )
        }
    }
}

/** Maps an account status to the theme color used to display it. */
@Composable
fun AccountStatus.toColor(): Color =
    when (this) {
        AccountStatus.ACTIVE ->
            KptTheme.colorScheme.primary

        AccountStatus.PENDING,
        AccountStatus.APPROVED,
        ->
            KptTheme.colorScheme.tertiary

        AccountStatus.REJECTED,
        AccountStatus.WITHDRAWN,
        AccountStatus.CLOSED,
        ->
            KptTheme.colorScheme.error

        AccountStatus.OVERPAID,
        AccountStatus.MATURED,
        ->
            KptTheme.colorScheme.secondary

        AccountStatus.UNKNOWN ->
            KptTheme.colorScheme.onSurface
    }

@Composable
internal fun EmptyPocketContent(
    onLinkFirstAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(KptTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            modifier = Modifier.size(180.dp),
            shape = CircleShape,
            color = KptTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = MifosIcons.Pocket,
                    contentDescription = null,
                    tint = KptTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(KptTheme.spacing.xl))

        Text(
            text = stringResource(Res.string.feature_pocket_empty_title),
            style = KptTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.md))

        Text(
            text = stringResource(Res.string.feature_pocket_empty_description),
            style = KptTheme.typography.bodyLarge,
            color = KptTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.xl))

        MifosButton(
            onClick = onLinkFirstAccount,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = KptTheme.colorScheme.primary,
                contentColor = KptTheme.colorScheme.onPrimary,
            ),
            content = {
                Text(
                    text = stringResource(Res.string.feature_pocket_empty_action),
                    style = KptTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Preview
@Composable
internal fun PocketDashboardContentPreview() {
    PocketDashboardContent(
        state = ScreenState.Content(
            PocketBuckets(
                totalBalance = "10,000.00",
                savingsAccounts = emptyList(),
                loanAccounts = emptyList(),
                shareAccounts = emptyList(),
            ),
        ),
        freshness = FreshnessSignal.initial(),
        onAction = {},
        onRetry = {},
    )
}

@Preview
@Composable
private fun EmptyPocketContentPreview() {
    EmptyPocketContent(
        onLinkFirstAccount = {},
    )
}
