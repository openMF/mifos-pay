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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.BasicDialogState
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkLoadingDialog
import kpt.core.designsystem.fork.component.ForkOutlinedTextField
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTopAppBar
import kpt.core.designsystem.fork.component.LoadingDialogState
import kpt.core.designsystem.fork.component.NavigationIcon
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.auth.generated.resources.Res
import kpt.feature.auth.generated.resources.feature_auth_enter_mobile_number
import kpt.feature.auth.generated.resources.feature_auth_enter_mobile_number_description
import kpt.feature.auth.generated.resources.feature_auth_enter_otp
import kpt.feature.auth.generated.resources.feature_auth_enter_otp_received
import kpt.feature.auth.generated.resources.feature_auth_phone_number
import kpt.feature.auth.generated.resources.feature_auth_verify_otp
import kpt.feature.auth.generated.resources.feature_auth_verify_phone
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun MobileVerificationScreen(
    onNavigateBack: () -> Unit,
    onOtpVerificationSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MobileVerificationViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    EventsEffect(viewModel) { event ->
        when (event) {
            is MobileVerificationEvent.NavigateBack -> onNavigateBack.invoke()

            is MobileVerificationEvent.NavigateToSignup -> {
                onOtpVerificationSuccess(event.phoneNo)
            }

            is MobileVerificationEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    MobileVerificationScreen(
        uiState = state,
        onEvent = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileVerificationScreen(
    uiState: MobileVerificationState,
    onEvent: (MobileVerificationAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val title = if (uiState is MobileVerificationState.VerifyPhoneState) {
        stringResource(Res.string.feature_auth_enter_mobile_number)
    } else {
        stringResource(Res.string.feature_auth_enter_otp)
    }

    ForkScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
        topBar = {
            ForkTopAppBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = NavigationIcon(
                    navigationIcon = ForkIcons.Back,
                    navigationIconContentDescription = "Go Back",
                    onNavigationIconClick = {
                        onEvent(MobileVerificationAction.CloseButtonClick)
                    },
                ),
            )
        },
    ) { paddingValues ->
        when (uiState) {
            is MobileVerificationState.VerifyPhoneState -> {
                PhoneNoVerifyContent(
                    modifier = Modifier.padding(paddingValues),
                    state = uiState,
                    onEvent = onEvent,
                )
            }

            is MobileVerificationState.VerifyOtpState -> {
                OtpVerifyContent(
                    modifier = Modifier.padding(paddingValues),
                    state = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}

/** Content for the phone-number entry step of mobile verification. */
@Composable
fun PhoneNoVerifyContent(
    modifier: Modifier = Modifier,
    state: MobileVerificationState.VerifyPhoneState,
    onEvent: (MobileVerificationAction) -> Unit,
) {
    MobileVerificationDialogs(
        dialogState = state.dialogState,
        onDismissRequest = {
            onEvent(MobileVerificationAction.DismissDialog)
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        ForkOutlinedTextField(
            modifier = modifier,
            value = state.phoneNo,
            label = stringResource(Res.string.feature_auth_phone_number),
            onValueChange = {
                onEvent(MobileVerificationAction.PhoneNoChanged(it))
            },
        )

        Text(
            text = stringResource(Res.string.feature_auth_enter_mobile_number_description),
            style = KptTheme.typography.bodySmall,
        )

        ForkButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.xl, vertical = KptTheme.spacing.md),
            enabled = state.isPhoneNoValid,
            onClick = {
                onEvent(MobileVerificationAction.VerifyPhoneBtnClicked)
            },
            contentPadding = PaddingValues(KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_auth_verify_phone).uppercase(),
                style = KptTheme.typography.labelLarge,
            )
        }
    }
}

/** Content for the OTP entry step of mobile verification. */
@Composable
fun OtpVerifyContent(
    modifier: Modifier = Modifier,
    state: MobileVerificationState.VerifyOtpState,
    onEvent: (MobileVerificationAction) -> Unit,
) {
    MobileVerificationDialogs(
        dialogState = state.dialogState,
        onDismissRequest = {
            onEvent(MobileVerificationAction.DismissDialog)
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        ForkOutlinedTextField(
            modifier = modifier,
            value = state.otp,
            label = stringResource(Res.string.feature_auth_enter_otp),
            onValueChange = {
                onEvent(MobileVerificationAction.PhoneNoChanged(it))
            },
        )

        Text(
            text = stringResource(Res.string.feature_auth_enter_otp_received),
            style = KptTheme.typography.bodySmall,
        )

        ForkButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.xl, vertical = KptTheme.spacing.md),
            enabled = state.isOtpValid,
            onClick = {
                onEvent(MobileVerificationAction.VerifyOtpBtnClicked)
            },
            contentPadding = PaddingValues(KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_auth_verify_otp).uppercase(),
                style = KptTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun MobileVerificationDialogs(
    dialogState: MobileVerificationState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is MobileVerificationState.DialogState.Error -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is MobileVerificationState.DialogState.Loading -> ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}

@Preview
@Composable
private fun MobileVerificationScreenVerifyPhonePreview() {
    ForkTheme {
        MobileVerificationScreen(
            uiState = MobileVerificationState.VerifyPhoneState(),
            onEvent = {},
        )
    }
}

@Preview
@Composable
private fun MobileVerificationScreenVerifyOtpPreview() {
    ForkTheme {
        MobileVerificationScreen(
            uiState = MobileVerificationState.VerifyOtpState(phoneNo = ""),
            onEvent = {},
        )
    }
}
