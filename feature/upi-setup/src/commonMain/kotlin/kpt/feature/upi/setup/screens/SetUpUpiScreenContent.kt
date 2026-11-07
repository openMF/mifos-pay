/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.upi.setup.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kpt.core.common.Constants
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.feature.upi.setup.viewmodel.OtpRequestState
import kpt.feature.upi.setup.viewmodel.OtpVerifyState
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun SetUpUpiScreenContent(
    type: String,
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onRequestOtp: () -> Unit,
    onVerifyOtp: (String) -> Unit,
    correctlySettingUpi: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        if (type == Constants.CHANGE) {
            ChangeUpi(
                otpRequestState = otpRequestState,
                otpVerifyState = otpVerifyState,
                onRequestOtp = onRequestOtp,
                onVerifyOtp = onVerifyOtp,
                correctlySettingUpi = correctlySettingUpi,
                modifier = modifier,
            )
        } else {
            SettingAndForgotUpi(
                otpRequestState = otpRequestState,
                otpVerifyState = otpVerifyState,
                onRequestOtp = onRequestOtp,
                onVerifyOtp = onVerifyOtp,
                correctlySettingUpi = correctlySettingUpi,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun SettingAndForgotUpi(
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onRequestOtp: () -> Unit,
    onVerifyOtp: (String) -> Unit,
    correctlySettingUpi: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var debitCardVerified by rememberSaveable { mutableStateOf(false) }
    var debitCardScreenVisible by rememberSaveable { mutableStateOf(true) }
    var otpScreenVisible by rememberSaveable { mutableStateOf(false) }
    var upiPinScreenVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(otpVerifyState) {
        if (otpVerifyState is OtpVerifyState.Verified) {
            otpScreenVisible = false
            upiPinScreenVisible = true
        }
    }

    Column(modifier) {
        DebitCardScreen(
            verificationStatus = debitCardVerified,
            isContentVisible = debitCardScreenVisible,
            onDebitCardVerified = {
                debitCardVerified = true
                debitCardScreenVisible = false
                otpScreenVisible = true
                // The debit-card step only validates the card FORMAT locally — the actual
                // OTP is requested here, from the real TwoFactorAuthRepository-backed
                // ViewModel, once the user reaches the OTP step.
                onRequestOtp()
            },
            onDebitCardVerificationFailed = {
            },
        )
        OtpScreen(
            verificationStatus = otpVerifyState is OtpVerifyState.Verified,
            contentVisibility = otpScreenVisible,
            otpRequestState = otpRequestState,
            otpVerifyState = otpVerifyState,
            onOtpEntered = onVerifyOtp,
        )
        UpiPinScreen(
            correctlySettingUpi = correctlySettingUpi,
            contentVisibility = upiPinScreenVisible,
        )
    }
}

@Composable
private fun ChangeUpi(
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onRequestOtp: () -> Unit,
    onVerifyOtp: (String) -> Unit,
    correctlySettingUpi: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var upiPinScreenVisible by rememberSaveable { mutableStateOf(false) }
    var otpScreenVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) { onRequestOtp() }

    LaunchedEffect(otpVerifyState) {
        if (otpVerifyState is OtpVerifyState.Verified) {
            otpScreenVisible = false
            upiPinScreenVisible = true
        }
    }

    Column(modifier) {
        OtpScreen(
            verificationStatus = otpVerifyState is OtpVerifyState.Verified,
            contentVisibility = otpScreenVisible,
            otpRequestState = otpRequestState,
            otpVerifyState = otpVerifyState,
            onOtpEntered = onVerifyOtp,
        )

        UpiPinScreen(
            correctlySettingUpi = correctlySettingUpi,
            contentVisibility = upiPinScreenVisible,
        )
    }
}

@Preview
@Composable
private fun PreviewSetUpUpiPin() {
    ForkTheme {
        SetUpUpiScreenContent(
            type = Constants.SETUP,
            otpRequestState = OtpRequestState.Idle,
            otpVerifyState = OtpVerifyState.Idle,
            onRequestOtp = {},
            onVerifyOtp = {},
            correctlySettingUpi = {},
        )
    }
}

@Preview
@Composable
private fun PreviewForgetUpiPin() {
    ForkTheme {
        SetUpUpiScreenContent(
            type = Constants.FORGOT,
            otpRequestState = OtpRequestState.Idle,
            otpVerifyState = OtpVerifyState.Idle,
            onRequestOtp = {},
            onVerifyOtp = {},
            correctlySettingUpi = {},
        )
    }
}

@Preview
@Composable
private fun PreviewChangeUpiPin() {
    ForkTheme {
        SetUpUpiScreenContent(
            type = Constants.CHANGE,
            otpRequestState = OtpRequestState.Idle,
            otpVerifyState = OtpVerifyState.Idle,
            onRequestOtp = {},
            onVerifyOtp = {},
            correctlySettingUpi = {},
        )
    }
}
