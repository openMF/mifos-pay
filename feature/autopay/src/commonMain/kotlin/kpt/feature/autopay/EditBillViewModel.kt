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
import kpt.core.data.util.BillValidator
import kpt.core.datastore.BillRepository
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillFormData
import kpt.core.model.autopay.BillValidationResult
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.NextPaymentDate
import kpt.core.model.autopay.RecurrencePattern
import kpt.core.ui.utils.BaseViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** View model for the edit-bill form; validates input, computes upcoming payment dates and updates the bill. */
class EditBillViewModel(
    savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository,
    private val billerRepository: kpt.core.datastore.BillerRepository,
) : BaseViewModel<EditBillState, EditBillEvent, EditBillAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: EditBillState(),
) {

    private val billId: String = savedStateHandle["billId"] ?: ""

    // Template idiom (core-base/store): the one-shot update goes through a SubmitHandler
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
                        sendEvent(EditBillEvent.BillUpdated(submitState.result))
                        submitBill.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to update bill: ${submitState.error.message}",
                            )
                        }
                        submitBill.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        loadBill()
        loadAvailableBillers()
    }

    override fun handleAction(action: EditBillAction) {
        when (action) {
            is EditBillAction.UpdateBillName -> {
                updateBillName(action.name)
            }
            is EditBillAction.UpdateAmount -> {
                updateAmount(action.amount)
            }
            is EditBillAction.UpdateDueDate -> {
                updateDueDate(action.dueDate)
            }
            is EditBillAction.UpdateRecurrencePattern -> {
                updateRecurrencePattern(action.recurrencePattern)
            }
            is EditBillAction.UpdateDescription -> {
                updateDescription(action.description)
            }
            is EditBillAction.UpdateBill -> {
                updateBill()
            }
            is EditBillAction.ValidateForm -> {
                validateForm()
            }
            is EditBillAction.ClearError -> {
                clearError()
            }
            is EditBillAction.ClearValidationErrors -> {
                clearValidationErrors()
            }
            is EditBillAction.CalculateNextPaymentDates -> {
                calculateNextPaymentDates()
            }
            is EditBillAction.SelectBiller -> {
                selectBiller(action.biller)
            }
            is EditBillAction.UpdateAutoPayEnabled -> {
                updateAutoPayEnabled(action.enabled)
            }
            is EditBillAction.UpdateAutoPayPaymentMethod -> {
                updateAutoPayPaymentMethod(action.paymentMethod)
            }
            is EditBillAction.UpdateAutoPaySourceAccount -> {
                updateAutoPaySourceAccount(action.sourceAccount)
            }
            is EditBillAction.UpdateAutoPayMaxAmount -> {
                updateAutoPayMaxAmount(action.maxAmount)
            }
        }
    }

    private fun loadBill() {
        viewModelScope.launch {
            mutableStateFlow.update { it.copy(isLoading = true) }

            try {
                val bill = billRepository.getBillById(billId)
                if (bill != null) {
                    val formData = BillFormData(
                        name = bill.name,
                        amount = bill.amount.toString(),
                        currency = bill.currency,
                        dueDate = bill.dueDate,
                        recurrencePattern = bill.recurrencePattern,
                        billerId = bill.billerId,
                        billerName = bill.billerName,
                        description = bill.description ?: "",
                        enableAutoPay = bill.autoPayEnabled,
                        autoPayPaymentMethod = bill.autoPayPaymentMethod ?: "",
                        autoPaySourceAccount = bill.autoPaySourceAccount ?: "",
                        autoPayMaxAmount = bill.autoPayMaxAmount?.toString() ?: "",
                    )
                    mutableStateFlow.update {
                        it.copy(
                            formData = formData,
                            isLoading = false,
                            error = null,
                        )
                    }
                    calculateNextPaymentDates()
                } else {
                    mutableStateFlow.update {
                        it.copy(
                            isLoading = false,
                            error = "Bill not found",
                        )
                    }
                }
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load bill: ${e.message}",
                    )
                }
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

    private fun updateBill() {
        val validationResult = validateForm()

        if (!validationResult.isValid) {
            return
        }

        val formData = mutableStateFlow.value.formData

        val bill = Bill(
            id = billId,
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
        // in `init`. The repository returns the updated bill or throws on failure,
        // which the handler surfaces as Failed.
        submitBill.submit {
            billRepository.updateBill(bill)
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

    /** Holds the saved-state key used to persist the form state. */
    companion object {
        private const val KEY_STATE = "edit_bill_state"
    }
}

/**
 * UI state of the edit-bill form.
 *
 * @property formData Bill details being edited.
 * @property validationResult Result of validating the form.
 * @property isLoading Whether a load or save is in progress.
 * @property isSuccess Whether the bill was updated.
 * @property error Error message to show, or null.
 * @property nextPaymentDates Upcoming payment dates for the chosen recurrence.
 * @property availableBillers Billers the user can pick from.
 */
@Serializable
data class EditBillState(
    val formData: BillFormData = BillFormData(),
    val validationResult: BillValidationResult = BillValidationResult(isValid = false),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nextPaymentDates: List<NextPaymentDate> = emptyList(),
    val availableBillers: List<Biller> = emptyList(),
)

/** One-off events emitted by the edit-bill view model. */
sealed interface EditBillEvent {
    /**
     * The bill was updated.
     *
     * @property bill The updated bill.
     */
    data class BillUpdated(val bill: Bill) : EditBillEvent
}

/** User actions handled by the edit-bill view model. */
sealed interface EditBillAction {
    /**
     * The bill name changed.
     *
     * @property name New bill name.
     */
    data class UpdateBillName(val name: String) : EditBillAction

    /**
     * The amount changed.
     *
     * @property amount New amount as typed.
     */
    data class UpdateAmount(val amount: String) : EditBillAction

    /**
     * The due date changed.
     *
     * @property dueDate New due date as epoch milliseconds.
     */
    data class UpdateDueDate(val dueDate: Long) : EditBillAction

    /**
     * The recurrence pattern changed.
     *
     * @property recurrencePattern New recurrence pattern.
     */
    data class UpdateRecurrencePattern(val recurrencePattern: RecurrencePattern) : EditBillAction

    /**
     * The description changed.
     *
     * @property description New description.
     */
    data class UpdateDescription(val description: String) : EditBillAction

    /** The user tapped update. */
    data object UpdateBill : EditBillAction

    /** Validate the form. */
    data object ValidateForm : EditBillAction

    /** Clear the current error. */
    data object ClearError : EditBillAction

    /** Clear field-level validation errors. */
    data object ClearValidationErrors : EditBillAction

    /** Recompute the upcoming payment dates. */
    data object CalculateNextPaymentDates : EditBillAction

    /**
     * A biller was selected.
     *
     * @property biller The selected biller.
     */
    data class SelectBiller(val biller: Biller) : EditBillAction

    // AutoPay actions
    /**
     * AutoPay was switched on or off for the bill.
     *
     * @property enabled Whether AutoPay is enabled.
     */
    data class UpdateAutoPayEnabled(val enabled: Boolean) : EditBillAction

    /**
     * The AutoPay payment method changed.
     *
     * @property paymentMethod New payment method.
     */
    data class UpdateAutoPayPaymentMethod(val paymentMethod: String) : EditBillAction

    /**
     * The AutoPay source account changed.
     *
     * @property sourceAccount New source account.
     */
    data class UpdateAutoPaySourceAccount(val sourceAccount: String) : EditBillAction

    /**
     * The AutoPay maximum amount changed.
     *
     * @property maxAmount New maximum amount as typed.
     */
    data class UpdateAutoPayMaxAmount(val maxAmount: String) : EditBillAction
}
