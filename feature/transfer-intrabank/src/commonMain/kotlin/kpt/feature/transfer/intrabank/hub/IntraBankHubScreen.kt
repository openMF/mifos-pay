/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.model.account.RecentPayee
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.ui.MifosProgressIndicatorMini
import kpt.core.ui.SimpleSearchBar
import kpt.core.ui.utils.EventsEffect
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_select_account_placeholder
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_see_all
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_send
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_tab_beneficiaries
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_tab_recents
import kpt.feature.transfer.intrabank.hub.components.BeneficiaryCard
import kpt.feature.transfer.intrabank.hub.components.EmptyBeneficiariesState
import kpt.feature.transfer.intrabank.hub.components.EmptyRecentsState
import kpt.feature.transfer.intrabank.hub.components.QuickActionsGrid
import kpt.feature.transfer.intrabank.hub.components.RecentPayeeCard
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/** Intra-bank transfer hub with quick actions, recent payees and saved beneficiaries. */
@Composable
fun IntraBankHubScreen(
    navigateToSelectAccountScreen: () -> Unit,
    navigateBack: () -> Unit,
    navigateToBeneficiary: () -> Unit,
    navigateToTransferConfirm: (
        toOfficeId: Int,
        toClientId: Long,
        toAccountId: Int,
        accountName: String,
        accountNo: String,
    ) -> Unit,
    modifier: Modifier = Modifier,
    navigateToHistory: () -> Unit = {},
    navigateToScanQr: () -> Unit = {},
    navigateToRequestMoney: () -> Unit = {},
    navigateToTransferBeneficiary: (Beneficiary) -> Unit = {},
    showTopBar: Boolean = true,
    viewModel: IntraBankHubViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            IntraBankHubEvent.NavigateToSearchAccountSelection -> navigateToSelectAccountScreen()

            IntraBankHubEvent.NavigateBack -> navigateBack()

            IntraBankHubEvent.NavigateToBeneficiary -> navigateToBeneficiary()

            IntraBankHubEvent.NavigateToHistory -> navigateToHistory()

            IntraBankHubEvent.NavigateToScanQr -> navigateToScanQr()

            IntraBankHubEvent.NavigateToRequestMoney -> navigateToRequestMoney()

            is IntraBankHubEvent.NavigateToTransfer -> {
                navigateToTransferConfirm(
                    event.toOfficeId,
                    event.toClientId,
                    event.toAccountId,
                    event.accountName,
                    event.accountNo,
                )
            }

            is IntraBankHubEvent.NavigateToTransferBeneficiary -> {
                navigateToTransferBeneficiary(event.beneficiary)
            }
        }
    }

    IntraBankHubScreenContent(
        state = state,
        modifier = modifier,
        showTopBar = showTopBar,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )

    LaunchedEffect(Unit) {
        viewModel.trySendAction(IntraBankHubAction.RefreshBeneficiaries)
    }
}

@Composable
private fun IntraBankHubScreenContent(
    state: IntraBankHubState,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    onAction: (IntraBankHubAction) -> Unit,
) {
    MifosScaffold(
        modifier = modifier,
        topBar = {
            if (showTopBar) {
                MifosTopBar(
                    topBarTitle = stringResource(Res.string.feature_send_money_send),
                    backPress = {
                        onAction(IntraBankHubAction.NavigateBack)
                    },
                )
            }
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            item {
                Spacer(Modifier.height(KptTheme.spacing.sm))

                SimpleSearchBar(
                    query = "",
                    placeHolder = stringResource(Res.string.feature_select_account_placeholder),
                    onQueryChange = {},
                    onClick = {
                        onAction(IntraBankHubAction.OnSearchBarClicked)
                    },
                    enabled = false,
                )
            }

            item {
                QuickActionsGrid(
                    onAddPayeeClick = { onAction(IntraBankHubAction.OnAddPayeeClicked) },
                    onHistoryClick = { onAction(IntraBankHubAction.OnHistoryClicked) },
                    onScanQrClick = { onAction(IntraBankHubAction.OnScanQrClicked) },
                    onRequestMoneyClick = { onAction(IntraBankHubAction.OnRequestMoneyClicked) },
                )
            }

            // Tab Row
            item {
                PayeeTabRow(
                    selectedTab = state.selectedTab,
                    onTabSelected = { onAction(IntraBankHubAction.OnTabSelected(it)) },
                )
            }

            // Content based on selected tab
            when (state.selectedTab) {
                PayeeTab.Recents -> {
                    when (state.recentPayeesState) {
                        RecentPayeesState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.lg),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    MifosProgressIndicatorMini()
                                }
                            }
                        }

                        RecentPayeesState.Success -> {
                            items(state.recentPayees, key = { it.clientId }) { payee ->
                                RecentPayeeCard(
                                    payee = payee,
                                    onPayClick = {
                                        onAction(IntraBankHubAction.OnPayRecentPayee(payee))
                                    },
                                )
                            }

                            item {
                                SeeAllTransactionsButton(
                                    onClick = {
                                        onAction(IntraBankHubAction.OnSeeAllTransactionsClicked)
                                    },
                                )
                            }
                        }

                        RecentPayeesState.Empty -> {
                            item {
                                EmptyRecentsState(
                                    onAddPayeeClick = {
                                        onAction(IntraBankHubAction.OnAddPayeeClicked)
                                    },
                                )
                            }
                        }

                        RecentPayeesState.Error -> {
                            // Don't show anything on error
                        }
                    }
                }

                PayeeTab.Beneficiaries -> {
                    when (state.beneficiariesState) {
                        BeneficiariesState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.lg),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    MifosProgressIndicatorMini()
                                }
                            }
                        }

                        BeneficiariesState.Success -> {
                            items(state.beneficiaries, key = { it.id }) { beneficiary ->
                                BeneficiaryCard(
                                    beneficiary = beneficiary,
                                    onPayClick = {
                                        onAction(IntraBankHubAction.OnPayBeneficiary(beneficiary))
                                    },
                                )
                            }
                        }

                        BeneficiariesState.Empty -> {
                            item {
                                EmptyBeneficiariesState(
                                    onAddPayeeClick = {
                                        onAction(IntraBankHubAction.OnAddPayeeClicked)
                                    },
                                )
                            }
                        }

                        BeneficiariesState.Error -> {
                            // Don't show anything on error
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(KptTheme.spacing.md))
            }
        }
    }
}

@Composable
private fun PayeeTabRow(
    selectedTab: PayeeTab,
    onTabSelected: (PayeeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        FilterChip(
            selected = selectedTab == PayeeTab.Recents,
            onClick = { onTabSelected(PayeeTab.Recents) },
            label = { Text(stringResource(Res.string.feature_send_money_tab_recents)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KptTheme.colorScheme.primaryContainer,
                selectedLabelColor = KptTheme.colorScheme.onPrimaryContainer,
            ),
        )
        FilterChip(
            selected = selectedTab == PayeeTab.Beneficiaries,
            onClick = { onTabSelected(PayeeTab.Beneficiaries) },
            label = { Text(stringResource(Res.string.feature_send_money_tab_beneficiaries)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KptTheme.colorScheme.primaryContainer,
                selectedLabelColor = KptTheme.colorScheme.onPrimaryContainer,
            ),
        )
    }
}

@Composable
private fun SeeAllTransactionsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = stringResource(Res.string.feature_send_money_see_all),
                style = KptTheme.typography.labelLarge,
                color = KptTheme.colorScheme.primary,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewIntraBankHubScreen() {
    KptMaterialTheme {
        IntraBankHubScreenContent(
            state = IntraBankHubState(),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun PreviewIntraBankHubScreenWithRecents() {
    val mockPayees = listOf(
        RecentPayee(
            clientId = 1,
            clientName = "John Doe",
            accountId = 101,
            accountNo = "ACC0017543",
            officeId = 1,
            officeName = "HQ",
            lastTransferDate = "2026-02-07",
            lastAmount = 63823.00,
            currency = "₹",
        ),
        RecentPayee(
            clientId = 2,
            clientName = "Alice Smith",
            accountId = 102,
            accountNo = "ACC0022696",
            officeId = 1,
            officeName = "HQ",
            lastTransferDate = "2026-01-28",
            lastAmount = 25000.00,
            currency = "₹",
        ),
    )

    KptMaterialTheme {
        IntraBankHubScreenContent(
            state = IntraBankHubState(
                recentPayees = mockPayees,
                recentPayeesState = RecentPayeesState.Success,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun PreviewIntraBankHubScreenWithBeneficiaries() {
    val mockBeneficiaries = listOf(
        Beneficiary(
            id = 1,
            name = "John Doe",
            officeName = "Head Office",
            clientName = "John Doe",
            accountType = Beneficiary.AccountType(
                id = 2,
                code = "SAVINGS",
                value = "Savings",
            ),
            accountNumber = "ACC1234567543",
            transferLimit = 100000,
        ),
        Beneficiary(
            id = 2,
            name = "Alice Smith",
            officeName = "Branch Office",
            clientName = "Alice Smith",
            accountType = Beneficiary.AccountType(
                id = 2,
                code = "SAVINGS",
                value = "Savings",
            ),
            accountNumber = "ACC9876543210",
            transferLimit = 50000,
        ),
    )

    KptMaterialTheme {
        IntraBankHubScreenContent(
            state = IntraBankHubState(
                selectedTab = PayeeTab.Beneficiaries,
                beneficiaries = mockBeneficiaries,
                beneficiariesState = BeneficiariesState.Success,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun PreviewIntraBankHubScreenEmpty() {
    KptMaterialTheme {
        IntraBankHubScreenContent(
            state = IntraBankHubState(
                recentPayeesState = RecentPayeesState.Empty,
            ),
            onAction = {},
        )
    }
}
