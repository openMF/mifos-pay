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
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.client.Client
import kpt.core.ui.fork.utils.AuthenticationUtils
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.passcode.generated.resources.Res
import kpt.feature.passcode.generated.resources.feature_passcode_biometrics_available
import kpt.feature.passcode.generated.resources.feature_passcode_biometrics_not_set
import org.jetbrains.compose.resources.getString
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult
/**
 * ViewModel for [BiometricSetupScreen].
 *
 * Drives the `PlatformAuthenticationProvider.registerUser` call against the
 * platform authenticator. On success, the biometrics library persists the
 * registration blob internally via [BiometricStorageAdapter] — there is no
 * adapter call here. Failure variants surface a dialog message via
 * [BiometricSetupScreenState.error].
 *
 * Identity seed is taken from [ForkPreferencesRepository.client] (snapshot
 * at construction time); falls back to [AuthenticationUtils.DEFAULT_USER_ID]
 * / `DEFAULT_USER_EMAIL` / `DEFAULT_DISPLAY_NAME` only when the snapshot
 * fields are absent.
 */
class BiometricSetupScreenViewmodel(
    userPreferencesRepository: ForkPreferencesRepository,
) : BaseViewModel<
    BiometricSetupScreenState,
    BiometricSetupScreenEvent,
    BiometricSetupScreenAction,
    >(BiometricSetupScreenState(client = userPreferencesRepository.client.value)) {
    override fun handleAction(action: BiometricSetupScreenAction) {
        when (action) {
            is BiometricSetupScreenAction.ClickSetupBiometric -> {
                registerUser(
                    platformAuthenticationProvider = action.platformAuthenticationProvider,
                    errorMessages = action.errorMessages,
                    promptStrings = action.promptStrings,
                )
            }
            BiometricSetupScreenAction.ClickSkipBiometric -> {
                sendEvent(BiometricSetupScreenEvent.OnSkipBiometricSetup)
            }

            BiometricSetupScreenAction.DismissErrorDialog -> {
                mutableStateFlow.update {
                    it.copy(error = null)
                }
            }
        }
    }

    private fun registerUser(
        platformAuthenticationProvider: PlatformAuthenticationProvider,
        errorMessages: BiometricErrorMessages,
        promptStrings: BiometricPromptStrings,
    ) {
        viewModelScope.launch {
            val client = mutableStateFlow.value.client
            val result = platformAuthenticationProvider.registerUser(
                userName = client?.id?.run { toString() } ?: AuthenticationUtils.DEFAULT_USER_ID,
                emailId = client?.emailAddress ?: AuthenticationUtils.DEFAULT_USER_EMAIL,
                displayName = client?.displayName ?: AuthenticationUtils.DEFAULT_DISPLAY_NAME,
                title = promptStrings.title,
                subtitle = promptStrings.subtitle,
                description = promptStrings.description,
                negativeButtonText = promptStrings.negativeButtonText,
            )

            when (result) {
                is RegistrationResult.Success -> {
                    sendEvent(BiometricSetupScreenEvent.OnBiometricSetupSuccess)
                }
                RegistrationResult.PlatformAuthenticatorNotSet -> {
                    mutableStateFlow.update {
                        it.copy(
                            error = getString(Res.string.feature_passcode_biometrics_not_set),
                        )
                    }
                }
                RegistrationResult.PlatformAuthenticatorNotAvailable -> {
                    mutableStateFlow.update {
                        it.copy(
                            error = getString(Res.string.feature_passcode_biometrics_available),
                        )
                    }
                }
                is RegistrationResult.Error -> {
                    mutableStateFlow.update {
                        it.copy(
                            error = errorMessages.localize(result.error),
                        )
                    }
                }
                RegistrationResult.UserCancelled -> { }
            }
        }
    }
}

/**
 * UI state for [BiometricSetupScreenViewmodel].
 *
 * @property client Snapshot of the signed-in client; used as the identity
 *           seed for `registerUser`. May be null in edge cases where the
 *           registration call has run before the client info was persisted —
 *           the VM falls back to `AuthenticationUtils` default constants.
 * @property error Non-null when a registration error should be shown via the
 *           screen's dialog. Cleared by [BiometricSetupScreenAction.DismissErrorDialog].
 */
data class BiometricSetupScreenState(
    val client: Client? = null,
    val error: String? = null,
)

/** Actions dispatched to [BiometricSetupScreenViewmodel]. */
sealed interface BiometricSetupScreenAction {
    /** Dismisses any currently-shown error dialog. */
    data object DismissErrorDialog : BiometricSetupScreenAction

    /** User tapped "Skip for now". Emits [BiometricSetupScreenEvent.OnSkipBiometricSetup]. */
    data object ClickSkipBiometric : BiometricSetupScreenAction

    /**
     * User tapped "Setup biometrics". Carries the composition-scoped
     * [PlatformAuthenticationProvider] (VMs cannot inject it directly) plus
     * pre-resolved biometric error/prompt strings, since the v2.3.0-beta
     * library requires caller-supplied OS-prompt copy on `registerUser(...)`
     * and a `BiometricError`-to-string mapping for `RegistrationResult.Error`.
     *
     * @property platformAuthenticationProvider Composition-scoped provider used to register the biometric.
     * @property errorMessages Localized biometric error strings.
     * @property promptStrings Localized OS prompt strings.
     */
    data class ClickSetupBiometric(
        val platformAuthenticationProvider: PlatformAuthenticationProvider,
        val errorMessages: BiometricErrorMessages,
        val promptStrings: BiometricPromptStrings,
    ) : BiometricSetupScreenAction
}

/** One-shot navigation events emitted by [BiometricSetupScreenViewmodel]. */
sealed interface BiometricSetupScreenEvent {
    /** User skipped setup. The route handler should proceed past this screen. */
    data object OnSkipBiometricSetup : BiometricSetupScreenEvent

    /** Registration succeeded; the library has already persisted the blob. */
    data object OnBiometricSetupSuccess : BiometricSetupScreenEvent
}
