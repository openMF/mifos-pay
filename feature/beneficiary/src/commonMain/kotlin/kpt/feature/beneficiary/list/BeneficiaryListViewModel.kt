/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.list

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.beneficiary.addupdatebeneficiary.BeneficiaryAddEditType
import org.jetbrains.compose.resources.StringResource
class BeneficiaryListViewModel(
    private val userRepository: ForkPreferencesRepository,
    private val repository: SelfServiceRepository,
    private val json: Json,
) : BaseViewModel<BeneficiaryListState, BeneficiaryListEvent, BeneficiaryListAction>(
    initialState = run {
        val clientId = requireNotNull(userRepository.clientId.value)
        val defaultAccount = userRepository.defaultAccountId.value

        BeneficiaryListState(
            clientId = clientId,
            defaultAccountId = defaultAccount,
        )
    },
) {
    // Template idiom (core-base/store): hold the native ScreenDataStream and
    // expose its pre-decided `state` straight to the Screen's `ScreenContent`.
    // The DecisionEngine inside the stream owns every Loading / Empty / NoNetwork
    // / Unauthenticated / Error / Content transition — no fork-ScreenState fold,
    // no 6→3 `when` collapse — and `refresh()` drives pull-to-refresh + retry.
    //
    // NOTE: named `listState` (NOT `state`) — `state` is BaseViewModel's
    // `protected val state: S` (the MVI view-state accessor) and would collide.
    private val stream = repository.getBeneficiaryListStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    val listState: StateFlow<ScreenState<List<Beneficiary>>> = stream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    /** Retry / pull-to-refresh entry point — re-drives the store fetch. */
    fun retry() = stream.refresh()

    override fun handleAction(action: BeneficiaryListAction) {
        when (action) {
            is BeneficiaryListAction.AddTPTBeneficiary -> {
                sendEvent(BeneficiaryListEvent.OnAddOrEditTPTBeneficiary(BeneficiaryAddEditType.AddItem()))
            }

            is BeneficiaryListAction.EditBeneficiary -> {
                viewModelScope.launch {
                    val beneficiary = json.encodeToString<Beneficiary>(action.beneficiary)
                    sendEvent(
                        BeneficiaryListEvent.OnAddOrEditTPTBeneficiary(
                            BeneficiaryAddEditType.EditItem(beneficiary),
                        ),
                    )
                }
            }

            is BeneficiaryListAction.RefreshList -> {
                stream.refresh()
            }
        }
    }

    /**
     * Refreshes the beneficiary list.
     * Called after successful delete from DeleteBeneficiaryViewModel.
     */
    fun refreshBeneficiaryList() {
        stream.refresh()
    }
}

data class BeneficiaryListState(
    val clientId: Long,
    val defaultAccountId: Long? = null,
)

sealed interface BeneficiaryListEvent {
    data class OnAddOrEditTPTBeneficiary(val type: BeneficiaryAddEditType) : BeneficiaryListEvent

    data class ShowToast(val message: StringResource) : BeneficiaryListEvent
}

sealed interface BeneficiaryListAction {
    data object AddTPTBeneficiary : BeneficiaryListAction
    data class EditBeneficiary(val beneficiary: Beneficiary) : BeneficiaryListAction
    data object RefreshList : BeneficiaryListAction
}
