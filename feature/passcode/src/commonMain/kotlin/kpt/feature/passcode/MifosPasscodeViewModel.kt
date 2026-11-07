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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.data.repository.AppLockRepository
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.passcode.generated.resources.Res
import kpt.feature.passcode.generated.resources.feature_passcode_user_not_registered_error_message
import org.jetbrains.compose.resources.getString
import org.mifos.authenticator.biometrics.platformAuthenticator.AuthenticationResult
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticatorStatus
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeResult
import org.mifos.authenticator.passcode.PasscodeStep
/**
 * ViewModel for [MifosPasscode]. Owns the glue between:
 *  - [PasscodeManager] (passcode library) — provides step/state and emits
 *    [PasscodeResult]s on creation, verify, change, forget.
 *  - `PlatformAuthenticationProvider` (biometrics library) — provides
 *    `isRegistered` + biometric-prompt invocation. **Not** injected here;
 *    it's composition-scoped and threaded in via
 *    [MifosPasscodeAction.OnResume], [MifosPasscodeAction.OnAuthenticatorClick],
 *    and [MifosPasscodeAction.ForgetPasscode] so the VM never holds a
 *    reference to a composition-scoped object.
 *  - [AppLockRepository] — the app-wide lock flag consulted by
 *    `MifosPayApp`'s background-resume gate.
 *
 * Lifecycle:
 *  - `OnStart`: if the screen is in `Enter` step, re-lock the app.
 *  - `OnResume`: if biometrics are allowed, hardware-available, and
 *    registered, and the screen is in `Enter` step, auto-trigger the
 *    biometric prompt.
 *  - `HandlePasscodeResult(Verified)`: unlock the app.
 *  - `ForgetPasscode`: clear app-lock + biometric registration via
 *    `provider.unregister()`, then emit `NavigateForResult(Forgotten)`
 *    after unregistration completes.
 */
class MifosPasscodeViewModel(
    private val passcodeManager: PasscodeManager,
    private val appLockRepository: AppLockRepository,
) : BaseViewModel<MifosPasscodeState, MifosPasscodeEvent, MifosPasscodeAction>(
    initialState = MifosPasscodeState(),
) {

    override fun handleAction(action: MifosPasscodeAction) {
        when (action) {
            is MifosPasscodeAction.OnStart -> {
                if (passcodeManager.state.value.passcodeStep == PasscodeStep.Enter) {
                    appLockRepository.lockApp()
                }
            }

            is MifosPasscodeAction.OnResume -> {
                handleResume(
                    systemAuthProvider = action.systemAuthProvider,
                    allowBiometricAuth = action.allowBiometricAuth,
                    errorMessages = action.errorMessages,
                    promptStrings = action.promptStrings,
                )
            }

            is MifosPasscodeAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is MifosPasscodeAction.HandlePasscodeResult -> {
                when (action.result) {
                    PasscodeResult.Created, PasscodeResult.Verified -> appLockRepository.unlockApp()
                    PasscodeResult.Changed,
                    PasscodeResult.Rejected,
                    PasscodeResult.Forgotten,
                    -> { }
                }
                sendEvent(MifosPasscodeEvent.NavigateForResult(action.result))
            }

            is MifosPasscodeAction.ForgetPasscode -> {
                appLockRepository.deleteLock()
                viewModelScope.launch {
                    action.systemAuthProvider.unregister()
                    sendEvent(MifosPasscodeEvent.NavigateForResult(PasscodeResult.Forgotten))
                }
            }

            is MifosPasscodeAction.OnAuthenticatorClick -> {
                authenticateWithBiometrics(
                    systemAuthProvider = action.systemAuthProvider,
                    errorMessages = action.errorMessages,
                    promptStrings = action.promptStrings,
                )
            }

            MifosPasscodeAction.ClickConfirmOnNotRegisteredDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }
        }
    }

    /**
     * Auto-triggers biometric authentication on resume — but only when **all
     * four** gates agree:
     *  - [allowBiometricAuth] is `true` (caller's UI-layer guard).
     *  - The authenticator status contains [PlatformAuthenticatorStatus.BIOMETRICS_SET].
     *  - The passcode screen is in [PasscodeStep.Enter] (not Create / Confirm
     *    / ChangeVerify — biometric must not be allowed to bypass change-flow).
     *  - The provider's `isRegistered` flow is `true`.
     *
     * Relaxing any one would silently re-enable biometric auth in flows where
     * it should be suppressed.
     */
    private fun handleResume(
        systemAuthProvider: PlatformAuthenticationProvider,
        allowBiometricAuth: Boolean,
        errorMessages: BiometricErrorMessages,
        promptStrings: BiometricPromptStrings,
    ) {
        if (!allowBiometricAuth) return
        val biometricsStatus = systemAuthProvider.authenticatorStatus.value
        if (
            biometricsStatus.contains(PlatformAuthenticatorStatus.BIOMETRICS_SET) &&
            passcodeManager.state.value.passcodeStep == PasscodeStep.Enter &&
            systemAuthProvider.isRegistered.value
        ) {
            authenticateWithBiometrics(systemAuthProvider, errorMessages, promptStrings)
        }
    }

    /**
     * Runs the biometric prompt and converts its outcome into UI state +
     * navigation events.
     *
     * Branch behaviour:
     *  - [AuthenticationResult.Success] — **re-checks** `passcodeStep == Enter`
     *    before unlocking. Guards against the step transitioning between
     *    when `onAuthenticatorClick` was dispatched and when it returns
     *    (e.g., the auto-resume kicks off, the user navigates into
     *    change-passcode while the prompt is on screen, prompt succeeds while
     *    step is now `ChangeVerify`). If still `Enter`, unlocks the app and
     *    re-emits as [PasscodeResult.Verified] so biometric and passcode
     *    success converge on the same composable callback. Otherwise the
     *    result is silently dropped — biometric auth must not satisfy a
     *    change-passcode / disable-biometrics flow.
     *  - [AuthenticationResult.UserNotRegistered] — the library has already
     *    cleared the stored blob and flipped `isRegistered` to false; surface
     *    the "re-setup" prompt so the user re-enrols from settings.
     *  - [AuthenticationResult.Error] — map the structured `BiometricError`
     *    payload to localized text via the pre-resolved `errorMessages`
     *    bundle (sample-mirroring pattern; library deliberately ships no
     *    error copy in v2.3.0-beta), then render the error dialog.
     *  - [AuthenticationResult.UserCancelled] — silent no-op.
     */
    private fun authenticateWithBiometrics(
        systemAuthProvider: PlatformAuthenticationProvider,
        errorMessages: BiometricErrorMessages,
        promptStrings: BiometricPromptStrings,
    ) {
        viewModelScope.launch {
            val result = systemAuthProvider.onAuthenticatorClick(
                title = promptStrings.title,
                subtitle = promptStrings.subtitle,
                description = promptStrings.description,
                negativeButtonText = promptStrings.negativeButtonText,
            )
            when (result) {
                is AuthenticationResult.Error -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = PasscodeDialogState.Error(errorMessages.localize(result.error)),
                        )
                    }
                }

                AuthenticationResult.Success -> {
                    if (passcodeManager.state.value.passcodeStep == PasscodeStep.Enter) {
                        appLockRepository.unlockApp()
                        sendEvent(MifosPasscodeEvent.NavigateForResult(PasscodeResult.Verified))
                    }
                }

                AuthenticationResult.UserNotRegistered -> {
                    val message =
                        getString(Res.string.feature_passcode_user_not_registered_error_message)
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = PasscodeDialogState.UserNotRegistered(message = message),
                        )
                    }
                }

                AuthenticationResult.UserCancelled -> {}
            }
        }
    }
}

/**
 * UI state for [MifosPasscodeViewModel].
 *
 * @property dialogState Non-null when an error / not-registered dialog should
 *           be shown. Cleared via [MifosPasscodeAction.DismissDialog] or
 *           [MifosPasscodeAction.ClickConfirmOnNotRegisteredDialog].
 */
data class MifosPasscodeState(
    val dialogState: PasscodeDialogState? = null,
)

/** Dialog variants surfaced by [MifosPasscodeViewModel]. */
sealed interface PasscodeDialogState {
    /**
     * Generic biometric error with a platform-provided message.
     *
     * @property message Platform-provided error message to display.
     */
    data class Error(val message: String) : PasscodeDialogState

    /**
     * The platform reported [AuthenticationResult.UserNotRegistered]; the
     * biometrics library has already wiped the invalid stored blob.
     * Confirming the dialog is terminal — the user must re-enrol from
     * settings.
     *
     * @property message Message to display in the dialog.
     */
    data class UserNotRegistered(val message: String) : PasscodeDialogState
}

/** Actions dispatched to [MifosPasscodeViewModel]. */
sealed interface MifosPasscodeAction {
    /** Fired on `Lifecycle.Event.ON_START`. Re-locks the app when in `Enter` step. */
    data object OnStart : MifosPasscodeAction

    /**
     * Fired on `Lifecycle.Event.ON_RESUME`. Carries the composition-scoped
     * provider + the caller's biometric-bypass guard + the localized
     * error/prompt strings so the VM can drive the auto-auth path inside
     * `viewModelScope.launch` (see `handleResume`). The string holders are
     * resolved once at the composable layer via
     * `rememberBiometricErrorMessages()` and `rememberBiometricPromptStrings()`.
     *
     * @property systemAuthProvider Composition-scoped platform authentication provider.
     * @property allowBiometricAuth Whether automatic biometric authentication is allowed on resume.
     * @property errorMessages Localized biometric error strings.
     * @property promptStrings Localized OS prompt strings.
     */
    data class OnResume(
        val systemAuthProvider: PlatformAuthenticationProvider,
        val allowBiometricAuth: Boolean,
        val errorMessages: BiometricErrorMessages,
        val promptStrings: BiometricPromptStrings,
    ) : MifosPasscodeAction

    /** User dismissed an error/not-registered dialog via system back or outside-tap. */
    data object DismissDialog : MifosPasscodeAction

    /**
     * Non-`Forgotten` [PasscodeResult] from the library. On `Verified` the VM
     * unlocks the app; other cases just forward to the screen for routing.
     *
     * @property result Non-forgotten passcode result from the library.
     */
    data class HandlePasscodeResult(val result: PasscodeResult) : MifosPasscodeAction

    /**
     * Dispatched for the `Forgotten` case only. Split from
     * [HandlePasscodeResult] so the provider reference (needed for
     * `unregister()`) is only carried on the one action that uses it.
     *
     * Handler emits `NavigateForResult(Forgotten)` only after `unregister()`
     * completes, so any downstream consumer of `isRegistered` sees the
     * post-unregistration state.
     *
     * @property systemAuthProvider Provider used to unregister the stored passcode.
     */
    data class ForgetPasscode(
        val systemAuthProvider: PlatformAuthenticationProvider,
    ) : MifosPasscodeAction

    /**
     * User tapped the biometric button. Carries the provider + the localized
     * error/prompt strings so the VM can invoke `onAuthenticatorClick()` with
     * the v2.3.0-beta caller-supplied prompt strings and map the structured
     * `BiometricError` payload back to localized text.
     *
     * @property systemAuthProvider Composition-scoped platform authentication provider.
     * @property errorMessages Localized biometric error strings.
     * @property promptStrings Localized OS prompt strings.
     */
    data class OnAuthenticatorClick(
        val systemAuthProvider: PlatformAuthenticationProvider,
        val errorMessages: BiometricErrorMessages,
        val promptStrings: BiometricPromptStrings,
    ) : MifosPasscodeAction

    /** User tapped OK on the [PasscodeDialogState.UserNotRegistered] dialog. */
    data object ClickConfirmOnNotRegisteredDialog : MifosPasscodeAction
}

/** One-shot events emitted by [MifosPasscodeViewModel] to the screen. */
sealed interface MifosPasscodeEvent {
    /**
     * Navigate based on the resulting [PasscodeResult]. Biometric success is
     * re-emitted as [PasscodeResult.Verified] here so the composable's single
     * result-switch handles both paths.
     *
     * @property result Passcode result that determines where to navigate.
     */
    data class NavigateForResult(val result: PasscodeResult) : MifosPasscodeEvent
}
