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

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.common.Constants
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.bank.BankAccountDetails
import kpt.feature.upi.setup.viewmodel.OtpRequestState
import kpt.feature.upi.setup.viewmodel.OtpVerifyState
import kpt.feature.upi.setup.viewmodel.SetUpUpiViewModal
import mifos_pay.feature.upi_setup.generated.resources.Res
import mifos_pay.feature.upi_setup.generated.resources.feature_upi_setup_back
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun SetupUpiPinScreenRoute(
    type: String,
    index: Int,
    bankAccountDetails: BankAccountDetails,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    setUpViewModel: SetUpUpiViewModal = koinViewModel(),
) {
    val state by setUpViewModel.stateFlow.collectAsStateWithLifecycle()

    SetupUpiPinScreen(
        type = type,
        index = index,
        bankAccountDetails = bankAccountDetails,
        otpRequestState = state.otpRequestState,
        otpVerifyState = state.otpVerifyState,
        onRequestOtp = setUpViewModel::requestOtp,
        onVerifyOtp = setUpViewModel::verifyOtp,
        setupUpiPin = {
            setUpViewModel.setupUpiPin(bankAccountDetails, it)
        },
        onBackPress = onBackPress,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@VisibleForTesting
internal fun SetupUpiPinScreen(
    type: String,
    index: Int,
    bankAccountDetails: BankAccountDetails,
    otpRequestState: OtpRequestState,
    otpVerifyState: OtpVerifyState,
    onRequestOtp: () -> Unit,
    onVerifyOtp: (String) -> Unit,
    setupUpiPin: (String) -> Unit,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (type) {
                            Constants.SETUP -> Constants.SETUP_UPI_PIN
                            Constants.CHANGE -> Constants.CHANGE_UPI_PIN
                            Constants.FORGOT -> Constants.FORGOT_UPI_PIN
                            else -> ""
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress,
                    ) {
                        Icon(
                            MifosIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.feature_upi_setup_back),
                        )
                    }
                },
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SetUpUpiScreenContent(
                    type = type,
                    otpRequestState = otpRequestState,
                    otpVerifyState = otpVerifyState,
                    onRequestOtp = onRequestOtp,
                    onVerifyOtp = onVerifyOtp,
                    correctlySettingUpi = {
                        setupUpiPin(it)
                    },
                )
            }
        },
    )
}

@Preview
@Composable
fun PreviewSetupUpiPin() {
    SetupUpiPinScreen(
        type = Constants.SETUP_UPI_PIN,
        index = 0,
        otpRequestState = OtpRequestState.Sent("+1 •••• ••1234"),
        otpVerifyState = OtpVerifyState.Idle,
        onRequestOtp = {},
        onVerifyOtp = {},
        bankAccountDetails = getBankAccountDetails(),
        onBackPress = {},
        setupUpiPin = {},
    )
}

@Preview
@Composable
fun PreviewChangeUpi() {
    SetupUpiPinScreen(
        type = Constants.CHANGE,
        index = 0,
        otpRequestState = OtpRequestState.Sent("+1 •••• ••1234"),
        otpVerifyState = OtpVerifyState.Idle,
        onRequestOtp = {},
        onVerifyOtp = {},
        bankAccountDetails = getBankAccountDetails(),
        onBackPress = {},
        setupUpiPin = {},
    )
}

@Preview
@Composable
fun PreviewForgetUpi() {
    SetupUpiPinScreen(
        type = Constants.FORGOT,
        index = 0,
        otpRequestState = OtpRequestState.Sent("+1 •••• ••1234"),
        otpVerifyState = OtpVerifyState.Idle,
        onRequestOtp = {},
        onVerifyOtp = {},
        bankAccountDetails = getBankAccountDetails(),
        onBackPress = {},
        setupUpiPin = {},
    )
}

fun getBankAccountDetails(): BankAccountDetails {
    return BankAccountDetails(
        accountNo = "SBI",
        bankName = "Ankur Sharma",
        accountHolderName = "New Delhi",
        branch = "XXXXXXXX9990XXX " + " ",
        ifsc = "Savings",
        type = "Debit",
        isUpiEnabled = false,
        upiPin = "0000",
    )
}
