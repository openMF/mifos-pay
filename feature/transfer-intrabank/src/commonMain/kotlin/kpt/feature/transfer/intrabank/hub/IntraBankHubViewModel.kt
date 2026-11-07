/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.intrabank.hub

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.common.ScreenState
import kpt.core.data.repository.RecentPayeeRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.data.repository.ThirdPartyTransferRepository
import kpt.core.data.util.toForkScreenStateFlow
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.account.RecentPayee
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.network.entity.templates.account.AccountOption
import kpt.core.ui.utils.BaseViewModel
/**
 * ViewModel for the intra-bank hub; loads recent payees, beneficiaries and the transfer template and handles hub
 * actions.
 */
class IntraBankHubViewModel(
    private val recentPayeeRepository: RecentPayeeRepository,
    private val repository: ThirdPartyTransferRepository,
    private val selfServiceRepository: SelfServiceRepository,
    private val userPreferencesRepository: ForkPreferencesRepository,
) : BaseViewModel<IntraBankHubState, IntraBankHubEvent, IntraBankHubAction>(
    initialState = IntraBankHubState(),
) {

    init {
        loadRecentPayees()
        loadBeneficiaries()
        loadTptTemplate()
    }

    override fun handleAction(action: IntraBankHubAction) {
        when (action) {
            IntraBankHubAction.NavigateBack -> {
                sendEvent(IntraBankHubEvent.NavigateBack)
            }

            IntraBankHubAction.OnSearchBarClicked -> {
                sendEvent(IntraBankHubEvent.NavigateToSearchAccountSelection)
            }

            IntraBankHubAction.OnAddPayeeClicked -> {
                sendEvent(IntraBankHubEvent.NavigateToBeneficiary)
            }

            is IntraBankHubAction.OnPayRecentPayee -> {
                sendEvent(
                    IntraBankHubEvent.NavigateToTransfer(
                        toOfficeId = action.payee.officeId.toInt(),
                        toClientId = action.payee.clientId,
                        toAccountId = action.payee.accountId.toInt(),
                        accountName = action.payee.clientName,
                        accountNo = action.payee.accountNo,
                    ),
                )
            }

            is IntraBankHubAction.OnPayBeneficiary -> {
                val matchedAccount = state.toAccountOptionsTemplate
                    ?.firstOrNull {
                        it.accountNo == action.beneficiary.accountNumber
                    }

                matchedAccount?.let { account ->
                    val officeId = account.officeId ?: return@let
                    val clientId = account.clientId ?: return@let
                    val accountId = account.accountId ?: return@let

                    sendEvent(
                        IntraBankHubEvent.NavigateToTransfer(
                            toOfficeId = officeId,
                            toClientId = clientId,
                            toAccountId = accountId,
                            accountName = action.beneficiary.name,
                            accountNo = action.beneficiary.accountNumber,
                        ),
                    )
                }
            }

            IntraBankHubAction.OnHistoryClicked -> {
                sendEvent(IntraBankHubEvent.NavigateToHistory)
            }

            IntraBankHubAction.OnScanQrClicked -> {
                sendEvent(IntraBankHubEvent.NavigateToScanQr)
            }

            IntraBankHubAction.OnRequestMoneyClicked -> {
                sendEvent(IntraBankHubEvent.NavigateToRequestMoney)
            }

            IntraBankHubAction.OnSeeAllTransactionsClicked -> {
                sendEvent(IntraBankHubEvent.NavigateToHistory)
            }

            IntraBankHubAction.RefreshBeneficiaries -> {
                loadBeneficiaries()
            }

            is IntraBankHubAction.OnTabSelected -> {
                mutableStateFlow.update {
                    it.copy(selectedTab = action.tab)
                }
            }
        }
    }

    private fun loadTptTemplate() {
        viewModelScope.launch {
            try {
                val toAccountOptions = repository.getTransferTemplate().toAccountOptions
                mutableStateFlow.update {
                    it.copy(toAccountOptionsTemplate = toAccountOptions)
                }
            } catch (e: Exception) {
                Logger.e(e) { "Failed to load account template" }
            }
        }
    }

    private fun loadRecentPayees() {
        val accountId = userPreferencesRepository.defaultAccountId.value
        if (accountId == null) {
            Logger.d { "RecentPayee: No default account ID found" }
            mutableStateFlow.update {
                it.copy(recentPayeesState = RecentPayeesState.Empty)
            }
            return
        }

        Logger.d { "RecentPayee: Loading recent payees for account $accountId" }

        // Phase-5 Batch-4: reads come through the store-native
        // `getRecentPayeesScreen(accountId, limit, scope): Flow<ScreenState<...>>`
        // — sourced from the Room-backed `wallet_recent_payees` cache
        // (offline-first); a background derive kick refreshes the cache on every
        // subscription.
        recentPayeeRepository
            .getRecentPayeesScreen(accountId, RECENT_PAYEES_LIMIT, viewModelScope)
            .onEach { screenState ->
                when (screenState) {
                    is ScreenState.Loading -> {
                        mutableStateFlow.update {
                            it.copy(recentPayeesState = RecentPayeesState.Loading)
                        }
                    }

                    is ScreenState.Empty -> {
                        mutableStateFlow.update {
                            it.copy(
                                recentPayees = emptyList(),
                                recentPayeesState = RecentPayeesState.Empty,
                            )
                        }
                    }

                    is ScreenState.Content -> {
                        Logger.d { "RecentPayee: Loaded ${screenState.data.size} recent payees" }
                        mutableStateFlow.update {
                            it.copy(
                                recentPayees = screenState.data,
                                recentPayeesState = if (screenState.data.isEmpty()) {
                                    RecentPayeesState.Empty
                                } else {
                                    RecentPayeesState.Success
                                },
                            )
                        }
                    }

                    is ScreenState.Error -> {
                        Logger.e(screenState.error) { "RecentPayee: Failed to load" }
                        mutableStateFlow.update {
                            it.copy(recentPayeesState = RecentPayeesState.Error)
                        }
                    }

                    is ScreenState.NoNetwork,
                    is ScreenState.Unauthenticated,
                    -> {
                        mutableStateFlow.update {
                            it.copy(recentPayeesState = RecentPayeesState.Error)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadBeneficiaries() {
        val clientId = userPreferencesRepository.client.value?.id ?: return
        viewModelScope.launch {
            // Offline-first: read beneficiaries through the Store5 beneficiary store
            // (Room SoT + CACHE_FIRST_SWR) so the cached list renders offline.
            selfServiceRepository.getBeneficiaryListStream(clientId, viewModelScope)
                .state.toForkScreenStateFlow()
                .onEach { screenState ->
                    when (screenState) {
                        is ScreenState.Loading -> {
                            mutableStateFlow.update {
                                it.copy(beneficiariesState = BeneficiariesState.Loading)
                            }
                        }

                        is ScreenState.Empty -> {
                            mutableStateFlow.update {
                                it.copy(
                                    beneficiaries = emptyList(),
                                    beneficiariesState = BeneficiariesState.Empty,
                                )
                            }
                        }

                        is ScreenState.Content -> {
                            Logger.d { "Beneficiaries: Loaded ${screenState.data.size} beneficiaries" }
                            mutableStateFlow.update {
                                it.copy(
                                    beneficiaries = screenState.data,
                                    beneficiariesState = if (screenState.data.isEmpty()) {
                                        BeneficiariesState.Empty
                                    } else {
                                        BeneficiariesState.Success
                                    },
                                )
                            }
                        }

                        is ScreenState.Error -> {
                            Logger.e(screenState.error) { "Beneficiaries: Failed to load" }
                            mutableStateFlow.update {
                                it.copy(beneficiariesState = BeneficiariesState.Error)
                            }
                        }

                        is ScreenState.NoNetwork,
                        is ScreenState.Unauthenticated,
                        -> {
                            mutableStateFlow.update {
                                it.copy(beneficiariesState = BeneficiariesState.Error)
                            }
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    /** Constants for [IntraBankHubViewModel]. */
    companion object {
        private const val RECENT_PAYEES_LIMIT = 10
    }
}

/** Tabs of the payee list on the hub. */
enum class PayeeTab {
    /** Recently paid payees. */
    Recents,

    /** Saved beneficiaries. */
    Beneficiaries,
}

/**
 * UI state of the intra-bank hub.
 * @property selectedTab Selected payee tab.
 * @property toAccountOptionsTemplate Destination account options from the transfer template, or null if not loaded.
 * @property recentPayees Recently paid payees.
 * @property recentPayeesState Loading state of the recent payees.
 * @property beneficiaries Saved beneficiaries.
 * @property beneficiariesState Loading state of the beneficiaries.
 */
@Serializable
data class IntraBankHubState(
    val selectedTab: PayeeTab = PayeeTab.Recents,
    val toAccountOptionsTemplate: List<AccountOption>? = null,
    @Transient
    val recentPayees: List<RecentPayee> = emptyList(),
    @Transient
    val recentPayeesState: RecentPayeesState = RecentPayeesState.Loading,
    @Transient
    val beneficiaries: List<Beneficiary> = emptyList(),
    @Transient
    val beneficiariesState: BeneficiariesState = BeneficiariesState.Loading,
)

/** Loading state of the recent payees list. */
enum class RecentPayeesState {
    /** Recent payees are loading. */
    Loading,

    /** Recent payees loaded. */
    Success,

    /** There are no recent payees. */
    Empty,

    /** Recent payees failed to load. */
    Error,
}

/** Loading state of the beneficiaries list. */
enum class BeneficiariesState {
    /** Beneficiaries are loading. */
    Loading,

    /** Beneficiaries loaded. */
    Success,

    /** There are no beneficiaries. */
    Empty,

    /** Beneficiaries failed to load. */
    Error,
}

/** One-shot navigation events emitted by [IntraBankHubViewModel]. */
sealed interface IntraBankHubEvent {
    /** Navigate to account search and selection. */
    data object NavigateToSearchAccountSelection : IntraBankHubEvent

    /** Navigate back. */
    data object NavigateBack : IntraBankHubEvent

    /** Navigate to the beneficiary screen. */
    data object NavigateToBeneficiary : IntraBankHubEvent

    /** Navigate to the transaction history. */
    data object NavigateToHistory : IntraBankHubEvent

    /** Navigate to the QR scanner. */
    data object NavigateToScanQr : IntraBankHubEvent

    /** Navigate to request money. */
    data object NavigateToRequestMoney : IntraBankHubEvent

    /**
     * Navigate to the transfer confirmation for a payee.
     * @property toOfficeId Office id of the destination account.
     * @property toClientId Client id of the destination account.
     * @property toAccountId Id of the destination account.
     * @property accountName Destination account holder name.
     * @property accountNo Destination account number.
     */
    data class NavigateToTransfer(
        val toOfficeId: Int,
        val toClientId: Long,
        val toAccountId: Int,
        val accountName: String,
        val accountNo: String,
    ) : IntraBankHubEvent

    /**
     * Navigate to the transfer to a saved beneficiary.
     * @property beneficiary Beneficiary to pay.
     */
    data class NavigateToTransferBeneficiary(
        val beneficiary: Beneficiary,
    ) : IntraBankHubEvent
}

/** Actions handled by [IntraBankHubViewModel]. */
sealed interface IntraBankHubAction {
    /** Navigate back. */
    data object NavigateBack : IntraBankHubAction

    /** The search bar was clicked. */
    data object OnSearchBarClicked : IntraBankHubAction

    /** The add-payee button was clicked. */
    data object OnAddPayeeClicked : IntraBankHubAction

    /** The history button was clicked. */
    data object OnHistoryClicked : IntraBankHubAction

    /** The scan-QR button was clicked. */
    data object OnScanQrClicked : IntraBankHubAction

    /** The request-money button was clicked. */
    data object OnRequestMoneyClicked : IntraBankHubAction

    /** The see-all-transactions link was clicked. */
    data object OnSeeAllTransactionsClicked : IntraBankHubAction

    /**
     * A payee tab was selected.
     * @property tab Selected tab.
     */
    data class OnTabSelected(val tab: PayeeTab) : IntraBankHubAction

    /**
     * A recent payee was chosen to pay.
     * @property payee Chosen recent payee.
     */
    data class OnPayRecentPayee(val payee: RecentPayee) : IntraBankHubAction

    /**
     * A beneficiary was chosen to pay.
     * @property beneficiary Chosen beneficiary.
     */
    data class OnPayBeneficiary(val beneficiary: Beneficiary) : IntraBankHubAction

    /** Reload the beneficiaries. */
    data object RefreshBeneficiaries : IntraBankHubAction
}
