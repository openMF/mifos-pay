/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pocket.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.data.repository.PocketRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.LinkableAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.pocket.generated.resources.Res
import kpt.feature.pocket.generated.resources.feature_pocket_error_delink_account
import kpt.feature.pocket.generated.resources.feature_pocket_error_link_accounts
/** *
 * **Architecture & Decisions:**
 * - **Empty State Evaluation**: `ScreenState.Empty` is only emitted if `fetchedAtInstant != null`
 *   and `!isRefreshing`. This prevents `204 No Content` API responses (0 items) from getting
 *   permanently trapped in a shimmer loading state waiting for data that will never arrive.
 * - **Submit Handlers**: `submitLink` and `submitDelink` are purely UI-layer Coroutine State Managers
 *   for one-shot button clicks (Submitting, Submitted, Failed). KPT's `submitHandler` provides
 *   structured states for click events. They are not offline outboxes.
 *
 * **Models & Calculations Rationale:**
 * - **`ManagePocketAccount` UI Model**: This model is created instead of using `DetailedPocketAccount`
 *   directly in the UI to centralize currency formatting (`balanceStr`) and fallback names out of the
 *   Composable. This prevents the UI from re-running heavy string operations on every frame.
 * - **`searchResults` StateFlow**: Searching is calculated in the ViewModel via `combine`
 *   so the UI can instantly filter the locally cached `availableUiState` list by search query and
 *   tab selection without triggering a network request, keeping the Compose UI declarative.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class ManagePocketViewModel(
    private val pocketRepository: PocketRepository,
    private val userPreferencesRepository: ForkPreferencesRepository,
) : BaseViewModel<ManagePocketState, ManagePocketEvent, ManagePocketAction>(
    initialState = ManagePocketState(
        clientId = requireNotNull(userPreferencesRepository.clientId.value),
    ),
) {

    private val linkedStream = pocketRepository.getLinkedPocketAccountsStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    val linkedUiState: StateFlow<ScreenState<List<DetailedPocketAccount>>> = linkedStream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    val mappedLinkedAccounts: StateFlow<ScreenState<List<ManagePocketAccount>>> = linkedUiState.map { state ->
        when (state) {
            is ScreenState.Content -> {
                val accounts = state.data.map {
                    ManagePocketAccount(
                        accountId = it.pocket.accountId,
                        mappingId = it.pocket.id,
                        name = it.productName,
                        accountNumber = it.pocket.accountNumber,
                        accountType = it.pocket.accountType,
                    )
                }
                ScreenState.Content(accounts, state.fetchedAt, state.freshnessSignal)
            }
            is ScreenState.Error -> state
            ScreenState.Loading -> ScreenState.Loading
            ScreenState.Empty -> ScreenState.Empty
            is ScreenState.NoNetwork -> state
            ScreenState.Unauthenticated -> ScreenState.Unauthenticated
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    val linkedFreshness: StateFlow<FreshnessSignal> = linkedStream.freshness.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FreshnessSignal.initial(),
    )

    private val availableStream = pocketRepository.getAvailableAccountsToLinkStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    val availableUiState: StateFlow<ScreenState<List<LinkableAccount>>> = availableStream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    val searchResults: StateFlow<List<LinkableAccount>> = combine(
        availableUiState,
        mutableStateFlow,
    ) { uiState, vmState ->
        val allAccounts = (uiState as? ScreenState.Content)?.data ?: emptyList()
        val query = vmState.searchQuery
        val tab = vmState.selectedTab

        allAccounts.filter { account ->
            account.accountType == tab && (
                (account.productName ?: "").contains(query, ignoreCase = true) ||
                    (account.accountNumber ?: "").contains(query, ignoreCase = true)
                )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val submitLink = viewModelScope.submitHandler<Unit>()
    private val submitDelink = viewModelScope.submitHandler<Unit>()

    init {
        observeLinkSubmit()
        observeDelinkSubmit()
    }

    /**
     * Observes the `submitLink` StateFlow (managed by `submitHandler`).
     * This provides structured states for the link button click (Submitting, Submitted, Failed),
     * allowing the UI to react instantly (e.g., show loaders, close dialogs) without managing
     * manual booleans. It is a purely UI-layer Coroutine State Manager.
     */
    private fun observeLinkSubmit() {
        submitLink.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        updateState { it.copy(dialogState = ManagePocketDialogState.Loading) }
                    }

                    is SubmitState.Submitted -> {
                        updateState {
                            it.copy(
                                dialogState = null,
                                selectedAccountIdentifiers = emptySet(),
                                searchQuery = "",
                            )
                        }
                        availableStream.refresh()
                        linkedStream.refresh()
                        submitLink.reset()
                    }

                    is SubmitState.Failed -> {
                        updateState {
                            it.copy(
                                dialogState = ManagePocketDialogState.Error(
                                    Res.string.feature_pocket_error_link_accounts,
                                ),
                            )
                        }
                        submitLink.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observes the `submitDelink` StateFlow (managed by `submitHandler`).
     * Similar to `observeLinkSubmit`, this maps one-shot asynchronous button clicks into
     * explicit UI states, ensuring loaders are shown and dialogs are cleared deterministically.
     */
    private fun observeDelinkSubmit() {
        submitDelink.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        updateState { it.copy(dialogState = ManagePocketDialogState.Loading) }
                    }

                    is SubmitState.Submitted -> {
                        updateState { it.copy(dialogState = null) }
                        availableStream.refresh()
                        linkedStream.refresh()
                        submitDelink.reset()
                    }

                    is SubmitState.Failed -> {
                        updateState {
                            it.copy(
                                dialogState = ManagePocketDialogState.Error(
                                    Res.string.feature_pocket_error_delink_account,
                                ),
                            )
                        }
                        submitDelink.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateState(update: (ManagePocketState) -> ManagePocketState) {
        mutableStateFlow.update(update)
    }

    override fun handleAction(action: ManagePocketAction) {
        when (action) {
            ManagePocketAction.NavigateBack -> sendEvent(ManagePocketEvent.NavigateBack)
            ManagePocketAction.Retry -> linkedStream.refresh()
            ManagePocketAction.RetryAvailable -> availableStream.refresh()
            ManagePocketAction.OpenLinkAccounts -> openLinkAccounts()
            ManagePocketAction.DismissDialog -> dismissDialog()
            is ManagePocketAction.OpenDelinkConfirmation -> openDelinkConfirmation(
                accountId = action.accountId,
                accountName = action.accountName,
                accountNumber = action.accountNumber,
            )
            is ManagePocketAction.TabSelected -> updateState { it.copy(selectedTab = action.accountType) }
            is ManagePocketAction.SearchQueryChanged -> updateState { it.copy(searchQuery = action.query) }
            is ManagePocketAction.AccountSelectionChanged -> updateSelectedAccount(
                accountId = action.accountId,
                accountType = action.accountType,
                selected = action.selected,
            )
            is ManagePocketAction.LinkSelectedAccounts -> linkSelectedAccounts()
            is ManagePocketAction.DelinkAccount -> delinkAccount(action.mappingId)
        }
    }

    private fun openLinkAccounts() {
        updateState {
            it.copy(
                dialogState = ManagePocketDialogState.LinkAccounts,
            )
        }
    }

    private fun dismissDialog() {
        updateState {
            it.copy(
                dialogState = null,
                selectedAccountIdentifiers = emptySet(),
                searchQuery = "",
            )
        }
    }

    private fun openDelinkConfirmation(accountId: Long, accountName: String, accountNumber: String) {
        updateState {
            it.copy(dialogState = ManagePocketDialogState.DelinkConfirmation(accountId, accountName, accountNumber))
        }
    }

    /**
     * Toggles the selection state of a given account in the 'Available to Link' sheet.
     * The selection is maintained uniquely via an `${accountId}_${accountType}` identifier set
     * because multiple accounts might share the same underlying ID across different types.
     */
    private fun updateSelectedAccount(
        accountId: Long,
        accountType: AccountType,
        selected: Boolean,
    ) {
        updateState { currentState ->
            val identifier = "${accountId}_${accountType.name}"
            val newSelection = if (selected) {
                currentState.selectedAccountIdentifiers + identifier
            } else {
                currentState.selectedAccountIdentifiers - identifier
            }
            currentState.copy(selectedAccountIdentifiers = newSelection)
        }
    }

    /**
     * Executes the network request to link the currently selected accounts.
     * Before pushing to the repository, it builds fully hydrated `DetailedPocketAccount`
     * instances by matching the UI selection against the local `availableUiState`.
     * This hydration is critical: the repository needs full details (productName, balance, etc)
     * to perform a robust offline-first optimistic insert.
     */
    private fun linkSelectedAccounts() {
        if (state.selectedAccountIdentifiers.isEmpty()) return

        val allAccounts = (availableUiState.value as? ScreenState.Content)?.data ?: emptyList()
        val explicitlyAddedAccounts = allAccounts
            .filter { "${it.accountId}_${it.accountType.name}" in state.selectedAccountIdentifiers }
            .map {
                DetailedPocketAccount(
                    pocket = PocketAccount(
                        pocketId = 0,
                        id = 0,
                        accountId = it.accountId,
                        accountType = it.accountType,
                        accountNumber = it.accountNumber ?: "",
                    ),
                    productName = it.productName,
                    balance = it.balance,
                    currencyCode = it.currencyCode,
                    decimalPlaces = it.decimalPlaces,
                    status = it.status,
                    currencyDisplaySymbol = it.currencyDisplaySymbol,
                )
            }

        submitLink.submit {
            pocketRepository.linkAccounts(
                explicitlyAddedAccounts = explicitlyAddedAccounts,
                clientId = state.clientId,
            )
        }
    }

    private fun delinkAccount(mappingId: Long) {
        submitDelink.submit {
            pocketRepository.delinkAccounts(
                pocketAccountMappingIds = listOf(mappingId),
                clientId = state.clientId,
            )
        }
    }
}

internal data class ManagePocketAccount(
    val accountId: Long,
    val mappingId: Long,
    val name: String?,
    val accountNumber: String,
    val accountType: AccountType,
)

internal data class ManagePocketState(
    val clientId: Long = 0,
    val selectedAccountIdentifiers: Set<String> = emptySet(),
    val selectedTab: AccountType = AccountType.SAVINGS,
    val searchQuery: String = "",
    val dialogState: ManagePocketDialogState? = null,
)

internal sealed interface ManagePocketDialogState {
    data object LinkAccounts : ManagePocketDialogState
    data object Loading : ManagePocketDialogState
    data class DelinkConfirmation(
        val accountId: Long,
        val accountName: String,
        val accountNumber: String,
    ) : ManagePocketDialogState
    data class Error(val message: org.jetbrains.compose.resources.StringResource) : ManagePocketDialogState
}

internal sealed interface ManagePocketEvent {
    data object NavigateBack : ManagePocketEvent
}

internal sealed interface ManagePocketAction {
    data object NavigateBack : ManagePocketAction
    data object Retry : ManagePocketAction
    data object RetryAvailable : ManagePocketAction
    data object OpenLinkAccounts : ManagePocketAction
    data object DismissDialog : ManagePocketAction
    data object LinkSelectedAccounts : ManagePocketAction
    data class OpenDelinkConfirmation(
        val accountId: Long,
        val accountName: String,
        val accountNumber: String,
    ) : ManagePocketAction
    data class DelinkAccount(val mappingId: Long) : ManagePocketAction
    data class TabSelected(val accountType: AccountType) : ManagePocketAction
    data class SearchQueryChanged(val query: String) : ManagePocketAction
    data class AccountSelectionChanged(
        val accountId: Long,
        val accountType: AccountType,
        val selected: Boolean,
    ) : ManagePocketAction
}
