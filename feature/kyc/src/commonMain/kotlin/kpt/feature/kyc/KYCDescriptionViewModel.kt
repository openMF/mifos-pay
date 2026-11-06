/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.kyc

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.KycLevelRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.BaseViewModel
class KYCDescriptionViewModel(
    private val repository: ForkPreferencesRepository,
    kycLevelRepository: KycLevelRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<KycState, KycEvent, KycAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(repository.clientId.value)
        KycState(clientId = clientId)
    },
) {

    companion object {
        private const val KEY_STATE = "kyc_state"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val kycState = kycLevelRepository.fetchKYCLevel1Details(state.clientId).mapLatest { screenState ->
        // Fold ScreenState → the existing 3-branch KYCDescriptionUiState.
        // Empty is treated as Content(currentLevel = null) — no KYC record yet
        // means the user hasn't started onboarding. NoNetwork / Unauthenticated
        // fold into Error until Phase-4 differentiates them.
        when (screenState) {
            is ScreenState.Loading -> KYCDescriptionUiState.Loading

            is ScreenState.Empty -> KYCDescriptionUiState.Content(currentLevel = null)

            is ScreenState.Content -> {
                val currentLevel = screenState.data?.let {
                    KycLevel.valueOf(it.currentLevel)
                }
                KYCDescriptionUiState.Content(currentLevel)
            }

            is ScreenState.Error -> KYCDescriptionUiState.Error

            is ScreenState.NoNetwork -> KYCDescriptionUiState.Error

            is ScreenState.Unauthenticated -> KYCDescriptionUiState.Error
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KYCDescriptionUiState.Loading,
    )

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: KycAction) {
        when (action) {
            KycAction.Level1Clicked -> {
                sendEvent(KycEvent.OnLevel1Clicked)
            }

            KycAction.Level2Clicked -> {
                sendEvent(KycEvent.OnLevel2Clicked)
            }

            KycAction.Level3Clicked -> {
                sendEvent(KycEvent.OnLevel3Clicked)
            }
        }
    }
}

@Serializable
data class KycState(
    val clientId: Long,
)

sealed interface KYCDescriptionUiState {
    data class Content(val currentLevel: KycLevel?) : KYCDescriptionUiState
    data object Error : KYCDescriptionUiState
    data object Loading : KYCDescriptionUiState
}

enum class KycLevel(
    val level: Int,
    val title: String,
    val icon: ImageVector,
) {
    KYC_LEVEL_1(level = 1, title = "Basic Details", icon = MifosIcons.Person),
    KYC_LEVEL_2(level = 2, title = "Upload Documents", icon = MifosIcons.Badge),
    KYC_LEVEL_3(level = 3, title = "Review & Submit", icon = MifosIcons.DataInfo),
}

sealed interface KycEvent {
    data object OnLevel1Clicked : KycEvent
    data object OnLevel2Clicked : KycEvent
    data object OnLevel3Clicked : KycEvent
}

sealed interface KycAction {
    data object Level1Clicked : KycAction
    data object Level2Clicked : KycAction
    data object Level3Clicked : KycAction
}
