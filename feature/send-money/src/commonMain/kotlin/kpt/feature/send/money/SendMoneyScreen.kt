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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.utils.maskString
import kpt.core.designsystem.fork.component.BasicDialogState.Shown
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkGradientBackground
import kpt.core.designsystem.fork.component.ForkLoadingDialog
import kpt.core.designsystem.fork.component.ForkLoadingWheel
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTextField
import kpt.core.designsystem.fork.component.ForkTopBar
import kpt.core.designsystem.fork.component.LoadingDialogState
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.toRoundedCornerShape
import kpt.core.model.search.AccountResult
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkDivider
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.send.money.generated.resources.Res
import kpt.feature.send.money.generated.resources.feature_send_money_amount
import kpt.feature.send.money.generated.resources.feature_send_money_bottom_bar
import kpt.feature.send.money.generated.resources.feature_send_money_close
import kpt.feature.send.money.generated.resources.feature_send_money_loading
import kpt.feature.send.money.generated.resources.feature_send_money_no_accounts_found
import kpt.feature.send.money.generated.resources.feature_send_money_oops
import kpt.feature.send.money.generated.resources.feature_send_money_proceed
import kpt.feature.send.money.generated.resources.feature_send_money_scan_qr
import kpt.feature.send.money.generated.resources.feature_send_money_selected
import kpt.feature.send.money.generated.resources.feature_send_money_send
import kpt.feature.send.money.generated.resources.feature_send_money_something_went_wrong
import kpt.feature.send.money.generated.resources.feature_send_money_to_account
import kpt.feature.send.money.generated.resources.feature_send_money_vpa_mobile_account_number
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
/** Screen for entering an amount and sending money to a selected payee. */
@Composable
fun SendMoneyScreen(
    onBackClick: () -> Unit,
    navigateToTransferScreen: (String) -> Unit,
    navigateToPayeeDetails: (String) -> Unit,
    navigateToScanQrScreen: () -> Unit,
    showTopBar: Boolean = true,
    modifier: Modifier = Modifier,
    viewModel: SendMoneyViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountListState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            SendMoneyEvent.OnNavigateBack -> onBackClick.invoke()

            is SendMoneyEvent.NavigateToTransferScreen -> {
                navigateToTransferScreen(event.data)
            }

            is SendMoneyEvent.NavigateToPayeeDetails -> {
                navigateToPayeeDetails(event.qrCodeData)
            }

            is SendMoneyEvent.NavigateToScanQrScreen -> navigateToScanQrScreen.invoke()

            is SendMoneyEvent.ShowToast -> {
                // TODO: Implement toast message display
                // For now, we'll just ignore it
            }
        }
    }

    SendMoneyDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SendMoneyAction.DismissDialog) }
        },
    )

    SendMoneyScreen(
        state = state,
        accountState = accountState,
        showTopBar = showTopBar,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun SendMoneyScreen(
    state: SendMoneyState,
    accountState: ViewState,
    showTopBar: Boolean,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onAction: (SendMoneyAction) -> Unit,
) {
    ForkGradientBackground {
        ForkScaffold(
            modifier = modifier,
            topBar = {
                AnimatedVisibility(
                    visible = showTopBar,
                ) {
                    ForkTopBar(
                        topBarTitle = stringResource(Res.string.feature_send_money_send),
                        backPress = {
                            onAction(SendMoneyAction.NavigateBack)
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onAction(SendMoneyAction.OnClickScan)
                                },
                            ) {
                                Icon(
                                    imageVector = ForkIcons.Scan,
                                    contentDescription = stringResource(Res.string.feature_send_money_scan_qr),
                                )
                            }
                        },
                    )
                }
            },
            bottomBar = {
                SendMoneyBottomBar(
                    showDetails = state.isProceedEnabled,
                    selectedAccount = state.selectedAccount,
                    onDeselect = {
                        onAction(SendMoneyAction.DeselectAccount)
                    },
                    onClickProceed = {
                        onAction(SendMoneyAction.OnProceedClicked)
                    },
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                state = lazyListState,
                contentPadding = PaddingValues(bottom = KptTheme.spacing.md),
            ) {
                stickyHeader {
                    SendMoneyCard(
                        state = state,
                        onAction = onAction,
                        modifier = Modifier.padding(bottom = KptTheme.spacing.sm),
                    )
                }

                accountListContent(
                    state = accountState,
                    onAction = onAction,
                    selected = { state.selectedAccount == it },
                )
            }
        }
    }
}

@Composable
private fun SendMoneyBottomBar(
    showDetails: Boolean,
    selectedAccount: AccountResult?,
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

            ForkButton(
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
    account: AccountResult,
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
                        text = account.parentName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                supportingContent = {
                    Text(text = maskString(account.entityAccountNo))
                },
                leadingContent = {
                    AvatarBox(icon = ForkIcons.Bank)
                },
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AccountBadge(
                            text = account.entityName,
                        )

                        AccountBadge(
                            text = account.entityType,
                            borderColor = KptTheme.colorScheme.secondary,
                        )

                        IconButton(
                            onClick = onDeselect,
                        ) {
                            Icon(
                                imageVector = ForkIcons.Close,
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

@Composable
private fun AccountBadge(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color = KptTheme.colorScheme.primary,
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = KptTheme.shapes.extraSmall,
    ) {
        Text(
            text = text,
            style = KptTheme.typography.labelSmall,
            modifier = Modifier.padding(KptTheme.spacing.xs),
        )
    }
}

@Composable
private fun SendMoneyCard(
    state: SendMoneyState,
    modifier: Modifier = Modifier,
    onAction: (SendMoneyAction) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = KptTheme.shapes.toRoundedCornerShape(
            bottomStart = KptTheme.spacing.md,
            bottomEnd = KptTheme.spacing.md,
        ),
    ) {
        Column(
            modifier = modifier.fillMaxWidth().padding(KptTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            ForkTextField(
                label = stringResource(Res.string.feature_send_money_amount),
                value = state.amount,
                isError = !state.amountIsValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                onValueChange = remember(onAction) {
                    { onAction(SendMoneyAction.AmountChanged(it)) }
                },
            )

            ForkTextField(
                label = stringResource(Res.string.feature_send_money_vpa_mobile_account_number),
                value = state.accountNumber,
                onValueChange = remember(onAction) {
                    { onAction(SendMoneyAction.AccountNumberChanged(it)) }
                },
            )
        }
    }
}

private fun LazyListScope.accountListContent(
    state: ViewState,
    selected: (AccountResult) -> Boolean,
    onAction: (SendMoneyAction.SelectAccount) -> Unit,
) {
    when (state) {
        is ViewState.Loading -> {
            item {
                Box(
                    modifier = Modifier.fillParentMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    ForkLoadingWheel(contentDesc = stringResource(Res.string.feature_send_money_loading))
                }
            }
        }

        is ViewState.Error -> {
            item {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_send_money_oops),
                    subTitle = stringResource(Res.string.feature_send_money_something_went_wrong),
                    modifier = Modifier.fillParentMaxSize(),
                    iconTint = KptTheme.colorScheme.error,
                )
            }
        }

        is ViewState.Empty -> {
            item {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_send_money_oops),
                    subTitle = stringResource(Res.string.feature_send_money_no_accounts_found),
                    modifier = Modifier.fillParentMaxSize(),
                )
            }
        }

        is ViewState.Content -> {
            itemsIndexed(
                items = state.data,
                key = { _, it -> it.entityId },
            ) { i, account ->
                AccountCard(
                    account = account,
                    selected = selected,
                    onClick = remember(account) {
                        { onAction(SendMoneyAction.SelectAccount(it)) }
                    },
                )

                if (i < state.data.lastIndex) {
                    ForkDivider()
                }
            }
        }

        is ViewState.InitialEmpty -> Unit
    }
}

@Composable
private fun AccountCard(
    account: AccountResult,
    selected: (AccountResult) -> Boolean,
    modifier: Modifier = Modifier,
    onClick: (AccountResult) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = account.parentName)
        },
        supportingContent = {
            Text(text = maskString(account.entityAccountNo))
        },
        leadingContent = {
            AvatarBox(
                icon = ForkIcons.Bank,
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
                    imageVector = ForkIcons.Check,
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
private fun SendMoneyDialogs(
    dialogState: SendMoneyState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is SendMoneyState.DialogState.Error.ResourceMessage -> ForkBasicDialog(
            visibilityState = Shown(
                message = stringResource(dialogState.message),
            ),
            onDismissRequest = onDismissRequest,
        )

        is SendMoneyState.DialogState.Error.GenericResourceMessage -> ForkBasicDialog(
            visibilityState = Shown(
                message = stringResource(
                    dialogState.message,
                    *dialogState.args.toTypedArray(),
                ),
            ),
            onDismissRequest = onDismissRequest,
        )

        is SendMoneyState.DialogState.Loading -> ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}
