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

import co.touchlab.kermit.Logger
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kpt.core.common.DateHelper
import kpt.core.ui.utils.BaseViewModel
class UpiTransactionHistoryViewModel() : BaseViewModel<UpiTransactionHistoryState, UpiTransactionHistoryEvent, UpiTransactionHistoryAction>(
    initialState = UpiTransactionHistoryState(),
) {

    private val dummyTransactions = generateDummyTransactions()

    override fun handleAction(action: UpiTransactionHistoryAction) {
        Logger.d("UPI_TRANSACTION_HISTORY UpiTransactionHistoryViewModel Action: $action")
        when (action) {
            UpiTransactionHistoryAction.NavigateBack -> {
                sendEvent(UpiTransactionHistoryEvent.NavigateBack)
            }
            is UpiTransactionHistoryAction.SearchQueryChanged -> {
                mutableStateFlow.value = mutableStateFlow.value.copy(searchQuery = action.query)
            }
            UpiTransactionHistoryAction.SearchPerformed -> {
                performSearch()
            }
            UpiTransactionHistoryAction.ClearSearch -> {
                clearSearch()
            }
            UpiTransactionHistoryAction.BackPressed -> {
                handleBackPressed()
            }
        }
    }

    private fun performSearch() {
        val query = mutableStateFlow.value.searchQuery.trim()
        Logger.d("UPI_TRANSACTION_HISTORY UpiTransactionHistoryViewModel Performing search with query: $query")

        val filteredTransactions = if (query.isEmpty()) {
            dummyTransactions
        } else {
            dummyTransactions.filter { transaction ->
                transaction.payeeName.contains(query, ignoreCase = true)
            }
        }

        val groupedTransactions = groupTransactionsByMonth(filteredTransactions)
        mutableStateFlow.value = mutableStateFlow.value.copy(
            groupedTransactions = groupedTransactions,
            isLoading = false,
            isSearchActive = query.isNotEmpty(),
        )
    }

    private fun clearSearch() {
        Logger.d("UPI_TRANSACTION_HISTORY UpiTransactionHistoryViewModel Clearing search")
        val groupedTransactions = groupTransactionsByMonth(dummyTransactions)
        mutableStateFlow.value = mutableStateFlow.value.copy(
            searchQuery = "",
            groupedTransactions = groupedTransactions,
            isSearchActive = false,
        )
    }

    private fun handleBackPressed() {
        val currentState = mutableStateFlow.value
        if (currentState.isSearchActive) {
            Logger.d("UPI_TRANSACTION_HISTORY UpiTransactionHistoryViewModel Back pressed - clearing search")
            clearSearch()
        } else {
            Logger.d("UPI_TRANSACTION_HISTORY UpiTransactionHistoryViewModel Back pressed - navigating back")
            sendEvent(UpiTransactionHistoryEvent.NavigateBack)
        }
    }

    private fun groupTransactionsByMonth(transactions: List<UpiTransaction>): List<MonthTransactionGroup> {
        return transactions
            .groupBy { transaction ->
                "${transaction.date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${transaction.date.year}"
            }
            .map { (monthYear, monthTransactions) ->
                val totalAmount = monthTransactions.sumOf { it.amount }
                MonthTransactionGroup(
                    monthYear = monthYear,
                    totalAmount = totalAmount,
                    transactions = monthTransactions.sortedByDescending { it.date },
                )
            }
            .sortedByDescending { group ->
                group.transactions.firstOrNull()?.date
            }
    }

    private fun generateDummyTransactions(): List<UpiTransaction> {
        val currentDateTime = DateHelper.currentDate
        val currentDate = LocalDate(currentDateTime.year, currentDateTime.month, currentDateTime.dayOfMonth)
        return listOf(
            UpiTransaction(
                id = "1",
                payeeName = "Rahul Sharma",
                amount = 2500.0,
                date = currentDate - DatePeriod(days = 1),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "2",
                payeeName = "Priya Patel",
                amount = 1800.0,
                date = currentDate - DatePeriod(days = 2),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "3",
                payeeName = "Amit Kumar",
                amount = 3200.0,
                date = currentDate - DatePeriod(days = 3),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "4",
                payeeName = "Neha Singh",
                amount = 950.0,
                date = currentDate - DatePeriod(days = 4),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "5",
                payeeName = "Vikram Mehta",
                amount = 4100.0,
                date = currentDate - DatePeriod(days = 5),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "6",
                payeeName = "Sneha Gupta",
                amount = 1200.0,
                date = currentDate - DatePeriod(days = 6),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "7",
                payeeName = "Rajesh Verma",
                amount = 2800.0,
                date = currentDate - DatePeriod(days = 7),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "8",
                payeeName = "Anita Joshi",
                amount = 1500.0,
                date = currentDate - DatePeriod(days = 8),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "9",
                payeeName = "Deepak Yadav",
                amount = 3600.0,
                date = currentDate - DatePeriod(days = 9),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "10",
                payeeName = "Pooja Agarwal",
                amount = 2200.0,
                date = currentDate - DatePeriod(days = 10),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "11",
                payeeName = "Ravi Tiwari",
                amount = 1900.0,
                date = currentDate - DatePeriod(days = 15),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "12",
                payeeName = "Sunita Reddy",
                amount = 3100.0,
                date = currentDate - DatePeriod(days = 16),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "13",
                payeeName = "Manoj Singh",
                amount = 1400.0,
                date = currentDate - DatePeriod(days = 17),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "14",
                payeeName = "Kavita Sharma",
                amount = 2700.0,
                date = currentDate - DatePeriod(days = 18),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "15",
                payeeName = "Suresh Kumar",
                amount = 3300.0,
                date = currentDate - DatePeriod(days = 19),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "16",
                payeeName = "Meera Patel",
                amount = 1600.0,
                date = currentDate - DatePeriod(days = 20),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "17",
                payeeName = "Arjun Malhotra",
                amount = 2900.0,
                date = currentDate - DatePeriod(days = 25),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "18",
                payeeName = "Divya Iyer",
                amount = 2100.0,
                date = currentDate - DatePeriod(days = 26),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "19",
                payeeName = "Nikhil Nair",
                amount = 3800.0,
                date = currentDate - DatePeriod(days = 27),
                profileImageUrl = null,
            ),
            UpiTransaction(
                id = "20",
                payeeName = "Shruti Desai",
                amount = 1700.0,
                date = currentDate - DatePeriod(days = 28),
                profileImageUrl = null,
            ),
        )
    }

    init {
        val groupedTransactions = groupTransactionsByMonth(dummyTransactions)
        mutableStateFlow.value = mutableStateFlow.value.copy(
            groupedTransactions = groupedTransactions,
            isLoading = false,
        )
    }
}

data class UpiTransactionHistoryState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val groupedTransactions: List<MonthTransactionGroup> = emptyList(),
    val isSearchActive: Boolean = false,
)

sealed class UpiTransactionHistoryAction {
    object NavigateBack : UpiTransactionHistoryAction()
    data class SearchQueryChanged(val query: String) : UpiTransactionHistoryAction()
    object SearchPerformed : UpiTransactionHistoryAction()
    object ClearSearch : UpiTransactionHistoryAction()
    object BackPressed : UpiTransactionHistoryAction()
}

sealed class UpiTransactionHistoryEvent {
    object NavigateBack : UpiTransactionHistoryEvent()
}
