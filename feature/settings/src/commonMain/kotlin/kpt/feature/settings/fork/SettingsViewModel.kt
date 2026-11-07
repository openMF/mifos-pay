/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.fork

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.data.repository.SavingsAccountRepository
import kpt.core.data.repository.UserVerificationRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.client.Client
import kpt.core.model.user.Language
import kpt.core.model.user.fork.LanguageConfig
import kpt.core.model.user.toLanguage
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.passcode.BiometricErrorMessages
import kpt.feature.passcode.BiometricPromptStrings
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_alert_disable_account
import kpt.feature.settings.generated.resources.feature_settings_alert_disable_account_desc
import kpt.feature.settings.generated.resources.feature_settings_biometrics_not_available
import kpt.feature.settings.generated.resources.feature_settings_biometrics_not_set
import kpt.feature.settings.generated.resources.feature_settings_empty
import kpt.feature.settings.generated.resources.feature_settings_log_out_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.mifos.authenticator.biometrics.platformAuthenticator.PlatformAuthenticationProvider
import org.mifos.authenticator.biometrics.platformAuthenticator.RegistrationResult
import org.mifos.authenticator.passcode.PasscodeManager
/**
 * `SavedStateHandle` key written by `internalMifosPasscodeScreen` and read by
 * [SettingsScreen]'s `LaunchedEffect` for the disable-biometrics
 * passcode-verification round trip.
 *
 * Flow: tap "Disable" → navigate to internal passcode with this key →
 * passcode screen writes `true` (verified) or `false` (cancelled) under this
 * key on the previous back-stack entry → [SettingsScreen] observes and
 * dispatches [SettingsAction.DisableBiometricsResult] → VM consumes the
 * 30-second [UserVerificationRepository] token and, if still valid, calls
 * `authProvider.unregister()`.
 */
const val DISABLE_BIOMETRICS_VERIFICATION_KEY = "org.mifospay.mifos.authentication.verification.key"

/**
 * ViewModel for [SettingsScreen]. Three concerns relevant to passcode /
 * biometrics:
 *  - **Change passcode** ([SettingsAction.ChangePasscode]): puts the
 *    library's `PasscodeManager` into `ChangeVerify` step then emits a
 *    nav event so the screen pushes the internal passcode screen.
 *  - **Toggle biometrics** ([SettingsAction.ToggleSystemAuth]): enable path
 *    calls `provider.registerUser(...)` directly; disable path emits a nav
 *    event so the screen routes to the internal passcode screen for
 *    verification — the result returns via [SettingsAction.DisableBiometricsResult].
 *  - **Disable biometrics result** ([SettingsAction.DisableBiometricsResult]):
 *    on success + a still-valid [UserVerificationRepository] token, calls
 *    `authProvider.unregister()`.
 *
 * Biometric registration state itself is **not** kept here — the screen
 * reads `authProvider.isRegistered` from the composition local and passes it
 * down. Keeping a single source of truth avoids drift between this VM and
 * the library StateFlow.
 */
class SettingsViewModel(
    private val userPreferencesRepository: ForkPreferencesRepository,
    private val repository: SavingsAccountRepository,
    private val passcodeManager: PasscodeManager,
    private val userVerificationRepository: UserVerificationRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsAction>(
    initialState = run {
        /** Client taken from the stored user preferences; required to build the initial state. */
        val client = requireNotNull(userPreferencesRepository.client.value)
        SettingsState(
            client = client,
            dialogState = null,
            language = userPreferencesRepository.language.value.takeIf {
                !it.localeName.isNullOrBlank()
            } ?: LanguageConfig.DEFAULT.toLanguage(),
        )
    },
) {

    // Template idiom (core-base/store): the disable-account one-shot write goes
    // through a SubmitHandler instead of a hand-folded DataState result action.
    // The handler owns the Submitting/Submitted/Failed lifecycle; we observe it
    // to drive this screen's existing Loading/Error dialog + logout navigation,
    // so the Screen is unchanged.
    private val submitDisableAccount = viewModelScope.submitHandler<Unit>()

    init {
        submitDisableAccount.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update { it.copy(dialogState = DialogState.Loading) }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(SettingsEvent.OnNavigateToLogout)
                        submitDisableAccount.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message ?: "Error"
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogState.Error(message))
                        }
                        submitDisableAccount.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.NavigateBack -> {
                sendEvent(SettingsEvent.OnNavigateBack)
            }

            is SettingsAction.NavigateToFaqScreen -> {
                sendEvent(SettingsEvent.OnNavigateToFaqScreen)
            }

            is SettingsAction.NavigateToProfile -> {
                sendEvent(SettingsEvent.OnNavigateToProfile)
            }

            is SettingsAction.ChangePasscode -> {
                passcodeManager.changePasscode()
                sendEvent(SettingsEvent.NavigateToPasscodeScreen)
            }

            is SettingsAction.ChangePassword -> {
                sendEvent(SettingsEvent.OnNavigateToEditPasswordScreen)
            }

            is SettingsAction.NavigateToNotificationSettings -> {
                sendEvent(SettingsEvent.OnNavigateToNotificationScreen)
            }

            is SettingsAction.ShowLanguageSelection -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = DialogState.LanguageSelection,
                        pendingLanguage = it.language,
                    )
                }
            }

            is SettingsAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = null,
                        pendingLanguage = null,
                    )
                }
            }

            is SettingsAction.DisableAccount -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = DialogState.DisableAccount(
                            title = Res.string.feature_settings_alert_disable_account,
                            message = Res.string.feature_settings_alert_disable_account_desc,
                            onConfirm = {
                                trySendAction(SettingsAction.Internal.DisableAccount)
                            },
                        ),
                    )
                }
            }

            is SettingsAction.Logout -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = DialogState.Logout(
                            title = Res.string.feature_settings_log_out_title,
                            message = Res.string.feature_settings_empty,
                            onConfirm = {
                                trySendAction(SettingsAction.DismissDialog)
                                sendEvent(SettingsEvent.OnNavigateToLogout)
                            },
                        ),
                    )
                }
            }

            is SettingsAction.Internal.DisableAccount -> handleDisableAccount()

            is SettingsAction.ToggleSystemAuth -> {
                if (action.isCurrentlyRegistered) {
                    sendEvent(SettingsEvent.NavigateToPasscodeScreen)
                } else {
                    handleBiometricsAuthRegistration(
                        systemAuthProvider = action.systemAuthProvider,
                        errorMessages = action.errorMessages,
                        promptStrings = action.promptStrings,
                    )
                }
            }

            is SettingsAction.DisableBiometricsResult -> {
                handleDisableBiometricsResult(action.success, action.systemAuthProvider)
            }

            SettingsAction.BiometricsNotAvailable -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = DialogState.Error("Biometrics not enabled in settings"),
                    )
                }
            }
        }
    }

    private fun handleDisableBiometricsResult(
        authenticationSuccess: Boolean,
        authProvider: PlatformAuthenticationProvider,
    ) {
        viewModelScope.launch {
            if (authenticationSuccess && userVerificationRepository.consumeVerification()) {
                authProvider.unregister()
            }
            savedStateHandle.remove<Boolean?>(DISABLE_BIOMETRICS_VERIFICATION_KEY)
        }
    }

    private fun handleBiometricsAuthRegistration(
        systemAuthProvider: PlatformAuthenticationProvider,
        errorMessages: BiometricErrorMessages,
        promptStrings: BiometricPromptStrings,
    ) {
        viewModelScope.launch {
            val result = systemAuthProvider.registerUser(
                userName = mutableStateFlow.value.client.id.toString(),
                emailId = mutableStateFlow.value.client.emailAddress,
                displayName = mutableStateFlow.value.client.displayName,
                title = promptStrings.title,
                subtitle = promptStrings.subtitle,
                description = promptStrings.description,
                negativeButtonText = promptStrings.negativeButtonText,
            )

            when (result) {
                is RegistrationResult.Success -> { }
                RegistrationResult.PlatformAuthenticatorNotSet -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = DialogState.Error(
                                message = getString(Res.string.feature_settings_biometrics_not_set),
                            ),
                        )
                    }
                }
                RegistrationResult.PlatformAuthenticatorNotAvailable -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = DialogState.Error(
                                message = getString(Res.string.feature_settings_biometrics_not_available),
                            ),
                        )
                    }
                }
                is RegistrationResult.Error -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = DialogState.Error(message = errorMessages.localize(result.error)),
                        )
                    }
                }
                RegistrationResult.UserCancelled -> { }
            }
        }
    }

    private fun handleDisableAccount() {
        // TODO:: this shouldn't work, we need account id to block account
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository write returns Unit and throws on failure; the
        // handler maps that to Submitted/Failed.
        submitDisableAccount.submit {
            repository.blockAccount(state.client.id)
        }
    }

    /** Stages [language] as the pending choice; nothing is applied until [updateAppLocale]. */
    fun updateLanguage(language: LanguageConfig) {
        viewModelScope.launch {
            mutableStateFlow.update {
                it.copy(
                    pendingLanguage = language.toLanguage(),
                )
            }
        }
    }

    /** Saves the pending language (or the current one) and asks the screen to apply its locale. */
    fun updateAppLocale() {
        viewModelScope.launch {
            val selectedLanguage = state.pendingLanguage ?: state.language

            userPreferencesRepository.setLanguage(selectedLanguage)

            mutableStateFlow.update {
                it.copy(
                    language = selectedLanguage,
                    pendingLanguage = null,
                )
            }

            sendEvent(
                SettingsEvent.ChangeLocale(
                    selectedLanguage.localeName ?: "en",
                ),
            )
        }
    }
}

/**
 * UI state of the settings screen.
 * @property client Current client.
 * @property dialogState Dialog currently shown, or null.
 * @property language Language currently applied.
 * @property pendingLanguage Language chosen in the dialog but not yet confirmed, or null.
 */
data class SettingsState(
    val client: Client,
    val dialogState: DialogState? = null,
    val language: Language,
    val pendingLanguage: Language? = null,
)

/** Dialogs the settings screen can show. */
sealed interface DialogState {
    /** Progress dialog. */
    data object Loading : DialogState

    /**
     * Error dialog.
     * @property message Error text.
     */
    data class Error(val message: String) : DialogState

    /**
     * Confirmation dialog for disabling the account.
     * @property title Dialog title.
     * @property message Dialog message.
     * @property onConfirm Called when the user confirms.
     */
    data class DisableAccount(
        val title: StringResource,
        val message: StringResource,
        val onConfirm: () -> Unit,
    ) : DialogState

    /**
     * Confirmation dialog for logging out.
     * @property title Dialog title.
     * @property message Dialog message.
     * @property onConfirm Called when the user confirms.
     */
    data class Logout(
        val title: StringResource,
        val message: StringResource,
        val onConfirm: () -> Unit,
    ) : DialogState

    /** Language selection dialog. */
    data object LanguageSelection : DialogState
}

/** One-shot events emitted by the settings view model. */
sealed interface SettingsEvent {
    /** Navigate back. */
    data object OnNavigateBack : SettingsEvent

    /** Navigate to the edit-password screen. */
    data object OnNavigateToEditPasswordScreen : SettingsEvent

    /** Navigate to the passcode screen. */
    data object NavigateToPasscodeScreen : SettingsEvent

    /** Navigate to log out. */
    data object OnNavigateToLogout : SettingsEvent

    /** Navigate to the FAQ screen. */
    data object OnNavigateToFaqScreen : SettingsEvent

    /** Navigate to the profile screen. */
    data object OnNavigateToProfile : SettingsEvent

    /** Navigate to the notification screen. */
    data object OnNavigateToNotificationScreen : SettingsEvent

    /**
     * Apply a new app locale.
     * @property locale Locale name such as "en".
     */
    data class ChangeLocale(val locale: String) : SettingsEvent
}

/** Actions handled by the settings view model. */
sealed interface SettingsAction {
    /** Navigate back. */
    data object NavigateBack : SettingsAction

    /** Ask to log out. */
    data object Logout : SettingsAction

    /** Ask to disable the account. */
    data object DisableAccount : SettingsAction

    /** Biometrics are not available on this device. */
    data object BiometricsNotAvailable : SettingsAction

    /** Show the language selection dialog. */
    data object ShowLanguageSelection : SettingsAction

    /**
     * Toggle biometric registration.
     *
     * Enable path (`isCurrentlyRegistered = false`): calls
     * `systemAuthProvider.registerUser(...)` immediately, using
     * [promptStrings] for the v2.3.0-beta caller-supplied OS-prompt strings
     * and [errorMessages] to map any `RegistrationResult.Error.error` to
     * localized text. The library persists the registration blob on success.
     *
     * Disable path (`isCurrentlyRegistered = true`): emits
     * [SettingsEvent.NavigateToPasscodeScreen] so the screen routes to the
     * internal passcode screen with [DISABLE_BIOMETRICS_VERIFICATION_KEY];
     * the verification result returns as [DisableBiometricsResult]. The
     * string holders are unused on this path but always carried so the
     * action shape stays stable.
     *
     * @property systemAuthProvider Platform authentication provider to register or unregister.
     * @property isCurrentlyRegistered Whether biometrics are currently registered.
     * @property errorMessages Localized messages used to map registration errors.
     * @property promptStrings Localized strings for the OS biometric prompt.
     */
    data class ToggleSystemAuth(
        val systemAuthProvider: PlatformAuthenticationProvider,
        val isCurrentlyRegistered: Boolean,
        val errorMessages: BiometricErrorMessages,
        val promptStrings: BiometricPromptStrings,
    ) : SettingsAction

    /**
     * Result of the disable-biometrics passcode-verification round trip.
     * Dispatched by the screen's `LaunchedEffect` observing
     * [DISABLE_BIOMETRICS_VERIFICATION_KEY]. On `success = true` and a
     * still-valid [UserVerificationRepository] token (30 s window), calls
     * `systemAuthProvider.unregister()`. On expiry or `success = false`,
     * just clears the savedStateHandle key.
     *
     * @property success Whether the passcode verification succeeded.
     * @property systemAuthProvider Platform authentication provider to unregister when verification succeeded.
     */
    data class DisableBiometricsResult(
        val success: Boolean,
        val systemAuthProvider: PlatformAuthenticationProvider,
    ) : SettingsAction

    /**
     * User tapped "Change Passcode". The VM puts `PasscodeManager` into
     * `ChangeVerify` step (so the next passcode screen prompts for the
     * existing passcode first) then emits
     * [SettingsEvent.NavigateToPasscodeScreen].
     */
    data object ChangePasscode : SettingsAction

    /** Ask to change the password. */
    data object ChangePassword : SettingsAction

    /** Navigate to the FAQ screen. */
    data object NavigateToFaqScreen : SettingsAction

    /** Navigate to the profile screen. */
    data object NavigateToProfile : SettingsAction

    /** Navigate to the notification settings screen. */
    data object NavigateToNotificationSettings : SettingsAction

    /** Dismiss the currently shown dialog. */
    data object DismissDialog : SettingsAction

    /** Actions dispatched internally by the view model rather than by the UI. */
    sealed interface Internal : SettingsAction {
        /** Perform the account disable after confirmation. */
        data object DisableAccount : Internal
    }
}
