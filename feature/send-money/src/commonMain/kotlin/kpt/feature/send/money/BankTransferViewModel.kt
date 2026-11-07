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
import kpt.core.ui.fork.utils.BaseViewModel
/**
 * ViewModel for the bank transfer screen; validates account details and handles IFSC selection and self transfers.
 */
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

/**
 * A linked bank account of the user.
 * @property id Unique identifier of the account.
 * @property bankName Name of the bank.
 * @property accountNumber Full account number.
 * @property accountType Account type, such as Savings Account.
 * @property isDefault Whether this is the default account.
 */
data class BankAccount(
    val id: String,
    val bankName: String,
    val accountNumber: String,
    val accountType: String,
    val isDefault: Boolean = false,
) {
    /** The account number with all but the last four digits hidden. */
    val maskedAccountNumber: String
        get() = "****${accountNumber.takeLast(4)}"
}

/**
 * State of the bank transfer screen.
 * @property isLoading Whether a request is in progress.
 * @property accountNumber Beneficiary account number.
 * @property ifscCode Beneficiary IFSC code.
 * @property reenterAccountNumber Re-entered account number for confirmation.
 * @property receiversName Name of the receiver.
 * @property showAdditionalFields Whether the confirmation fields are shown.
 * @property isAccountNumberMasked Whether the account number is masked.
 * @property isReenterAccountNumberMasked Whether the re-entered account number is masked.
 * @property isIfscFocused Whether the IFSC field has focus.
 * @property isAccountNumberFocused Whether the account number field has focus.
 * @property isReenterAccountNumberFocused Whether the re-enter field has focus.
 * @property isReceiversNameFocused Whether the receiver name field has focus.
 * @property selectedFromBankAccount Source account for a self transfer.
 * @property selectedToBankAccount Destination account for a self transfer.
 * @property isFromBankAccountDropdownExpanded Whether the source account dropdown is open.
 * @property isToBankAccountDropdownExpanded Whether the destination account dropdown is open.
 */
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
    /** True if the account number is non-empty and alphanumeric. */
    val isAccountNumberValid: Boolean
        get() = accountNumber.isNotEmpty() && accountNumber.all { it.isLetterOrDigit() }

    /** True if the IFSC code matches four letters followed by seven letters or digits. */
    val isIfscCodeValid: Boolean
        get() = ifscCode.isNotEmpty() && ifscCode.matches(Regex("^[A-Z]{4}[A-Z0-9]{7}$"))

    /** True if the re-entered account number is non-empty and alphanumeric. */
    val isReenterAccountNumberValid: Boolean
        get() = reenterAccountNumber.isNotEmpty() && reenterAccountNumber.all { it.isLetterOrDigit() }

    /** True if the receiver name has at least two characters. */
    val isReceiversNameValid: Boolean
        get() = receiversName.isNotEmpty() && receiversName.trim().length >= 2

    /** True if the re-entered account number equals the account number. */
    val isAccountNumberMatching: Boolean
        get() = accountNumber == reenterAccountNumber

    /** True if both account number and IFSC code are valid. */
    val isFormValid: Boolean
        get() = isAccountNumberValid && isIfscCodeValid

    /** True if the confirmation fields and receiver name are valid and the account numbers match. */
    val isAdditionalFieldsValid: Boolean
        get() = isReenterAccountNumberValid && isReceiversNameValid && isAccountNumberMatching

    /** True if two different accounts are selected for a self transfer. */
    val isSelfTransferValid: Boolean
        get() = selectedFromBankAccount != null &&
            selectedToBankAccount != null &&
            selectedFromBankAccount.id != selectedToBankAccount.id

    /** Sample linked bank accounts offered for self transfers. */
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

    /** Returns [accountNumber] with every character replaced by an asterisk. */
    fun getMaskedAccountNumber(accountNumber: String): String {
        return if (accountNumber.isNotEmpty()) {
            "*".repeat(accountNumber.length)
        } else {
            accountNumber
        }
    }

    /** Returns the masked or plain [accountNumber] depending on [isMasked]. */
    fun getDisplayAccountNumber(accountNumber: String, isMasked: Boolean): String {
        return if (isMasked && accountNumber.isNotEmpty()) {
            getMaskedAccountNumber(accountNumber)
        } else {
            accountNumber
        }
    }
}

/** One-off events emitted by the bank transfer screen. */
sealed interface BankTransferEvent {
    /** Navigate back. */
    data object NavigateBack : BankTransferEvent

    /** Open the IFSC search screen. */
    data object ShowIfscSearch : BankTransferEvent

    /** Navigate to the next step of the transfer. */
    data object NavigateToNext : BankTransferEvent

    /** Open the add bank account flow. */
    data object AddBankAccount : BankTransferEvent

    /**
     * An IFSC code was chosen.
     * @property ifscCode The chosen record.
     */
    data class IfscCodeSelected(val ifscCode: IfscCode) : BankTransferEvent
}

/** User actions on the bank transfer screen. */
sealed interface BankTransferAction {
    /** The user tapped back. */
    data object NavigateBack : BankTransferAction

    /**
     * The account number text changed.
     * @property accountNumber New account number text.
     */
    data class UpdateAccountNumber(val accountNumber: String) : BankTransferAction

    /**
     * The IFSC code text changed.
     * @property ifscCode New IFSC code text.
     */
    data class UpdateIfscCode(val ifscCode: String) : BankTransferAction

    /** The user tapped search IFSC. */
    data object SearchIfsc : BankTransferAction

    /** The user tapped continue. */
    data object Continue : BankTransferAction

    /** The user tapped continue on a self transfer. */
    data object ContinueSelfTransfer : BankTransferAction

    /**
     * Opens or closes the source account dropdown.
     * @property expanded Whether the dropdown should be open.
     */
    data class ToggleFromBankAccountDropdown(val expanded: Boolean) : BankTransferAction

    /**
     * Opens or closes the destination account dropdown.
     * @property expanded Whether the dropdown should be open.
     */
    data class ToggleToBankAccountDropdown(val expanded: Boolean) : BankTransferAction

    /**
     * The user picked the source account.
     * @property bankAccount The selected account.
     */
    data class SelectFromBankAccount(val bankAccount: BankAccount) : BankTransferAction

    /**
     * The user picked the destination account.
     * @property bankAccount The selected account.
     */
    data class SelectToBankAccount(val bankAccount: BankAccount) : BankTransferAction

    /** The user tapped add bank account. */
    data object AddBankAccount : BankTransferAction

    /**
     * The user picked an IFSC record.
     * @property ifscCode The picked record.
     */
    data class SelectIfscCode(val ifscCode: IfscCode) : BankTransferAction

    /**
     * The re-entered account number changed.
     * @property accountNumber New re-entered account number.
     */
    data class UpdateReenterAccountNumber(val accountNumber: String) : BankTransferAction

    /**
     * The receiver name changed.
     * @property name New receiver name.
     */
    data class UpdateReceiversName(val name: String) : BankTransferAction

    /** The user proceeded with the transfer. */
    data object ProceedWithTransfer : BankTransferAction

    /** The user toggled masking of the account number. */
    data object ToggleAccountNumberMask : BankTransferAction

    /** The user toggled masking of the re-entered account number. */
    data object ToggleReenterAccountNumberMask : BankTransferAction

    /**
     * The IFSC field focus changed.
     * @property focused Whether the field has focus.
     */
    data class SetIfscFocus(val focused: Boolean) : BankTransferAction

    /**
     * The account number field focus changed.
     * @property focused Whether the field has focus.
     */
    data class SetAccountNumberFocus(val focused: Boolean) : BankTransferAction

    /**
     * The re-enter account number field focus changed.
     * @property focused Whether the field has focus.
     */
    data class SetReenterAccountNumberFocus(val focused: Boolean) : BankTransferAction

    /**
     * The receiver name field focus changed.
     * @property focused Whether the field has focus.
     */
    data class SetReceiversNameFocus(val focused: Boolean) : BankTransferAction
}
