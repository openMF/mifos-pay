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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.DateHelper
import kpt.core.common.ScreenState
import kpt.core.data.repository.InterBankRepository
import kpt.core.data.repository.PocketRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.account.Account
import kpt.core.model.client.Client
import kpt.core.model.enums.AccountType
import kpt.core.model.interbank.Amount
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.model.interbank.InterBankTransferRequest
import kpt.core.model.interbank.InterBankTransferResponse
import kpt.core.model.interbank.Party
import kpt.core.model.interbank.TransactionType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.core.ui.generated.resources.core_ui_error_msg_generic
import kpt.core.ui.generated.resources.core_ui_pocket_add_failed
import kpt.core.ui.generated.resources.core_ui_pocket_added_successfully
import kpt.feature.transfer.interbank.generated.resources.Res
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_amount_greater_than_zero
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_enter_amount
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_enter_description
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_failed_to_search_recipient
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_invalid_amount
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_no_network
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_phone_number_digits
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_select_recipient
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_select_sender_account
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_error_session_expired
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_no_recipients_found
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kpt.core.ui.generated.resources.Res as CoreUiRes

/** SavedStateHandle key used for the passcode verification round trip before an interbank transfer is confirmed. */
const val INTER_BANK_TRANSFER_VERIFICATION_KEY = "inter-banking_transfer_verification_key"

/**
 * ViewModel for managing interbank transfer flow
 * Handles all stages: account selection, recipient search, transfer details, preview, and confirmation
 */
class InterbankTransferViewModel(
    private val selfServiceRepository: SelfServiceRepository,
    private val interBankRepository: InterBankRepository,
    private val pocketRepository: PocketRepository,
    private val preferencesRepository: ForkPreferencesRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<InterbankTransferState, InterbankTransferEvent, InterbankTransferAction>(
    initialState = run {
        /** Client taken from the stored preferences; required to build the initial state. */
        val client = requireNotNull(preferencesRepository.client.value)
        InterbankTransferState(client = client)
    },
) {

    // Template idiom (core-base/store): the money-movement write goes through a
    // SubmitHandler instead of a hand-folded result action. The handler owns
    // the Submitting/Submitted/Failed lifecycle; we observe it to drive this flow's
    // existing processing/success/failed steps + events, so the Screens are unchanged.
    private val submitTransfer = viewModelScope.submitHandler<InterBankTransferResponse>()
    private val submitLink = viewModelScope.submitHandler<Unit>()

    init {
        launchIO {
            loadFromAccounts()
        }

        pocketRepository.observeLinkedPocketAccounts(state.client.id)
            .onEach { pocketAccounts ->
                val pocketAccountIds = pocketAccounts.filter { it.accountType == AccountType.SAVINGS }
                    .map { it.accountId }.toSet()
                /*
                 * Pocket Account Suggestion Feature:
                 * Sorts the accounts so that linked pocket accounts appear at the top.
                 */
                mutableStateFlow.update { state ->
                    val sortedAccounts = state.fromAccounts.sortedByDescending { it.id in pocketAccountIds }
                    state.copy(pocketAccountIds = pocketAccountIds, fromAccounts = sortedAccounts)
                }
            }
            .launchIn(viewModelScope)

        submitTransfer.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(isProcessing = true)
                        }
                    }

                    is SubmitState.Submitted -> {
                        val transferResponse = submitState.result
                        val responseMessage = "Transfer ID: ${transferResponse.transactionId}"
                        mutableStateFlow.update {
                            it.copy(
                                isProcessing = false,
                                currentStep = InterbankTransferState.Step.TransferSuccess,
                                transferResponse = responseMessage,
                            )
                        }
                        sendEvent(InterbankTransferEvent.OnTransferSuccess)
                        submitTransfer.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(
                                isProcessing = false,
                                currentStep = InterbankTransferState.Step.TransferFailed,
                                errorMessage = message,
                            )
                        }
                        sendEvent(InterbankTransferEvent.OnTransferFailed(message))
                        submitTransfer.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        submitLink.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> Unit
                    is SubmitState.Submitted -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = InterbankTransferState.DialogState.Success(
                                    getString(CoreUiRes.string.core_ui_pocket_added_successfully),
                                ),
                                pocketAccountIds = it.pocketAccountIds + (it.selectedFromAccount?.id ?: 0L),
                            )
                        }
                        submitLink.reset()
                    }
                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = InterbankTransferState.DialogState.ErrorResource(
                                    getString(CoreUiRes.string.core_ui_pocket_add_failed),
                                ),
                            )
                        }
                        submitLink.reset()
                    }
                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: InterbankTransferAction) {
        when (action) {
            // Navigation actions
            is InterbankTransferAction.ConfirmAddToPocket -> confirmAddToPocket(action.add)
            is InterbankTransferAction.DismissDialog -> dismissDialog()
            is InterbankTransferAction.NavigateToRecipientSearch -> selectFromAccount(action.account)

            is InterbankTransferAction.NavigateToTransferDetails -> showTransferDetails(action.participantInfo)

            is InterbankTransferAction.NavigateToPreview -> showPreview()

            InterbankTransferAction.NavigateBack -> navigateBack()

            InterbankTransferAction.EditFromAccount -> editFromAccount()

            InterbankTransferAction.EditRecipient -> editRecipient()

            // Transfer details actions
            is InterbankTransferAction.UpdateAmount -> updateAmount(action.amount)

            is InterbankTransferAction.UpdateDate -> updateDate(action.date)

            is InterbankTransferAction.UpdateDescription -> updateDescription(action.description)

            // Transfer confirmation
            InterbankTransferAction.ConfirmTransfer -> {
                validateAndInitiateTransfer()
            }

            // Error handling
            InterbankTransferAction.RetryTransfer -> retryTransfer()

            InterbankTransferAction.DismissError -> dismissError()

            is InterbankTransferAction.SearchRecipient -> {
                searchRecipient(action.phoneNumber)
            }
        }
    }

    private fun dismissDialog() {
        val wasPocket = state.dialogState is InterbankTransferState.DialogState.Success ||
            state.dialogState is InterbankTransferState.DialogState.ErrorResource
        mutableStateFlow.update { it.copy(dialogState = null) }
        if (wasPocket && state.currentStep == InterbankTransferState.Step.SelectAccount) {
            mutableStateFlow.update { it.copy(currentStep = InterbankTransferState.Step.SearchRecipient) }
        }
    }

    private fun selectFromAccount(account: Account) = mutableStateFlow.update {
        it.copy(
            selectedFromAccount = account,
            currentStep = if (account.id in state.pocketAccountIds) {
                InterbankTransferState.Step.SearchRecipient
            } else {
                state.currentStep
            },
            dialogState = if (account.id in state.pocketAccountIds) {
                null
            } else {
                InterbankTransferState.DialogState.AddToPocketConfirmation
            },
        )
    }

    private fun showTransferDetails(info: InterBankPartyInfoResponse) = mutableStateFlow.update {
        it.copy(currentStep = InterbankTransferState.Step.TransferDetails, selectedParticipantInfo = info)
    }

    private fun showPreview() = mutableStateFlow.update {
        it.copy(currentStep = InterbankTransferState.Step.PreviewTransfer)
    }

    private fun navigateBack() {
        val previousStep = when (state.currentStep) {
            InterbankTransferState.Step.SelectAccount -> {
                sendEvent(InterbankTransferEvent.OnNavigateBack)
                return
            }
            InterbankTransferState.Step.SearchRecipient -> InterbankTransferState.Step.SelectAccount
            InterbankTransferState.Step.TransferDetails -> InterbankTransferState.Step.SearchRecipient
            InterbankTransferState.Step.PreviewTransfer -> InterbankTransferState.Step.TransferDetails
            InterbankTransferState.Step.TransferSuccess -> InterbankTransferState.Step.PreviewTransfer
            InterbankTransferState.Step.TransferFailed -> InterbankTransferState.Step.PreviewTransfer
        }
        mutableStateFlow.update { it.copy(currentStep = previousStep) }
    }

    private fun editFromAccount() = mutableStateFlow.update {
        it.copy(currentStep = InterbankTransferState.Step.SelectAccount)
    }

    private fun editRecipient() = mutableStateFlow.update {
        it.copy(currentStep = InterbankTransferState.Step.SearchRecipient)
    }

    private fun updateAmount(amount: String) = mutableStateFlow.update { it.copy(transferAmount = amount) }

    private fun updateDate(date: Long) = mutableStateFlow.update {
        it.copy(transferDate = DateHelper.getDateAsStringFromLong(date))
    }

    private fun updateDescription(description: String) =
        mutableStateFlow.update { it.copy(transferDescription = description) }

    private fun retryTransfer() = mutableStateFlow.update {
        it.copy(currentStep = InterbankTransferState.Step.PreviewTransfer)
    }

    private fun dismissError() = mutableStateFlow.update { it.copy(errorMessage = null) }

    private suspend fun loadFromAccounts() {
        try {
            mutableStateFlow.update {
                it.copy(loadingState = InterbankTransferState.LoadingState.Loading)
            }

            selfServiceRepository.getActiveAccountsWithAccountTransferTemplate(state.client.id)
                .onEach { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {
                            mutableStateFlow.update {
                                it.copy(loadingState = InterbankTransferState.LoadingState.Loading)
                            }
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update {
                                it.copy(
                                    loadingState = InterbankTransferState.LoadingState.Error(
                                        "No accounts available",
                                    ),
                                )
                            }
                        }

                        is ScreenState.Content -> {
                            val accounts = screenState.data
                            if (accounts.isEmpty()) {
                                mutableStateFlow.update {
                                    it.copy(
                                        loadingState = InterbankTransferState.LoadingState.Error(
                                            "No accounts available",
                                        ),
                                    )
                                }
                            } else {
                                mutableStateFlow.update {
                                    it.copy(
                                        loadingState = InterbankTransferState.LoadingState.Success,
                                        fromAccounts = accounts.sortedByDescending { acc ->
                                            acc.id in it.pocketAccountIds
                                        },
                                    )
                                }
                            }
                        }

                        is ScreenState.Error -> {
                            mutableStateFlow.update {
                                it.copy(
                                    loadingState = InterbankTransferState.LoadingState.Error(
                                        getString(CoreUiRes.string.core_ui_error_msg_generic),
                                    ),
                                )
                            }
                        }

                        is ScreenState.NoNetwork -> {
                            mutableStateFlow.update {
                                it.copy(
                                    loadingState = InterbankTransferState.LoadingState.Error(
                                        getString(Res.string.feature_send_interbank_error_no_network),
                                    ),
                                )
                            }
                        }

                        is ScreenState.Unauthenticated -> {
                            mutableStateFlow.update {
                                it.copy(
                                    loadingState = InterbankTransferState.LoadingState.Error(
                                        getString(Res.string.feature_send_interbank_error_session_expired),
                                    ),
                                )
                            }
                        }
                    }
                }.launchIn(viewModelScope)
        } catch (e: Exception) {
            mutableStateFlow.update {
                it.copy(
                    loadingState = InterbankTransferState.LoadingState.Error(
                        getString(CoreUiRes.string.core_ui_error_msg_generic),
                    ),
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun validateAndInitiateTransfer() {
        val validationError = validateTransferDetails()
        if (validationError != null) {
            viewModelScope.launch {
                val errorString = getString(validationError)
                mutableStateFlow.update {
                    it.copy(errorMessage = errorString)
                }
            }
            return
        }

        // Validation above guarantees a non-null participant.
        val participantInfo = state.selectedParticipantInfo ?: return

        // Build InterBank transfer request from participantInfo and selected account
        val transferRequest = InterBankTransferRequest(
            homeTransactionId = Uuid.random().toString(),
            from = Party(
                fspId = participantInfo.sourceFspId,
                idType = participantInfo.partyIdType,
                idValue = state.selectedFromAccount?.externalId
                    ?: state.selectedFromAccount?.number ?: "",
            ),
            to = Party(
                fspId = participantInfo.destinationFspId,
                idType = participantInfo.partyIdType,
                idValue = participantInfo.partyId,
            ),
            amountType = "SEND",
            amount = Amount(
                currencyCode = state.selectedFromAccount?.currency?.code
                    ?: participantInfo.currencyCode,
                amount = state.transferAmount.toDoubleOrNull() ?: 0.0,
            ),
            transactionType = TransactionType(
                scenario = "TRANSFER",
                subScenario = "DOMESTIC",
                initiator = "PAYER",
                initiatorType = "CUSTOMER",
            ),
            note = state.transferDescription,
        )

        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`, and no-ops on a re-tap while Submitting (double-submit protection).
        // The repository throws on failure, which the handler reports as Failed.
        submitTransfer.submit {
            interBankRepository.interBankMakeTransfer(transferRequest)
        }
    }

    /**
     * Handles the user's decision to add the selected account to their Pocket.
     * If true, it initiates a link submission using the PocketRepository.
     * If false, it simply proceeds to the next step.
     */
    private fun confirmAddToPocket(add: Boolean) {
        if (!add) {
            mutableStateFlow.update {
                it.copy(
                    dialogState = null,
                    currentStep = InterbankTransferState.Step.SearchRecipient,
                )
            }
            return
        }
        mutableStateFlow.update { it.copy(dialogState = null) }
        if (add) {
            val accountId = state.selectedFromAccount?.id
            val accountNumber = state.selectedFromAccount?.number
            if (accountId == null || accountNumber.isNullOrBlank()) {
                return
            }
            val detailedAccount = DetailedPocketAccount(
                pocket = PocketAccount(
                    pocketId = 0,
                    id = 0,
                    accountId = accountId,
                    accountType = AccountType.SAVINGS,
                    accountNumber = accountNumber,
                ),
                productName = state.selectedFromAccount?.productName ?: state.selectedFromAccount?.name,
                balance = state.selectedFromAccount?.balance,
                currencyCode = state.selectedFromAccount?.currency?.code,
                decimalPlaces = state.selectedFromAccount?.currency?.decimalPlaces,
                status = state.selectedFromAccount?.status?.let {
                    when {
                        it.active -> AccountStatus.ACTIVE
                        it.approved -> AccountStatus.APPROVED
                        it.rejected -> AccountStatus.REJECTED
                        it.closed -> AccountStatus.CLOSED
                        it.submittedAndPendingApproval -> AccountStatus.PENDING
                        else -> null
                    }
                },
                currencyDisplaySymbol = state.selectedFromAccount?.currency?.displaySymbol,
            )
            val clientId = state.client.id
            submitLink.submit {
                pocketRepository.linkAccounts(listOf(detailedAccount), clientId)
            }
        } else {
            if (state.currentStep == InterbankTransferState.Step.SelectAccount) {
                mutableStateFlow.update { it.copy(currentStep = InterbankTransferState.Step.SearchRecipient) }
            }
        }
    }
    private fun validateTransferDetails(): org.jetbrains.compose.resources.StringResource? {
        return when {
            state.selectedFromAccount == null -> Res.string.feature_send_interbank_error_select_sender_account
            state.selectedParticipantInfo == null -> Res.string.feature_send_interbank_error_select_recipient
            state.transferAmount.isBlank() -> Res.string.feature_send_interbank_error_enter_amount
            state.transferAmount.toDoubleOrNull() == null -> Res.string.feature_send_interbank_error_invalid_amount
            state.transferAmount.toDouble() <= 0 -> Res.string.feature_send_interbank_error_amount_greater_than_zero
            state.transferDescription.isBlank() -> Res.string.feature_send_interbank_error_enter_description
            else -> null
        }
    }

    private fun searchRecipient(phoneNumber: String) {
        launchIO {
            if (phoneNumber.length < 10) {
                mutableStateFlow.update {
                    it.copy(
                        searchRecipientState = InterbankTransferState.SearchRecipientState.Error(
                            getString(Res.string.feature_send_interbank_error_phone_number_digits),
                        ),
                        searchResults = emptyList(),
                    )
                }
                return@launchIO
            }

            interBankRepository.findParticipant(
                partyId = phoneNumber,
                currencyCode = mutableStateFlow.value.selectedFromAccount?.currency?.code ?: "MXN",
            ).onEach { screenState ->
                when (screenState) {
                    is ScreenState.Loading -> {
                        mutableStateFlow.update {
                            it.copy(searchRecipientState = InterbankTransferState.SearchRecipientState.Loading)
                        }
                    }

                    is ScreenState.Content -> {
                        val partyInfo = screenState.data
                        mutableStateFlow.update {
                            it.copy(
                                searchRecipientState = InterbankTransferState.SearchRecipientState.Success,
                                searchResults = listOf(partyInfo),
                            )
                        }
                    }

                    is ScreenState.Empty -> {
                        mutableStateFlow.update {
                            it.copy(
                                searchRecipientState = InterbankTransferState.SearchRecipientState.Error(
                                    getString(Res.string.feature_send_interbank_no_recipients_found),
                                ),
                                searchResults = emptyList(),
                            )
                        }
                    }

                    is ScreenState.Error -> {
                        val message = screenState.error.message
                            ?: getString(Res.string.feature_send_interbank_error_failed_to_search_recipient)
                        mutableStateFlow.update {
                            it.copy(
                                searchRecipientState = InterbankTransferState.SearchRecipientState.Error(message),
                                searchResults = emptyList(),
                            )
                        }
                    }

                    is ScreenState.NoNetwork -> {
                        mutableStateFlow.update {
                            it.copy(
                                searchRecipientState = InterbankTransferState.SearchRecipientState.Error(
                                    getString(Res.string.feature_send_interbank_error_no_network),
                                ),
                                searchResults = emptyList(),
                            )
                        }
                    }

                    is ScreenState.Unauthenticated -> {
                        mutableStateFlow.update {
                            it.copy(
                                searchRecipientState = InterbankTransferState.SearchRecipientState.Error(
                                    getString(Res.string.feature_send_interbank_error_session_expired),
                                ),
                                searchResults = emptyList(),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * UI state of the interbank transfer flow.
 * @property client Client making the transfer.
 * @property currentStep Step of the flow currently shown.
 * @property loadingState Loading state of the source accounts.
 * @property fromAccounts Accounts the transfer can be made from.
 * @property selectedFromAccount Chosen source account, or null.
 * @property selectedParticipantInfo Chosen recipient, or null.
 * @property transferAmount Amount entered, as text.
 * @property transferDate Transfer date formatted for display.
 * @property initialDate Initial date picker value as epoch milliseconds.
 * @property transferDescription Description (remark) of the transfer.
 * @property isProcessing Whether the transfer is being submitted.
 * @property errorMessage Error text, or null.
 * @property transferResponse Response text of a completed transfer, or null.
 * @property searchRecipientState State of the recipient search.
 * @property searchResults Recipients found by the search.
 * @property pocketAccountIds Ids of accounts already saved to the pocket.
 * @property dialogState Dialog currently shown, or null.
 */
@OptIn(ExperimentalTime::class)
data class InterbankTransferState(
    val client: Client,
    val currentStep: Step = Step.SelectAccount,
    val loadingState: LoadingState = LoadingState.Loading,
    val fromAccounts: List<Account> = emptyList(),
    val selectedFromAccount: Account? = null,
    val selectedParticipantInfo: InterBankPartyInfoResponse? = null,
    val transferAmount: String = "1.0",
    val transferDate: String = DateHelper.getDateAsString(
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
    ),

    @Transient
    val initialDate: Long = Clock.System.now().toEpochMilliseconds(),

    val transferDescription: String = "Interbank Transfer",
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val transferResponse: String? = null,
    val searchRecipientState: SearchRecipientState = SearchRecipientState.Idle,
    val searchResults: List<InterBankPartyInfoResponse> = emptyList(),
    val pocketAccountIds: Set<Long> = emptySet(),
    @Transient val dialogState: DialogState? = null,
) {
    /** Dialogs the transfer flow can show. */
    sealed interface DialogState {
        /** Progress dialog. */
        data object Loading : DialogState

        /** Asks whether to add the recipient to the pocket. */
        data object AddToPocketConfirmation : DialogState

        /**
         * Success dialog.
         * @property message Message to display.
         */
        data class Success(val message: String) : DialogState

        /**
         * Error dialog.
         * @property message Error text to display.
         */
        data class ErrorResource(val message: String) : DialogState
    }

    /** Steps of the interbank transfer flow. */
    sealed interface Step {
        /** Choose the source account. */
        data object SelectAccount : Step

        /** Search for the recipient. */
        data object SearchRecipient : Step

        /** Enter the amount, date and description. */
        data object TransferDetails : Step

        /** Review the transfer before confirming. */
        data object PreviewTransfer : Step

        /** The transfer succeeded. */
        data object TransferSuccess : Step

        /** The transfer failed. */
        data object TransferFailed : Step
    }

    /** Loading state of the source accounts. */
    sealed interface LoadingState {
        /** Accounts are loading. */
        data object Loading : LoadingState

        /** Accounts loaded. */
        data object Success : LoadingState

        /**
         * Loading failed.
         * @property message Error text.
         */
        data class Error(val message: String) : LoadingState
    }

    /** State of the recipient search. */
    sealed interface SearchRecipientState {
        /** No search has been made. */
        data object Idle : SearchRecipientState

        /** A search is running. */
        data object Loading : SearchRecipientState

        /** The search completed. */
        data object Success : SearchRecipientState

        /**
         * The search failed.
         * @property message Error text.
         */
        data class Error(val message: String) : SearchRecipientState
    }
}

/** One-shot events emitted by [InterbankTransferViewModel]. */
sealed interface InterbankTransferEvent {
    /** Navigate back out of the flow. */
    data object OnNavigateBack : InterbankTransferEvent

    /** The transfer succeeded. */
    data object OnTransferSuccess : InterbankTransferEvent

    /**
     * The transfer failed.
     * @property message Failure message.
     */
    data class OnTransferFailed(val message: String) : InterbankTransferEvent
}

/** Actions handled by [InterbankTransferViewModel]. */
sealed interface InterbankTransferAction {
    // Navigation
    /**
     * Move to recipient search for the chosen account.
     * @property account Chosen source account.
     */
    data class NavigateToRecipientSearch(val account: Account) : InterbankTransferAction

    /**
     * Move to the transfer details for the chosen recipient.
     * @property participantInfo Chosen recipient.
     */
    data class NavigateToTransferDetails(val participantInfo: InterBankPartyInfoResponse) :
        InterbankTransferAction

    /** Move to the transfer preview step. */
    data object NavigateToPreview : InterbankTransferAction

    /** Go back one step. */
    data object NavigateBack : InterbankTransferAction

    /** Return to source account selection. */
    data object EditFromAccount : InterbankTransferAction

    /** Return to recipient search. */
    data object EditRecipient : InterbankTransferAction

    // Transfer details
    /**
     * The amount changed.
     * @property amount New amount as entered.
     */
    data class UpdateAmount(val amount: String) : InterbankTransferAction

    /**
     * The transfer date changed.
     * @property date New date as epoch milliseconds.
     */
    data class UpdateDate(val date: Long) : InterbankTransferAction

    /**
     * The description changed.
     * @property description New description.
     */
    data class UpdateDescription(val description: String) : InterbankTransferAction

    // Recipient search
    /**
     * Search for a recipient by phone number.
     * @property phoneNumber Phone number to look up.
     */
    data class SearchRecipient(val phoneNumber: String) : InterbankTransferAction

    // Transfer confirmation
    /** Confirm and submit the transfer. */
    data object ConfirmTransfer : InterbankTransferAction

    /** Retry a failed transfer. */
    data object RetryTransfer : InterbankTransferAction

    /** Dismiss the current error. */
    data object DismissError : InterbankTransferAction

    /**
     * Answer the add-to-pocket prompt.
     * @property add True to add the recipient to the pocket.
     */
    data class ConfirmAddToPocket(val add: Boolean) : InterbankTransferAction

    /** Dismiss the currently shown dialog. */
    data object DismissDialog : InterbankTransferAction
}
