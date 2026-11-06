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
import kpt.core.designsystem.component.BasicDialogState.Shown
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosLoadingWheel
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.toRoundedCornerShape
import kpt.core.model.search.AccountResult
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosDivider
import kpt.core.ui.utils.EventsEffect
import mifos_pay.feature.send_money.generated.resources.Res
import mifos_pay.feature.send_money.generated.resources.feature_send_money_amount
import mifos_pay.feature.send_money.generated.resources.feature_send_money_bottom_bar
import mifos_pay.feature.send_money.generated.resources.feature_send_money_close
import mifos_pay.feature.send_money.generated.resources.feature_send_money_loading
import mifos_pay.feature.send_money.generated.resources.feature_send_money_no_accounts_found
import mifos_pay.feature.send_money.generated.resources.feature_send_money_oops
import mifos_pay.feature.send_money.generated.resources.feature_send_money_proceed
import mifos_pay.feature.send_money.generated.resources.feature_send_money_scan_qr
import mifos_pay.feature.send_money.generated.resources.feature_send_money_selected
import mifos_pay.feature.send_money.generated.resources.feature_send_money_send
import mifos_pay.feature.send_money.generated.resources.feature_send_money_something_went_wrong
import mifos_pay.feature.send_money.generated.resources.feature_send_money_to_account
import mifos_pay.feature.send_money.generated.resources.feature_send_money_vpa_mobile_account_number
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                AnimatedVisibility(
                    visible = showTopBar,
                ) {
                    MifosTopBar(
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
                                    imageVector = MifosIcons.Scan,
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
                    AvatarBox(icon = MifosIcons.Bank)
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
                                imageVector = MifosIcons.Close,
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
            MifosTextField(
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

            MifosTextField(
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
                    MifosLoadingWheel(contentDesc = stringResource(Res.string.feature_send_money_loading))
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
                    MifosDivider()
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
private fun SendMoneyDialogs(
    dialogState: SendMoneyState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is SendMoneyState.DialogState.Error.ResourceMessage -> MifosBasicDialog(
            visibilityState = Shown(
                message = stringResource(dialogState.message),
            ),
            onDismissRequest = onDismissRequest,
        )

        is SendMoneyState.DialogState.Error.GenericResourceMessage -> MifosBasicDialog(
            visibilityState = Shown(
                message = stringResource(
                    dialogState.message,
                    *dialogState.args.toTypedArray(),
                ),
            ),
            onDismissRequest = onDismissRequest,
        )

        is SendMoneyState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}
