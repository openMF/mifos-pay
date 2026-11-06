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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.common.CurrencyFormatter
import kpt.core.data.repository.PocketRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.ui.utils.BaseViewModel
/**
 * Manages the UI state for the Pocket Dashboard.
 *
 * **Architecture & Decisions:**
 * - **PocketBuckets Model**: Accounts are grouped into `savingsAccounts`, `loanAccounts`, and
 *   `shareAccounts` in the ViewModel because the Dashboard screen renders distinct horizontal
 *   or vertical sections for each type. Pre-bucketing them means the Composable just safely
 *   iterates lists without needing `if/else` filter checks during composition.
 * - **Pre-computed Balances**: The `balanceStr` string is calculated directly in the mapper
 *   because currency formatting requires iterating through properties (decimal places, symbols).
 *   Doing this in the ViewModel ensures it executes exactly once per data emission, preventing
 *   scroll-lag or frame drops in the UI.
 * - **Localized Fallbacks**: Missing names or statuses are explicitly output as `null`.
 *   This allows the Compose UI to use `?: stringResource(...)` to resolve localized
 *   fallbacks directly at the rendering site, keeping the ViewModel free of hardcoded strings.
 */
internal class PocketDashboardViewModel(
    private val pocketRepository: PocketRepository,
    userPreferencesRepository: ForkPreferencesRepository,
) : BaseViewModel<PocketDashboardState, PocketDashboardEvent, PocketDashboardAction>(
    initialState = PocketDashboardState(
        clientId = requireNotNull(userPreferencesRepository.clientId.value),
    ),
) {
    private val stream = pocketRepository.getDetailedPocketAccountsStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    val uiState: StateFlow<ScreenState<PocketBuckets>> = stream.state.map { state ->
        when (state) {
            is ScreenState.Content -> {
                ScreenState.Content(
                    data = state.data.toPocketBuckets(),
                    fetchedAt = state.fetchedAt,
                    freshnessSignal = state.freshnessSignal,
                )
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

    val freshness: StateFlow<FreshnessSignal> = stream.freshness.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FreshnessSignal.initial(),
    )

    fun retry() = stream.refresh()

    override fun handleAction(action: PocketDashboardAction) {
        when (action) {
            PocketDashboardAction.NavigateBack -> sendEvent(PocketDashboardEvent.NavigateBack)
            PocketDashboardAction.ManagePocket -> sendEvent(PocketDashboardEvent.ManagePocket)
            PocketDashboardAction.LinkFirstAccount -> sendEvent(PocketDashboardEvent.ManagePocket)
            PocketDashboardAction.Retry -> retry()
            PocketDashboardAction.Refresh -> retry()
            is PocketDashboardAction.NavigateToLoanDetail -> {
                sendEvent(PocketDashboardEvent.NavigateToLoanDetail(action.accountId))
            }
            is PocketDashboardAction.NavigateToSavingsDetail -> {
                sendEvent(PocketDashboardEvent.NavigateToSavingsDetail(action.accountId))
            }
            is PocketDashboardAction.NavigateToShareDetail -> {
                sendEvent(PocketDashboardEvent.NavigateToShareDetail(action.accountId))
            }
        }
    }
}

data class PocketDashboardState(
    val clientId: Long = 0,
)

data class DetailedPocket(
    val accountId: Long,
    val name: String?,
    val accountNumber: String,
    val balanceOrStatus: String?,
    val status: AccountStatus,
)

internal sealed interface PocketDashboardEvent {
    data object NavigateBack : PocketDashboardEvent
    data object ManagePocket : PocketDashboardEvent
    data class NavigateToLoanDetail(val accountId: Long) : PocketDashboardEvent
    data class NavigateToSavingsDetail(val accountId: Long) : PocketDashboardEvent
    data class NavigateToShareDetail(val accountId: Long) : PocketDashboardEvent
}

internal sealed interface PocketDashboardAction {
    data object NavigateBack : PocketDashboardAction
    data class NavigateToLoanDetail(val accountId: Long) : PocketDashboardAction
    data class NavigateToSavingsDetail(val accountId: Long) : PocketDashboardAction
    data class NavigateToShareDetail(val accountId: Long) : PocketDashboardAction
    data object ManagePocket : PocketDashboardAction
    data object LinkFirstAccount : PocketDashboardAction
    data object Refresh : PocketDashboardAction
    data object Retry : PocketDashboardAction
}

internal data class PocketBuckets(
    val totalBalance: String,
    val savingsAccounts: List<DetailedPocket>,
    val loanAccounts: List<DetailedPocket>,
    val shareAccounts: List<DetailedPocket>,
)

private fun formatBalance(
    balance: Double,
    currencyCode: String?,
    displaySymbol: String?,
    decimalPlaces: Int?,
): String {
    val code = currencyCode.orEmpty()
    val symbol = displaySymbol.orEmpty()
    val formattedNum = CurrencyFormatter.format(balance, decimalPlaces)
    return if (code.isNotEmpty()) "$code $symbol$formattedNum" else "$symbol$formattedNum"
}

/**
 * Maps a raw DetailedPocketAccount into a UI-ready DetailedPocket model.
 *
 * The `balanceStr` string is calculated directly in this mapper because currency formatting
 * requires iterating through properties (decimal places, symbols). Doing this natively in
 * the ViewModel ensures it executes exactly once per data emission, preventing scroll-lag
 * or frame drops in the UI during recomposition.
 */
private fun DetailedPocketAccount.toUiModel(): DetailedPocket {
    val balanceStr = if (status == AccountStatus.ACTIVE) {
        val currentBalance = balance
        if (currentBalance != null) {
            formatBalance(currentBalance, currencyCode, currencyDisplaySymbol, decimalPlaces)
        } else {
            null
        }
    } else {
        status?.name
    }

    return DetailedPocket(
        accountId = pocket.accountId,
        name = productName,
        accountNumber = pocket.accountNumber,
        balanceOrStatus = balanceStr,
        status = status ?: AccountStatus.UNKNOWN,
    )
}

/**
 * Groups accounts into `savingsAccounts`, `loanAccounts`, and `shareAccounts`.
 *
 * Pre-bucketing them here means the Composable just safely iterates these lists without
 * needing to run heavy `if/else` filter checks repeatedly during composition passes.
 * It also calculates the total aggregate balance across all accounts by currency.
 */
private fun List<DetailedPocketAccount>.toPocketBuckets(): PocketBuckets {
    val loanList = mutableListOf<DetailedPocket>()
    val savingsList = mutableListOf<DetailedPocket>()
    val shareList = mutableListOf<DetailedPocket>()

    for (account in this) {
        val uiModel = account.toUiModel()
        when (account.pocket.accountType) {
            AccountType.LOAN -> loanList.add(uiModel)
            AccountType.SAVINGS -> savingsList.add(uiModel)
            AccountType.SHARE -> shareList.add(uiModel)
        }
    }

    val balancesByCurrency = this
        .filter { it.status == AccountStatus.ACTIVE && it.balance != null && it.currencyCode != null }
        .groupBy { it.currencyCode!! }
        .map { (currencyCode, accounts) ->
            val sum = accounts.sumOf { it.balance ?: 0.0 }
            val first = accounts.first()
            formatBalance(sum, currencyCode, first.currencyDisplaySymbol, first.decimalPlaces)
        }

    val formattedTotal = if (balancesByCurrency.isNotEmpty()) {
        balancesByCurrency.joinToString("\n")
    } else {
        "0.00"
    }

    return PocketBuckets(
        totalBalance = formattedTotal,
        savingsAccounts = savingsList,
        loanAccounts = loanList,
        shareAccounts = shareList,
    )
}
