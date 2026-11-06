/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.merchants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import kpt.core.ui.utils.BaseViewModel
// STUB VERDICT (Phase-5 Batch-4, 2026-08-01):
// -----------------------------------------------------------------------------
// The `merchants` feature has NO backend endpoint today. Neither
// `SelfServiceApiManager` nor `FineractApiManager` exposes a `merchantApi`
// property (verified by grep across `core/network`), and this ViewModel returns
// a hardcoded `emptyList()` without depending on any repository. Consequently
// there is no data source to cache and no store to build.
//
// Phase-5 Batch-4 therefore emits NO store for merchants — no `MerchantEntity`,
// no `MerchantDao`, no `MerchantStore`, no DI wiring. When a Fineract-side
// merchant list endpoint materializes (or a fork-side directory is authored),
// add:
//   1. `core/model/src/.../merchant/Merchant.kt` (domain).
//   2. `core/network/.../services/MerchantService.kt` +
//      `SelfServiceApiManager.merchantApi` (or the Fineract mount if that is
//      where the endpoint lives).
//   3. `core/database/.../wallet/merchant/{MerchantEntity,MerchantDao,MerchantEntityMapper}.kt`.
//   4. `core/store/.../wallet/merchant/{MerchantKey,MerchantStore}.kt` — LEDGER
//      read (`createStore` + CACHE_FIRST_SWR + `replacePage`) mirroring the
//      Batch-1 `beneficiary` / `savedCards` shape (client-scoped) OR the
//      Batch-3 `offices` shape (global singleton-keyed reference data) —
//      whichever the endpoint semantics dictate.
//   5. AppDatabase v-bump + AutoMigration; AppStoreRegistry + StoreModule
//      qualifier + register; MerchantRepository / MerchantRepositoryImpl;
//      RepositoryModule wiring; MerchantViewModel cutover to a store-backed
//      `Flow<ScreenState<List<Merchant>>>` (drop the `arrayListOf()` stub).
//
// TODO: merchants has no backend endpoint; add MerchantStore when the API materializes.
//
// @Deferred(reason = "no server endpoint", ticket = "sub-plans/MERCHANTS_ENDPOINT_VERDICT.md")
// Re-verified 2026-08-17 (offline-first-template-migration Phase 17) — fresh grep across
// core/data/repository, core/data/repositoryImpl, and core/network still returns zero merchant
// hits, and FineractApiManager's 18 bound APIs still carry no merchant surface. Verdict unchanged;
// see sub-plans/MERCHANTS_ENDPOINT_VERDICT.md for the full rationale.
// -----------------------------------------------------------------------------
class MerchantViewModel(
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<MerchantState, MerchantEvent, MerchantAction>(
    initialState = MerchantState(),
) {

    /**
     * The following three [StateFlow]s preserve the ViewModel's original public
     * surface (`merchantUiState`, `merchantsListUiState`, `isRefreshing`) so the
     * Screen compiles unchanged — each is a projection of the single MVI
     * [MerchantState] exposed by [BaseViewModel.stateFlow].
     */
    val merchantUiState: StateFlow<MerchantUiState> = stateFlow
        .map { it.merchantUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MerchantUiState.Empty,
        )

    val merchantsListUiState: StateFlow<MerchantUiState> = stateFlow
        .map { it.merchantListUiState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MerchantUiState.ShowMerchants(arrayListOf()),
        )

    val isRefreshing: StateFlow<Boolean> = stateFlow
        .map { it.isRefreshing }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Thin wrapper kept so `viewModel::updateSearchQuery` in the Screen stays
     * valid — delegates to the MVI action stream.
     */
    fun updateSearchQuery(query: String) = trySendAction(MerchantAction.UpdateSearchQuery(query))

    /**
     * Thin wrapper kept so `viewModel::refresh` in the Screen stays valid —
     * delegates to the MVI action stream.
     */
    fun refresh() = trySendAction(MerchantAction.Refresh)

    override fun handleAction(action: MerchantAction) {
        when (action) {
            is MerchantAction.UpdateSearchQuery -> {
                mutableStateFlow.update { current ->
                    // Preserves the original derivation: the stub always yields
                    // an empty merchant list (no backend), so both branches map
                    // to a ShowMerchants with an empty list.
                    val list = when (current.merchantUiState) {
                        is MerchantUiState.ShowMerchants ->
                            MerchantUiState.ShowMerchants(emptyList())

                        else -> MerchantUiState.ShowMerchants(arrayListOf())
                    }
                    current.copy(
                        searchQuery = action.query,
                        merchantListUiState = list,
                    )
                }
            }

            is MerchantAction.Refresh -> {
                viewModelScope.launch {
                    mutableStateFlow.update { it.copy(isRefreshing = true) }
                    delay(200)
                    mutableStateFlow.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }
}

data class MerchantState(
    val merchantUiState: MerchantUiState = MerchantUiState.Empty,
    val merchantListUiState: MerchantUiState = MerchantUiState.ShowMerchants(arrayListOf()),
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
)

sealed interface MerchantEvent

sealed interface MerchantAction {
    data class UpdateSearchQuery(val query: String) : MerchantAction
    data object Refresh : MerchantAction
}

sealed class MerchantUiState {
    data object Loading : MerchantUiState()
    data object Empty : MerchantUiState()
    data class Error(val message: String) : MerchantUiState()
    data class ShowMerchants(val merchants: List<SavingsWithAssociationsEntity>) : MerchantUiState()
}
