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
import kpt.core.data.repository.AccountRepository
import kpt.core.data.util.StandardUpiQrCodeProcessor
import kpt.core.data.util.UpiQrCodeProcessor
import kpt.core.model.search.AccountResult
import kpt.core.model.utils.PaymentQrData
import kpt.core.model.utils.toAccount
import kpt.core.ui.utils.BackgroundEvent
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.send.money.SendMoneyAction.HandleRequestData
import kpt.feature.send.money.SendMoneyState.DialogState.Error
import mifos_pay.feature.send_money.generated.resources.Res
import mifos_pay.feature.send_money.generated.resources.feature_send_money_error_account_cannot_be_empty
import mifos_pay.feature.send_money.generated.resources.feature_send_money_error_amount_cannot_be_empty
import mifos_pay.feature.send_money.generated.resources.feature_send_money_error_invalid_amount
import mifos_pay.feature.send_money.generated.resources.feature_send_money_error_requesting_payment_qr_but_found
import mifos_pay.feature.send_money.generated.resources.feature_send_money_error_requesting_payment_qr_data_missing
import mifos_pay.feature.send_money.generated.resources.feature_send_money_upi_qr_parsed_successfully
import org.jetbrains.compose.resources.StringResource
class SendMoneyViewModel(
    private val scanner: QrScanner,
    repository: AccountRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SendMoneyState, SendMoneyEvent, SendMoneyAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: SendMoneyState(),
) {

    companion object {
        private const val KEY_STATE = "send_payment_state"
    }

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

@Serializable
data class SendMoneyState(
    val amount: String = "",
    val accountNumber: String = "",
    val selectedAccount: AccountResult? = null,
    @Transient val dialogState: DialogState? = null,
) {
    val amountIsValid: Boolean
        get() = amount.isNotEmpty() &&
            amount.toDoubleOrNull() != null &&
            amount.toDouble() > 0

    val isProceedEnabled: Boolean
        get() = selectedAccount != null && amountIsValid

    val paymentQrData: PaymentQrData
        get() = PaymentQrData(
            clientId = selectedAccount?.parentId ?: 0,
            clientName = selectedAccount?.parentName ?: "",
            accountNo = selectedAccount?.entityAccountNo ?: "",
            accountId = selectedAccount?.entityId ?: 0,
            amount = amount,
        )

    sealed interface DialogState {

        data object Loading : DialogState

        @Serializable
        sealed class Error : DialogState {
            @Serializable
            data class ResourceMessage(
                @Serializable(with = StringResourceSerializer::class)
                val message: StringResource,
            ) : Error()

            data class GenericResourceMessage(
                @Serializable(with = StringResourceSerializer::class)
                val message: StringResource,
                val args: List<String>,
            ) : Error()
        }
    }
}

sealed interface ViewState {
    data object Loading : ViewState
    data class Error(val message: String) : ViewState
    data object Empty : ViewState
    data object InitialEmpty : ViewState
    data class Content(val data: List<AccountResult>) : ViewState
}

sealed interface SendMoneyEvent {
    data object OnNavigateBack : SendMoneyEvent
    data class NavigateToTransferScreen(val data: String) : SendMoneyEvent
    data object NavigateToScanQrScreen : SendMoneyEvent

    data class NavigateToPayeeDetails(val qrCodeData: String) : SendMoneyEvent, BackgroundEvent
    data class ShowToast(val message: StringResource) : SendMoneyEvent
}

sealed interface SendMoneyAction {
    data object NavigateBack : SendMoneyAction

    data object OnClickScan : SendMoneyAction

    data class AmountChanged(val amount: String) : SendMoneyAction

    data class AccountNumberChanged(val accountNumber: String) : SendMoneyAction

    data class SelectAccount(val account: AccountResult) : SendMoneyAction

    data object DeselectAccount : SendMoneyAction

    data object DismissDialog : SendMoneyAction

    data object OnProceedClicked : SendMoneyAction

    data class HandleRequestData(val requestData: String) : SendMoneyAction
}
