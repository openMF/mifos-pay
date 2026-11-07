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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.DateHelper
import kpt.core.common.ScreenState
import kpt.core.common.UiError
import kpt.core.common.toUiError
import kpt.core.common.utils.capitalizeWords
import kpt.core.data.repository.ClientRepository
import kpt.core.data.repository.PocketRepository
import kpt.core.data.repository.ThirdPartyTransferRepository
import kpt.core.data.repository.UserVerificationRepository
import kpt.core.model.enums.AccountType
import kpt.core.model.network.entity.payload.TransferPayload
import kpt.core.model.network.entity.templates.account.AccountOption
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.model.savingsaccount.SavingAccountEntity
import kpt.core.ui.DefaultErrorMessageProvider
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.core.ui.generated.resources.core_ui_error_msg_generic
import kpt.core.ui.generated.resources.core_ui_error_no_network
import kpt.core.ui.generated.resources.core_ui_error_session_expired
import kpt.core.ui.generated.resources.core_ui_pocket_add_failed
import kpt.core.ui.generated.resources.core_ui_pocket_added_successfully
import kpt.feature.transfer.intrabank.generated.resources.Res
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_error_empty_amount
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_error_empty_description
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_error_insufficient_balance
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_error_invalid_amount
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_error_same_account
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_error_select_account
import kpt.feature.transfer.intrabank.generated.resources.feature_make_transfer_user_verification_failed
import kpt.feature.transfer.intrabank.navigation.TransferConfirmRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kpt.core.ui.generated.resources.Res as CoreUiRes

/**
 * `SavedStateHandle` key written by `internalPasscodeScreen` and read by
 * [TransferConfirmScreen] for the intra-bank transfer auth-gate round trip
 * (MW-390).
 *
 * Mirrors the disable-biometrics flow in `:feature:settings`, but with its
 * own key so the two flows don't cross-fire.
 */
const val INTRA_BANK_TRANSFER_VERIFICATION_KEY = "intra-banking_transfer_verification_key"

/**
 * ViewModel for the intra-bank transfer confirmation screen.
 *
 * Owns the **passcode/biometric auth gate** required before initiating a
 * transfer (MW-390):
 *  1. On [TransferConfirmAction.InitiateTransfer], runs validation; if it
 *     passes, calls `handleUserVerification` which emits
 *     [TransferConfirmEvent.NavigateForPasscodeVerification]. The screen
 *     responds by navigating to `internalPasscodeScreen` with
 *     [INTRA_BANK_TRANSFER_VERIFICATION_KEY].
 *  2. The screen observes its own saved-state-handle for the round-trip
 *     boolean and re-dispatches it as
 *     [TransferConfirmAction.UpdateUserVerificationResult]. The VM resumes
 *     `handleUserVerification` on the result.
 *  3. On `success = true`, the VM calls
 *     [UserVerificationRepository.consumeVerification]. If the 30 s token is
 *     still valid the transfer proceeds; if expired, the transfer is
 *     cancelled with a "user verification failed" dialog.
 *
 * The VM does **not** import any `org.mifos.authenticator.*` symbol — the
 * library integration is purely on the screen and navigation side. This
 * class only needs the [UserVerificationRepository] contract.
 */
internal class TransferConfirmViewModel(
    private val repository: ThirdPartyTransferRepository,
    private val pocketRepository: PocketRepository,
    private val clientRepo: ClientRepository,
    private val userVerificationRepository: UserVerificationRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<TransferConfirmState, TransferConfirmEvent, TransferConfirmAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<TransferConfirmRoute>()
        TransferConfirmState(
            toOfficeId = route.toOfficeId,
            toClientId = route.toClientId,
            toAccountType = route.toAccountTypeId,
            toAccountId = route.accountId.toInt(),
            toAccountName = route.toAccountName,
            toAccountNo = route.toAccountNo,
            amount = if (route.amount == 0) "" else route.amount.toString(),
        )
    },
) {

    private val submitLink = viewModelScope.submitHandler<Unit>()

    init {
        viewModelScope.launch {
            getFromAccounts()
        }

        submitLink.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> Unit
                    is SubmitState.Submitted -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = TransferConfirmState.DialogState.Success(
                                    getString(CoreUiRes.string.core_ui_pocket_added_successfully),
                                ),
                                pocketAccountIds = it.pocketAccountIds +
                                    (it.selectedAccount?.accountId?.toLong() ?: 0L),
                            )
                        }
                        submitLink.reset()
                    }
                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = TransferConfirmState.DialogState.Error.ResourceMessage(
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

    override fun handleAction(action: TransferConfirmAction) {
        when (action) {
            TransferConfirmAction.NavigateBack -> navigateBack()
            is TransferConfirmAction.AmountChanged -> updateAmount(action.amount)
            is TransferConfirmAction.DescriptionChanged -> updateDescription(action.desc)
            is TransferConfirmAction.SelectAccount -> selectAccount(action.account)
            is TransferConfirmAction.DismissDialog -> dismissDialog()
            is TransferConfirmAction.InitiateTransfer -> startTransfer()
            is TransferConfirmAction.ConfirmAddToPocket -> confirmAddToPocket(action.add)
            TransferConfirmAction.RetryTransfer -> retryTransfer()
            TransferConfirmAction.CloseBottomSheet -> setBottomSheetVisible(false)
            TransferConfirmAction.OpenBottomSheet -> setBottomSheetVisible(true)
            is TransferConfirmAction.UpdateUserVerificationResult -> updateVerificationResult(action.result)
        }
    }

    private fun navigateBack() = sendEvent(TransferConfirmEvent.OnNavigateBack)

    private fun updateAmount(amount: String) = mutableStateFlow.update { it.copy(amount = amount) }

    private fun updateDescription(description: String) =
        mutableStateFlow.update { it.copy(description = description) }

    private fun selectAccount(account: AccountOption?) {
        mutableStateFlow.update {
            it.copy(
                selectedAccount = account,
                selectedAccountBalance = state.balanceMap[account?.accountNo] ?: 0.0,
                showBottomSheet = false,
            )
        }
    }

    private fun dismissDialog() {
        val wasPocketSuccess = state.dialogState is TransferConfirmState.DialogState.Success
        mutableStateFlow.update { it.copy(dialogState = null) }
        if (wasPocketSuccess) validateTransfer()
    }

    private fun startTransfer() {
        if (state.isProcessing) return
        mutableStateFlow.update { it.copy(isProcessing = true, dialogState = null) }
        if (state.selectedAccount?.accountId?.toLong() !in state.pocketAccountIds) {
            mutableStateFlow.update {
                it.copy(dialogState = TransferConfirmState.DialogState.AddToPocketConfirmation, isProcessing = false)
            }
        } else {
            validateTransfer()
        }
    }

    private fun retryTransfer() {
        mutableStateFlow.update { it.copy(dialogState = null) }
        trySendAction(TransferConfirmAction.InitiateTransfer)
    }

    private fun setBottomSheetVisible(visible: Boolean) =
        mutableStateFlow.update { it.copy(showBottomSheet = visible) }

    private fun updateVerificationResult(result: Boolean) {
        mutableStateFlow.update {
            it.copy(userVerificationResult = result, isAwaitingPasscodeVerification = false)
        }
    }

    private suspend fun getFromAccounts() {
        try {
            val res = repository.getTransferTemplate()
            val fromAccounts = res.fromAccountOptions?.filter {
                it.accountType?.id == 2
            }
            if (fromAccounts.isNullOrEmpty()) {
                mutableStateFlow.update {
                    it.copy(
                        state = TransferConfirmState.State.NoAccounts,
                    )
                }
            } else {
                val clientId = fromAccounts.firstOrNull()?.clientId?.toLong() ?: -1L
                if (clientId != -1L) {
                    pocketRepository.observeLinkedPocketAccounts(clientId).onEach { pocketAccounts ->
                        val pocketAccountIds = pocketAccounts.filter { it.accountType == AccountType.SAVINGS }
                            .mapNotNull { it.accountId }.toSet()
                        mutableStateFlow.update { state ->
                            /*
                             * Pocket Account Suggestion Feature:
                             * Sorts the accounts so that linked pocket accounts appear at the top.
                             */
                            val sortedAccounts = state.fromAccountOptions
                                ?.sortedByDescending { it.accountId?.toLong() in pocketAccountIds }
                            state.copy(
                                pocketAccountIds = pocketAccountIds,
                                fromAccountOptions = sortedAccounts ?: state.fromAccountOptions,
                            )
                        }
                    }.launchIn(viewModelScope)
                }

                mutableStateFlow.update { it.copy(fromAccountOptions = fromAccounts) }
                getBalanceOfAccounts()
            }
        } catch (e: Exception) {
            mutableStateFlow.update {
                it.copy(
                    state = TransferConfirmState.State.Error(e.message ?: ""),
                )
            }
        }
    }

    private suspend fun getBalanceOfAccounts() {
        try {
            val clientId = state.fromAccountOptions?.first()?.clientId ?: -1
            val result = clientRepo.getClientAccounts(clientId)
            val balanceMap: Map<String, Double> =
                result.savingsAccounts.associate { account ->
                    account.accountNo to account.accountBalance
                }
            val savingsAccountsMap = result.savingsAccounts.associateBy { it.accountNo }
            mutableStateFlow.update {
                it.copy(
                    state = TransferConfirmState.State.Success,
                    balanceMap = balanceMap,
                    savingsAccountsMap = savingsAccountsMap,
                )
            }
            val firstAccount = state.fromAccountOptions?.first()
            mutableStateFlow.update {
                it.copy(
                    selectedAccount = firstAccount,
                    selectedAccountBalance = balanceMap.getOrElse(
                        firstAccount?.accountNo ?: "",
                        { 0.0 },
                    ),
                )
            }
        } catch (e: Exception) {
            mutableStateFlow.update {
                it.copy(
                    state = TransferConfirmState.State.Error(e.message ?: ""),
                )
            }
        }
    }

    /**
     * Handles the user's decision to add the selected account to their Pocket.
     * If true, it initiates a link submission using the PocketRepository.
     * If false, it simply proceeds to the next step.
     */
    private fun confirmAddToPocket(add: Boolean) {
        mutableStateFlow.update { it.copy(dialogState = null) }
        if (add) {
            val accountId = state.selectedAccount?.accountId?.toLong()
            val accountNumber = state.selectedAccount?.accountNo
            if (accountId == null || accountNumber.isNullOrBlank()) {
                return
            }
            val savingsAccount = state.savingsAccountsMap[accountNumber]
            val detailedAccount = DetailedPocketAccount(
                pocket = PocketAccount(
                    pocketId = 0,
                    id = 0,
                    accountId = accountId,
                    accountType = AccountType.SAVINGS,
                    accountNumber = accountNumber,
                ),
                productName = savingsAccount?.productName ?: state.selectedAccount?.clientName,
                balance = savingsAccount?.accountBalance ?: state.balanceMap[accountNumber],
                currencyCode = savingsAccount?.currency?.code,
                decimalPlaces = savingsAccount?.currency?.decimalPlaces,
                status = savingsAccount?.status?.let {
                    when {
                        it.active -> kpt.core.model.pocket.AccountStatus.ACTIVE
                        it.approved -> kpt.core.model.pocket.AccountStatus.APPROVED
                        it.rejected -> kpt.core.model.pocket.AccountStatus.REJECTED
                        it.closed -> kpt.core.model.pocket.AccountStatus.CLOSED
                        it.submittedAndPendingApproval -> kpt.core.model.pocket.AccountStatus.PENDING
                        else -> null
                    }
                },
                currencyDisplaySymbol = savingsAccount?.currency?.displaySymbol,
            )
            val clientId = state.selectedAccount?.clientId?.toLong() ?: return
            submitLink.submit {
                pocketRepository.linkAccounts(listOf(detailedAccount), clientId)
            }
        } else {
            validateTransfer()
        }
    }

    private fun validateTransfer() = when {
        state.amount.isBlank() -> updateValidationError(Res.string.feature_make_transfer_error_empty_amount)

        state.amount.toDoubleOrNull() == null -> updateValidationError(Res.string.feature_make_transfer_error_invalid_amount)

        state.description.isBlank() -> updateValidationError(Res.string.feature_make_transfer_error_empty_description)

        state.selectedAccount == null -> updateValidationError(Res.string.feature_make_transfer_error_select_account)

        state.selectedAccount?.accountId == state.toAccountId -> {
            updateValidationError(Res.string.feature_make_transfer_error_same_account)
        }

        state.amount.toDouble() > state.selectedAccountBalance -> {
            updateValidationError(Res.string.feature_make_transfer_error_insufficient_balance)
        }

        else -> initiateTransfer()
    }

    private fun initiateTransfer() {
        viewModelScope.launch {
            handleUserVerification(
                onSuccess = {
                    if (userVerificationRepository.consumeVerification()) {
                        handleTransfer()
                    } else {
                        mutableStateFlow.update {
                            it.copy(
                                isProcessing = false,
                                dialogState = TransferConfirmState.DialogState
                                    .Error.ValidationError(Res.string.feature_make_transfer_user_verification_failed),
                            )
                        }
                    }
                },
                onFailed = {
                    mutableStateFlow.update {
                        it.copy(
                            isProcessing = false,
                            dialogState = TransferConfirmState.DialogState
                                .Error.ValidationError(Res.string.feature_make_transfer_user_verification_failed),
                        )
                    }
                },
            )
        }
    }

    private suspend fun handleTransfer() {
        mutableStateFlow.update {
            it.copy(dialogState = TransferConfirmState.DialogState.Loading)
        }
        // Phase-3 fold: `ThirdPartyTransferRepository.makeTransfer` was
        // migrated to `ScreenStateStream<TPTResponse>`. Content produces the
        // TransferResult; Empty is defensively surfaced as a generic error (a
        // submit endpoint should not emit Empty); NoNetwork / Unauthenticated
        // fold into the existing `Error.ApiError` surface until Phase-4
        // differentiates them.
        repository.makeTransfer(state.transferPayload).collect { result ->
            when (result) {
                is ScreenState.Loading -> {
                    mutableStateFlow.update {
                        it.copy(
                            isProcessing = true,
                            dialogState = null,
                        )
                    }
                }

                is ScreenState.Empty -> {
                    mutableStateFlow.update {
                        it.copy(
                            isProcessing = false,
                            dialogState = TransferConfirmState.DialogState.Error.ResourceMessage(
                                getString(kpt.core.ui.generated.resources.Res.string.core_ui_error_msg_generic),
                            ),
                        )
                    }
                }

                is ScreenState.Error -> {
                    // Use UiError for automatic error message parsing with localized strings
                    val uiError = result.error.toUiError(DefaultErrorMessageProvider)
                    mutableStateFlow.update {
                        it.copy(
                            isProcessing = false,
                            dialogState = TransferConfirmState.DialogState.Error.ApiError(uiError),
                        )
                    }
                }

                is ScreenState.NoNetwork -> {
                    mutableStateFlow.update {
                        it.copy(
                            isProcessing = false,
                            dialogState = TransferConfirmState.DialogState.Error.ResourceMessage(
                                getString(kpt.core.ui.generated.resources.Res.string.core_ui_error_no_network),
                            ),
                        )
                    }
                }

                is ScreenState.Unauthenticated -> {
                    mutableStateFlow.update {
                        it.copy(
                            isProcessing = false,
                            dialogState = TransferConfirmState.DialogState.Error.ResourceMessage(
                                getString(kpt.core.ui.generated.resources.Res.string.core_ui_error_session_expired),
                            ),
                        )
                    }
                }

                is ScreenState.Content -> {
                    val response = result.data
                    val transferResult = TransferResult(
                        transactionId = response.resourceId ?: "",
                        amount = state.amount.toDoubleOrNull() ?: 0.0,
                        fromAccountNo = state.selectedAccount?.accountNo ?: "",
                        fromAccountName = state.selectedAccount?.clientName ?: "",
                        toAccountNo = state.toAccountNo,
                        toAccountName = state.toAccountName,
                        transferDate = DateHelper.formattedShortDate,
                        description = state.description.trim(),
                    )
                    mutableStateFlow.update {
                        it.copy(
                            isProcessing = false,
                            dialogState = null,
                            transferResult = transferResult,
                        )
                    }
                }
            }
        }
    }

    /**
     * Suspends until the passcode/biometric gate round trip completes.
     *
     * Emits [TransferConfirmEvent.NavigateForPasscodeVerification], then
     * waits for [TransferConfirmState.userVerificationResult] to flip from
     * null. The screen is responsible for calling
     * [TransferConfirmAction.UpdateUserVerificationResult] with the boolean
     * written under [INTRA_BANK_TRANSFER_VERIFICATION_KEY] in the
     * saved-state-handle.
     *
     * Branches:
     *  - `true` → [onSuccess] (caller still needs to call
     *    [UserVerificationRepository.consumeVerification] to confirm the
     *    30 s token is valid).
     *  - `false` → [onFailed] (user cancelled or rejected).
     *  - `null` → no-op (defensive; the filter above prevents this).
     */
    private suspend fun handleUserVerification(
        onSuccess: suspend () -> Unit,
        onFailed: suspend () -> Unit,
    ) {
        mutableStateFlow.update {
            it.copy(
                userVerificationResult = null,
                isAwaitingPasscodeVerification = true,
            )
        }

        sendEvent(TransferConfirmEvent.NavigateForPasscodeVerification)

        val result = stateFlow
            .map { it.userVerificationResult }
            .filter { it != null }
            .first()

        when (result) {
            true -> onSuccess()
            false -> onFailed()
            null -> {}
        }
    }

    private fun updateValidationError(message: StringResource) {
        mutableStateFlow.update {
            it.copy(
                isProcessing = false,
                dialogState = TransferConfirmState.DialogState.Error.ValidationError(message),
            )
        }
    }
}

internal data class TransferConfirmState(
    val toOfficeId: Int? = null,
    val toClientId: Long? = null,
    val toAccountType: Int? = null,
    val toAccountId: Int? = null,
    val toAccountName: String = "",
    val toAccountNo: String = "",
    val amount: String = "",

    val showBottomSheet: Boolean = false,
    val state: State = State.Loading,
    val description: String = "",
    val selectedAccount: AccountOption? = null,
    val selectedAccountBalance: Double = 0.0,
    val dialogState: DialogState? = null,
    val fromAccountOptions: List<AccountOption>? = emptyList(),
    val balanceMap: Map<String, Double> = emptyMap(),
    val savingsAccountsMap: Map<String, SavingAccountEntity> = emptyMap(),
    val pocketAccountIds: Set<Long> = emptySet(),
    val isProcessing: Boolean = false,
    /**
     * `true` between [TransferConfirmEvent.NavigateForPasscodeVerification]
     * being emitted and [TransferConfirmAction.UpdateUserVerificationResult]
     * being received. Used by the screen to suppress double-taps on the
     * confirm button during the round trip.
     */
    val isAwaitingPasscodeVerification: Boolean = false,
    val transferResult: TransferResult? = null,
    /**
     * Round-trip result from the passcode/biometric gate:
     *  - `null` — not yet returned (initial / mid-flight),
     *  - `true` — verified; transfer can proceed,
     *  - `false` — cancelled / rejected; transfer is aborted.
     *
     * Cleared back to `null` on each new [handleUserVerification] call.
     */
    val userVerificationResult: Boolean? = null,
) {
    val amountIsValid: Boolean
        get() = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() <= selectedAccountBalance

    val descriptionIsValid: Boolean
        get() = description.trim().isNotEmpty()

    val transferPayload: TransferPayload
        get() = TransferPayload(
            fromOfficeId = selectedAccount?.officeId,
            fromClientId = selectedAccount?.clientId,
            fromAccountType = selectedAccount?.accountType?.id,
            fromAccountId = selectedAccount?.accountId,
            toOfficeId = toOfficeId,
            toClientId = toClientId,
            toAccountType = toAccountType,
            toAccountId = toAccountId,
            transferDate = DateHelper.formattedShortDate,
            transferAmount = amount.toDoubleOrNull() ?: 0.0,
            transferDescription = description.trim().capitalizeWords(),
            locale = "en_IN",
            dateFormat = DateHelper.SHORT_MONTH,
        )

    sealed interface DialogState {
        data object Loading : DialogState

        data object AddToPocketConfirmation : DialogState

        data class Success(val message: String) : DialogState

        sealed class Error : DialogState {
            abstract val canRetry: Boolean

            /**
             * Validation error with a string resource message.
             * Used for client-side validation errors.
             */
            data class ValidationError(
                val message: StringResource,
            ) : Error() {
                override val canRetry: Boolean = false
            }

            data class ResourceMessage(val message: String) : Error() {
                override val canRetry: Boolean = false
            }

            /**
             * API error that wraps [UiError] for automatic error message handling.
             * The [UiError] contains user-friendly title, message and retry capability.
             */
            data class ApiError(
                val error: UiError,
            ) : Error() {
                override val canRetry: Boolean = error.canRetry
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data object NoAccounts : State
        data object Success : State
        data class Error(val message: String) : State
    }
}

/**
 * Transfer result data containing all the details of a successful transfer.
 *
 * @property transactionId Id of the completed transaction.
 * @property amount Amount transferred.
 * @property fromAccountNo Source account number.
 * @property fromAccountName Source account holder name.
 * @property toAccountNo Destination account number.
 * @property toAccountName Destination account holder name.
 * @property transferDate Transfer date formatted for display.
 * @property description Description (remark) of the transfer.
 */
data class TransferResult(
    val transactionId: String,
    val amount: Double,
    val fromAccountNo: String,
    val fromAccountName: String,
    val toAccountNo: String,
    val toAccountName: String,
    val transferDate: String,
    val description: String,
)

internal sealed interface TransferConfirmEvent {
    data object OnNavigateBack : TransferConfirmEvent

    /**
     * Tells the screen to push `internalPasscodeScreen` with
     * [INTRA_BANK_TRANSFER_VERIFICATION_KEY]. The screen is responsible for
     * observing its own saved-state-handle for the round-trip boolean and
     * dispatching it back as
     * [TransferConfirmAction.UpdateUserVerificationResult].
     */
    data object NavigateForPasscodeVerification : TransferConfirmEvent
}

internal sealed interface TransferConfirmAction {
    data object NavigateBack : TransferConfirmAction
    data class ConfirmAddToPocket(val add: Boolean) : TransferConfirmAction
    data object DismissDialog : TransferConfirmAction
    data object InitiateTransfer : TransferConfirmAction
    data object RetryTransfer : TransferConfirmAction
    data class AmountChanged(val amount: String) : TransferConfirmAction
    data class DescriptionChanged(val desc: String) : TransferConfirmAction
    data class SelectAccount(val account: AccountOption?) : TransferConfirmAction
    data object OpenBottomSheet : TransferConfirmAction
    data object CloseBottomSheet : TransferConfirmAction

    /**
     * Round-trip result from the passcode/biometric gate, dispatched by
     * [TransferConfirmScreen] after observing
     * [INTRA_BANK_TRANSFER_VERIFICATION_KEY] flip on its saved-state-handle.
     * `true` lets the transfer proceed (subject to a still-valid
     * [UserVerificationRepository] token); `false` aborts.
     */
    data class UpdateUserVerificationResult(val result: Boolean) : TransferConfirmAction
}
