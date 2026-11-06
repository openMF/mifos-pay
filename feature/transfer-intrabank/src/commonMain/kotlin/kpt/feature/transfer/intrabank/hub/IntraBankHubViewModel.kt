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

    companion object {
        private const val RECENT_PAYEES_LIMIT = 10
    }
}

enum class PayeeTab {
    Recents,
    Beneficiaries,
}

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

enum class RecentPayeesState {
    Loading,
    Success,
    Empty,
    Error,
}

enum class BeneficiariesState {
    Loading,
    Success,
    Empty,
    Error,
}

sealed interface IntraBankHubEvent {
    data object NavigateToSearchAccountSelection : IntraBankHubEvent
    data object NavigateBack : IntraBankHubEvent
    data object NavigateToBeneficiary : IntraBankHubEvent
    data object NavigateToHistory : IntraBankHubEvent
    data object NavigateToScanQr : IntraBankHubEvent
    data object NavigateToRequestMoney : IntraBankHubEvent
    data class NavigateToTransfer(
        val toOfficeId: Int,
        val toClientId: Long,
        val toAccountId: Int,
        val accountName: String,
        val accountNo: String,
    ) : IntraBankHubEvent
    data class NavigateToTransferBeneficiary(
        val beneficiary: Beneficiary,
    ) : IntraBankHubEvent
}

sealed interface IntraBankHubAction {
    data object NavigateBack : IntraBankHubAction
    data object OnSearchBarClicked : IntraBankHubAction
    data object OnAddPayeeClicked : IntraBankHubAction
    data object OnHistoryClicked : IntraBankHubAction
    data object OnScanQrClicked : IntraBankHubAction
    data object OnRequestMoneyClicked : IntraBankHubAction
    data object OnSeeAllTransactionsClicked : IntraBankHubAction
    data class OnTabSelected(val tab: PayeeTab) : IntraBankHubAction
    data class OnPayRecentPayee(val payee: RecentPayee) : IntraBankHubAction
    data class OnPayBeneficiary(val beneficiary: Beneficiary) : IntraBankHubAction

    data object RefreshBeneficiaries : IntraBankHubAction
}
