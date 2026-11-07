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
/**
 * ViewModel that checks the format of a debit card number before UPI setup continues.
 */
class DebitCardViewModel :
    BaseViewModel<DebitCardUiState, DebitCardEvent, DebitCardAction>(
        initialState = DebitCardUiState.Initials,
    ) {

    /** The debit-card step's state, under the name the screen reads. */
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

    /**
     * Requests verification of the given debit card details.
     */
    fun verifyDebitCard(debitCardNumber: String, month: String, year: String) {
        trySendAction(DebitCardAction.VerifyDebitCard(debitCardNumber, month, year))
    }

    private fun verifyDebitCardNumber(debitCardNumber: String): Boolean {
        return debitCardNumber.length in 12..19
    }
}

/**
 * One-off events of the debit card ViewModel (none defined).
 */
sealed interface DebitCardEvent

/**
 * Actions handled by the debit card ViewModel.
 */
sealed interface DebitCardAction {
    /**
     * Verify a debit card.
     * @property debitCardNumber Card number to check.
     * @property month Expiry month.
     * @property year Expiry year.
     */
    data class VerifyDebitCard(
        val debitCardNumber: String,
        val month: String,
        val year: String,
    ) : DebitCardAction
}

/**
 * UI state of the debit card verification.
 */
sealed class DebitCardUiState {
    /**
     * Nothing entered or verified yet.
     */
    data object Initials : DebitCardUiState()

    /**
     * Verification in progress.
     */
    data object Verifying : DebitCardUiState()

    /**
     * Card number format accepted.
     */
    data object Verified : DebitCardUiState()

    /**
     * Verification failed.
     * @property errorMessage Reason shown to the user.
     */
    data class VerificationFailed(val errorMessage: String) : DebitCardUiState()
}
