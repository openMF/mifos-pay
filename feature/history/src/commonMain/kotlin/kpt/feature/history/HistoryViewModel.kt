/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history

import androidx.lifecycle.viewModelScope
import com.mobilebytelabs.kmptoolkit.pdfgenerator.ExperimentalPdfGeneratorApi
import com.mobilebytelabs.kmptoolkit.pdfgenerator.PageConfig
import com.mobilebytelabs.kmptoolkit.pdfgenerator.PdfError
import com.mobilebytelabs.kmptoolkit.pdfgenerator.PdfGenerator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.common.ScreenState
import kpt.core.data.fork.util.toForkScreenStateFlow
import kpt.core.data.repository.AccountRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.account.Account
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.history.components.HistoryHtmlTemplate
import kpt.feature.history.generated.resources.Res
import kpt.feature.history.generated.resources.feature_history_error
import kpt.feature.history.generated.resources.feature_history_no_account
import org.jetbrains.compose.resources.StringResource
/** ViewModel that loads, filters and exports (as PDF) the client's transaction history. */
class HistoryViewModel
@OptIn(ExperimentalPdfGeneratorApi::class)
constructor(
    private val preferencesRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val pdfGenerator: PdfGenerator,
    private val accountRepository: AccountRepository,
) : BaseViewModel<HistoryState, HistoryEvent, HistoryAction>(
    initialState = run {
        /** Id of the signed-in client, read from preferences. */
        val clientId = requireNotNull(preferencesRepository.clientId.value)
        HistoryState(
            clientId = clientId,
            viewState = HistoryState.ViewState.Loading,
        )
    },
) {
    init {
        loadActiveAccounts()
    }

    override fun handleAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.SetTransactionType -> handleSetTransactionType(action.filter)

            is HistoryAction.ViewTransaction -> handleViewTransaction(action.transferId)

            is HistoryAction.OnFilterClick -> handleFilterClick()

            is HistoryAction.OnApplyFilterClick -> handleApplyFilterClick()

            is HistoryAction.SetSelectedAccount -> handleSetSelectedAccount(action.account)

            is HistoryAction.ExportPdf -> {
                viewModelScope.launch {
                    exportPdf()
                }
            }

            HistoryAction.ClearFilters -> handleClearFilters()
        }
    }

    private fun loadTransactions(accountId: Long) {
        // Phase-4 Batch-A LEDGER read (GOAL D13) — switched from the transitional
        // `getTransactions(accountId, null)` (`asScreenStateFlow` shim over the raw
        // Ktorfit flow) to the store-native `getTransactionsScreen(...)` that consumes
        // the `history` Store5 read (`createStore` + Room SoT + CACHE_FIRST_SWR).
        // Same `Flow<ScreenState<List<Transaction>>>` shape; consumer branches
        // are unchanged. Requires `viewModelScope` for the stream's internal
        // reconnect + periodic + SWR side-fetch coroutines.
        //
        // Fold the 6-branch ScreenState into the existing 4-branch HistoryState
        // ViewState (Loading/Empty/Error/Content). Content(list) drives the
        // filter cascade; Empty short-circuits directly to the Empty view.
        // NoNetwork / Unauthenticated fold into the existing Error surface
        // until Phase-4 wires per-branch messaging.
        repository.getTransactionsStream(accountId, limit = null, scope = viewModelScope)
            .state.toForkScreenStateFlow().onEach { result ->
                when (result) {
                    is ScreenState.Loading -> {
                        mutableStateFlow.update {
                            it.copy(viewState = HistoryState.ViewState.Loading)
                        }
                    }

                    is ScreenState.Empty -> {
                        mutableStateFlow.update {
                            it.copy(
                                transactions = emptyList(),
                                viewState = HistoryState.ViewState.Empty,
                            )
                        }
                    }

                    is ScreenState.Content -> {
                        val transactions = result.data
                        mutableStateFlow.update {
                            it.copy(
                                transactions = transactions,
                                viewState = HistoryState.ViewState.Content(transactions),
                            )
                        }
                        applyFilter(transactions)
                    }

                    is ScreenState.Error,
                    is ScreenState.NoNetwork,
                    is ScreenState.Unauthenticated,
                    -> {
                        mutableStateFlow.update {
                            it.copy(viewState = HistoryState.ViewState.Error(Res.string.feature_history_error))
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun loadActiveAccounts() {
        // Fold the 6-branch ScreenState. Content(non-empty) cascades to
        // loadTransactions; Empty specifically shows the "no account" surface
        // (existing feature_history_no_account string). NoNetwork /
        // Unauthenticated fold into the generic error surface for now — the
        // "no account" error copy is reserved for the semantic empty case.
        // Offline-first: read self-accounts through the Store5 SelfAccounts store
        // (Room SoT + CACHE_FIRST_SWR) so cached accounts render offline. The store
        // returns ALL accounts, so filter `status.active` to preserve the previous
        // `getActiveAccounts` semantics.
        accountRepository.getSelfAccountsStream(state.clientId, viewModelScope)
            .state.toForkScreenStateFlow().onEach { result ->
                when (result) {
                    is ScreenState.Loading -> {
                        mutableStateFlow.update {
                            it.copy(viewState = HistoryState.ViewState.Loading)
                        }
                    }

                    is ScreenState.Empty -> {
                        mutableStateFlow.update {
                            it.copy(viewState = HistoryState.ViewState.Error(Res.string.feature_history_no_account))
                        }
                    }

                    is ScreenState.Content -> {
                        val accounts = result.data.filter { it.status.active }
                        if (accounts.isEmpty()) {
                            mutableStateFlow.update {
                                it.copy(
                                    viewState = HistoryState.ViewState.Error(
                                        Res.string.feature_history_no_account,
                                    ),
                                )
                            }
                        } else {
                            loadTransactions(accounts.first().id)
                            mutableStateFlow.update { state ->
                                state.copy(
                                    accounts = accounts,
                                    selectedAccount = accounts.firstOrNull(),
                                )
                            }
                        }
                    }

                    is ScreenState.Error,
                    is ScreenState.NoNetwork,
                    is ScreenState.Unauthenticated,
                    -> {
                        mutableStateFlow.update {
                            it.copy(viewState = HistoryState.ViewState.Error(Res.string.feature_history_error))
                        }
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun handleClearFilters() {
        mutableStateFlow.update {
            it.copy(
                selectedTransactionType = TransactionType.OTHER,
                showFilter = false,
            )
        }
        applyFilter(state.transactions)
    }

    private fun handleApplyFilterClick() {
        mutableStateFlow.update {
            it.copy(
                showFilter = false,
            )
        }
        val accountIdOfLoadedTransactions: Long? =
            if (state.transactions.isNotEmpty()) state.transactions.first().accountId else null

        if (state.selectedAccount?.id == accountIdOfLoadedTransactions) {
            applyFilter(state.transactions)
        } else {
            state.selectedAccount?.let { account ->
                loadTransactions(account.id)
            }
        }
    }

    private fun handleSetTransactionType(filter: TransactionType) {
        mutableStateFlow.update {
            it.copy(selectedTransactionType = filter)
        }
    }

    private fun handleSetSelectedAccount(account: Account) {
        mutableStateFlow.update {
            it.copy(selectedAccount = account)
        }
    }

    private fun handleViewTransaction(transferId: Long) {
        sendEvent(HistoryEvent.OnTransactionDetail(transferId))
    }

    private fun handleFilterClick() {
        mutableStateFlow.update {
            it.copy(showFilter = !state.showFilter)
        }
    }

    private fun applyFilter(transactions: List<Transaction>) {
        val filter = state.selectedTransactionType
        val filteredTransactions = transactions.filter {
            if (filter == TransactionType.OTHER) {
                true
            } else {
                it.transactionType == filter
            }
        }

        mutableStateFlow.update {
            it.copy(
                viewState = HistoryState.ViewState.Content(filteredTransactions),
            )
        }
    }

    @OptIn(ExperimentalPdfGeneratorApi::class)
    private suspend fun exportPdf() {
        if (state.isExportingPdf) return
        val content = state.viewState as? HistoryState.ViewState.Content ?: return

        mutableStateFlow.update { it.copy(isExportingPdf = true) }
        try {
            val html = HistoryHtmlTemplate.generate(
                accountNumber = state.selectedAccount?.number.orEmpty(),
                transactionType = state.selectedTransactionType.name,
                transactions = content.list,
            )

            try {
                pdfGenerator.generateAndSharePdf(
                    htmlContent = html,
                    fileName = "transaction_history.pdf",
                    pageConfig = PageConfig(),
                )
            } catch (e: PdfError) {
                sendEvent(HistoryEvent.PdfExportError(message = "Couldn't export PDF: ${e.message}"))
            }
        } finally {
            mutableStateFlow.update { it.copy(isExportingPdf = false) }
        }
    }
}

/**
 * UI state of the history screen.
 * @property clientId Id of the signed-in client.
 * @property viewState Loading, empty, error or content state.
 * @property selectedTransactionType Applied transaction type filter.
 * @property transactions Transactions currently shown.
 * @property accounts Accounts available for filtering.
 * @property selectedAccount Account filter, or null for all accounts.
 * @property showFilter Whether the filter sheet is visible.
 * @property isExportingPdf Whether a PDF export is in progress.
 */
data class HistoryState(
    val clientId: Long,
    val viewState: ViewState,
    val selectedTransactionType: TransactionType = TransactionType.OTHER,
    val transactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val showFilter: Boolean = false,
    val isExportingPdf: Boolean = false,
) {
    /** Display state of the history list. */
    sealed interface ViewState {
        /** Transactions are loading. */
        data object Loading : ViewState

        /** There are no transactions to show. */
        data object Empty : ViewState

        /**
         * Loading failed.
         * @property message Localized error message.
         */
        data class Error(val message: StringResource) : ViewState

        /**
         * Transactions loaded.
         * @property list Transactions to display.
         */
        data class Content(val list: List<Transaction>) : ViewState
    }
}

/** One-off events emitted by [HistoryViewModel]. */
sealed interface HistoryEvent {
    /**
     * Requests navigation to a transaction's detail.
     * @property transferId Id of the transfer to open.
     */
    data class OnTransactionDetail(val transferId: Long) : HistoryEvent

    /**
     * Reports that exporting the PDF failed.
     * @property message Error message to display.
     */
    data class PdfExportError(val message: String) : HistoryEvent
}

/** User actions handled by [HistoryViewModel]. */
sealed interface HistoryAction {
    /**
     * Applies a transaction type filter.
     * @property filter Transaction type to filter by.
     */
    data class SetTransactionType(val filter: TransactionType) : HistoryAction

    /**
     * Opens a transaction's detail.
     * @property transferId Id of the transfer to view.
     */
    data class ViewTransaction(val transferId: Long) : HistoryAction

    /** User tapped the filter button. */
    data object OnFilterClick : HistoryAction

    /**
     * Selects an account to filter by.
     * @property account Account to filter by.
     */
    data class SetSelectedAccount(val account: Account) : HistoryAction

    /** User confirmed the selected filters. */
    data object OnApplyFilterClick : HistoryAction

    /** User cleared all filters. */
    data object ClearFilters : HistoryAction

    /** User requested a PDF export of the history. */
    data object ExportPdf : HistoryAction
}
