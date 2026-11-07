/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.upi.setup.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.common.ScreenState
import kpt.core.data.repository.TwoFactorAuthRepository
import kpt.core.model.bank.BankAccountDetails
import kpt.core.ui.utils.BaseViewModel
/**
 * Real OTP request/verify — reads through [TwoFactorAuthRepository] (a real Fineract
 * self-service two-factor-auth endpoint, previously bound in DI but never actually consumed
 * anywhere in this app). [requestOtp] triggers server-side delivery (SMS/email — the server
 * never returns the code); [verifyOtp] is the ONLY place the entered code is checked, via a
 * real `validateToken` round-trip. See `sub-plans/UPI_OTP_STUB_VERDICT.md`.
 */
class SetUpUpiViewModal(
    private val twoFactorAuthRepository: TwoFactorAuthRepository,
) : BaseViewModel<SetUpUpiState, SetUpUpiEvent, SetUpUpiAction>(
    initialState = SetUpUpiState(),
) {

    override fun handleAction(action: SetUpUpiAction) {
        when (action) {
            is SetUpUpiAction.SetupUpiPin -> {
                // to do setup upi pin api
            }

            SetUpUpiAction.RequestOtp -> requestOtp()

            is SetUpUpiAction.VerifyOtp -> verifyOtp(action.code)
        }
    }

    /**
     * Requests an OTP through the first available delivery method and updates [SetUpUpiState.otpRequestState].
     */
    fun requestOtp() {
        viewModelScope.launch {
            mutableStateFlow.update { it.copy(otpRequestState = OtpRequestState.Requesting) }

            val methodsState = twoFactorAuthRepository.deliveryMethods()
                .first { it !is ScreenState.Loading }
            val method = (methodsState as? ScreenState.Content)?.data?.firstOrNull()
            val methodName: String? = method?.name
            if (methodName == null) {
                mutableStateFlow.update {
                    it.copy(otpRequestState = OtpRequestState.Error("No OTP delivery method available"))
                }
                return@launch
            }
            val methodTarget = method.target.orEmpty()

            val otpState = twoFactorAuthRepository.requestOTP(methodName)
                .first { it !is ScreenState.Loading }
            mutableStateFlow.update {
                it.copy(
                    otpRequestState = when (otpState) {
                        is ScreenState.Content -> OtpRequestState.Sent(methodTarget)
                        is ScreenState.Error ->
                            OtpRequestState.Error(otpState.error.message ?: "Failed to send OTP")

                        else -> OtpRequestState.Error("Failed to send OTP")
                    },
                )
            }
        }
    }

    /**
     * Validates the OTP [code] and updates [SetUpUpiState.otpVerifyState].
     */
    fun verifyOtp(code: String) {
        viewModelScope.launch {
            mutableStateFlow.update { it.copy(otpVerifyState = OtpVerifyState.Verifying) }

            val result = twoFactorAuthRepository.validateToken(code)
                .first { it !is ScreenState.Loading }
            when (result) {
                is ScreenState.Content -> {
                    mutableStateFlow.update { it.copy(otpVerifyState = OtpVerifyState.Verified) }
                    sendEvent(SetUpUpiEvent.OnOtpVerified)
                }

                else -> {
                    mutableStateFlow.update { it.copy(otpVerifyState = OtpVerifyState.Failed) }
                }
            }
        }
    }

    /**
     * Requests setting the UPI PIN [mSetupUpiPin] for [bankAccountDetails].
     */
    fun setupUpiPin(bankAccountDetails: BankAccountDetails?, mSetupUpiPin: String?) {
        trySendAction(SetUpUpiAction.SetupUpiPin(bankAccountDetails, mSetupUpiPin))
    }
}

/**
 * UI state of the UPI PIN setup flow.
 * @property otpRequestState Progress of sending the OTP.
 * @property otpVerifyState Progress of verifying the OTP.
 */
data class SetUpUpiState(
    val otpRequestState: OtpRequestState = OtpRequestState.Idle,
    val otpVerifyState: OtpVerifyState = OtpVerifyState.Idle,
)

/**
 * Progress of requesting an OTP.
 */
sealed interface OtpRequestState {
    /**
     * No OTP requested yet.
     */
    data object Idle : OtpRequestState

    /**
     * OTP request in flight.
     */
    data object Requesting : OtpRequestState

    /**
     * OTP sent.
     * @property target Masked destination the OTP was sent to.
     */
    data class Sent(val target: String) : OtpRequestState

    /**
     * OTP request failed.
     * @property message Error text.
     */
    data class Error(val message: String) : OtpRequestState
}

/**
 * Progress of verifying an OTP.
 */
sealed interface OtpVerifyState {
    /**
     * No verification attempted yet.
     */
    data object Idle : OtpVerifyState

    /**
     * Verification in flight.
     */
    data object Verifying : OtpVerifyState

    /**
     * OTP accepted.
     */
    data object Verified : OtpVerifyState

    /**
     * OTP rejected or validation failed.
     */
    data object Failed : OtpVerifyState
}

/**
 * One-off events of the UPI setup ViewModel.
 */
sealed interface SetUpUpiEvent {
    /**
     * Emitted once the OTP has been verified successfully.
     */
    data object OnOtpVerified : SetUpUpiEvent
}

/**
 * Actions handled by the UPI setup ViewModel.
 */
sealed interface SetUpUpiAction {
    /**
     * Request an OTP.
     */
    data object RequestOtp : SetUpUpiAction

    /**
     * Verify an OTP.
     * @property code OTP entered by the user.
     */
    data class VerifyOtp(val code: String) : SetUpUpiAction

    /**
     * Set the UPI PIN.
     * @property bankAccountDetails Account the PIN is for.
     * @property mSetupUpiPin PIN chosen by the user.
     */
    data class SetupUpiPin(
        val bankAccountDetails: BankAccountDetails?,
        val mSetupUpiPin: String?,
    ) : SetUpUpiAction
}
