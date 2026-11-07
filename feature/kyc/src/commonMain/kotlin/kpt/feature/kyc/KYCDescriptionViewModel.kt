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
/** ViewModel exposing the client's current KYC level and routing level selection events. */
class KYCDescriptionViewModel(
    private val repository: ForkPreferencesRepository,
    kycLevelRepository: KycLevelRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<KycState, KycEvent, KycAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        /** Id of the logged-in client, read from the stored fork preferences. */
        val clientId = requireNotNull(repository.clientId.value)
        KycState(clientId = clientId)
    },
) {

    private companion object {
        private const val KEY_STATE = "kyc_state"
    }

    /** The client's KYC level, as the description screen's state. */
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

/**
 * Persisted UI state of the KYC description screen.
 *
 * @property clientId Id of the client whose KYC level is shown.
 */
@Serializable
data class KycState(
    val clientId: Long,
)

/** Loading, content and error states of the KYC description screen. */
sealed interface KYCDescriptionUiState {
    /**
     * KYC level loaded successfully.
     *
     * @property currentLevel The client's current KYC level, or null if none is completed.
     */
    data class Content(val currentLevel: KycLevel?) : KYCDescriptionUiState

    /** Loading the KYC level failed. */
    data object Error : KYCDescriptionUiState

    /** The KYC level is being loaded. */
    data object Loading : KYCDescriptionUiState
}

/**
 * KYC verification levels offered in the wallet.
 *
 * @property level Numeric level.
 * @property title Display title of the level.
 * @property icon Icon shown for the level.
 */
enum class KycLevel(
    val level: Int,
    val title: String,
    val icon: ImageVector,
) {
    /** Level 1: basic details. */
    KYC_LEVEL_1(level = 1, title = "Basic Details", icon = MifosIcons.Person),

    /** Level 2: document upload. */
    KYC_LEVEL_2(level = 2, title = "Upload Documents", icon = MifosIcons.Badge),

    /** Level 3: review and submit. */
    KYC_LEVEL_3(level = 3, title = "Review & Submit", icon = MifosIcons.DataInfo),
}

/** One-off navigation events emitted by the KYC description screen. */
sealed interface KycEvent {
    /** Navigate to KYC level 1. */
    data object OnLevel1Clicked : KycEvent

    /** Navigate to KYC level 2. */
    data object OnLevel2Clicked : KycEvent

    /** Navigate to KYC level 3. */
    data object OnLevel3Clicked : KycEvent
}

/** User actions on the KYC description screen. */
sealed interface KycAction {
    /** The user tapped KYC level 1. */
    data object Level1Clicked : KycAction

    /** The user tapped KYC level 2. */
    data object Level2Clicked : KycAction

    /** The user tapped KYC level 3. */
    data object Level3Clicked : KycAction
}
