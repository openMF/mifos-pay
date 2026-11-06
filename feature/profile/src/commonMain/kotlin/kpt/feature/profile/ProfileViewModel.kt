/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.repository.ClientRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.client.Client
import kpt.core.ui.utils.BaseViewModel
import org.jetbrains.compose.resources.StringResource
import kpt.core.common.ScreenState as CommonScreenState
internal class ProfileViewModel(
    private val preferencesRepository: ForkPreferencesRepository,
    private val clientRepository: ClientRepository,
) : BaseViewModel<ProfileState, ProfileEvent, ProfileAction>(
    initialState = run {
        val clientId = requireNotNull(preferencesRepository.clientId.value)
        ProfileState(clientId = clientId)
    },
) {
    // Template idiom (core-base/store): hold the native ScreenDataStream and
    // expose its pre-decided `state` straight to the screen's `ScreenContent`.
    // No fork-ScreenState fold, no 6→3 `when` mapping — the DecisionEngine
    // inside the stream owns every Loading / Empty / NoNetwork / Unauthenticated
    // / Error / Content transition, and `refresh()` drives retry. Switched from
    // the fork-shim `getClientInfoScreen(...): Flow<ScreenState<Client>>` to the
    // store-native `getClientInfoStream(...): ScreenDataStream<Client>`
    // (`clientDetail` Store5: createStore + Room SoT + CACHE_FIRST_SWR +
    // single-row upsert). Requires `viewModelScope` for the stream's internal
    // reconnect + periodic + SWR side-fetch coroutines.
    //
    // Named `clientState` (not `state`) because BaseViewModel already exposes a
    // protected `state: ProfileState`; this is the Client read stream.
    //
    // NOTE — `getClientImage(...)` (the image blob-URL stream, observed in the
    // `init` block below) intentionally stays on the transitional shim; its
    // caching story (Coil vs Room blob) is out of scope for this batch.
    private val stream = clientRepository.getClientInfoStream(
        clientId = state.clientId,
        scope = viewModelScope,
    )

    val clientState: StateFlow<ScreenState<Client>> = stream.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    fun retry() = stream.refresh()

    init {
        // Load the client image side-stream. The prior implementation shuttled
        // a `DataState<String>` through an internal action; with `getClientImage`
        // now returning a ScreenState stream we fold it inline via the
        // `observeScreen` bridge. Only success updates `clientImage` — the
        // image tile silently falls back to the placeholder for all other
        // branches (matches prior behaviour, which suppressed error dialogs
        // for image failures per the commented-out lines in the old code).
        clientRepository.getClientImage(state.clientId).observeScreen { screenState ->
            // Image stream stays on the transitional fork ScreenState shim
            // (aliased to avoid colliding with the store-native ScreenState the
            // `clientState` read above now uses).
            if (screenState is CommonScreenState.Content) {
                mutableStateFlow.update { it.copy(clientImage = screenState.data) }
            }
        }
    }

    override fun handleAction(action: ProfileAction) {
        when (action) {
            ProfileAction.NavigateToEditProfile -> {
                sendEvent(ProfileEvent.OnEditProfile)
            }

            ProfileAction.NavigateToLinkBankAccount -> {
                sendEvent(ProfileEvent.OnLinkBankAccount)
            }

            ProfileAction.ShowPersonalQRCode -> {
                sendEvent(ProfileEvent.ShowQRCode)
            }

            ProfileAction.DismissErrorDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is ProfileAction.NavigateBack -> {
                sendEvent(ProfileEvent.OnNavigateBack)
            }
        }
    }
}

internal data class ProfileState(
    val clientId: Long,
    val clientImage: String? = null,
    val dialogState: DialogState? = null,
) {
    sealed interface DialogState {
        data object Loading : DialogState
        data class Error(val message: StringResource) : DialogState
    }
}

internal sealed interface ProfileEvent {
    data object OnEditProfile : ProfileEvent
    data object OnLinkBankAccount : ProfileEvent
    data object ShowQRCode : ProfileEvent
    data object OnNavigateBack : ProfileEvent
}

internal sealed interface ProfileAction {
    data object NavigateToEditProfile : ProfileAction
    data object NavigateToLinkBankAccount : ProfileAction
    data object ShowPersonalQRCode : ProfileAction

    data object DismissErrorDialog : ProfileAction

    data object NavigateBack : ProfileAction
}
