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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.common.ScreenState
import kpt.core.common.StringResourceSerializer
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.fork.util.StandardUpiQrCodeProcessor
import kpt.core.data.fork.util.UpiQrCodeProcessor
import kpt.core.data.repository.AccountRepository
import kpt.core.model.search.AccountResult
import kpt.core.model.utils.PaymentQrData
import kpt.core.model.utils.toAccount
import kpt.core.ui.fork.utils.BackgroundEvent
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.send.money.SendMoneyAction.HandleRequestData
import kpt.feature.send.money.SendMoneyState.DialogState.Error
import kpt.feature.send.money.generated.resources.Res
import kpt.feature.send.money.generated.resources.feature_send_money_error_account_cannot_be_empty
import kpt.feature.send.money.generated.resources.feature_send_money_error_amount_cannot_be_empty
import kpt.feature.send.money.generated.resources.feature_send_money_error_invalid_amount
import kpt.feature.send.money.generated.resources.feature_send_money_error_requesting_payment_qr_but_found
import kpt.feature.send.money.generated.resources.feature_send_money_error_requesting_payment_qr_data_missing
import kpt.feature.send.money.generated.resources.feature_send_money_upi_qr_parsed_successfully
import org.jetbrains.compose.resources.StringResource
/**
 * ViewModel for the Fineract send-money screen; searches accounts by number and builds the payment QR data.
 * @property scanner QR scanner used to scan a payment code.
 */
class SendMoneyViewModel(
    private val scanner: QrScanner,
    repository: AccountRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SendMoneyState, SendMoneyEvent, SendMoneyAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: SendMoneyState(),
) {

    /** Holds the saved-state key for this view model. */
    companion object {
        private const val KEY_STATE = "send_payment_state"
    }

    /** Account search results, debounced and emitted once at least four digits of the account number are typed. */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val accountListState = stateFlow.map { it.accountNumber }
        .distinctUntilChanged()
        .debounce(300)
        .filter { it.length >= 4 }
        .flatMapLatest {
            repository.searchAccounts(it)
        }.mapLatest { screenState ->
            // Fold ScreenState → the existing 5-branch ViewState. Empty maps to
            // the dedicated ViewState.Empty branch (no accounts found). NoNetwork
            // / Unauthenticated fold into Error until Phase-4 differentiates.
            when (screenState) {
                is ScreenState.Loading -> ViewState.Loading

                is ScreenState.Empty -> ViewState.Empty

                is ScreenState.Content -> {
                    if (screenState.data.isEmpty()) {
                        ViewState.Empty
                    } else {
                        ViewState.Content(screenState.data)
                    }
                }

                is ScreenState.Error -> ViewState.Error(screenState.error.message.toString())

                is ScreenState.NoNetwork ->
                    ViewState.Error("No network. Please check your connection.")

                is ScreenState.Unauthenticated ->
                    ViewState.Error("Session expired. Please log in again.")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewState.InitialEmpty,
        )

    init {
        stateFlow.onEach {
            savedStateHandle.setSerialized(key = KEY_STATE, value = it)
        }.launchIn(viewModelScope)

        savedStateHandle.get<String>("requestData")?.let {
            trySendAction(HandleRequestData(it))
        }
    }

    override fun handleAction(action: SendMoneyAction) {
        when (action) {
            is SendMoneyAction.AmountChanged -> {
                mutableStateFlow.update {
                    it.copy(amount = action.amount)
                }
            }

            is SendMoneyAction.AccountNumberChanged -> {
                mutableStateFlow.update {
                    it.copy(accountNumber = action.accountNumber)
                }
            }

            is SendMoneyAction.SelectAccount -> {
                mutableStateFlow.update {
                    it.copy(selectedAccount = action.account)
                }
            }

            SendMoneyAction.NavigateBack -> {
                sendEvent(SendMoneyEvent.OnNavigateBack)
            }

            SendMoneyAction.OnClickScan -> {
                scanner.startScanning().onEach { data ->
                    data?.let { result ->
                        if (StandardUpiQrCodeProcessor.isValidUpiQrCode(result)) {
                            sendEvent(SendMoneyEvent.NavigateToPayeeDetails(result))
                        } else {
                            sendAction(HandleRequestData(result))
                        }
                    }
                }.launchIn(viewModelScope)
                // Using Play Service Code Scanner until Qr Scan module is stable
                // sendEvent(SendMoneyEvent.NavigateToScanQrScreen)
            }

            is SendMoneyAction.DeselectAccount -> {
                mutableStateFlow.update {
                    it.copy(selectedAccount = null)
                }
            }

            SendMoneyAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            SendMoneyAction.OnProceedClicked -> validateTransferFlow()

            is HandleRequestData -> handleRequestData(action)
        }
    }

    private fun validateTransferFlow() = when {
        state.amount.isBlank() -> updateErrorState(Res.string.feature_send_money_error_amount_cannot_be_empty)

        state.amount.toDoubleOrNull() == null -> updateErrorState(Res.string.feature_send_money_error_invalid_amount)

        state.selectedAccount == null -> updateErrorState(Res.string.feature_send_money_error_account_cannot_be_empty)

        else -> initiateTransfer()
    }

    private fun initiateTransfer() {
        viewModelScope.launch {
            mutableStateFlow.update {
                it.copy(dialogState = null)
            }

            val paymentString = UpiQrCodeProcessor.encodeUpiString(state.paymentQrData)

            sendEvent(SendMoneyEvent.NavigateToTransferScreen(paymentString))
        }
    }

    private fun updateErrorState(res: StringResource) {
        mutableStateFlow.update {
            it.copy(dialogState = Error.ResourceMessage(res))
        }
    }

    private fun handleRequestData(action: HandleRequestData) {
        viewModelScope.launch {
            try {
                val requestData = try {
                    UpiQrCodeProcessor.decodeUpiString(action.requestData)
                } catch (e: Exception) {
                    if (StandardUpiQrCodeProcessor.isValidUpiQrCode(action.requestData)) {
                        val standardData = StandardUpiQrCodeProcessor.parseUpiQrCode(action.requestData)
                        StandardUpiQrCodeProcessor.toPaymentQrData(standardData)
                    } else {
                        throw e
                    }
                }

                mutableStateFlow.update { state ->
                    state.copy(
                        amount = requestData.amount,
                        accountNumber = requestData.accountNo,
                        selectedAccount = requestData.toAccount(),
                    )
                }

                sendEvent(SendMoneyEvent.ShowToast(Res.string.feature_send_money_upi_qr_parsed_successfully))
            } catch (e: Exception) {
                val errorState = if (action.requestData.isNotEmpty()) {
                    Error.GenericResourceMessage(
                        Res.string.feature_send_money_error_requesting_payment_qr_but_found,
                        listOf(action.requestData),
                    )
                } else {
                    Error.ResourceMessage(Res.string.feature_send_money_error_requesting_payment_qr_data_missing)
                }

                mutableStateFlow.update {
                    it.copy(
                        dialogState = errorState,
                    )
                }
            }
        }
    }
}

/**
 * State of the send-money screen.
 * @property amount Amount typed by the user.
 * @property accountNumber Destination account number typed by the user.
 * @property selectedAccount Account picked from the search results.
 * @property dialogState Dialog currently shown, if any.
 */
@Serializable
data class SendMoneyState(
    val amount: String = "",
    val accountNumber: String = "",
    val selectedAccount: AccountResult? = null,
    @Transient val dialogState: DialogState? = null,
) {
    /** True if the amount parses to a number greater than zero. */
    val amountIsValid: Boolean
        get() = amount.isNotEmpty() &&
            amount.toDoubleOrNull() != null &&
            amount.toDouble() > 0

    /** True once an account is selected and the amount is valid. */
    val isProceedEnabled: Boolean
        get() = selectedAccount != null && amountIsValid

    /** QR payload describing the selected account and amount. */
    val paymentQrData: PaymentQrData
        get() = PaymentQrData(
            clientId = selectedAccount?.parentId ?: 0,
            clientName = selectedAccount?.parentName ?: "",
            accountNo = selectedAccount?.entityAccountNo ?: "",
            accountId = selectedAccount?.entityId ?: 0,
            amount = amount,
        )

    /** Dialogs the send-money screen can show. */
    sealed interface DialogState {

        /** A loading dialog. */
        data object Loading : DialogState

        /** Error dialogs. */
        @Serializable
        sealed class Error : DialogState {
            /**
             * Error dialog with a single string resource message.
             * @property message Message resource to display.
             */
            @Serializable
            data class ResourceMessage(
                @Serializable(with = StringResourceSerializer::class)
                val message: StringResource,
            ) : Error()

            /**
             * Error dialog with a formatted string resource message.
             * @property message Message resource to display.
             * @property args Format arguments for the message.
             */
            data class GenericResourceMessage(
                @Serializable(with = StringResourceSerializer::class)
                val message: StringResource,
                val args: List<String>,
            ) : Error()
        }
    }
}

/** State of the account search results list. */
sealed interface ViewState {
    /** Search is in progress. */
    data object Loading : ViewState

    /**
     * Search failed.
     * @property message Error message.
     */
    data class Error(val message: String) : ViewState

    /** Search returned no accounts. */
    data object Empty : ViewState

    /** No search has been run yet. */
    data object InitialEmpty : ViewState

    /**
     * Search returned accounts.
     * @property data Matching accounts.
     */
    data class Content(val data: List<AccountResult>) : ViewState
}

/** One-off events emitted by the send-money screen. */
sealed interface SendMoneyEvent {
    /** Navigate back. */
    data object OnNavigateBack : SendMoneyEvent

    /**
     * Navigate to the transfer screen.
     * @property data Serialized payment data to pass along.
     */
    data class NavigateToTransferScreen(val data: String) : SendMoneyEvent

    /** Navigate to the QR scan screen. */
    data object NavigateToScanQrScreen : SendMoneyEvent

    /**
     * Navigate to payee details for a scanned QR code.
     * @property qrCodeData Raw scanned QR value.
     */
    data class NavigateToPayeeDetails(val qrCodeData: String) : SendMoneyEvent, BackgroundEvent

    /**
     * Show a toast message.
     * @property message Message resource to show.
     */
    data class ShowToast(val message: StringResource) : SendMoneyEvent
}

/** User actions on the send-money screen. */
sealed interface SendMoneyAction {
    /** The user tapped back. */
    data object NavigateBack : SendMoneyAction

    /** The user tapped scan. */
    data object OnClickScan : SendMoneyAction

    /**
     * The amount text changed.
     * @property amount New amount text.
     */
    data class AmountChanged(val amount: String) : SendMoneyAction

    /**
     * The account number text changed.
     * @property accountNumber New account number text.
     */
    data class AccountNumberChanged(val accountNumber: String) : SendMoneyAction

    /**
     * The user picked an account.
     * @property account The selected account.
     */
    data class SelectAccount(val account: AccountResult) : SendMoneyAction

    /** The user cleared the selected account. */
    data object DeselectAccount : SendMoneyAction

    /** The user dismissed the dialog. */
    data object DismissDialog : SendMoneyAction

    /** The user tapped proceed. */
    data object OnProceedClicked : SendMoneyAction

    /**
     * Handles payment request data received from a scan or link.
     * @property requestData Raw request data.
     */
    data class HandleRequestData(val requestData: String) : SendMoneyAction
}
