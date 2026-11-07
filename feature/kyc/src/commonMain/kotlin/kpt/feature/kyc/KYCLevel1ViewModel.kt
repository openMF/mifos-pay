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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.DateHelper
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.KycLevelRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.kyc.KYCLevel1Details
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.kyc.KycLevel1State.DialogState.Error
import kpt.feature.kyc.generated.resources.Res
import kpt.feature.kyc.generated.resources.feature_kyc_successkyc1
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class KYCLevel1ViewModel(
    private val kycLevelRepository: KycLevelRepository,
    private val repository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<KycLevel1State, KycLevel1Event, KycLevel1Action>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(repository.clientId.value)

        KycLevel1State(clientId = clientId)
    },
) {

    companion object {
        private const val KEY_STATE = "kyc_level_1_state"
    }

    // Template idiom (core-base/store): the one-shot KYCLevel1 write (add/update) goes
    // through a SubmitHandler instead of a hand-folded result action. The handler
    // owns the Submitting/Submitted/Failed lifecycle; we observe it below to drive this
    // screen's existing Loading dialog + toast + navigate-to-level-2 UX, so the Screen is
    // unchanged. Writes return Unit; the success toast is a feature StringResource.
    private val submitKyc = viewModelScope.submitHandler<Unit>()

    init {
        submitKyc.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = KycLevel1State.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(KycLevel1Event.ShowToast(Res.string.feature_kyc_successkyc1))
                        sendEvent(KycLevel1Event.NavigateToKycLevel2)
                        submitKyc.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(dialogState = Error(message))
                        }
                        submitKyc.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        // Prefill the form from the existing KYCLevel1 record (if any). The
        // stream naturally terminates after one Content emission from the
        // ktorfit call, so no takeUntilResultSuccess-equivalent is needed.
        // Empty / NoNetwork / Unauthenticated / Error simply leave the form
        // blank and clear any residual Loading dialog.
        kycLevelRepository.fetchKYCLevel1Details(state.clientId)
            .observeScreen { screenState ->
                when (screenState) {
                    is ScreenState.Loading -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = KycLevel1State.DialogState.Loading)
                        }
                    }

                    is ScreenState.Content -> {
                        screenState.data?.let { data ->
                            mutableStateFlow.update {
                                it.copy(
                                    firstNameInput = data.firstName,
                                    lastNameInput = data.lastName,
                                    addressLine1Input = data.addressLine1,
                                    addressLine2Input = data.addressLine2,
                                    mobileNoInput = data.mobileNo,
                                    dobInput = data.dob,
                                    currentLevelInput = data.currentLevel,
                                    doesExist = true,
                                    dialogState = null,
                                )
                            }
                        } ?: run {
                            mutableStateFlow.update {
                                it.copy(dialogState = null)
                            }
                        }
                    }

                    is ScreenState.Empty,
                    is ScreenState.Error,
                    is ScreenState.NoNetwork,
                    is ScreenState.Unauthenticated,
                    -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = null)
                        }
                    }
                }
            }
    }

    override fun handleAction(action: KycLevel1Action) {
        when (action) {
            is KycLevel1Action.FirstNameChanged -> {
                mutableStateFlow.update {
                    it.copy(firstNameInput = action.firstName)
                }
            }

            is KycLevel1Action.LastNameChanged -> {
                mutableStateFlow.update {
                    it.copy(lastNameInput = action.lastName)
                }
            }

            is KycLevel1Action.MobileNoChanged -> {
                mutableStateFlow.update {
                    it.copy(mobileNoInput = action.mobileNo)
                }
            }

            is KycLevel1Action.AddressLine1Changed -> {
                mutableStateFlow.update {
                    it.copy(addressLine1Input = action.addressLine1)
                }
            }

            is KycLevel1Action.AddressLine2Changed -> {
                mutableStateFlow.update {
                    it.copy(addressLine2Input = action.addressLine2)
                }
            }

            is KycLevel1Action.DobChanged -> {
                val formattedDate = DateHelper.getDateAsStringFromLong(action.dob)

                mutableStateFlow.update {
                    it.copy(dobInput = formattedDate)
                }
            }

            KycLevel1Action.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            KycLevel1Action.NavigateToKycLevel2 -> {
                sendEvent(KycLevel1Event.NavigateToKycLevel2)
            }

            is KycLevel1Action.NavigateBack -> {
                sendEvent(KycLevel1Event.OnNavigateBack)
            }

            KycLevel1Action.SubmitClicked -> initiateKycLevel1Submission()
        }
    }

    private fun initiateKycLevel1Submission() = when {
        state.firstNameInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("First name is required"))
            }
        }

        state.lastNameInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Last name is required"))
            }
        }

        state.mobileNoInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Mobile number is required"))
            }
        }

        state.mobileNoInput.length != 10 -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Mobile number should be 10 digits"))
            }
        }

        state.addressLine1Input.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Address line 1 is required"))
            }
        }

        state.addressLine2Input.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Address line 2 is required"))
            }
        }

        state.dobInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Date of birth is required"))
            }
        }

        else -> submitKycLevel1Details()
    }

    private fun submitKycLevel1Details() {
        mutableStateFlow.update {
            it.copy(dialogState = KycLevel1State.DialogState.Loading)
        }

        // Submit through the handler — it drives Submitting/Submitted/Failed, observed in
        // `init`. The write returns Unit and throws on error, so the handler reports Failed.
        submitKyc.submit {
            if (state.doesExist) {
                kycLevelRepository.updateKYCLevel1Details(state.clientId, state.details)
            } else {
                kycLevelRepository.addKYCLevel1Details(state.clientId, state.details)
            }
        }
    }
}

@Serializable
internal data class KycLevel1State(
    val clientId: Long,
    val firstNameInput: String = "",
    val lastNameInput: String = "",
    val addressLine1Input: String = "",
    val addressLine2Input: String = "",
    val mobileNoInput: String = "",
    val dobInput: String = "",
    val currentLevelInput: String = KycLevel.KYC_LEVEL_1.name,
    val doesExist: Boolean = false,
    val dialogState: DialogState? = null,
) {
    val title: String
        get() = if (doesExist) "Update Basic Details" else "Enter Basic Details"

    val submitButtonText: String
        get() = if (doesExist) "Update" else "Submit"

    @OptIn(ExperimentalTime::class)
    val initialDate = Clock.System.now().toEpochMilliseconds()

    @Transient
    val details = KYCLevel1Details(
        firstName = firstNameInput,
        lastName = lastNameInput,
        addressLine1 = addressLine1Input,
        addressLine2 = addressLine2Input,
        mobileNo = mobileNoInput,
        dob = dobInput,
        currentLevel = currentLevelInput,
    )

    @Serializable
    sealed interface DialogState {
        @Serializable
        data object Loading : DialogState

        @Serializable
        data class Error(val message: String) : DialogState
    }
}

internal sealed interface KycLevel1Event {
    data object NavigateToKycLevel2 : KycLevel1Event
    data object OnNavigateBack : KycLevel1Event
    data class ShowToast(val message: StringResource) : KycLevel1Event
}

internal sealed interface KycLevel1Action {
    data class FirstNameChanged(val firstName: String) : KycLevel1Action
    data class LastNameChanged(val lastName: String) : KycLevel1Action
    data class AddressLine1Changed(val addressLine1: String) : KycLevel1Action
    data class AddressLine2Changed(val addressLine2: String) : KycLevel1Action
    data class MobileNoChanged(val mobileNo: String) : KycLevel1Action
    data class DobChanged(val dob: Long) : KycLevel1Action

    data object SubmitClicked : KycLevel1Action
    data object DismissDialog : KycLevel1Action
    data object NavigateBack : KycLevel1Action
    data object NavigateToKycLevel2 : KycLevel1Action
}
