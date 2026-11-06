/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.account.Account
import kpt.core.model.savingsaccount.Status
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.MifosSmallChip
import kpt.core.ui.RevealDirection
import kpt.core.ui.RevealSwipe
import kpt.core.ui.rememberRevealState
import kpt.core.ui.utils.EventsEffect
import kpt.feature.accounts.savingsaccount.SavingsAddEditType
import kpt.feature.beneficiary.addupdatebeneficiary.BeneficiaryAddEditType
import mifos_pay.feature.accounts.generated.resources.Res
import mifos_pay.feature.accounts.generated.resources.baseline_check
import mifos_pay.feature.accounts.generated.resources.baseline_unchecked
import mifos_pay.feature.accounts.generated.resources.feature_accounts_add
import mifos_pay.feature.accounts.generated.resources.feature_accounts_apply_for_loan
import mifos_pay.feature.accounts.generated.resources.feature_accounts_check
import mifos_pay.feature.accounts.generated.resources.feature_accounts_default
import mifos_pay.feature.accounts.generated.resources.feature_accounts_edit
import mifos_pay.feature.accounts.generated.resources.feature_accounts_error_oops
import mifos_pay.feature.accounts.generated.resources.feature_accounts_info
import mifos_pay.feature.accounts.generated.resources.feature_accounts_savings_account
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_active
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_approved
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_closed
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_matured
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_pending_approval
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_prematurely_closed
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_rejected
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_transfer_in_progress
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_transfer_on_hold
import mifos_pay.feature.accounts.generated.resources.feature_accounts_status_withdrawn
import mifos_pay.feature.accounts.generated.resources.feature_accounts_unexpected_error_subtitle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun AccountsScreen(
    onViewSavingAccountDetails: (Long) -> Unit,
    onAddEditSavingsAccount: (SavingsAddEditType) -> Unit,
    onAddOrEditBeneficiary: (BeneficiaryAddEditType) -> Unit,
    onApplyForLoanClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accountState by viewModel.accountState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AccountEvent.OnAddEditSavingsAccount -> {
                onAddEditSavingsAccount(event.type)
            }

            is AccountEvent.OnNavigateToAccountDetail -> {
                onViewSavingAccountDetails(event.accountId)
            }

            is AccountEvent.OnAddOrEditTPTBeneficiary -> {
                onAddOrEditBeneficiary(event.type)
            }

            is AccountEvent.OnApplyForLoan -> {
                onApplyForLoanClick(event.clientId)
            }

            is AccountEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }
        }
    }

    AccountDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(AccountAction.DismissDialog) }
        },
    )

    AccountsScreenContent(
        defaultAccountId = state.defaultAccountId,
        state = accountState,
        onAction = viewModel::trySendAction,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
internal fun AccountsScreenContent(
    defaultAccountId: Long?,
    state: AccountState.ViewState,
    onAction: (AccountAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.hasFab,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        onAction(AccountAction.CreateSavingsAccount)
                    },
                ) {
                    Icon(
                        imageVector = MifosIcons.Add,
                        stringResource(Res.string.feature_accounts_add),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is AccountState.ViewState.Loading -> MifosProgressIndicator()

                is AccountState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_accounts_error_oops),
                        subTitle = stringResource(Res.string.feature_accounts_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is AccountState.ViewState.Content -> {
                    AccountsScreenContent(
                        state = state,
                        defaultAccountId = defaultAccountId,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AccountsScreenContent(
    defaultAccountId: Long?,
    state: AccountState.ViewState.Content,
    onAction: (AccountAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AccountsList(
        modifier = modifier,
        defaultAccountId = defaultAccountId,
        accounts = state.accounts,
        onAccountClicked = { accId, accNo ->
            onAction(AccountAction.SetDefaultAccount(accId, accNo))
        },
        onClickEditAccount = {
            onAction(AccountAction.EditSavingsAccount(it))
        },
        onClickViewAccount = {
            onAction(AccountAction.ViewAccountDetails(it))
        },
        onApplyForLoanClick = {
            onAction(AccountAction.ApplyForLoan)
        },
    )
}

@Composable
private fun AccountsList(
    defaultAccountId: Long?,
    accounts: List<Account>,
    onAccountClicked: (Long, String) -> Unit,
    onClickEditAccount: (Long) -> Unit,
    onClickViewAccount: (Long) -> Unit,
    onApplyForLoanClick: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        item {
            ApplyForLoanCard(onClick = onApplyForLoanClick)
        }

        item {
            Text(
                text = stringResource(Res.string.feature_accounts_savings_account),
                style = KptTheme.typography.labelLarge,
            )
        }

        items(
            items = accounts,
            key = { "account_${it.id}" },
        ) { account ->
            AccountItem(
                account = account,
                isDefault = defaultAccountId == account.id,
                onClick = onAccountClicked,
                onClickEditAccount = onClickEditAccount,
                onClickViewAccount = onClickViewAccount,
            )
        }
    }
}

@Composable
private fun AccountItem(
    account: Account,
    isDefault: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Long, String) -> Unit,
    onClickEditAccount: (Long) -> Unit,
    onClickViewAccount: (Long) -> Unit,
) {
    val state = rememberRevealState(
        maxRevealDp = if (account.status.submittedAndPendingApproval) 105.dp else 75.dp,
        directions = setOf(RevealDirection.EndToStart),
    )

    RevealSwipe(
        modifier = modifier,
        state = state,
        shape = KptTheme.shapes.small,
        backgroundCardStartColor = KptTheme.colorScheme.tertiary,
        backgroundCardEndColor = KptTheme.colorScheme.secondary,
        backgroundStartActionLabel = null,
        backgroundEndActionLabel = stringResource(Res.string.feature_accounts_edit),
        card = { shape, content ->
            Card(
                modifier = Modifier.matchParentSize(),
                colors = CardDefaults.cardColors(
                    contentColor = KptTheme.colorScheme.onSecondary,
                    containerColor = Color.Transparent,
                ),
                shape = shape,
                content = content,
            )
        },
        hiddenContentEnd = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(
                    KptTheme.spacing.sm,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(
                    visible = account.status.submittedAndPendingApproval,
                ) {
                    IconButton(
                        onClick = { onClickEditAccount(account.id) },
                    ) {
                        Icon(
                            imageVector = MifosIcons.Edit2,
                            contentDescription = stringResource(Res.string.feature_accounts_edit),
                        )
                    }
                }

                IconButton(
                    onClick = { onClickViewAccount(account.id) },
                ) {
                    Icon(
                        imageVector = MifosIcons.Info,
                        contentDescription = stringResource(Res.string.feature_accounts_info),
                    )
                }
            }
        },
        onContentClick = if (account.status.active) {
            { onClick(account.id, account.number) }
        } else {
            null
        },
    ) {
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = it,
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.Transparent,
                contentColor = KptTheme.colorScheme.onSurface,
            ),
        ) {
            ListItem(
                headlineContent = {
                    Text(text = account.name)
                },
                supportingContent = {
                    Text(text = account.number)
                },
                leadingContent = {
                    AvatarBox(
                        icon = MifosIcons.Bank,
                        backgroundColor = KptTheme.colorScheme.secondaryContainer,
                    )
                },
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SavingAccountStatusCard(account.status)

                        AnimatedVisibility(isDefault) {
                            OutlinedCard(
                                onClick = {},
                                shape = KptTheme.shapes.extraSmall,
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = KptTheme.colorScheme.tertiary,
                                ),
                            ) {
                                Text(
                                    text = stringResource(Res.string.feature_accounts_default),
                                    style = KptTheme.typography.bodySmall,
                                    modifier = Modifier.padding(KptTheme.spacing.xs),
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isDefault) {
                                vectorResource(Res.drawable.baseline_check)
                            } else {
                                vectorResource(Res.drawable.baseline_unchecked)
                            },
                            contentDescription = stringResource(Res.string.feature_accounts_check),
                        )
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
private fun ApplyForLoanCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.secondaryContainer,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(text = stringResource(Res.string.feature_accounts_apply_for_loan))
            },
            leadingContent = {
                AvatarBox(
                    icon = MifosIcons.CreditCard,
                    backgroundColor = KptTheme.colorScheme.tertiaryContainer,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun AccountDialogs(
    dialogState: AccountState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is AccountState.DialogState.DeleteBeneficiary -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
            onConfirm = dialogState.onConfirm,
            onDismissRequest = onDismissRequest,
        )

        is AccountState.DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is AccountState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavingAccountStatusCard(
    status: Status,
    modifier: Modifier = Modifier,
) {
    val activeStatus = SavingAccountStatus.entries.filter { it.isActive(status) }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        modifier = modifier,
    ) {
        activeStatus.forEach { statusEnum ->
            StatusChip(
                label = stringResource(statusEnum.labelRes),
                color = statusEnum.color,
            )
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    MifosSmallChip(
        label = label,
        containerColor = color,
        contentColor = KptTheme.colorScheme.scrim,
    )
}

enum class SavingAccountStatus(
    val isActive: (Status) -> Boolean,
    val labelRes: StringResource,
) {
    PendingApproval(
        { it.submittedAndPendingApproval },
        Res.string.feature_accounts_status_pending_approval,
    ),
    Approved({ it.approved }, Res.string.feature_accounts_status_approved),
    Rejected({ it.rejected }, Res.string.feature_accounts_status_rejected),
    Withdrawn({ it.withdrawnByApplicant }, Res.string.feature_accounts_status_withdrawn),
    Active({ it.active }, Res.string.feature_accounts_status_active),
    Closed({ it.closed }, Res.string.feature_accounts_status_closed),
    PrematureClosed({ it.prematureClosed }, Res.string.feature_accounts_status_prematurely_closed),
    TransferInProgress(
        { it.transferInProgress },
        Res.string.feature_accounts_status_transfer_in_progress,
    ),
    TransferOnHold({ it.transferOnHold }, Res.string.feature_accounts_status_transfer_on_hold),
    Matured({ it.matured }, Res.string.feature_accounts_status_matured),
}

val SavingAccountStatus.color: Color
    @Composable get() = when (this) {
        SavingAccountStatus.PendingApproval -> KptTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        SavingAccountStatus.Approved -> KptTheme.colorScheme.tertiaryContainer
        SavingAccountStatus.Rejected -> KptTheme.colorScheme.errorContainer
        SavingAccountStatus.Withdrawn -> KptTheme.colorScheme.secondaryContainer
        SavingAccountStatus.Active -> KptTheme.colorScheme.primaryContainer
        SavingAccountStatus.Closed -> KptTheme.colorScheme.surfaceVariant
        SavingAccountStatus.PrematureClosed -> KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        SavingAccountStatus.TransferInProgress -> KptTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        SavingAccountStatus.TransferOnHold -> KptTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        SavingAccountStatus.Matured -> KptTheme.colorScheme.tertiaryContainer
    }
