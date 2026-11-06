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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kpt.core.ui.utils.BaseViewModel
class DebitCardViewModel :
    BaseViewModel<DebitCardUiState, DebitCardEvent, DebitCardAction>(
        initialState = DebitCardUiState.Initials,
    ) {

    val debitCardUiState: StateFlow<DebitCardUiState> get() = stateFlow

    override fun handleAction(action: DebitCardAction) {
        when (action) {
            is DebitCardAction.VerifyDebitCard -> {
                viewModelScope.launch {
                    mutableStateFlow.value = DebitCardUiState.Verifying
                    delay(2000)

                    val isVerified = verifyDebitCardNumber(action.debitCardNumber)
                    if (isVerified) {
                        // Card FORMAT is valid — this does not, and cannot, know the real OTP
                        // (a real OTP is delivered out-of-band and only ever verified
                        // server-side). Verification happens downstream via
                        // SetUpUpiViewModal.requestOtp()/verifyOtp(...) against
                        // TwoFactorAuthRepository — see sub-plans/UPI_OTP_STUB_VERDICT.md.
                        mutableStateFlow.value = DebitCardUiState.Verified
                    } else {
                        mutableStateFlow.value = DebitCardUiState.VerificationFailed(
                            "Invalid Debit Card Number",
                        )
                    }
                }
            }
        }
    }

    fun verifyDebitCard(debitCardNumber: String, month: String, year: String) {
        trySendAction(DebitCardAction.VerifyDebitCard(debitCardNumber, month, year))
    }

    private fun verifyDebitCardNumber(debitCardNumber: String): Boolean {
        return debitCardNumber.length in 12..19
    }
}

sealed interface DebitCardEvent

sealed interface DebitCardAction {
    data class VerifyDebitCard(
        val debitCardNumber: String,
        val month: String,
        val year: String,
    ) : DebitCardAction
}

sealed class DebitCardUiState {
    data object Initials : DebitCardUiState()
    data object Verifying : DebitCardUiState()
    data object Verified : DebitCardUiState()
    data class VerificationFailed(val errorMessage: String) : DebitCardUiState()
}
