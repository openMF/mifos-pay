/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.DateHelper
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.util.BillValidator
import kpt.core.datastore.BillRepository
import kpt.core.datastore.BillerRepository
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillFormData
import kpt.core.model.autopay.BillValidationResult
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.NextPaymentDate
import kpt.core.model.autopay.RecurrencePattern
import kpt.core.ui.utils.BaseViewModel
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AddBillViewModel(
    savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository,
    private val billerRepository: BillerRepository,
) : BaseViewModel<AddBillState, AddBillEvent, AddBillAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AddBillState(),
) {

    companion object {
        private const val KEY_STATE = "add_bill_state"
    }

    // Template idiom (core-base/store): the one-shot create goes through a SubmitHandler
    // instead of a hand-folded DataState result. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it to drive this screen's
    // existing loading/success/error UX, so the Screen is unchanged.
    private val submitBill = viewModelScope.submitHandler<Bill>()

    init {
        submitBill.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update { it.copy(isLoading = true) }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update {
                            it.copy(isLoading = false, isSuccess = true)
                        }
                        sendEvent(AddBillEvent.BillSaved(submitState.result))
                        submitBill.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to save bill: ${submitState.error.message}",
                            )
                        }
                        submitBill.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        loadAvailableBillers()
    }

    override fun handleAction(action: AddBillAction) {
        when (action) {
            is AddBillAction.UpdateBillName -> {
                updateBillName(action.name)
            }
            is AddBillAction.UpdateAmount -> {
                updateAmount(action.amount)
            }
            is AddBillAction.UpdateDueDate -> {
                updateDueDate(action.dueDate)
            }
            is AddBillAction.UpdateRecurrencePattern -> {
                updateRecurrencePattern(action.recurrencePattern)
            }
            is AddBillAction.UpdateDescription -> {
                updateDescription(action.description)
            }
            is AddBillAction.SaveBill -> {
                saveBill()
            }
            is AddBillAction.ValidateForm -> {
                validateForm()
            }
            is AddBillAction.ClearError -> {
                clearError()
            }
            is AddBillAction.ClearValidationErrors -> {
                clearValidationErrors()
            }
            is AddBillAction.CalculateNextPaymentDates -> {
                calculateNextPaymentDates()
            }
            is AddBillAction.SelectBiller -> {
                selectBiller(action.biller)
            }
            is AddBillAction.RefreshBillers -> {
                loadAvailableBillers()
            }
            is AddBillAction.UpdateAutoPayEnabled -> {
                updateAutoPayEnabled(action.enabled)
            }
            is AddBillAction.UpdateAutoPayPaymentMethod -> {
                updateAutoPayPaymentMethod(action.paymentMethod)
            }
            is AddBillAction.UpdateAutoPaySourceAccount -> {
                updateAutoPaySourceAccount(action.sourceAccount)
            }
            is AddBillAction.UpdateAutoPayMaxAmount -> {
                updateAutoPayMaxAmount(action.maxAmount)
            }
        }
    }

    private fun updateBillName(name: String) {
        val nameError = BillValidator.validateBillFormData(
            BillFormData(name = name),
        ).nameError
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(name = name),
                validationResult = it.validationResult.copy(nameError = nameError),
            )
        }
    }

    private fun updateAmount(amount: String) {
        val amountError = BillValidator.validateBillFormData(
            BillFormData(amount = amount),
        ).amountError
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(amount = amount),
                validationResult = it.validationResult.copy(amountError = amountError),
            )
        }
    }

    private fun updateDueDate(dueDate: Long) {
        val dueDateError = BillValidator.validateBillFormData(
            BillFormData(dueDate = dueDate),
        ).dueDateError
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(dueDate = dueDate),
                validationResult = it.validationResult.copy(dueDateError = dueDateError),
            )
        }
        calculateNextPaymentDates()
    }

    private fun updateRecurrencePattern(recurrencePattern: RecurrencePattern) {
        val recurrencePatternError = BillValidator.validateBillFormData(
            BillFormData(recurrencePattern = recurrencePattern),
        ).recurrencePatternError
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(recurrencePattern = recurrencePattern),
                validationResult = it.validationResult.copy(recurrencePatternError = recurrencePatternError),
            )
        }
        calculateNextPaymentDates()
    }

    private fun updateDescription(description: String) {
        mutableStateFlow.update {
            it.copy(formData = it.formData.copy(description = description))
        }
    }

    private fun validateForm(): BillValidationResult {
        val currentState = stateFlow.value
        val formData = currentState.formData

        val validationResult = BillValidator.validateBillFormData(formData)

        mutableStateFlow.update { it.copy(validationResult = validationResult) }
        return validationResult
    }

    @OptIn(ExperimentalTime::class)
    private fun generateUniqueId(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(100000, 999999)
        return "$timestamp-$random"
    }

    @OptIn(ExperimentalTime::class)
    private fun calculateNextPaymentDates() {
        val currentState = stateFlow.value
        val formData = currentState.formData

        if (formData.dueDate == 0L || formData.recurrencePattern == RecurrencePattern.NONE) {
            mutableStateFlow.update { it.copy(nextPaymentDates = emptyList()) }
            return
        }

        val nextDates = mutableListOf<NextPaymentDate>()
        val currentTime = Clock.System.now().toEpochMilliseconds()
        var nextDate = formData.dueDate

        // Generate next 5 payment dates
        repeat(5) {
            if (nextDate >= currentTime) {
                val isOverdue = nextDate < currentTime
                val formattedDate = formatDate(nextDate)
                nextDates.add(NextPaymentDate(nextDate, formattedDate, isOverdue))
            }

            // Calculate next date based on recurrence pattern
            nextDate += (formData.recurrencePattern.interval * 24 * 60 * 60 * 1000L)
        }

        mutableStateFlow.update { it.copy(nextPaymentDates = nextDates) }
    }

    private fun formatDate(timestamp: Long): String {
        return DateHelper.getDateAsStringFromLong(timestamp)
    }

    private fun saveBill() {
        val validationResult = validateForm()

        if (!validationResult.isValid) {
            return
        }

        val formData = stateFlow.value.formData

        val bill = Bill(
            id = generateUniqueId(),
            name = formData.name.trim(),
            amount = formData.amount.toDoubleOrNull() ?: 0.0,
            currency = formData.currency,
            dueDate = formData.dueDate,
            recurrencePattern = formData.recurrencePattern,
            billerId = formData.billerId,
            billerName = formData.billerName,
            description = formData.description.takeIf { it.isNotBlank() },
            // AutoPay configuration
            autoPayEnabled = formData.enableAutoPay,
            autoPayPaymentMethod = formData.autoPayPaymentMethod.takeIf { it.isNotBlank() },
            autoPaySourceAccount = formData.autoPaySourceAccount.takeIf { it.isNotBlank() },
            autoPayMaxAmount = formData.autoPayMaxAmount.toDoubleOrNull(),
        )

        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository returns the saved bill or throws on failure,
        // which the handler surfaces as Failed.
        submitBill.submit {
            billRepository.saveBill(bill)
        }
    }

    private fun loadAvailableBillers() {
        viewModelScope.launch {
            try {
                billerRepository.getAllBillers().collect { billers ->
                    mutableStateFlow.update { it.copy(availableBillers = billers) }
                }
            } catch (exception: Exception) {
                // Handle error silently for now
            }
        }
    }

    private fun selectBiller(biller: Biller) {
        val billerError = BillValidator.validateBillFormData(
            BillFormData(billerId = biller.id, billerName = biller.name),
        ).billerError
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(
                    billerId = biller.id,
                    billerName = biller.name,
                ),
                validationResult = it.validationResult.copy(billerError = billerError),
            )
        }
    }

    private fun updateAutoPayEnabled(enabled: Boolean) {
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(enableAutoPay = enabled),
            )
        }
    }

    private fun updateAutoPayPaymentMethod(paymentMethod: String) {
        val paymentMethodError = if (stateFlow.value.formData.enableAutoPay && paymentMethod.isBlank()) {
            "Payment method is required when AutoPay is enabled"
        } else {
            null
        }

        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(autoPayPaymentMethod = paymentMethod),
                validationResult = it.validationResult.copy(autoPayPaymentMethodError = paymentMethodError),
            )
        }
    }

    private fun updateAutoPaySourceAccount(sourceAccount: String) {
        val sourceAccountError = if (stateFlow.value.formData.enableAutoPay && sourceAccount.isBlank()) {
            "Source account is required when AutoPay is enabled"
        } else {
            null
        }

        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(autoPaySourceAccount = sourceAccount),
                validationResult = it.validationResult.copy(autoPaySourceAccountError = sourceAccountError),
            )
        }
    }

    private fun updateAutoPayMaxAmount(maxAmount: String) {
        val maxAmountError = if (maxAmount.isNotBlank()) {
            val amount = maxAmount.toDoubleOrNull()
            if (amount == null) {
                "Invalid amount format"
            } else if (amount <= 0) {
                "Maximum amount must be greater than 0"
            } else {
                null
            }
        } else {
            null
        }

        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(autoPayMaxAmount = maxAmount),
                validationResult = it.validationResult.copy(autoPayMaxAmountError = maxAmountError),
            )
        }
    }

    private fun clearError() {
        mutableStateFlow.update { it.copy(error = null) }
    }

    private fun clearValidationErrors() {
        mutableStateFlow.update {
            it.copy(
                validationResult = BillValidationResult(isValid = false),
            )
        }
    }
}

@Serializable
data class AddBillState(
    val formData: BillFormData = BillFormData(),
    val validationResult: BillValidationResult = BillValidationResult(isValid = false),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nextPaymentDates: List<NextPaymentDate> = emptyList(),
    val availableBillers: List<Biller> = emptyList(),
)

sealed interface AddBillEvent {
    data class BillSaved(val bill: Bill) : AddBillEvent
}

sealed interface AddBillAction {
    data class UpdateBillName(val name: String) : AddBillAction
    data class UpdateAmount(val amount: String) : AddBillAction
    data class UpdateDueDate(val dueDate: Long) : AddBillAction
    data class UpdateRecurrencePattern(val recurrencePattern: RecurrencePattern) : AddBillAction
    data class UpdateDescription(val description: String) : AddBillAction
    data object SaveBill : AddBillAction
    data object ValidateForm : AddBillAction
    data object ClearError : AddBillAction
    data object ClearValidationErrors : AddBillAction
    data object CalculateNextPaymentDates : AddBillAction
    data class SelectBiller(val biller: Biller) : AddBillAction
    data object RefreshBillers : AddBillAction

    // AutoPay actions
    data class UpdateAutoPayEnabled(val enabled: Boolean) : AddBillAction
    data class UpdateAutoPayPaymentMethod(val paymentMethod: String) : AddBillAction
    data class UpdateAutoPaySourceAccount(val sourceAccount: String) : AddBillAction
    data class UpdateAutoPayMaxAmount(val maxAmount: String) : AddBillAction
}
