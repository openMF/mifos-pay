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

    fun setupUpiPin(bankAccountDetails: BankAccountDetails?, mSetupUpiPin: String?) {
        trySendAction(SetUpUpiAction.SetupUpiPin(bankAccountDetails, mSetupUpiPin))
    }
}

data class SetUpUpiState(
    val otpRequestState: OtpRequestState = OtpRequestState.Idle,
    val otpVerifyState: OtpVerifyState = OtpVerifyState.Idle,
)

sealed interface OtpRequestState {
    data object Idle : OtpRequestState
    data object Requesting : OtpRequestState
    data class Sent(val target: String) : OtpRequestState
    data class Error(val message: String) : OtpRequestState
}

sealed interface OtpVerifyState {
    data object Idle : OtpVerifyState
    data object Verifying : OtpVerifyState
    data object Verified : OtpVerifyState
    data object Failed : OtpVerifyState
}

sealed interface SetUpUpiEvent {
    data object OnOtpVerified : SetUpUpiEvent
}

sealed interface SetUpUpiAction {
    data object RequestOtp : SetUpUpiAction
    data class VerifyOtp(val code: String) : SetUpUpiAction
    data class SetupUpiPin(
        val bankAccountDetails: BankAccountDetails?,
        val mSetupUpiPin: String?,
    ) : SetUpUpiAction
}
