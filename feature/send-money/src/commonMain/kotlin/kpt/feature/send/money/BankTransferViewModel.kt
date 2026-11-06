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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.ui.utils.BaseViewModel
class BankTransferViewModel : BaseViewModel<BankTransferState, BankTransferEvent, BankTransferAction>(
    initialState = BankTransferState(
        selectedFromBankAccount = BankTransferState().bankAccounts.first { it.isDefault },
        selectedToBankAccount = BankTransferState().bankAccounts.first { it.isDefault },
        isFromBankAccountDropdownExpanded = true,
    ),
) {

    override fun handleAction(action: BankTransferAction) {
        when (action) {
            is BankTransferAction.NavigateBack -> {
                sendEvent(BankTransferEvent.NavigateBack)
            }
            is BankTransferAction.UpdateAccountNumber -> {
                mutableStateFlow.update { it.copy(accountNumber = action.accountNumber) }
            }
            is BankTransferAction.UpdateIfscCode -> {
                mutableStateFlow.update { it.copy(ifscCode = action.ifscCode) }
            }
            is BankTransferAction.SearchIfsc -> {
                sendEvent(BankTransferEvent.ShowIfscSearch)
            }
            is BankTransferAction.Continue -> {
                if (state.isFormValid) {
                    mutableStateFlow.update { it.copy(isLoading = true) }
                    // Simulate network call with delay
                    viewModelScope.launch {
                        delay(1500)
                        mutableStateFlow.update { it.copy(isLoading = false, showAdditionalFields = true) }
                    }
                }
            }
            is BankTransferAction.ContinueSelfTransfer -> {
                if (state.isSelfTransferValid) {
                    sendEvent(BankTransferEvent.NavigateToNext)
                }
            }
            is BankTransferAction.ToggleFromBankAccountDropdown -> {
                mutableStateFlow.update {
                    it.copy(
                        isFromBankAccountDropdownExpanded = action.expanded,
                        isToBankAccountDropdownExpanded = false,
                    )
                }
            }
            is BankTransferAction.ToggleToBankAccountDropdown -> {
                mutableStateFlow.update {
                    it.copy(
                        isToBankAccountDropdownExpanded = action.expanded,
                        isFromBankAccountDropdownExpanded = false,
                    )
                }
            }
            is BankTransferAction.SelectFromBankAccount -> {
                mutableStateFlow.update {
                    it.copy(
                        selectedFromBankAccount = action.bankAccount,
                        isFromBankAccountDropdownExpanded = false,
                        isToBankAccountDropdownExpanded = true,
                    )
                }
            }
            is BankTransferAction.SelectToBankAccount -> {
                mutableStateFlow.update {
                    it.copy(
                        selectedToBankAccount = action.bankAccount,
                        isToBankAccountDropdownExpanded = false,
                    )
                }
            }
            is BankTransferAction.AddBankAccount -> {
                sendEvent(BankTransferEvent.AddBankAccount)
            }
            is BankTransferAction.SelectIfscCode -> {
                mutableStateFlow.update { it.copy(ifscCode = action.ifscCode.code) }
                sendEvent(BankTransferEvent.IfscCodeSelected(action.ifscCode))
            }
            is BankTransferAction.UpdateReenterAccountNumber -> {
                mutableStateFlow.update { it.copy(reenterAccountNumber = action.accountNumber) }
            }
            is BankTransferAction.UpdateReceiversName -> {
                mutableStateFlow.update { it.copy(receiversName = action.name) }
            }
            is BankTransferAction.ProceedWithTransfer -> {
                if (state.isAdditionalFieldsValid) {
                    sendEvent(BankTransferEvent.NavigateToNext)
                }
            }
            is BankTransferAction.ToggleAccountNumberMask -> {
                mutableStateFlow.update { it.copy(isAccountNumberMasked = !it.isAccountNumberMasked) }
                if (!state.isAccountNumberMasked) {
                    // When unmasking, we need to focus the field
                    // This will be handled by the UI layer
                }
            }
            is BankTransferAction.ToggleReenterAccountNumberMask -> {
                mutableStateFlow.update { it.copy(isReenterAccountNumberMasked = !it.isReenterAccountNumberMasked) }
            }
            is BankTransferAction.SetIfscFocus -> {
                mutableStateFlow.update { it.copy(isIfscFocused = action.focused) }
                if (action.focused && state.accountNumber.isNotEmpty()) {
                    mutableStateFlow.update { it.copy(isAccountNumberMasked = true) }
                }
            }
            is BankTransferAction.SetAccountNumberFocus -> {
                mutableStateFlow.update { it.copy(isAccountNumberFocused = action.focused) }
            }
            is BankTransferAction.SetReenterAccountNumberFocus -> {
                mutableStateFlow.update { it.copy(isReenterAccountNumberFocused = action.focused) }
            }
            is BankTransferAction.SetReceiversNameFocus -> {
                mutableStateFlow.update { it.copy(isReceiversNameFocused = action.focused) }
                if (action.focused && state.reenterAccountNumber.isNotEmpty()) {
                    mutableStateFlow.update { it.copy(isReenterAccountNumberMasked = true) }
                }
            }
        }
    }
}

data class BankAccount(
    val id: String,
    val bankName: String,
    val accountNumber: String,
    val accountType: String,
    val isDefault: Boolean = false,
) {
    val maskedAccountNumber: String
        get() = "****${accountNumber.takeLast(4)}"
}

data class BankTransferState(
    val isLoading: Boolean = false,
    val accountNumber: String = "",
    val ifscCode: String = "",
    val reenterAccountNumber: String = "",
    val receiversName: String = "",
    val showAdditionalFields: Boolean = false,
    val isAccountNumberMasked: Boolean = false,
    val isReenterAccountNumberMasked: Boolean = false,
    val isIfscFocused: Boolean = false,
    val isAccountNumberFocused: Boolean = false,
    val isReenterAccountNumberFocused: Boolean = false,
    val isReceiversNameFocused: Boolean = false,
    val selectedFromBankAccount: BankAccount? = null,
    val selectedToBankAccount: BankAccount? = null,
    val isFromBankAccountDropdownExpanded: Boolean = false,
    val isToBankAccountDropdownExpanded: Boolean = false,
) {
    val isAccountNumberValid: Boolean
        get() = accountNumber.isNotEmpty() && accountNumber.all { it.isLetterOrDigit() }

    val isIfscCodeValid: Boolean
        get() = ifscCode.isNotEmpty() && ifscCode.matches(Regex("^[A-Z]{4}[A-Z0-9]{7}$"))

    val isReenterAccountNumberValid: Boolean
        get() = reenterAccountNumber.isNotEmpty() && reenterAccountNumber.all { it.isLetterOrDigit() }

    val isReceiversNameValid: Boolean
        get() = receiversName.isNotEmpty() && receiversName.trim().length >= 2

    val isAccountNumberMatching: Boolean
        get() = accountNumber == reenterAccountNumber

    val isFormValid: Boolean
        get() = isAccountNumberValid && isIfscCodeValid

    val isAdditionalFieldsValid: Boolean
        get() = isReenterAccountNumberValid && isReceiversNameValid && isAccountNumberMatching

    val isSelfTransferValid: Boolean
        get() = selectedFromBankAccount != null &&
            selectedToBankAccount != null &&
            selectedFromBankAccount.id != selectedToBankAccount.id

    val bankAccounts: List<BankAccount> = listOf(
        BankAccount(
            id = "1",
            bankName = "State Bank of India",
            accountNumber = "1234567890",
            accountType = "Savings Account",
            isDefault = true,
        ),
        BankAccount(
            id = "2",
            bankName = "HDFC Bank",
            accountNumber = "0987654321",
            accountType = "Current Account",
        ),
    )

    fun getMaskedAccountNumber(accountNumber: String): String {
        return if (accountNumber.isNotEmpty()) {
            "*".repeat(accountNumber.length)
        } else {
            accountNumber
        }
    }

    fun getDisplayAccountNumber(accountNumber: String, isMasked: Boolean): String {
        return if (isMasked && accountNumber.isNotEmpty()) {
            getMaskedAccountNumber(accountNumber)
        } else {
            accountNumber
        }
    }
}

sealed interface BankTransferEvent {
    data object NavigateBack : BankTransferEvent
    data object ShowIfscSearch : BankTransferEvent
    data object NavigateToNext : BankTransferEvent
    data object AddBankAccount : BankTransferEvent
    data class IfscCodeSelected(val ifscCode: IfscCode) : BankTransferEvent
}

sealed interface BankTransferAction {
    data object NavigateBack : BankTransferAction
    data class UpdateAccountNumber(val accountNumber: String) : BankTransferAction
    data class UpdateIfscCode(val ifscCode: String) : BankTransferAction
    data object SearchIfsc : BankTransferAction
    data object Continue : BankTransferAction
    data object ContinueSelfTransfer : BankTransferAction
    data class ToggleFromBankAccountDropdown(val expanded: Boolean) : BankTransferAction
    data class ToggleToBankAccountDropdown(val expanded: Boolean) : BankTransferAction
    data class SelectFromBankAccount(val bankAccount: BankAccount) : BankTransferAction
    data class SelectToBankAccount(val bankAccount: BankAccount) : BankTransferAction
    data object AddBankAccount : BankTransferAction
    data class SelectIfscCode(val ifscCode: IfscCode) : BankTransferAction
    data class UpdateReenterAccountNumber(val accountNumber: String) : BankTransferAction
    data class UpdateReceiversName(val name: String) : BankTransferAction
    data object ProceedWithTransfer : BankTransferAction
    data object ToggleAccountNumberMask : BankTransferAction
    data object ToggleReenterAccountNumberMask : BankTransferAction
    data class SetIfscFocus(val focused: Boolean) : BankTransferAction
    data class SetAccountNumberFocus(val focused: Boolean) : BankTransferAction
    data class SetReenterAccountNumberFocus(val focused: Boolean) : BankTransferAction
    data class SetReceiversNameFocus(val focused: Boolean) : BankTransferAction
}
