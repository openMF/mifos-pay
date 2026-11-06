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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.data.util.StandardUpiQrCodeProcessor
import kpt.core.ui.utils.BaseViewModel
class PayeeDetailsViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PayeeDetailsState, PayeeDetailsEvent, PayeeDetailsAction>(
    initialState = PayeeDetailsState(),
) {

    init {
        val safeQrCodeDataString = savedStateHandle.get<String>("qrCodeData") ?: ""

        if (safeQrCodeDataString.isNotEmpty()) {
            val qrCodeDataString = safeQrCodeDataString.urlDecode()
            val isUpiCode = StandardUpiQrCodeProcessor.isValidUpiQrCode(qrCodeDataString)

            val qrCodeData = if (isUpiCode) {
                StandardUpiQrCodeProcessor.parseUpiQrCode(qrCodeDataString)
            } else {
                StandardUpiQrCodeProcessor.parseUpiQrCode("upi://pay?pa=$qrCodeDataString&pn=Unknown")
            }

            val amountInPaise = if (qrCodeData.amount.isNotEmpty()) {
                AmountUtils.rupeesToPaise(qrCodeData.amount)
            } else {
                ""
            }

            mutableStateFlow.update {
                it.copy(
                    payeeName = qrCodeData.payeeName,
                    upiId = qrCodeData.payeeVpa,
                    phoneNumber = "",
                    amount = qrCodeData.amount,
//                    amount = amountInPaise,
                    note = qrCodeData.transactionNote,
                    isAmountEditable = qrCodeData.amount.isEmpty(),
                    isUpiCode = true,
                )
            }
        }
    }

    override fun handleAction(action: PayeeDetailsAction) {
        when (action) {
            is PayeeDetailsAction.NavigateBack -> {
                sendEvent(PayeeDetailsEvent.NavigateBack)
            }
            is PayeeDetailsAction.UpdateAmount -> {
                val cleanAmount = action.amount.replace(",", "")
                val isValidAmount = cleanAmount.isEmpty() || cleanAmount.toDoubleOrNull() != null
            }

            is PayeeDetailsAction.UpdateInputAmount -> {
                val validatedAmount =
                    AmountUtils.validateAndFormatAmountInput(action.inputAmount)
                val isValidAmount = AmountUtils.isValidAmountInput(validatedAmount)

                if (isValidAmount) {
                    val amountInPaise = if (validatedAmount.isNotEmpty()) {
                        AmountUtils.rupeesToPaise(validatedAmount)
                    } else {
                        ""
                    }

                    val amountValue = validatedAmount.toDoubleOrNull() ?: 0.0
                    val showMaxMessage = amountValue > 500000
                    val showMinMessage = amountValue > 0 && amountValue < 1

                    val currentAmount = stateFlow.value.amount
                    val shouldClearAccount = amountInPaise != currentAmount
                    val showMessage = amountValue > 500000

                    mutableStateFlow.value = stateFlow.value.copy(
                        amount = amountInPaise,
                        inputAmount = validatedAmount,
                        showMaxAmountMessage = showMaxMessage,
                        showMinAmountMessage = showMinMessage,
                        selectedAccount = if (shouldClearAccount) null else stateFlow.value.selectedAccount,
                    )

                    if (showMaxMessage) {
                        viewModelScope.launch {
                            delay(2000)
                            mutableStateFlow.value = stateFlow.value.copy(
                                showMaxAmountMessage = false,
                            )
                        }
                    }

                    if (showMinMessage) {
                        viewModelScope.launch {
                            delay(2000)
                            mutableStateFlow.value = stateFlow.value.copy(
                                showMinAmountMessage = false,
                            )
                        }
                    }
                }
            }
            is PayeeDetailsAction.UpdateNote -> {
                val currentNote = stateFlow.value.note
                val shouldClearAccount = action.note != currentNote

                mutableStateFlow.value = stateFlow.value.copy(
                    note = action.note,
                    selectedAccount = if (shouldClearAccount) null else stateFlow.value.selectedAccount,
                )
            }
            is PayeeDetailsAction.NoteFieldFocused -> {
                mutableStateFlow.value = stateFlow.value.copy(
                    hasNoteFieldBeenFocused = true,
                    selectedAccount = null,
                )
            }
            is PayeeDetailsAction.AmountFieldFocused -> {
                mutableStateFlow.value = stateFlow.value.copy(selectedAccount = null)
            }
            is PayeeDetailsAction.ProceedToPayment -> {
                if (stateFlow.value.selectedAccount == null) {
                    val defaultAccount = BankAccount(
                        id = "1",
                        bankName = "State Bank of India",
                        accountNumber = "****1234",
                        isDefault = true,
                        accountType = "",
                    )
                    mutableStateFlow.value = stateFlow.value.copy(
                        selectedAccount = defaultAccount,
                    )
                } else {
                    mutableStateFlow.value =
                        stateFlow.value.copy(showAccountSelectionSheet = true)
                }
            }
            is PayeeDetailsAction.SelectAccount -> {
                mutableStateFlow.value = stateFlow.value.copy(
                    selectedAccount = action.account,
                    showAccountSelectionSheet = false,
                )
            }
            is PayeeDetailsAction.DismissAccountSelection -> {
                mutableStateFlow.value = stateFlow.value.copy(showAccountSelectionSheet = false)
            }
            is PayeeDetailsAction.ContinueFromBottomSheet -> {
                mutableStateFlow.value = stateFlow.value.copy(showAccountSelectionSheet = false)
            }
            is PayeeDetailsAction.ConfirmPayment -> {
                val currentState = stateFlow.value
                if (currentState.isUpiCode) {
                    sendEvent(PayeeDetailsEvent.NavigateToUpiPayment(currentState))
                } else {
                    sendEvent(PayeeDetailsEvent.NavigateToFineractPayment(currentState))
                }
            }
        }
    }
}

data class PayeeDetailsState(
    val payeeName: String = "",
    val upiId: String = "",
    val phoneNumber: String = "",
    val amount: String = "",
    val inputAmount: String = "",
    val note: String = "",
    val isAmountEditable: Boolean = true,
    val isUpiCode: Boolean = false,
    val isLoading: Boolean = false,
    val showMaxAmountMessage: Boolean = false,
    val showMinAmountMessage: Boolean = false,
    val hasNoteFieldBeenFocused: Boolean = false,
    val showAccountSelectionSheet: Boolean = false,
    val selectedAccount: BankAccount? = null,
    val refId: String = "",
) {
    val formattedAmount: String
        get() = if (amount.isEmpty()) {
            "0"
        } else {
            val rupees = AmountUtils.paiseToRupees(amount)
            formatAmountWithCommas(rupees)
        }

    val displayAmount: String
        get() = if (inputAmount.isNotEmpty()) {
            inputAmount
        } else if (amount.isNotEmpty()) {
            AmountUtils.formatAmountForInput(AmountUtils.paiseToRupees(amount))
        } else {
            ""
        }

    val isAmountExceedingMax: Boolean
        get() {
            val rupees = if (amount.isNotEmpty()) AmountUtils.paiseToRupees(amount) else "0.00"
            return rupees.toDoubleOrNull()?.let { it > 500000 } ?: false
        }

    val isAmountBelowMin: Boolean
        get() {
            val rupees = if (amount.isNotEmpty()) AmountUtils.paiseToRupees(amount) else "0.00"
            return rupees.toDoubleOrNull()?.let { it < 1 } ?: false
        }

    private fun formatAmountWithCommas(amountStr: String): String {
        val cleanAmount = amountStr.replace(",", "")
        return try {
            val amount = cleanAmount.toDouble()
            if (amount == 0.0) return if (isUpiCode) "0.00" else "0"

            val parts = amount.toString().split(".")
            val integerPart = parts[0]
            val decimalPart = if (parts.size > 1) parts[1] else ""

            val formattedInteger = integerPart.reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()

            if (isUpiCode) {
                val paddedDecimalPart = decimalPart.padEnd(2, '0').take(2)
                "$formattedInteger.$paddedDecimalPart"
            } else {
                if (decimalPart.isNotEmpty()) {
                    "$formattedInteger.$decimalPart"
                } else {
                    formattedInteger
                }
            }
        } catch (e: NumberFormatException) {
            amountStr
        }
    }
}

sealed interface PayeeDetailsEvent {
    data object NavigateBack : PayeeDetailsEvent
    data class NavigateToUpiPayment(val state: PayeeDetailsState) : PayeeDetailsEvent
    data class NavigateToFineractPayment(val state: PayeeDetailsState) : PayeeDetailsEvent
}

sealed interface PayeeDetailsAction {
    data object NavigateBack : PayeeDetailsAction
    data class UpdateAmount(val amount: String) : PayeeDetailsAction
    data class UpdateInputAmount(val inputAmount: String) : PayeeDetailsAction
    data class UpdateNote(val note: String) : PayeeDetailsAction
    data object NoteFieldFocused : PayeeDetailsAction
    data object AmountFieldFocused : PayeeDetailsAction
    data object ProceedToPayment : PayeeDetailsAction
    data class SelectAccount(val account: BankAccount) : PayeeDetailsAction
    data object DismissAccountSelection : PayeeDetailsAction
    data object ContinueFromBottomSheet : PayeeDetailsAction
    data object ConfirmPayment : PayeeDetailsAction
}

/**
 * URL decodes a string to restore special characters from navigation
 *
 * Optimized for UPI QR codes with future-proofing for common special characters.
 *
 * Essential UPI characters (12):
 * - URL structure: ?, &, =, %
 * - VPA format: @
 * - Common text: space, ", ', comma
 * - URLs: /, :, #, +
 *
 * Future-proofing characters (5):
 * - Currency symbols: $
 * - URL parameters: ;
 * - JSON/structured data: [, ], {, }
 *
 * Note: %25 (percent) must be decoded last to avoid double decoding.
 */
private fun String.urlDecode(): String {
    return this.replace("%20", " ")
        .replace("%26", "&")
        .replace("%3D", "=")
        .replace("%3F", "?")
        .replace("%40", "@")
        .replace("%2B", "+")
        .replace("%2F", "/")
        .replace("%3A", ":")
        .replace("%23", "#")
        .replace("%22", "\"")
        .replace("%27", "'")
        .replace("%2C", ",")
        .replace("%24", "$")
        .replace("%3B", ";")
        .replace("%5B", "[")
        .replace("%5D", "]")
        .replace("%7B", "{")
        .replace("%7D", "}")
        .replace("%25", "%")
}
