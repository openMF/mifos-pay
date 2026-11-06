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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillStatus
import kpt.core.ui.utils.BaseViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AutoPayScheduleManagementViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AutoPayScheduleManagementState, AutoPayScheduleManagementEvent, AutoPayScheduleManagementAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AutoPayScheduleManagementState(),
) {

    companion object {
        private const val KEY_STATE = "autopay_schedule_management_state"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        loadSchedules()
    }

    override fun handleAction(action: AutoPayScheduleManagementAction) {
        when (action) {
            is AutoPayScheduleManagementAction.RefreshSchedules -> {
                loadSchedules()
            }
            is AutoPayScheduleManagementAction.ToggleAutoPay -> {
                toggleAutoPay(action.billId, action.enabled)
            }
            is AutoPayScheduleManagementAction.EditBill -> {
                sendEvent(AutoPayScheduleManagementEvent.NavigateToEditBill(action.billId))
            }
            is AutoPayScheduleManagementAction.DeleteBill -> {
                deleteBill(action.billId)
            }
            is AutoPayScheduleManagementAction.AddNewBill -> {
                sendEvent(AutoPayScheduleManagementEvent.NavigateToAddBill)
            }
            is AutoPayScheduleManagementAction.ViewAllBills -> {
                sendEvent(AutoPayScheduleManagementEvent.NavigateToBillList)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun loadSchedules() {
        mutableStateFlow.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                delay(1000)

                val dummyBillsWithAutoPay = listOf(
                    Bill(
                        id = "1",
                        name = "Monthly Rent Payment",
                        amount = 1200.0,
                        currency = "USD",
                        dueDate = Clock.System.now().toEpochMilliseconds() + (15 * 24 * 60 * 60 * 1000L),
                        recurrencePattern = kpt.core.model.autopay.RecurrencePattern.MONTHLY,
                        billerId = "biller1",
                        billerName = "Landlord Corp",
                        description = "Monthly rent payment",
                        isActive = true,
                        status = BillStatus.ACTIVE,
                        autoPayEnabled = true,
                        autoPayPaymentMethod = "Bank Account",
                        autoPaySourceAccount = "****1234",
                        autoPayMaxAmount = 1500.0,
                    ),
                    Bill(
                        id = "2",
                        name = "Internet Bill",
                        amount = 89.99,
                        currency = "USD",
                        dueDate = Clock.System.now().toEpochMilliseconds() + (20 * 24 * 60 * 60 * 1000L),
                        recurrencePattern = kpt.core.model.autopay.RecurrencePattern.MONTHLY,
                        billerId = "biller2",
                        billerName = "NetConnect",
                        description = "Monthly internet service",
                        isActive = true,
                        status = BillStatus.ACTIVE,
                        autoPayEnabled = true,
                        autoPayPaymentMethod = "Credit Card",
                        autoPaySourceAccount = "****5678",
                        autoPayMaxAmount = 100.0,
                    ),
                    Bill(
                        id = "3",
                        name = "Gym Membership",
                        amount = 45.0,
                        currency = "USD",
                        dueDate = Clock.System.now().toEpochMilliseconds() + (25 * 24 * 60 * 60 * 1000L),
                        recurrencePattern = kpt.core.model.autopay.RecurrencePattern.MONTHLY,
                        billerId = "biller3",
                        billerName = "FitLife Gym",
                        description = "Monthly gym membership",
                        isActive = true,
                        status = BillStatus.PAUSED,
                        autoPayEnabled = true,
                        autoPayPaymentMethod = "Bank Account",
                        autoPaySourceAccount = "****9012",
                        autoPayMaxAmount = 50.0,
                    ),
                )

                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        billsWithAutoPay = dummyBillsWithAutoPay,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load schedules",
                    )
                }
            }
        }
    }

    private fun toggleAutoPay(billId: String, enabled: Boolean) {
        mutableStateFlow.update { currentState ->
            val updatedBills = currentState.billsWithAutoPay.map { bill ->
                if (bill.id == billId) {
                    bill.copy(autoPayEnabled = enabled)
                } else {
                    bill
                }
            }
            currentState.copy(billsWithAutoPay = updatedBills)
        }

        viewModelScope.launch {
            try {
                delay(500)
                sendEvent(AutoPayScheduleManagementEvent.AutoPayToggled(billId, enabled))
            } catch (e: Exception) {
                loadSchedules()
            }
        }
    }

    private fun deleteBill(billId: String) {
        mutableStateFlow.update { currentState ->
            val updatedBills = currentState.billsWithAutoPay.filter { it.id != billId }
            currentState.copy(billsWithAutoPay = updatedBills)
        }

        viewModelScope.launch {
            try {
                delay(500)
            } catch (e: Exception) {
                loadSchedules()
            }
        }
    }
}

@Serializable
data class AutoPayScheduleManagementState(
    val billsWithAutoPay: List<Bill> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface AutoPayScheduleManagementEvent {
    data object NavigateToAddBill : AutoPayScheduleManagementEvent
    data class NavigateToEditBill(val billId: String) : AutoPayScheduleManagementEvent
    data object NavigateToBillList : AutoPayScheduleManagementEvent
    data class AutoPayToggled(val billId: String, val enabled: Boolean) : AutoPayScheduleManagementEvent
}

sealed interface AutoPayScheduleManagementAction {
    data object RefreshSchedules : AutoPayScheduleManagementAction
    data class ToggleAutoPay(val billId: String, val enabled: Boolean) : AutoPayScheduleManagementAction
    data class EditBill(val billId: String) : AutoPayScheduleManagementAction
    data class DeleteBill(val billId: String) : AutoPayScheduleManagementAction
    data object AddNewBill : AutoPayScheduleManagementAction
    data object ViewAllBills : AutoPayScheduleManagementAction
}
