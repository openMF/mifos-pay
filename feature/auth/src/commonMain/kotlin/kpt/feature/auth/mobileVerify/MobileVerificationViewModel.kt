/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.mobileVerify

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.SearchRepository
import kpt.core.data.util.Constants
import kpt.core.ui.utils.BaseViewModel
/** View model that verifies a mobile number: requests an OTP for the number, then verifies the entered OTP. */
class MobileVerificationViewModel(
    private val searchRepository: SearchRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<MobileVerificationState, MobileVerificationEvent, MobileVerificationAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
        ?: MobileVerificationState.VerifyPhoneState(),
) {

    /** Holds the saved-state key used to persist the verification state. */
    companion object {
        private const val KEY_STATE = "mobile_verification"
    }

    // Template idiom (core-base/store): the two one-shot writes on this screen — request-OTP
    // (verify the mobile number is free, then request an OTP) and verify-OTP — each go through
    // their own SubmitHandler instead of a hand-folded DataState result action. The handlers own
    // the Submitting/Submitted/Failed lifecycle; we observe them below to drive this screen's
    // existing loading/error dialogs, phone→OTP state transition, toast and navigation, so the
    // Screen is unchanged. Both carry the phone number as the success result.
    private val submitRequestOtp = viewModelScope.submitHandler<String>()
    private val submitVerifyOtp = viewModelScope.submitHandler<String>()

    init {
        submitRequestOtp.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            if (it is MobileVerificationState.VerifyPhoneState) {
                                it.copy(dialogState = MobileVerificationState.DialogState.Loading)
                            } else {
                                it
                            }
                        }
                    }

                    is SubmitState.Submitted -> {
                        // Uniqueness passed + OTP requested — advance to the OTP entry state.
                        mutableStateFlow.update {
                            MobileVerificationState.VerifyOtpState(phoneNo = submitState.result)
                        }
                        submitRequestOtp.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message ?: "Something Went Wrong!"
                        mutableStateFlow.update {
                            if (it is MobileVerificationState.VerifyPhoneState) {
                                it.copy(
                                    dialogState = MobileVerificationState.DialogState.Error(message),
                                )
                            } else {
                                it
                            }
                        }
                        submitRequestOtp.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        submitVerifyOtp.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            (it as? MobileVerificationState.VerifyOtpState)?.copy(
                                dialogState = MobileVerificationState.DialogState.Loading,
                            ) ?: it
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update {
                            (it as? MobileVerificationState.VerifyOtpState)?.copy(
                                dialogState = null,
                            ) ?: it
                        }
                        sendEvent(MobileVerificationEvent.ShowToast("Otp Verified Successfully"))
                        sendEvent(MobileVerificationEvent.NavigateToSignup(submitState.result))
                        submitVerifyOtp.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            (it as? MobileVerificationState.VerifyOtpState)?.copy(
                                dialogState = MobileVerificationState.DialogState.Error(
                                    "Otp Verification Failed",
                                ),
                            ) ?: it
                        }
                        submitVerifyOtp.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: MobileVerificationAction) {
        when (action) {
            is MobileVerificationAction.PhoneNoChanged -> {
                mutableStateFlow.update {
                    if (it is MobileVerificationState.VerifyPhoneState) {
                        it.copy(phoneNo = action.phoneNo)
                    } else {
                        it
                    }
                }
            }

            is MobileVerificationAction.VerifyPhoneBtnClicked -> {
                val currentState = state as? MobileVerificationState.VerifyPhoneState ?: return
                verifyPhoneNo(currentState)
            }

            is MobileVerificationAction.OtpChanged -> {
                mutableStateFlow.update {
                    if (it is MobileVerificationState.VerifyOtpState) {
                        it.copy(otp = action.otp)
                    } else {
                        it
                    }
                }
            }

            is MobileVerificationAction.VerifyOtpBtnClicked -> {
                val currentState = state as? MobileVerificationState.VerifyOtpState ?: return
                verifyEnteredOTP(currentState)
            }

            is MobileVerificationAction.ChangePhoneNumber -> handleChangePhoneNoClick()

            is MobileVerificationAction.DismissDialog -> handleDismissDialog()

            is MobileVerificationAction.CloseButtonClick -> handleCloseButtonClick()
        }
    }

    private fun verifyPhoneNo(currentState: MobileVerificationState.VerifyPhoneState) {
        if (currentState.isPhoneNoValid) {
            verifyMobileAndRequestOtp(currentState.phoneNo)
        } else {
            mutableStateFlow.update {
                currentState.copy(
                    dialogState = MobileVerificationState.DialogState.Error("Phone no isn't valid"),
                )
            }
        }
    }

    private fun verifyMobileAndRequestOtp(phoneNo: String) {
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed in
        // `init`. The block runs the uniqueness search: return the phone no on success (unique),
        // throw on error / already-exists so the handler reports Failed with that message.
        submitRequestOtp.submit {
            val result = searchRepository.searchResources(
                query = phoneNo,
                resources = Constants.CLIENTS,
                exactMatch = true,
            )
            if (result.isEmpty()) {
                // TODO:: Call repository request an otp to this phone no.
                phoneNo
            } else {
                throw Exception("Mobile number already exists.")
            }
        }
    }

    private fun verifyEnteredOTP(state: MobileVerificationState.VerifyOtpState) {
        if (state.isOtpValid) {
            // Submit through the handler — Submitting/Submitted/Failed observed in `init`.
            submitVerifyOtp.submit {
                // TODO:: Match send otp to entered otp — stub succeeds with the verified phone no.
                state.phoneNo
            }
        } else {
            mutableStateFlow.update {
                state.copy(
                    dialogState = MobileVerificationState.DialogState.Error("OTP isn't valid"),
                )
            }
        }
    }

    private fun handleChangePhoneNoClick() {
        viewModelScope.launch {
            mutableStateFlow.update {
                if (it is MobileVerificationState.VerifyOtpState) {
                    MobileVerificationState.VerifyPhoneState(phoneNo = it.phoneNo)
                } else {
                    it
                }
            }
        }
    }

    private fun handleDismissDialog() {
        viewModelScope.launch {
            mutableStateFlow.update {
                when (it) {
                    is MobileVerificationState.VerifyOtpState -> {
                        it.copy(dialogState = null)
                    }

                    is MobileVerificationState.VerifyPhoneState -> {
                        it.copy(dialogState = null)
                    }
                }
            }
        }
    }

    private fun handleCloseButtonClick() {
        viewModelScope.launch {
            if (state is MobileVerificationState.VerifyOtpState) {
                sendEvent(MobileVerificationEvent.NavigateBack)
            } else {
                sendAction(MobileVerificationAction.ChangePhoneNumber)
            }
        }
    }
}

/** UI state of the mobile verification flow, one subtype per step. */
@Serializable
sealed class MobileVerificationState {
    /**
     * State of the phone-number step.
     *
     * @property phoneNo Phone number typed by the user.
     * @property dialogState Loading or error dialog currently shown, or null.
     */
    @Serializable
    data class VerifyPhoneState(
        val phoneNo: String = "",
        @Transient
        val dialogState: DialogState? = null,
    ) : MobileVerificationState() {
        /** True when the phone number has exactly 10 digits. */
        val isPhoneNoValid: Boolean
            get() = phoneNo.length == 10
    }

    /**
     * State of the OTP step.
     *
     * @property phoneNo Phone number the OTP was sent to.
     * @property otp OTP typed by the user.
     * @property dialogState Loading or error dialog currently shown, or null.
     */
    @Serializable
    data class VerifyOtpState(
        val phoneNo: String,
        val otp: String = "",
        @Transient
        val dialogState: DialogState? = null,
    ) : MobileVerificationState() {
        /** True when the OTP has exactly 6 digits. */
        val isOtpValid: Boolean
            get() = otp.length == 6
    }

    /** Dialog shown over the verification screens. */
    sealed class DialogState {
        /**
         * Error dialog.
         *
         * @property message Error text to show.
         */
        data class Error(val message: String) : DialogState()

        /** Blocking progress dialog shown while a request is in flight. */
        data object Loading : DialogState()
    }
}

/** One-off events emitted by the mobile verification view model. */
sealed interface MobileVerificationEvent {
    /** Navigate back from the verification screen. */
    data object NavigateBack : MobileVerificationEvent

    /**
     * Navigate to signup once the number is verified.
     *
     * @property phoneNo Verified phone number to prefill.
     */
    data class NavigateToSignup(val phoneNo: String) : MobileVerificationEvent

    /**
     * Show a toast.
     *
     * @property message Text of the toast.
     */
    data class ShowToast(val message: String) : MobileVerificationEvent
}

/** User actions handled by the mobile verification view model. */
sealed interface MobileVerificationAction {
    /**
     * The phone-number field changed.
     *
     * @property phoneNo New phone number value.
     */
    data class PhoneNoChanged(val phoneNo: String) : MobileVerificationAction

    /** The user tapped the button that requests an OTP. */
    data object VerifyPhoneBtnClicked : MobileVerificationAction

    /**
     * The OTP field changed.
     *
     * @property otp New OTP value.
     */
    data class OtpChanged(val otp: String) : MobileVerificationAction

    /** The user chose to go back and edit the phone number. */
    data object ChangePhoneNumber : MobileVerificationAction

    /** The user tapped the button that verifies the OTP. */
    data object VerifyOtpBtnClicked : MobileVerificationAction

    /** The user dismissed the open dialog. */
    data object DismissDialog : MobileVerificationAction

    /** The user tapped the close button. */
    data object CloseButtonClick : MobileVerificationAction
}
