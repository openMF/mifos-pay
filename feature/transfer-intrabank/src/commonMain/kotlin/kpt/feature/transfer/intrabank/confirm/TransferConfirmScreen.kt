/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.confirm

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkBottomSheetScaffold
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkDialogBox
import kpt.core.designsystem.fork.component.ForkTextField
import kpt.core.designsystem.fork.component.ForkTopBar
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.model.network.entity.templates.account.AccountOption
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ErrorBottomSheet
import kpt.core.ui.ErrorType
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.ForkProgressIndicatorOverlay
import kpt.core.ui.fork.utils.EventsEffect
import kpt.core.ui.generated.resources.core_ui_add_to_pocket_message
import kpt.core.ui.generated.resources.core_ui_add_to_pocket_title
import kpt.core.ui.generated.resources.core_ui_error
import kpt.core.ui.generated.resources.core_ui_no
import kpt.core.ui.generated.resources.core_ui_ok
import kpt.core.ui.generated.resources.core_ui_success
import kpt.core.ui.generated.resources.core_ui_yes
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_amount
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_amount_error
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_available_balance
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_check_icon_description
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_continue_button
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_description_label
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_from_account
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_from_account_title
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_no_accounts_found
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_oops_title
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_review_title
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_show_balance
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_to_account
import kpt.feature.transfer.intrabank.generated.resources.feature_transfer_cancel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kpt.core.ui.generated.resources.Res as UiRes

/**
 * Intra-bank transfer confirmation route. Owns the **passcode/biometric
 * auth-gate plumbing** for MW-390:
 *
 *  - Observes [entryStateHandle] for [INTRA_BANK_TRANSFER_VERIFICATION_KEY]
 *    (written by `internalPasscodeScreen` on success/failure) and
 *    re-dispatches as [TransferConfirmAction.UpdateUserVerificationResult]
 *    so the suspended `handleUserVerification` in
 *    [TransferConfirmViewModel] resumes.
 *  - Cancellation guard: if the user backs out of the passcode screen
 *    without verifying (the saved-state-handle stays null but
 *    `isAwaitingPasscodeVerification` is true), the `repeatOnLifecycle`
 *    block dispatches `UpdateUserVerificationResult(false)` on RESUMED so
 *    the VM doesn't hang forever.
 *  - Routes [TransferConfirmEvent.NavigateForPasscodeVerification] to
 *    [navigateForPasscodeVerification] with
 *    [INTRA_BANK_TRANSFER_VERIFICATION_KEY] — caller binds this to
 *    `navController::navigateToInternalPasscodeScreen`.
 *
 * @param navigateForPasscodeVerification `(verificationKey) -> Unit` —
 *        caller binds to `navController::navigateToInternalPasscodeScreen`.
 *        The verificationKey passed in is the same constant the screen
 *        observes on its saved-state-handle, so the round trip closes.
 * @param entryStateHandle The transfer-confirm destination's own
 *        `SavedStateHandle`, hoisted from the nav-graph builder so the
 *        round-trip channel works.
 */
@Composable
internal fun TransferConfirmScreen(
    navigateBack: () -> Unit,
    onTransferSuccess: (TransferResult) -> Unit,
    navigateForPasscodeVerification: (String) -> Unit,
    entryStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    viewModel: TransferConfirmViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val authResult by entryStateHandle
        .getStateFlow<Boolean?>(INTRA_BANK_TRANSFER_VERIFICATION_KEY, null)
        .collectAsStateWithLifecycle()

    LaunchedEffect(authResult) {
        authResult?.let { result ->
            entryStateHandle.remove<Boolean>(INTRA_BANK_TRANSFER_VERIFICATION_KEY)
            viewModel.trySendAction(TransferConfirmAction.UpdateUserVerificationResult(result))
        }
    }

    LaunchedEffect(state.transferResult) {
        state.transferResult?.let { result ->
            onTransferSuccess(result)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (authResult == null && state.isAwaitingPasscodeVerification) {
                viewModel.trySendAction(
                    TransferConfirmAction.UpdateUserVerificationResult(false),
                )
            }
        }
    }

    EventsEffect(viewModel) { event ->
        when (event) {
            TransferConfirmEvent.OnNavigateBack -> navigateBack.invoke()
            TransferConfirmEvent.NavigateForPasscodeVerification -> {
                navigateForPasscodeVerification(INTRA_BANK_TRANSFER_VERIFICATION_KEY)
            }
        }
    }

    TransferConfirmDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(TransferConfirmAction.DismissDialog) }
        },
        onRetry = remember(viewModel) {
            { viewModel.trySendAction(TransferConfirmAction.RetryTransfer) }
        },
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )

    TransferConfirmScreen(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun TransferConfirmDialogs(
    dialogState: TransferConfirmState.DialogState?,
    onDismissRequest: () -> Unit,
    onRetry: () -> Unit,
    onAction: (TransferConfirmAction) -> Unit = {},
) {
    when (dialogState) {
        is TransferConfirmState.DialogState.AddToPocketConfirmation -> {
            ForkDialogBox(
                showDialogState = true,
                onDismiss = { onAction(TransferConfirmAction.ConfirmAddToPocket(false)) },
                title = stringResource(UiRes.string.core_ui_add_to_pocket_title),
                message = stringResource(UiRes.string.core_ui_add_to_pocket_message),
                confirmButtonText = stringResource(UiRes.string.core_ui_yes),
                onConfirm = { onAction(TransferConfirmAction.ConfirmAddToPocket(true)) },
                dismissButtonText = stringResource(UiRes.string.core_ui_no),
            )
        }
        is TransferConfirmState.DialogState.Error.ResourceMessage -> {
            ForkDialogBox(
                showDialogState = true,
                onDismiss = { onAction(TransferConfirmAction.DismissDialog) },
                title = stringResource(UiRes.string.core_ui_error),
                message = dialogState.message,
                confirmButtonText = stringResource(UiRes.string.core_ui_ok),
                onConfirm = { onAction(TransferConfirmAction.DismissDialog) },
            )
        }
        is TransferConfirmState.DialogState.Success -> {
            ForkDialogBox(
                showDialogState = true,
                onDismiss = { onAction(TransferConfirmAction.DismissDialog) },
                title = stringResource(UiRes.string.core_ui_success),
                message = dialogState.message,
                confirmButtonText = stringResource(UiRes.string.core_ui_ok),
                onConfirm = { onAction(TransferConfirmAction.DismissDialog) },
            )
        }
        is TransferConfirmState.DialogState.Error.ValidationError -> {
            ErrorBottomSheet(
                title = stringResource(Res.string.feature_make_transfer_oops_title),
                message = stringResource(dialogState.message),
                errorType = ErrorType.VALIDATION,
                onDismiss = onDismissRequest,
                dismissButtonText = stringResource(Res.string.feature_transfer_cancel),
            )
        }

        is TransferConfirmState.DialogState.Error.ApiError -> {
            val error = dialogState.error
            val errorType = when {
                error.errorCode in 500..599 -> ErrorType.SERVER
                error.errorCode in 400..499 -> ErrorType.CLIENT
                error.errorCode == null && error.canRetry -> ErrorType.NETWORK
                else -> ErrorType.GENERIC
            }

            ErrorBottomSheet(
                error = error,
                errorType = errorType,
                onDismiss = onDismissRequest,
                onRetry = onRetry,
            )
        }

        TransferConfirmState.DialogState.Loading -> {
            ForkProgressIndicatorOverlay()
        }

        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransferConfirmScreen(
    state: TransferConfirmState,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onAction: (TransferConfirmAction) -> Unit,
) {
    ForkBottomSheetScaffold(
        topBar = {
            ForkTopBar(
                topBarTitle = stringResource(Res.string.feature_make_transfer_review_title),
                backPress = {
                    onAction(TransferConfirmAction.NavigateBack)
                },
            )
        },
        sheetPeekHeight = if (state.showBottomSheet) 200.dp else 0.dp,
        sheetContent = {
            if (state.showBottomSheet) {
                AccountList(
                    accounts = state.fromAccountOptions ?: emptyList(),
                    selected = remember(state) {
                        { state.selectedAccount == it }
                    },
                    onClick = {
                        onAction(TransferConfirmAction.SelectAccount(it))
                    },
                    balanceMap = state.balanceMap,
                )
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        when (state.state) {
            is TransferConfirmState.State.Error -> {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_make_transfer_oops_title),
                    subTitle = stringResource(Res.string.feature_make_transfer_no_accounts_found),
                    iconTint = KptTheme.colorScheme.error,
                )
            }
            TransferConfirmState.State.Loading -> {
                ForkProgressIndicator()
            }
            TransferConfirmState.State.NoAccounts -> {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_make_transfer_oops_title),
                    subTitle = stringResource(Res.string.feature_make_transfer_no_accounts_found),
                )
            }
            TransferConfirmState.State.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(KptTheme.spacing.md)
                        .imePadding(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                ) {
                    item {
                        ClientCard(
                            name = state.toAccountName,
                            account = state.toAccountNo,
                            onEdit = { onAction(TransferConfirmAction.NavigateBack) },
                        )
                    }

                    if (state.selectedAccount != null) {
                        item {
                            FromAccountCard(
                                account = state.selectedAccount,
                                onAction = onAction,
                                isOpened = state.showBottomSheet,
                                balance = state.selectedAccountBalance.toString(),
                            )
                        }
                    }

                    item {
                        EnterAmountCard(
                            state = state,
                            onAction = onAction,
                            onFocusChanged = {
                                onAction(TransferConfirmAction.CloseBottomSheet)
                            },
                        )
                    }

                    item {
                        ForkTextField(
                            label = stringResource(Res.string.feature_make_transfer_description_label),
                            value = state.description,
                            isError = !state.descriptionIsValid,
                            onValueChange = { onAction(TransferConfirmAction.DescriptionChanged(it)) },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                            ),
                            onFocusChanged = {
                                onAction(TransferConfirmAction.CloseBottomSheet)
                            },
                        )
                    }

                    item {
                        ForkButton(
                            onClick = { onAction(TransferConfirmAction.InitiateTransfer) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.amountIsValid && state.descriptionIsValid && !state.isProcessing,
                        ) {
                            if (state.isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(text = stringResource(Res.string.feature_make_transfer_continue_button))
                        }
                    }
                }

                // Show overlay when processing
                if (state.isProcessing) {
                    ForkProgressIndicatorOverlay()
                }
            }
        }
    }
}

@Composable
private fun FromAccountCard(
    account: AccountOption,
    balance: String,
    isOpened: Boolean,
    onAction: (TransferConfirmAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
            .clickable {
                if (isOpened) {
                    onAction(TransferConfirmAction.CloseBottomSheet)
                } else {
                    onAction(TransferConfirmAction.OpenBottomSheet)
                }
            },
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        ListItem(
            headlineContent = {
                BasicText(
                    text = stringResource(
                        Res.string.feature_make_transfer_from_account,
                        account.clientName ?: "",
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 6.sp,
                        maxFontSize = 16.sp,
                        stepSize = 1.sp,
                    ),
                    style = LocalTextStyle.current.copy(
                        color = KptTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                )
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(text = account.accountNo ?: "")
                    Text(text = stringResource(Res.string.feature_make_transfer_available_balance, balance))
                }
            },
            leadingContent = {
                AvatarBox(
                    icon = ForkIcons.Bank,
                    backgroundColor = KptTheme.colorScheme.surfaceContainerHigh,
                )
            },
            trailingContent = {
                if (isOpened) {
                    Icon(
                        imageVector = ForkIcons.KeyboardArrowUp,
                        contentDescription = stringResource(Res.string.feature_make_transfer_check_icon_description),
                    )
                } else {
                    Icon(
                        imageVector = ForkIcons.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.feature_make_transfer_check_icon_description),
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun EnterAmountCard(
    state: TransferConfirmState,
    onAction: (TransferConfirmAction) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onFocusChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        onFocusChanged(isFocused)
    }
    OutlinedCard(
        modifier = modifier.fillMaxWidth().then(
            if (!state.amountIsValid) {
                Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.medium,
                )
            } else {
                Modifier
            },
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_make_transfer_amount),
                style = KptTheme.typography.labelLarge,
            )

            TextField(
                leadingIcon = {
                    Text(
                        text = "$",
                        style = KptTheme.typography.headlineMedium,
                    )
                },
                value = state.amount,
                onValueChange = {
                    onAction(TransferConfirmAction.AmountChanged(it))
                },
                interactionSource = interactionSource,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
                isError = !state.amountIsValid,
                textStyle = KptTheme.typography.headlineMedium,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = KptTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )

            if ((state.amount.toDoubleOrNull() ?: 0.0) > state.selectedAccountBalance) {
                Spacer(modifier = Modifier.height(KptTheme.spacing.sm))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Icon(
                        imageVector = ForkIcons.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = stringResource(Res.string.feature_make_transfer_amount_error),
                        color = MaterialTheme.colorScheme.error,
                        style = KptTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountList(
    accounts: List<AccountOption?>,
    balanceMap: Map<String, Double>,
    selected: (AccountOption?) -> Boolean,
    modifier: Modifier = Modifier,
    onClick: (AccountOption?) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.feature_make_transfer_from_account_title),
            style = KptTheme.typography.labelLarge,
        )
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            items(items = accounts) { account ->
                AccountItem(
                    account = account,
                    selected = selected(account),
                    onClick = remember(account) {
                        { onClick(account) }
                    },
                    balance = balanceMap[account?.accountNo ?: ""].toString(),
                )
            }
        }
    }
}

@Composable
private fun AccountItem(
    account: AccountOption?,
    balance: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var revealBalance by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
        onClick = onClick,
    ) {
        ListItem(
            headlineContent = {
                Text(text = account?.clientName ?: "")
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = account?.accountNo ?: "")

                    if (revealBalance) {
                        Text(text = stringResource(Res.string.feature_make_transfer_available_balance, balance))
                    } else {
                        Text(
                            text = stringResource(Res.string.feature_make_transfer_show_balance),
                            color = KptTheme.colorScheme.primary,
                            style = KptTheme.typography.bodySmall,
                            modifier = Modifier.clickable { revealBalance = true },
                        )
                    }
                }
            },
            leadingContent = {
                AvatarBox(
                    icon = ForkIcons.Bank,
                    backgroundColor = KptTheme.colorScheme.surfaceContainerHigh,
                )
            },
            trailingContent = {
                AnimatedContent(
                    targetState = selected,
                    label = "radioAnim",
                ) { isSelected ->
                    Icon(
                        imageVector = if (isSelected) {
                            ForkIcons.RadioButtonChecked
                        } else {
                            ForkIcons.RadioButtonUnchecked
                        },
                        contentDescription = stringResource(
                            Res.string.feature_make_transfer_check_icon_description,
                        ),
                        tint = if (isSelected) {
                            KptTheme.colorScheme.primary
                        } else {
                            KptTheme.colorScheme.outlineVariant
                        },
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

/** Card showing a client's name and account number with an edit action. */
@Composable
fun ClientCard(
    name: String,
    account: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        ListItem(
            headlineContent = {
                BasicText(
                    text = stringResource(
                        Res.string.feature_make_transfer_to_account,
                        name,
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 6.sp,
                        maxFontSize = 16.sp,
                        stepSize = 1.sp,
                    ),
                    maxLines = 1,
                    style = LocalTextStyle.current.copy(
                        color = KptTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            },
            supportingContent = {
                Text(text = account)
            },
            leadingContent = {
                AvatarBox(
                    icon = ForkIcons.Bank,
                    backgroundColor = KptTheme.colorScheme.surfaceContainerHigh,
                )
            },
            trailingContent = {
                Icon(
                    imageVector = ForkIcons.Edit,
                    contentDescription = stringResource(
                        Res.string.feature_make_transfer_check_icon_description,
                    ),
                    modifier = Modifier.clickable { onEdit() },
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewMakeTransferLoading() {
    KptTheme {
        TransferConfirmScreen(
            state = TransferConfirmState(
                state = TransferConfirmState.State.Loading,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun PreviewMakeTransferNoAccounts() {
    KptTheme {
        TransferConfirmScreen(
            state = TransferConfirmState(
                state = TransferConfirmState.State.NoAccounts,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun PreviewMakeTransferSuccess() {
    KptTheme {
        TransferConfirmScreen(
            state = TransferConfirmState(
                state = TransferConfirmState.State.Success,
                toAccountName = "John Doe",
                toAccountNo = "123456789",
                amount = "250",
                description = "Payment for groceries",
                selectedAccount = AccountOption(
                    clientName = "Jane Smith",
                    accountNo = "987654321",
                ),
                selectedAccountBalance = 1000.0,
                balanceMap = mapOf("987654321" to 1000.0),
            ),
            onAction = {},
        )
    }
}
