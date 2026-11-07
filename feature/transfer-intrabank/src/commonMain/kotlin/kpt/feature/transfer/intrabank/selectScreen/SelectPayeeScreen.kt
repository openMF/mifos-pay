/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.selectScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.toRoundedCornerShape
import kpt.core.model.network.entity.templates.account.AccountOption
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.SimpleSearchBar
import kpt.core.ui.utils.EventsEffect
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_select_account_placeholder
import kpt.feature.transfer.intrabank.generated.resources.feature_select_payment_title
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_bottom_bar
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_close
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_no_accounts_found
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_no_accounts_found_for_search
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_oops
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_proceed
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_selected
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_something_went_wrong
import kpt.feature.transfer.intrabank.generated.resources.feature_send_money_to_account
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/** Screen for choosing the destination account by number or name and proceeding to the transfer. */
@Composable
fun SelectPayeeScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    navigateToTransferConfirm: (
        toOfficeId: Int?,
        toClientId: Long?,
        toAccountTypeId: Int?,
        toAccountId: Int,
        amount: Int,
        accountName: String,
        accountNo: String,
    ) -> Unit,
    viewModel: SelectScreenViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is SelectScreenEvent.NavigateToTransferScreen -> {
                navigateToTransferConfirm(
                    state.selectedAccount?.officeId,
                    state.selectedAccount?.clientId,
                    state.selectedAccount?.accountType?.id ?: 0,
                    state.selectedAccount?.accountId ?: 0,
                    state.amount.toIntOrNull() ?: 0,
                    state.selectedAccount?.clientName ?: "",
                    state.selectedAccount?.accountNo ?: "",
                )
            }

            SelectScreenEvent.NavigateBack -> navigateBack()
        }
    }

    SelectAccountScreen(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SelectAccountScreen(
    state: SelectScreenState,
    modifier: Modifier = Modifier,
    onAction: (SelectScreenAction) -> Unit,
) {
    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                MifosTopBar(
                    topBarTitle = stringResource(Res.string.feature_select_payment_title),
                    backPress = {
                        onAction(SelectScreenAction.NavigateBack)
                    },
                )
            },
            bottomBar = {
                SendMoneyBottomBar(
                    showDetails = state.isProceedEnabled,
                    selectedAccount = state.selectedAccount,
                    onDeselect = {
                        onAction(SelectScreenAction.DeselectAccount)
                    },
                    onClickProceed = {
                        onAction(SelectScreenAction.OnProceedClicked)
                    },
                )
            },
        ) { paddingValues ->
            SelectAccountContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun SelectAccountContent(
    state: SelectScreenState,
    onAction: (SelectScreenAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    when (state.state) {
        is SelectScreenState.State.Error -> {
            EmptyContentScreen(
                title = stringResource(Res.string.feature_send_money_oops),
                subTitle = stringResource(Res.string.feature_send_money_something_went_wrong),
                modifier = Modifier.fillMaxSize(),
                iconTint = KptTheme.colorScheme.error,
            )
        }
        SelectScreenState.State.Loading -> {
            MifosProgressIndicator()
        }
        SelectScreenState.State.NoAccounts -> {
            EmptyContentScreen(
                title = stringResource(Res.string.feature_send_money_oops),
                subTitle = stringResource(Res.string.feature_send_money_no_accounts_found),
                modifier = Modifier.fillMaxSize(),
            )
        }
        SelectScreenState.State.Success -> {
            LazyColumn(
                modifier = modifier.padding(horizontal = KptTheme.spacing.md),
            ) {
                stickyHeader {
                    SimpleSearchBar(
                        query = state.accountNumber,
                        placeHolder = stringResource(Res.string.feature_select_account_placeholder),
                        onQueryChange = {
                            onAction(SelectScreenAction.AccountNumberChanged(it))
                        },
                        onClearQuery = {
                            onAction(SelectScreenAction.AccountNumberChanged(""))
                        },
                    )
                }
                accountListContent(
                    state = state,
                    onAction = {
                        onAction(it)
                        keyboardController?.hide()
                    },
                    selected = { state.selectedAccount == it },
                )
                if (state.filteredToAccounts?.isEmpty() == true) {
                    item {
                        EmptyContentScreen(
                            title = stringResource(Res.string.feature_send_money_oops),
                            subTitle = stringResource(Res.string.feature_send_money_no_accounts_found_for_search),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.accountListContent(
    state: SelectScreenState,
    selected: (AccountOption?) -> Boolean,
    onAction: (SelectScreenAction.SelectAccount) -> Unit,
) {
    items(state.filteredToAccounts?.size ?: 0) { it ->
        AccountCard(
            account = state.filteredToAccounts?.get(it),
            selected = selected,
            onClick = remember(state.filteredToAccounts?.get(it)) {
                {
                    onAction(SelectScreenAction.SelectAccount(it))
                }
            },
        )
    }
}

@Composable
private fun AccountCard(
    account: AccountOption?,
    selected: (AccountOption?) -> Boolean,
    modifier: Modifier = Modifier,
    onClick: (AccountOption?) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = account?.clientName ?: "")
        },
        supportingContent = {
            Text(text = account?.accountNo ?: "")
        },
        leadingContent = {
            AvatarBox(
                icon = MifosIcons.Bank,
                backgroundColor = KptTheme.colorScheme.tertiary,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        trailingContent = {
            AnimatedVisibility(
                visible = selected(account),
            ) {
                Icon(
                    imageVector = MifosIcons.Check,
                    contentDescription = stringResource(Res.string.feature_send_money_selected),
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick(account)
            },
    )
}

@Composable
private fun SendMoneyBottomBar(
    showDetails: Boolean,
    selectedAccount: AccountOption?,
    modifier: Modifier = Modifier,
    onClickProceed: () -> Unit,
    onDeselect: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.toRoundedCornerShape(
            topStart = KptTheme.spacing.sm,
            topEnd = KptTheme.spacing.sm,
        ),
        color = KptTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            AnimatedVisibility(
                visible = showDetails && selectedAccount != null,
                label = stringResource(Res.string.feature_send_money_bottom_bar),
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { fullHeight ->
                        fullHeight / 4
                    },
                ),
                exit = fadeOut(tween(300)) + slideOutVertically(
                    targetOffsetY = { fullHeight ->
                        fullHeight / 4
                    },
                ),
            ) {
                selectedAccount?.let {
                    SelectedAccountCard(
                        account = selectedAccount,
                        onDeselect = onDeselect,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            MifosButton(
                onClick = onClickProceed,
                enabled = showDetails,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.feature_send_money_proceed))
            }
        }
    }
}

@Composable
private fun SelectedAccountCard(
    account: AccountOption,
    modifier: Modifier = Modifier,
    onDeselect: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(Res.string.feature_send_money_to_account),
            style = KptTheme.typography.labelLarge,
        )

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.Transparent,
            ),
            border = BorderStroke(1.dp, KptTheme.colorScheme.primary),
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = account.clientName ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                supportingContent = {
                    Text(text = account.accountNo ?: "")
                },
                leadingContent = {
                    AvatarBox(icon = MifosIcons.Bank)
                },
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onDeselect,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.feature_send_money_close),
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSelectAccountScreen() {
    val mockAccount = AccountOption(
        accountId = 1,
        accountNo = "1234567890",
        clientId = 10,
        clientName = "Alex Doe",
        officeId = 101,
    )
    val state = SelectScreenState(
        state = SelectScreenState.State.Success,
        filteredToAccounts = listOf(mockAccount),
        selectedAccount = mockAccount,
    )

    KptTheme {
        SelectAccountScreen(
            state = state,
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAccountCardSelected() {
    val mockAccount = AccountOption(
        accountId = 1,
        accountNo = "1234567890",
        clientId = 10,
        clientName = "Jane Smith",
        officeId = 101,
    )

    KptTheme {
        AccountCard(
            account = mockAccount,
            selected = { it?.accountId == 1 },
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewSendMoneyBottomBar() {
    val mockAccount = AccountOption(
        accountId = 2,
        accountNo = "9876543210",
        clientId = 11,
        clientName = "Chris Evans",
        officeId = 102,
    )

    KptTheme {
        SendMoneyBottomBar(
            showDetails = true,
            selectedAccount = mockAccount,
            onClickProceed = {},
            onDeselect = {},
        )
    }
}

@Preview
@Composable
private fun PreviewSelectedAccountCard() {
    val mockAccount = AccountOption(
        accountId = 3,
        accountNo = "111122223333",
        clientId = 12,
        clientName = "Natasha Romanoff",
        officeId = 103,
    )

    KptTheme {
        SelectedAccountCard(
            account = mockAccount,
            onDeselect = {},
        )
    }
}
