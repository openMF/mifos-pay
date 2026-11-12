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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.ui.OtpTextField
import kpt.core.ui.VerifyStepHeader
import kpt.feature.upi.setup.generated.resources.Res
import kpt.feature.upi.setup.generated.resources.feature_upi_setup_enter_otp
import kpt.feature.upi.setup.viewmodel.OtpRequestState
import kpt.feature.upi.setup.viewmodel.OtpVerifyState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * Real server-verified OTP step — [otpRequestState] reflects whether the code was actually
 * sent (via `TwoFactorAuthRepository.requestOTP`); [onOtpEntered] hands the typed digits to
 * the caller, which verifies them against the server (`validateToken`) and reports the
 * outcome back via [otpVerifyState]. No OTP value is ever known or compared client-side.
 */
@Composable
internal fun OtpScreen(
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onOtpEntered: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentVisibility: Boolean = false,
    verificationStatus: Boolean = false,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = KptTheme.spacing.md,
                bottom = KptTheme.spacing.md,
                start = KptTheme.spacing.sm,
                end = KptTheme.spacing.sm,
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = KptTheme.elevation.level1,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VerifyStepHeader("OtpSetUp", verificationStatus)
            if (contentVisibility) {
                OtpScreenContent(otpRequestState, otpVerifyState, onOtpEntered)
            }
        }
    }
}

@Composable
private fun OtpScreenContent(
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onOtpEntered: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = when (otpRequestState) {
                is OtpRequestState.Sent ->
                    stringResource(Res.string.feature_upi_setup_enter_otp) +
                        " (" + otpRequestState.target + ")"

                else -> stringResource(Res.string.feature_upi_setup_enter_otp)
            },
            color = KptTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            style = KptTheme.typography.headlineMedium,
        )
        when (otpRequestState) {
            is OtpRequestState.Requesting -> CircularProgressIndicator(
                modifier = Modifier.padding(top = KptTheme.spacing.lg),
            )

            is OtpRequestState.Error -> Text(
                text = otpRequestState.message,
                color = KptTheme.colorScheme.error,
                style = KptTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = KptTheme.spacing.sm),
            )

            is OtpRequestState.Sent, OtpRequestState.Idle -> {
                OtpTextField(
                    onOtpEntered = onOtpEntered,
                    modifier = Modifier.padding(top = KptTheme.spacing.lg),
                    isError = otpVerifyState is OtpVerifyState.Failed,
                    errorMessage = "Invalid OTP — please try again",
                )
                if (otpVerifyState is OtpVerifyState.Verifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = KptTheme.spacing.sm),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun OtpScreenPreview() {
    ForkTheme {
        OtpScreen(
            otpRequestState = OtpRequestState.Sent("+1 •••• ••1234"),
            otpVerifyState = OtpVerifyState.Idle,
            contentVisibility = true,
            verificationStatus = false,
            onOtpEntered = {},
        )
    }
}

@Preview
@Composable
private fun OtpScreenVerifiedPreview() {
    ForkTheme {
        OtpScreen(
            otpRequestState = OtpRequestState.Sent("+1 •••• ••1234"),
            otpVerifyState = OtpVerifyState.Verified,
            contentVisibility = false,
            verificationStatus = true,
            onOtpEntered = {},
        )
    }
}
