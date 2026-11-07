/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.interbank

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosDialogBox
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.ui.MifosProgressIndicatorOverlay
import kpt.core.ui.generated.resources.core_ui_add_to_pocket_message
import kpt.core.ui.generated.resources.core_ui_add_to_pocket_title
import kpt.core.ui.generated.resources.core_ui_error
import kpt.core.ui.generated.resources.core_ui_no
import kpt.core.ui.generated.resources.core_ui_ok
import kpt.core.ui.generated.resources.core_ui_success
import kpt.core.ui.generated.resources.core_ui_yes
import kpt.core.ui.utils.EventsEffect
import kpt.feature.transfer.interbank.screens.PreviewTransferScreen
import kpt.feature.transfer.interbank.screens.SearchRecipientScreen
import kpt.feature.transfer.interbank.screens.SelectAccountScreen
import kpt.feature.transfer.interbank.screens.TransferDetailsScreen
import kpt.feature.transfer.interbank.screens.TransferFailedScreen
import kpt.feature.transfer.interbank.screens.TransferSuccessScreen
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kpt.core.ui.generated.resources.Res as UiRes

/**
 * Main orchestrator screen for the interbank transfer flow
 * Manages navigation between all steps of the transfer process
 */
@Composable
fun InterbankTransferFlowScreen(
    onBackClick: () -> Unit,
    onTransferSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    initialPhoneNumber: String? = null,
    initialAmount: String? = null,
    viewModel: InterbankTransferViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf(initialPhoneNumber ?: "") }

    // Pre-fill amount if provided
    LaunchedEffect(initialAmount) {
        initialAmount?.let { amount ->
            if (amount.isNotBlank()) {
                viewModel.trySendAction(InterbankTransferAction.UpdateAmount(amount))
            }
        }
    }

    EventsEffect(viewModel) { event ->
        when (event) {
            InterbankTransferEvent.OnNavigateBack -> onBackClick()
            else -> {
                // Other steps don't require navigation
            }
        }
    }

    when (state.currentStep) {
        InterbankTransferState.Step.SelectAccount -> {
            SelectAccountScreen(
                accounts = state.fromAccounts,
                isLoading = state.loadingState is InterbankTransferState.LoadingState.Loading,
                error = (state.loadingState as? InterbankTransferState.LoadingState.Error)?.message,
                onAccountSelected = { account ->
                    viewModel.trySendAction(
                        InterbankTransferAction.NavigateToRecipientSearch(account),
                    )
                },
                onBackClick = {
                    viewModel.trySendAction(InterbankTransferAction.NavigateBack)
                },
                modifier = modifier,
            )
        }

        InterbankTransferState.Step.SearchRecipient -> {
            val searchState = state.searchRecipientState
            val isSearching = searchState is InterbankTransferState.SearchRecipientState.Loading
            val searchError = (searchState as? InterbankTransferState.SearchRecipientState.Error)?.message

            SearchRecipientScreen(
                searchQuery = searchQuery,
                onSearchQueryChanged = { query ->
                    searchQuery = query
                },
                recipients = state.searchResults,
                onRecipientSelected = { participantInfo ->
                    viewModel.trySendAction(
                        InterbankTransferAction.NavigateToTransferDetails(participantInfo),
                    )
                },
                onSearchClick = { phoneNumber ->
                    viewModel.trySendAction(
                        InterbankTransferAction.SearchRecipient(phoneNumber),
                    )
                },
                isSearching = isSearching,
                searchError = searchError,
                onBackClick = {
                    viewModel.trySendAction(InterbankTransferAction.NavigateBack)
                },
                modifier = modifier,
            )
        }

        InterbankTransferState.Step.TransferDetails -> {
            TransferDetailsScreen(
                fromAccount = state.selectedFromAccount,
                recipient = state.selectedParticipantInfo,
                amount = state.transferAmount,
                onAmountChanged = { amount ->
                    viewModel.trySendAction(InterbankTransferAction.UpdateAmount(amount))
                },
                date = state.transferDate,
                description = state.transferDescription,
                onDescriptionChanged = { desc ->
                    viewModel.trySendAction(InterbankTransferAction.UpdateDescription(desc))
                },
                onContinueClick = {
                    viewModel.trySendAction(InterbankTransferAction.NavigateToPreview)
                },
                onBackClick = {
                    viewModel.trySendAction(InterbankTransferAction.NavigateBack)
                },
                onEditFromAccount = {
                    viewModel.trySendAction(InterbankTransferAction.EditFromAccount)
                },
                onEditRecipient = {
                    viewModel.trySendAction(InterbankTransferAction.EditRecipient)
                },
                modifier = modifier,
            )
        }

        InterbankTransferState.Step.PreviewTransfer -> {
            PreviewTransferScreen(
                amount = state.transferAmount,
                transferDate = state.transferDate,
                transferDescription = state.transferDescription,
                fromAccountName = state.selectedFromAccount?.name ?: "Unknown",
                fromAccountNo = state.selectedFromAccount?.number ?: "N/A",
                fromAccountBalance = state.selectedFromAccount?.balance ?: 0.0,
                fromAccountType = "${state.selectedFromAccount?.accountType?.value ?: ""} | ${state.selectedFromAccount?.currency?.name ?: ""}".trim(),
                recipientInfo = state.selectedParticipantInfo,
                isProcessing = state.isProcessing,
                currencyCode = state.selectedFromAccount?.currency?.code ?: "MXN",
                onEditClick = {
                    viewModel.trySendAction(InterbankTransferAction.NavigateBack)
                },
                onConfirmClick = {
                    viewModel.trySendAction(InterbankTransferAction.ConfirmTransfer)
                },
                onBackClick = {
                    viewModel.trySendAction(InterbankTransferAction.NavigateBack)
                },
                modifier = modifier,
            )
        }

        InterbankTransferState.Step.TransferSuccess -> {
            TransferSuccessScreen(
                recipientName = "${state.selectedParticipantInfo?.firstName ?: ""} ${state.selectedParticipantInfo?.lastName ?: ""}".trim().ifEmpty { "Recipient" },
                amount = state.transferAmount,
                transactionReference = state.transferResponse ?: "N/A",
                fromAccount = state.selectedFromAccount?.number ?: "N/A",
                fromAccountName = state.selectedFromAccount?.name ?: "Unknown",
                toAccount = "${state.selectedParticipantInfo?.firstName ?: ""} ${state.selectedParticipantInfo?.lastName ?: ""}".trim(),
                toAccountNumber = "Account: ${state.selectedParticipantInfo?.partyId ?: "N/A"}",
                transactionDate = state.transferDate,
                description = state.transferDescription,
                currencyCode = state.selectedFromAccount?.currency?.code ?: "MXN",
                onBackToHome = onTransferSuccess,
                modifier = modifier,
            )
        }

        InterbankTransferState.Step.TransferFailed -> {
            TransferFailedScreen(
                errorMessage = state.errorMessage ?: "Unknown error occurred",
                errorTitle = "Transfer Failed",
                attemptedAmount = CurrencyFormatter.format(
                    state.transferAmount.toDoubleOrNull() ?: 0.0,
                    state.selectedFromAccount?.currency?.code ?: "MXN",
                    null,
                ),
                availableBalance = CurrencyFormatter.format(
                    state.selectedFromAccount?.balance ?: 0.0,
                    state.selectedFromAccount?.currency?.code ?: "MXN",
                    null,
                ),
                fromAccount = state.selectedFromAccount?.number ?: "N/A",
                fromAccountName = state.selectedFromAccount?.name ?: "Unknown",
                toAccount = "${state.selectedParticipantInfo?.firstName ?: ""} ${state.selectedParticipantInfo?.lastName ?: ""}".trim(),
                toAccountNumber = "Account: ${state.selectedParticipantInfo?.partyId ?: "N/A"}",
                transactionDate = state.transferDate,
                description = state.transferDescription,
                onRetry = {
                    viewModel.trySendAction(InterbankTransferAction.RetryTransfer)
                },
                onBackToHome = onBackClick,
                modifier = modifier,
            )
        }
    }
    when (val dialogState = state.dialogState) {
        is InterbankTransferState.DialogState.AddToPocketConfirmation -> {
            MifosDialogBox(
                showDialogState = true,
                onDismiss = { viewModel.trySendAction(InterbankTransferAction.ConfirmAddToPocket(false)) },
                title = stringResource(UiRes.string.core_ui_add_to_pocket_title),
                message = stringResource(UiRes.string.core_ui_add_to_pocket_message),
                confirmButtonText = stringResource(UiRes.string.core_ui_yes),
                onConfirm = { viewModel.trySendAction(InterbankTransferAction.ConfirmAddToPocket(true)) },
                dismissButtonText = stringResource(UiRes.string.core_ui_no),
            )
        }
        is InterbankTransferState.DialogState.ErrorResource -> {
            MifosDialogBox(
                showDialogState = true,
                onDismiss = { viewModel.trySendAction(InterbankTransferAction.DismissDialog) },
                title = stringResource(UiRes.string.core_ui_error),
                message = dialogState.message,
                confirmButtonText = stringResource(UiRes.string.core_ui_ok),
                onConfirm = { viewModel.trySendAction(InterbankTransferAction.DismissDialog) },
            )
        }
        is InterbankTransferState.DialogState.Success -> {
            MifosDialogBox(
                showDialogState = true,
                onDismiss = { viewModel.trySendAction(InterbankTransferAction.DismissDialog) },
                title = stringResource(UiRes.string.core_ui_success),
                message = dialogState.message,
                confirmButtonText = stringResource(UiRes.string.core_ui_ok),
                onConfirm = { viewModel.trySendAction(InterbankTransferAction.DismissDialog) },
            )
        }
        is InterbankTransferState.DialogState.Loading -> {
            MifosProgressIndicatorOverlay()
        }
        null -> Unit
    }
}

/** Compose preview of [InterbankTransferFlowScreen]. */
@Preview
@Composable
fun InterbankTransferFlowScreenPreview() {
    MifosTheme {
        InterbankTransferFlowScreen(
            onBackClick = {},
            onTransferSuccess = {},
        )
    }
}
