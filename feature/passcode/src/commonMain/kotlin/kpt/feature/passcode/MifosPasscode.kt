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

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosDialogBox
import kpt.feature.passcode.generated.resources.Res
import kpt.feature.passcode.generated.resources.feature_passcode_error
import kpt.feature.passcode.generated.resources.feature_passcode_ok
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.authenticator.biometrics.platformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAvailableAuthenticationOption
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeResult
import org.mifos.authenticator.passcode.screen.PasscodeAppearanceConfig
import org.mifos.authenticator.passcode.screen.PasscodeButtonConfig
import org.mifos.authenticator.passcode.screen.PasscodeDialogConfig
import org.mifos.authenticator.passcode.screen.PasscodeDotConfig
import org.mifos.authenticator.passcode.screen.PasscodeKeyConfig
import org.mifos.authenticator.passcode.screen.PasscodeLogoConfig
import org.mifos.authenticator.passcode.screen.PasscodeScreen
import org.mifos.authenticator.passcode.screen.PasscodeSwitchConfig
/** Navigation-event info marker for the passcode destination. */
internal object MifosPasscodeCurrentInfo : NavigationEventInfo()

/**
 * Mifos Pay-themed wrapper around the library's `PasscodeScreen` that bridges
 * the passcode and biometrics libraries (which never import each other).
 *
 * Every passcode route in the app — root unlock, re-auth on background
 * resume, in-app sensitive-operation gate — delegates to this composable
 * with different result-callback wiring. Biometric success is converted into
 * a synthetic [PasscodeResult.Verified] inside [MifosPasscodeViewModel] so
 * the consumer only has to handle [PasscodeResult] values; biometric and
 * passcode success converge on [onAuthenticationSuccess].
 *
 * Must be hosted inside a `PlatformAuthenticatorCompositionProvider` so the
 * `platformAuthenticationProvider` / `platformAvailableAuthenticationOption`
 * composition locals resolve.
 *
 * @param onAuthenticationSuccess Fired on [PasscodeResult.Verified] (passcode
 *        or biometric success).
 * @param onBackPress Invoked when the user performs a system-back gesture
 *        and [allowBackNavigation] is `true`; no-op otherwise.
 * @param navigateToLogin Fired on [PasscodeResult.Forgotten] — the passcode
 *        and biometric registration have just been wiped.
 * @param onPasscodeCreation Fired on [PasscodeResult.Created] (first-time
 *        passcode setup).
 * @param onPasscodeChanged Fired on [PasscodeResult.Changed] (settings
 *        change-passcode flow).
 * @param onAuthenticationFailed Fired on [PasscodeResult.Rejected] (wrong
 *        passcode entered).
 * @param allowBackNavigation `false` for unlock screens (root + re-auth) so
 *        the user can't escape the lock; `true` for in-app gates so the user
 *        can cancel.
 * @param allowBiometricAuth `false` to suppress both the biometric button and
 *        the auto-authenticate-on-resume behaviour — set this for flows where
 *        biometric bypass would defeat the security check (verifying before
 *        disabling biometrics, change-passcode). The library also hides the
 *        biometric button during `Create` / `Confirm` / `ChangeVerify`
 *        steps regardless of this flag.
 * @param viewModel Koin-resolved [MifosPasscodeViewModel].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MifosPasscode(
    onAuthenticationSuccess: () -> Unit,
    onBackPress: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
    onPasscodeCreation: () -> Unit = {},
    onPasscodeChanged: () -> Unit = {},
    onAuthenticationFailed: () -> Unit = {},
    allowBackNavigation: Boolean = false,
    allowBiometricAuth: Boolean = true,
    viewModel: MifosPasscodeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val passcodeManager: PasscodeManager = koinInject<PasscodeManager>()

    val systemAuthProvider = platformAuthenticationProvider.current
    val systemAvailableAuthOption = platformAvailableAuthenticationOption.current
    val isRegistered by systemAuthProvider.isRegistered.collectAsStateWithLifecycle()
    val lifeCycleOwner = LocalLifecycleOwner.current

    // Pre-resolve biometric error/prompt strings once at composition; ferry
    // them across the viewModelScope.launch boundary via the action constructors
    // (stringResource() is not callable from inside the suspend launch block).
    val biometricErrorMessages = rememberBiometricErrorMessages()
    val biometricPromptStrings = rememberBiometricPromptStrings()

    val navEventState = rememberNavigationEventState(
        currentInfo = MifosPasscodeCurrentInfo,
    )

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = true,
        onBackCancelled = { },
        onBackCompleted = {
            if (allowBackNavigation) {
                onBackPress()
            }
        },
    )

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is MifosPasscodeEvent.NavigateForResult -> {
                    when (event.result) {
                        PasscodeResult.Verified -> onAuthenticationSuccess()
                        PasscodeResult.Created -> onPasscodeCreation()
                        PasscodeResult.Changed -> onPasscodeChanged()
                        PasscodeResult.Forgotten -> navigateToLogin()
                        PasscodeResult.Rejected -> onAuthenticationFailed()
                    }
                }
            }
        }
    }

    DisposableEffect(lifeCycleOwner, allowBiometricAuth, biometricErrorMessages, biometricPromptStrings) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.trySendAction(MifosPasscodeAction.OnStart)
                }

                Lifecycle.Event.ON_RESUME -> {
                    viewModel.trySendAction(
                        MifosPasscodeAction.OnResume(
                            systemAuthProvider = systemAuthProvider,
                            allowBiometricAuth = allowBiometricAuth,
                            errorMessages = biometricErrorMessages,
                            promptStrings = biometricPromptStrings,
                        ),
                    )
                }
                else -> {}
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose { lifeCycleOwner.lifecycle.removeObserver(observer) }
    }

    MifosDialogBox(
        title = stringResource(Res.string.feature_passcode_error),
        showDialogState = state.dialogState != null,
        confirmButtonText = stringResource(Res.string.feature_passcode_ok),
        dismissButtonText = null,
        onConfirm = {
            when (state.dialogState) {
                is PasscodeDialogState.UserNotRegistered -> {
                    viewModel.trySendAction(MifosPasscodeAction.ClickConfirmOnNotRegisteredDialog)
                }
                else -> viewModel.trySendAction(MifosPasscodeAction.DismissDialog)
            }
        },
        onDismiss = {
            viewModel.trySendAction(MifosPasscodeAction.DismissDialog)
        },
        message = when (val dialogState = state.dialogState) {
            is PasscodeDialogState.Error -> dialogState.message
            is PasscodeDialogState.UserNotRegistered -> dialogState.message
            null -> null
        },
    )

    PasscodeScreen(
        passcodeManager = passcodeManager,
        onResult = { result ->
            if (result == PasscodeResult.Forgotten) {
                viewModel.trySendAction(
                    MifosPasscodeAction.ForgetPasscode(
                        systemAuthProvider = systemAuthProvider,
                    ),
                )
            } else {
                viewModel.trySendAction(MifosPasscodeAction.HandlePasscodeResult(result = result))
            }
        },
        appearanceConfig = PasscodeAppearanceConfig(
            backgroundColor = KptTheme.colorScheme.background,
            headerTextStyle = KptTheme.typography.headlineMedium,
        ),
        logoConfig = PasscodeLogoConfig(),
        dotConfig = PasscodeDotConfig(
            dotColor = KptTheme.colorScheme.primary,
            inactiveDotColor = KptTheme.colorScheme.onBackground,
            visiblePasscodeTextStyle = KptTheme.typography.headlineSmall,
        ),
        keyConfig = PasscodeKeyConfig(
            shouldShuffleKeys = true,
            keyTextStyle = null,
            keyColor = KptTheme.colorScheme.primary,
            keyShape = CircleShape,
            keyElevation = null,
            keyContainerColor = KptTheme.colorScheme.surface,
            keySize = 60.dp,
        ),
        buttonConfig = PasscodeButtonConfig(
            forgotButtonTextStyle = KptTheme.typography.labelLarge,
        ),
        switchConfig = PasscodeSwitchConfig(
            switchTabColor = KptTheme.colorScheme.primary,
            switchTrackColor = KptTheme.colorScheme.surfaceContainerHighest,
            switchUnselectedTextColor = KptTheme.colorScheme.onSurface,
            switchSelectedTextColor = KptTheme.colorScheme.surface,
            switchTextStyle = null,
        ),
        dialogConfig = PasscodeDialogConfig(
            dialogContainerColor = KptTheme.colorScheme.surface,
            dialogTitleColor = KptTheme.colorScheme.onSurface,
            dialogButtonTextColor = KptTheme.colorScheme.onSurface,
            dialogShape = null,
        ),
        isExternalAuthEnabled = allowBiometricAuth && isRegistered,
        externalAuthButton = if (allowBiometricAuth) {
            { modifier ->
                BiometricsKey(
                    modifier = modifier,
                    systemAvailableAuthOption = systemAvailableAuthOption,
                    onClick = {
                        viewModel.trySendAction(
                            MifosPasscodeAction.OnAuthenticatorClick(
                                systemAuthProvider = systemAuthProvider,
                                errorMessages = biometricErrorMessages,
                                promptStrings = biometricPromptStrings,
                            ),
                        )
                    },
                )
            }
        } else {
            null
        },
    )
}
