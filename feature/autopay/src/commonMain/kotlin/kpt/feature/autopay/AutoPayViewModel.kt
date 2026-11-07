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
import kpt.core.model.autopay.RecurrencePattern
import kpt.core.ui.fork.utils.BaseViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** View model for the AutoPay dashboard; loads schedules and upcoming payments and routes to related screens. */
class AutoPayViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AutoPayState, AutoPayEvent, AutoPayAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AutoPayState(),
) {

    /** Holds the saved-state key used to persist the state. */
    companion object {
        private const val KEY_STATE = "autopay_state"
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        loadDashboardData()
    }

    override fun handleAction(action: AutoPayAction) {
        when (action) {
            is AutoPayAction.ManageRecurringPayment -> {
                setupRecurringPayment()
            }
            is AutoPayAction.ManagePaymentPreferences -> {
                managePaymentPreferences()
            }
            is AutoPayAction.ToggleAutoPay -> {
                toggleAutoPay(action.enabled)
            }
            is AutoPayAction.GetPaymentHistory -> {
                getPaymentHistory()
            }
            is AutoPayAction.RefreshDashboard -> {
                refreshDashboard()
            }
            is AutoPayAction.AddNewSchedule -> {
                addNewSchedule()
            }
            is AutoPayAction.ManageExistingSchedules -> {
                manageExistingSchedules()
            }
            is AutoPayAction.ViewScheduleDetails -> {
                viewScheduleDetails(action.scheduleId)
            }
            is AutoPayAction.AddNewBiller -> {
                addNewBiller()
            }
            is AutoPayAction.ViewBillerList -> {
                viewBillerList()
            }
            is AutoPayAction.AddNewBill -> {
                addNewBill()
            }
            is AutoPayAction.ViewBillList -> {
                viewBillList()
            }
        }
    }

    private fun loadDashboardData() {
        mutableStateFlow.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                delay(1000)

                val billsWithAutoPay = getBillsWithAutoPay()

                val activeSchedules = billsWithAutoPay
                    .filter { it.status == BillStatus.ACTIVE }
                    .map { it.toAutoPaySchedule() }

                val upcomingPayments = billsWithAutoPay
                    .getActiveBillsWithAutoPay()
                    .calculateUpcomingPayments()

                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        activeSchedules = activeSchedules,
                        upcomingPayments = upcomingPayments,
                        totalActiveSchedules = activeSchedules.size,
                        totalUpcomingPayments = upcomingPayments.size,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load dashboard data",
                    )
                }
            }
        }
    }

    /**
     * Gets bills with AutoPay enabled
     * In a real implementation, this would come from a repository
     */
    @OptIn(ExperimentalTime::class)
    private fun getBillsWithAutoPay(): List<Bill> {
        return listOf(
            Bill(
                id = "1",
                name = "Monthly Rent Payment",
                amount = 1200.0,
                currency = "USD",
                dueDate = Clock.System.now().toEpochMilliseconds() + (15 * 24 * 60 * 60 * 1000L),
                recurrencePattern = RecurrencePattern.MONTHLY,
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
                recurrencePattern = RecurrencePattern.MONTHLY,
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
                recurrencePattern = RecurrencePattern.MONTHLY,
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
    }

    private fun refreshDashboard() {
        loadDashboardData()
    }

    private fun addNewSchedule() {
        sendEvent(AutoPayEvent.NavigateToScheduleManagement)
    }

    private fun manageExistingSchedules() {
        sendEvent(AutoPayEvent.NavigateToScheduleManagement)
    }

    private fun viewScheduleDetails(scheduleId: String) {
        sendEvent(AutoPayEvent.NavigateToScheduleDetails(scheduleId))
    }

    private fun setupRecurringPayment() {
        sendEvent(AutoPayEvent.NavigateToScheduleManagement)
    }

    private fun managePaymentPreferences() {
        sendEvent(AutoPayEvent.NavigateToPreferences)
    }

    private fun toggleAutoPay(enabled: Boolean) {
        mutableStateFlow.update {
            it.copy(isAutoPayEnabled = enabled)
        }
    }

    private fun getPaymentHistory() {
        sendEvent(AutoPayEvent.NavigateToHistory)
    }

    private fun addNewBiller() {
        sendEvent(AutoPayEvent.NavigateToAddBiller)
    }

    private fun viewBillerList() {
        sendEvent(AutoPayEvent.NavigateToBillerList)
    }

    private fun addNewBill() {
        sendEvent(AutoPayEvent.NavigateToAddBill)
    }

    private fun viewBillList() {
        sendEvent(AutoPayEvent.NavigateToBillList)
    }
}

/**
 * UI state of the AutoPay dashboard.
 *
 * @property isAutoPayEnabled Whether AutoPay is switched on.
 * @property isLoading Whether data is loading.
 * @property error Error message to show, or null.
 * @property activeSchedules Currently active schedules.
 * @property upcomingPayments Payments due soon.
 * @property totalActiveSchedules Number of active schedules.
 * @property totalUpcomingPayments Number of upcoming payments.
 */
@Serializable
data class AutoPayState(
    val isAutoPayEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeSchedules: List<AutoPaySchedule> = emptyList(),
    val upcomingPayments: List<UpcomingPayment> = emptyList(),
    val totalActiveSchedules: Int = 0,
    val totalUpcomingPayments: Int = 0,
)

/**
 * A recurring payment schedule.
 *
 * @property id Schedule id.
 * @property name Schedule name.
 * @property amount Amount paid each time.
 * @property currency Currency of the amount.
 * @property frequency How often the payment recurs.
 * @property nextPaymentDate Date of the next payment.
 * @property status Status of the schedule.
 * @property recipientName Name of the payee.
 * @property accountNumber Payee account number.
 */
@Serializable
data class AutoPaySchedule(
    val id: String,
    val name: String,
    val amount: Double,
    val currency: String,
    val frequency: String,
    val nextPaymentDate: String,
    val status: AutoPayStatus,
    val recipientName: String,
    val accountNumber: String,
)

/**
 * A payment due soon.
 *
 * @property id Payment id.
 * @property scheduleName Name of the schedule it belongs to.
 * @property amount Amount due.
 * @property currency Currency of the amount.
 * @property dueDate Date the payment is due.
 * @property status Status of the payment.
 * @property recipientName Name of the payee.
 */
@Serializable
data class UpcomingPayment(
    val id: String,
    val scheduleName: String,
    val amount: Double,
    val currency: String,
    val dueDate: String,
    val status: PaymentStatus,
    val recipientName: String,
)

/** Lifecycle status of an AutoPay schedule. */
@Serializable
enum class AutoPayStatus {
    /** The schedule is running. */
    ACTIVE,

    /** The schedule is paused. */
    PAUSED,

    /** The schedule was cancelled. */
    CANCELLED,

    /** The schedule finished. */
    COMPLETED,
}

/** Status of an individual payment. */
@Serializable
enum class PaymentStatus {
    /** The payment is scheduled but not yet due. */
    UPCOMING,

    /** The payment is being processed. */
    PROCESSING,

    /** The payment completed. */
    COMPLETED,

    /** The payment failed. */
    FAILED,

    /** The payment was cancelled. */
    CANCELLED,
}

/** One-off events emitted by the AutoPay dashboard view model. */
sealed interface AutoPayEvent {
    /** Navigate to schedule management. */
    data object NavigateToScheduleManagement : AutoPayEvent

    /** Navigate to preferences. */
    data object NavigateToPreferences : AutoPayEvent

    /** Navigate to history. */
    data object NavigateToHistory : AutoPayEvent

    /** Navigate to add biller. */
    data object NavigateToAddBiller : AutoPayEvent

    /** Navigate to the biller list. */
    data object NavigateToBillerList : AutoPayEvent

    /** Navigate to add bill. */
    data object NavigateToAddBill : AutoPayEvent

    /** Navigate to the bill list. */
    data object NavigateToBillList : AutoPayEvent

    /**
     * Navigate to schedule details.
     *
     * @property scheduleId Id of the schedule.
     */
    data class NavigateToScheduleDetails(val scheduleId: String) : AutoPayEvent
}

/** User actions handled by the AutoPay dashboard view model. */
sealed interface AutoPayAction {
    /** The user chose to manage recurring payments. */
    data object ManageRecurringPayment : AutoPayAction

    /** The user chose to manage payment preferences. */
    data object ManagePaymentPreferences : AutoPayAction

    /** The user chose to view payment history. */
    data object GetPaymentHistory : AutoPayAction

    /**
     * Toggle AutoPay.
     *
     * @property enabled Desired AutoPay state.
     */
    data class ToggleAutoPay(val enabled: Boolean) : AutoPayAction

    /** Reload the dashboard. */
    data object RefreshDashboard : AutoPayAction

    /** Add a new schedule. */
    data object AddNewSchedule : AutoPayAction

    /** Manage existing schedules. */
    data object ManageExistingSchedules : AutoPayAction

    /** Add a new biller. */
    data object AddNewBiller : AutoPayAction

    /** View the biller list. */
    data object ViewBillerList : AutoPayAction

    /** Add a new bill. */
    data object AddNewBill : AutoPayAction

    /** View the bill list. */
    data object ViewBillList : AutoPayAction

    /**
     * View a schedule's details.
     *
     * @property scheduleId Id of the schedule.
     */
    data class ViewScheduleDetails(val scheduleId: String) : AutoPayAction
}
