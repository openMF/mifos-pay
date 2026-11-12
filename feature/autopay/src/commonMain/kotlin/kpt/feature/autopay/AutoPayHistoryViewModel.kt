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
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.common.ScreenState
import kpt.core.data.repository.AutoPayHistoryRepository
import kpt.core.data.repository.AutoPayHistoryStatistics
import kpt.core.model.autopay.AutoPayHistory
import kpt.core.model.autopay.PaymentStatus
import kpt.core.model.network.entity.Page
import kpt.core.ui.fork.utils.BaseViewModel
/**
 * ViewModel for AutoPay history screen.
 *
 * This ViewModel provides read-only access to AutoPay history data.
 * Users can view, search, and filter history but cannot edit or delete entries.
 */
class AutoPayHistoryViewModel(
    private val autoPayHistoryRepository: AutoPayHistoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<AutoPayHistoryUiState, AutoPayHistoryEvent, AutoPayHistoryAction>(
    initialState = AutoPayHistoryUiState(),
) {

    private val autoPayId: Long = savedStateHandle.get<Long>("autoPayId") ?: 0L

    // MVI view-state surface. `state` (BaseViewModel) is the current value and
    // `stateFlow` is the hot StateFlow. The Screen consumes `uiState` unchanged,
    // aliased here to `stateFlow` so the public API and the Screen stay intact.
    /** The screen state, under the name the screen reads. */
    val uiState: StateFlow<AutoPayHistoryUiState> get() = stateFlow

    // Search/filter selection surfaces are separate from the MVI view-state and
    // preserved as-is to keep the ViewModel's existing public API + behavior.
    private val _searchQuery = MutableStateFlow("")

    /** Current search query text. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)

    /** Selected payment status filter, or null for all statuses. */
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<Pair<String?, String?>>(null to null)

    /** Selected date range filter as (from, to) strings; null entries are unbounded. */
    val selectedDateRange: StateFlow<Pair<String?, String?>> = _selectedDateRange.asStateFlow()

    init {
        loadAutoPayHistory()
        loadHistoryStatistics()
    }

    override fun handleAction(action: AutoPayHistoryAction) {
        when (action) {
            is AutoPayHistoryAction.LoadWithPagination -> loadAutoPayHistoryWithPaginationInternal(
                autoPayId = action.autoPayId,
                limit = action.limit,
                offset = action.offset,
            )

            is AutoPayHistoryAction.Search -> searchHistoryInternal(action.query)

            is AutoPayHistoryAction.FilterByStatus -> filterByStatusInternal(action.status)

            is AutoPayHistoryAction.FilterByDateRange -> filterByDateRangeInternal(
                fromDate = action.fromDate,
                toDate = action.toDate,
            )

            AutoPayHistoryAction.ClearFilters -> clearFiltersInternal()

            AutoPayHistoryAction.RefreshHistory -> refreshHistoryInternal()

            AutoPayHistoryAction.ClearError -> clearErrorInternal()
        }
    }

    /** Loads one page of AutoPay history. */
    fun loadAutoPayHistoryWithPagination(
        autoPayId: Long,
        limit: Int = 20,
        offset: Int = 0,
    ) {
        trySendAction(AutoPayHistoryAction.LoadWithPagination(autoPayId, limit, offset))
    }

    /** Filters the history by a search query. */
    fun searchHistory(query: String) {
        trySendAction(AutoPayHistoryAction.Search(query))
    }

    /** Filters the history by payment status; null clears the status filter. */
    fun filterByStatus(status: String?) {
        trySendAction(AutoPayHistoryAction.FilterByStatus(status))
    }

    /** Filters the history to a date range. */
    fun filterByDateRange(fromDate: String?, toDate: String?) {
        trySendAction(AutoPayHistoryAction.FilterByDateRange(fromDate, toDate))
    }

    /** Clears the search, status and date filters. */
    fun clearFilters() {
        trySendAction(AutoPayHistoryAction.ClearFilters)
    }

    /** Reloads the history from the repository. */
    fun refreshHistory() {
        trySendAction(AutoPayHistoryAction.RefreshHistory)
    }

    /** Clears the current error. */
    fun clearError() {
        trySendAction(AutoPayHistoryAction.ClearError)
    }

    private fun loadAutoPayHistory() {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Loading history for AutoPay ID: $autoPayId")

        mutableStateFlow.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                delay(2000)

                val dummyHistory = createDummyHistoryData()
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        historyList = dummyHistory,
                        error = null,
                    )
                }
                Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Successfully loaded ${dummyHistory.size} dummy history entries")
            } catch (exception: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred",
                    )
                }
                Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Exception in history flow: ${exception.message}")
            }
        }
    }

    private fun loadAutoPayHistoryWithPaginationInternal(
        autoPayId: Long,
        limit: Int,
        offset: Int,
    ) {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Loading paginated history for AutoPay ID: $autoPayId, limit: $limit, offset: $offset")

        mutableStateFlow.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            autoPayHistoryRepository.getAutoPayHistoryWithPagination(autoPayId, limit, offset)
                .onEach { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {
                            mutableStateFlow.update { it.copy(isLoading = true, error = null) }
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isLoading = false,
                                    historyPage = null,
                                    error = null,
                                )
                            }
                        }

                        is ScreenState.Content -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isLoading = false,
                                    historyPage = screenState.data,
                                    error = null,
                                )
                            }
                            Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Successfully loaded paginated history: ${screenState.data.pageItems.size} entries")
                        }

                        is ScreenState.Error -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isLoading = false,
                                    error = screenState.error.message ?: "Failed to load history",
                                )
                            }
                            Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Error loading paginated history: ${screenState.error.message}")
                        }

                        is ScreenState.NoNetwork -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No network. Please check your connection.",
                                )
                            }
                        }

                        is ScreenState.Unauthenticated -> {
                            mutableStateFlow.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Session expired. Please log in again.",
                                )
                            }
                        }
                    }
                }
                .catch { exception ->
                    mutableStateFlow.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Unknown error occurred",
                        )
                    }
                    Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Exception in paginated history flow: ${exception.message}")
                }
                .launchIn(this)
        }
    }

    private fun loadHistoryStatistics() {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Loading statistics for AutoPay ID: $autoPayId")

        viewModelScope.launch {
            try {
                delay(1500)

                val dummyStatistics = createDummyStatistics()
                mutableStateFlow.update { it.copy(statistics = dummyStatistics) }
                Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Successfully loaded dummy statistics: ${dummyStatistics.totalTransactions} total transactions")
            } catch (exception: Exception) {
                Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Exception in statistics flow: ${exception.message}")
            }
        }
    }

    private fun searchHistoryInternal(query: String) {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Searching history with query: $query")

        _searchQuery.value = query

        if (query.isBlank()) {
            mutableStateFlow.update { it.copy(filteredHistoryList = state.historyList) }
            return
        }

        viewModelScope.launch {
            autoPayHistoryRepository.searchHistory(query)
                .onEach { screenState ->
                    when (screenState) {
                        is ScreenState.Content -> {
                            mutableStateFlow.update { it.copy(filteredHistoryList = screenState.data) }
                            Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Search completed: ${screenState.data.size} results")
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update { it.copy(filteredHistoryList = emptyList()) }
                        }

                        is ScreenState.Error -> {
                            Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Error searching history: ${screenState.error.message}")
                        }

                        is ScreenState.Loading,
                        is ScreenState.NoNetwork,
                        is ScreenState.Unauthenticated,
                        -> {
                            // Search loading + non-fatal transient states handled separately
                        }
                    }
                }
                .catch { exception ->
                    Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Exception in search flow: ${exception.message}")
                }
                .launchIn(this)
        }
    }

    private fun filterByStatusInternal(status: String?) {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Filtering by status: $status")

        _selectedStatus.value = status

        if (status == null) {
            mutableStateFlow.update { it.copy(filteredHistoryList = state.historyList) }
            return
        }

        viewModelScope.launch {
            autoPayHistoryRepository.getHistoryByStatus(status)
                .onEach { screenState ->
                    when (screenState) {
                        is ScreenState.Content -> {
                            mutableStateFlow.update { it.copy(filteredHistoryList = screenState.data) }
                            Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Status filter applied: ${screenState.data.size} results")
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update { it.copy(filteredHistoryList = emptyList()) }
                        }

                        is ScreenState.Error -> {
                            Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Error filtering by status: ${screenState.error.message}")
                        }

                        is ScreenState.Loading,
                        is ScreenState.NoNetwork,
                        is ScreenState.Unauthenticated,
                        -> {
                            // Filter loading + non-fatal transient states handled separately
                        }
                    }
                }
                .catch { exception ->
                    Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Exception in status filter flow: ${exception.message}")
                }
                .launchIn(this)
        }
    }

    private fun filterByDateRangeInternal(fromDate: String?, toDate: String?) {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Filtering by date range: $fromDate to $toDate")

        _selectedDateRange.value = fromDate to toDate

        if (fromDate == null || toDate == null) {
            mutableStateFlow.update { it.copy(filteredHistoryList = state.historyList) }
            return
        }

        viewModelScope.launch {
            autoPayHistoryRepository.getHistoryByDateRange(fromDate, toDate)
                .onEach { screenState ->
                    when (screenState) {
                        is ScreenState.Content -> {
                            mutableStateFlow.update { it.copy(filteredHistoryList = screenState.data) }
                            Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Date range filter applied: ${screenState.data.size} results")
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update { it.copy(filteredHistoryList = emptyList()) }
                        }

                        is ScreenState.Error -> {
                            Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Error filtering by date range: ${screenState.error.message}")
                        }

                        is ScreenState.Loading,
                        is ScreenState.NoNetwork,
                        is ScreenState.Unauthenticated,
                        -> {
                            // Filter loading + non-fatal transient states handled separately
                        }
                    }
                }
                .catch { exception ->
                    Logger.e("AUTOPAY_HISTORY AutoPayHistoryViewModel Exception in date range filter flow: ${exception.message}")
                }
                .launchIn(this)
        }
    }

    private fun clearFiltersInternal() {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Clearing all filters")

        _searchQuery.value = ""
        _selectedStatus.value = null
        _selectedDateRange.value = null to null
        mutableStateFlow.update { it.copy(filteredHistoryList = state.historyList) }
    }

    private fun refreshHistoryInternal() {
        Logger.d("AUTOPAY_HISTORY AutoPayHistoryViewModel Refreshing history for AutoPay ID: $autoPayId")

        loadAutoPayHistory()
        loadHistoryStatistics()
    }

    private fun clearErrorInternal() {
        mutableStateFlow.update { it.copy(error = null) }
    }

    private fun createDummyHistoryData(): List<AutoPayHistory> {
        return listOf(
            AutoPayHistory(
                id = 1L,
                autoPayId = autoPayId,
                amount = 1200.00,
                currency = "USD",
                status = PaymentStatus.COMPLETED,
                transactionDate = "Jan 15, 2025",
                recipientName = "Landlord Corp",
                recipientAccountNumber = "****1234",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001234",
                failureReason = null,
                createdDate = "Jan 15, 2025 10:30 AM",
            ),
            AutoPayHistory(
                id = 2L,
                autoPayId = autoPayId,
                amount = 89.99,
                currency = "USD",
                status = PaymentStatus.COMPLETED,
                transactionDate = "Jan 10, 2025",
                recipientName = "Comcast",
                recipientAccountNumber = "****5678",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001235",
                failureReason = null,
                createdDate = "Jan 10, 2025 08:15 AM",
            ),
            AutoPayHistory(
                id = 3L,
                autoPayId = autoPayId,
                amount = 156.75,
                currency = "USD",
                status = PaymentStatus.FAILED,
                transactionDate = "Jan 5, 2025",
                recipientName = "Power Company",
                recipientAccountNumber = "****9012",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001236",
                failureReason = "Insufficient funds",
                createdDate = "Jan 5, 2025 14:20 PM",
            ),
            AutoPayHistory(
                id = 4L,
                autoPayId = autoPayId,
                amount = 85.50,
                currency = "USD",
                status = PaymentStatus.COMPLETED,
                transactionDate = "Dec 28, 2024",
                recipientName = "Verizon",
                recipientAccountNumber = "****3456",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001237",
                failureReason = null,
                createdDate = "Dec 28, 2024 09:45 AM",
            ),
            AutoPayHistory(
                id = 5L,
                autoPayId = autoPayId,
                amount = 45.00,
                currency = "USD",
                status = PaymentStatus.COMPLETED,
                transactionDate = "Dec 20, 2024",
                recipientName = "Fitness Center",
                recipientAccountNumber = "****7890",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001238",
                failureReason = null,
                createdDate = "Dec 20, 2024 07:30 AM",
            ),
            AutoPayHistory(
                id = 6L,
                autoPayId = autoPayId,
                amount = 15.99,
                currency = "USD",
                status = PaymentStatus.UPCOMING,
                transactionDate = "Dec 15, 2024",
                recipientName = "Netflix",
                recipientAccountNumber = "****2468",
                sourceAccountNumber = "****5678",
                referenceNumber = null,
                failureReason = null,
                createdDate = "Dec 15, 2024 16:00 PM",
            ),
            AutoPayHistory(
                id = 7L,
                autoPayId = autoPayId,
                amount = 9.99,
                currency = "USD",
                status = PaymentStatus.CANCELLED,
                transactionDate = "Dec 10, 2024",
                recipientName = "Spotify",
                recipientAccountNumber = "****1357",
                sourceAccountNumber = "****5678",
                referenceNumber = null,
                failureReason = "Schedule cancelled by user",
                createdDate = "Dec 10, 2024 11:15 AM",
            ),
            AutoPayHistory(
                id = 8L,
                autoPayId = autoPayId,
                amount = 250.00,
                currency = "USD",
                status = PaymentStatus.PROCESSING,
                transactionDate = "Jan 20, 2023",
                recipientName = "Insurance Company",
                recipientAccountNumber = "****9753",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001239",
                failureReason = null,
                createdDate = "Jan 20, 2023 12:00 PM",
            ),
            AutoPayHistory(
                id = 9L,
                autoPayId = autoPayId,
                amount = 75.25,
                currency = "USD",
                status = PaymentStatus.PENDING,
                transactionDate = "Jan 18, 2023",
                recipientName = "Water Department",
                recipientAccountNumber = "****8642",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001240",
                failureReason = null,
                createdDate = "Jan 18, 2023 15:30 PM",
            ),
            AutoPayHistory(
                id = 10L,
                autoPayId = autoPayId,
                amount = 199.99,
                currency = "USD",
                status = PaymentStatus.COMPLETED,
                transactionDate = "Jan 12, 2023",
                recipientName = "Credit Card Payment",
                recipientAccountNumber = "****7531",
                sourceAccountNumber = "****5678",
                referenceNumber = "REF001241",
                failureReason = null,
                createdDate = "Jan 12, 2023 13:45 PM",
            ),
        )
    }

    private fun createDummyStatistics(): AutoPayHistoryStatistics {
        return AutoPayHistoryStatistics(
            totalTransactions = 10,
            successfulTransactions = 6,
            failedTransactions = 1,
            pendingTransactions = 1,
            totalAmount = 2128.46,
            successfulAmount = 1476.48,
            failedAmount = 156.75,
            currency = "USD",
        )
    }
}

/**
 * UI state of the AutoPay history screen.
 *
 * @property isLoading Whether history is loading.
 * @property historyList All loaded history entries.
 * @property filteredHistoryList Entries matching the current filters.
 * @property historyPage Paged response the entries came from, or null.
 * @property statistics Aggregate statistics for the history, or null.
 * @property error Error message to show, or null.
 * @property isReadOnly Whether entries are read-only (always true for history).
 */
data class AutoPayHistoryUiState(
    val isLoading: Boolean = false,
    val historyList: List<AutoPayHistory> = emptyList(),
    val filteredHistoryList: List<AutoPayHistory> = emptyList(),
    val historyPage: Page<AutoPayHistory>? = null,
    val statistics: AutoPayHistoryStatistics? = null,
    val error: String? = null,
    val isReadOnly: Boolean = true,
) {
    /** Filtered entries when a filter is active, otherwise all entries. */
    val displayHistoryList: List<AutoPayHistory>
        get() = filteredHistoryList.ifEmpty { historyList }
}

/** One-off events of the history screen; none are currently emitted. */
sealed interface AutoPayHistoryEvent

/** User actions handled by the history view model. */
sealed interface AutoPayHistoryAction {
    /**
     * Load one page of history.
     *
     * @property autoPayId Id of the AutoPay schedule.
     * @property limit Page size.
     * @property offset Index of the first entry to load.
     */
    data class LoadWithPagination(
        val autoPayId: Long,
        val limit: Int,
        val offset: Int,
    ) : AutoPayHistoryAction

    /**
     * Search the history.
     *
     * @property query Search text.
     */
    data class Search(val query: String) : AutoPayHistoryAction

    /**
     * Filter by payment status.
     *
     * @property status Status to keep, or null for all.
     */
    data class FilterByStatus(val status: String?) : AutoPayHistoryAction

    /**
     * Filter by date range.
     *
     * @property fromDate Start of the range, or null.
     * @property toDate End of the range, or null.
     */
    data class FilterByDateRange(
        val fromDate: String?,
        val toDate: String?,
    ) : AutoPayHistoryAction

    /** Clear all filters. */
    data object ClearFilters : AutoPayHistoryAction

    /** Reload the history. */
    data object RefreshHistory : AutoPayHistoryAction

    /** Clear the current error. */
    data object ClearError : AutoPayHistoryAction
}
