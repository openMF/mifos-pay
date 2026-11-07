/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.passcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkDialogBox
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.passcode.generated.resources.Res
import kpt.feature.passcode.generated.resources.feature_passcode_biometric_setup_confirm
import kpt.feature.passcode.generated.resources.feature_passcode_biometric_setup_headline
import kpt.feature.passcode.generated.resources.feature_passcode_biometric_setup_skip
import kpt.feature.passcode.generated.resources.feature_passcode_biometrics_usage_message
import kpt.feature.passcode.generated.resources.feature_passcode_error
import kpt.feature.passcode.generated.resources.feature_passcode_ok
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.passcode.components.MifosIcon
/** Navigation-event info marker for the biometric setup destination. */
internal object BiometricSetupScreenCurrentInfo : NavigationEventInfo()

/**
 * First-time biometric enrolment screen, shown after [PasscodeResult.Created]
 * lands on the root passcode flow. Calls
 * `PlatformAuthenticationProvider.registerUser(...)` from the composition-
 * scoped provider via [BiometricSetupScreenViewmodel].
 *
 * Both [onBiometricsRegistrationSuccess] and [onSkipBiometricSetup] are
 * terminal — the caller should pop this screen and route to main; whichever
 * branch the user takes, the passcode is already created so the app is
 * authenticated.
 *
 * @param onBiometricsRegistrationSuccess Fired after a successful
 *        `RegistrationResult.Success`. The library has already persisted the
 *        registration blob via [BiometricStorageAdapter] — no caller-side
 *        save required.
 * @param onSkipBiometricSetup Fired when the user taps "Skip for now".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricSetupScreen(
    onBiometricsRegistrationSuccess: () -> Unit,
    onSkipBiometricSetup: () -> Unit,
    viewModel: BiometricSetupScreenViewmodel = koinViewModel(),
) {
    val navEventState = rememberNavigationEventState(
        currentInfo = BiometricSetupScreenCurrentInfo,
    )

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = true,
        onBackCancelled = { },
        onBackCompleted = { },
    )

    val platformAuthenticationProvider = platformAuthenticationProvider.current
    val biometricErrorMessages = rememberBiometricErrorMessages()
    val biometricPromptStrings = rememberBiometricPromptStrings()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            BiometricSetupScreenEvent.OnBiometricSetupSuccess -> onBiometricsRegistrationSuccess()
            BiometricSetupScreenEvent.OnSkipBiometricSetup -> onSkipBiometricSetup()
        }
    }

    BiometricSetupContent(
        state = state,
        onSetupBiometrics = {
            viewModel.trySendAction(
                BiometricSetupScreenAction.ClickSetupBiometric(
                    platformAuthenticationProvider = platformAuthenticationProvider,
                    errorMessages = biometricErrorMessages,
                    promptStrings = biometricPromptStrings,
                ),
            )
        },
        onSkipBiometricSetup = {
            viewModel.trySendAction(BiometricSetupScreenAction.ClickSkipBiometric)
        },
        onDismissErrorDialog = {
            viewModel.trySendAction(BiometricSetupScreenAction.DismissErrorDialog)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BiometricSetupContent(
    state: BiometricSetupScreenState,
    onSetupBiometrics: () -> Unit,
    onSkipBiometricSetup: () -> Unit,
    onDismissErrorDialog: () -> Unit,
) {
    ForkDialogBox(
        title = stringResource(Res.string.feature_passcode_error),
        showDialogState = state.error != null,
        confirmButtonText = stringResource(Res.string.feature_passcode_ok),
        dismissButtonText = null,
        onConfirm = onDismissErrorDialog,
        onDismiss = onDismissErrorDialog,
        message = state.error,
    )

    ForkScaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KptTheme.colorScheme.background)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MifosIcon(modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(40.dp))

            Text(
                text = stringResource(Res.string.feature_passcode_biometric_setup_headline),
                style = KptTheme.typography.headlineSmall,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.feature_passcode_biometrics_usage_message),
                style = KptTheme.typography.bodyLarge,
                color = KptTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onSetupBiometrics,
                modifier = Modifier.widthIn(min = 200.dp),
                shape = RoundedCornerShape(20),
            ) {
                Text(
                    text = stringResource(Res.string.feature_passcode_biometric_setup_confirm),
                    color = KptTheme.colorScheme.onPrimary,
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onSkipBiometricSetup,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.feature_passcode_biometric_setup_skip))
            }
        }
    }
}

@Preview
@Composable
private fun BiometricSetupScreenPreview() {
    ForkTheme {
        BiometricSetupContent(
            state = BiometricSetupScreenState(),
            onSetupBiometrics = {},
            onSkipBiometricSetup = {},
            onDismissErrorDialog = {},
        )
    }
}
