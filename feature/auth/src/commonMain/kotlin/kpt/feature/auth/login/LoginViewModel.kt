/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.domain.LoginUseCase
import kpt.core.model.fork.user.UserInfo
import kpt.core.network.main.config.InstanceConfigManager
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.feature.auth.generated.resources.Res
import kpt.feature.auth.generated.resources.feature_auth_error_login_failed
import org.jetbrains.compose.resources.StringResource
/** View model for the login screen; authenticates with username and password and drives the status dialog. */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val instanceConfigManager: InstanceConfigManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoginState, LoginEvent, LoginAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
        ?: LoginState(dialogState = null),
) {

    /** Holds the saved-state key used to persist the login state. */
    companion object {
        private const val KEY_STATE = "state"
    }

    // Template idiom (core-base/store): the login (authenticate) write goes through a
    // SubmitHandler instead of a hand-folded DataState result action. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it below to drive this screen's
    // existing loading/error dialog + passcode navigation, so the Screen is unchanged.
    private val submitLogin = viewModelScope.submitHandler<UserInfo>()

    init {
        submitLogin.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = LoginState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(LoginEvent.NavigateToPasscode)
                        submitLogin.reset()
                    }

                    is SubmitState.Failed -> {
                        // The resource, not its text: the screen resolves it, so the state stays
                        // synchronous and testable on every target.
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = LoginState.DialogState.Error(
                                    Res.string.feature_auth_error_login_failed,
                                ),
                            )
                        }
                        submitLogin.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        // Observe selected instance changes (uses default if none selected)
        instanceConfigManager.selectedInstance
            .onEach { instance ->
                val currentInstance = instance ?: instanceConfigManager.getCurrentInstance()
                mutableStateFlow.update {
                    it.copy(
                        selectedInstanceLabel = currentInstance.label,
                        selectedInstanceEndpoint = currentInstance.endpoint,
                    )
                }
            }
            .launchIn(viewModelScope)

        savedStateHandle.get<String>("username")?.let {
            trySendAction(LoginAction.UsernameChanged(it))
        }
    }

    override fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.UsernameChanged -> {
                mutableStateFlow.update {
                    it.copy(username = action.username)
                }
            }

            is LoginAction.PasswordChanged -> {
                mutableStateFlow.update {
                    it.copy(password = action.password)
                }
            }

            is LoginAction.TogglePasswordVisibility -> {
                mutableStateFlow.update {
                    it.copy(isPasswordVisible = !it.isPasswordVisible)
                }
            }

            is LoginAction.LoginClicked -> {
                loginUser(state.username, state.password)
            }

            is LoginAction.SignupClicked -> {
                sendEvent(LoginEvent.NavigateToSignup)
            }

            is LoginAction.ErrorDialogDismiss -> {
                mutableStateFlow.update { it.copy(dialogState = null) }
            }
        }
    }

    private fun loginUser(
        username: String,
        password: String,
    ) {
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed in
        // `init`. The use-case returns the UserInfo on success and throws on failure, so
        // the handler reports Submitted/Failed directly.
        submitLogin.submit {
            loginUseCase(username, password)
        }
    }
}

/**
 * UI state of the login screen.
 *
 * @property username Username typed by the user.
 * @property password Password typed by the user (not persisted).
 * @property isPasswordVisible Whether the password field shows plain text.
 * @property dialogState Loading or error dialog currently shown, or null.
 * @property selectedInstanceLabel Display label of the selected backend instance.
 * @property selectedInstanceEndpoint Endpoint URL of the selected backend instance.
 */
@Serializable
data class LoginState(
    val username: String = "",
    @Transient
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    @Transient
    val dialogState: DialogState? = null,
    @Transient
    val selectedInstanceLabel: String? = null,
    @Transient
    val selectedInstanceEndpoint: String? = null,
) {
    /** Dialog shown over the login screen. */
    sealed class DialogState {
        /**
         * Error dialog.
         *
         * @property message The error text, resolved by the screen.
         */
        data class Error(val message: StringResource) : DialogState()

        /** Blocking progress dialog shown while the login request is in flight. */
        data object Loading : DialogState()
    }
}

/** One-off events emitted by the login view model. */
sealed class LoginEvent {
    /** Navigate back from the login screen. */
    data object NavigateBack : LoginEvent()

    /** Navigate to the signup flow. */
    data object NavigateToSignup : LoginEvent()

    /** Navigate to the passcode screen after a successful login. */
    data object NavigateToPasscode : LoginEvent()

    /**
     * Show a toast.
     *
     * @property message Text of the toast.
     */
    data class ShowToast(val message: String) : LoginEvent()
}

/** User actions handled by the login view model. */
sealed class LoginAction {
    /**
     * The username field changed.
     *
     * @property username New username value.
     */
    data class UsernameChanged(val username: String) : LoginAction()

    /**
     * The password field changed.
     *
     * @property password New password value.
     */
    data class PasswordChanged(val password: String) : LoginAction()

    /** The user toggled password visibility. */
    data object TogglePasswordVisibility : LoginAction()

    /** The user dismissed the error dialog. */
    data object ErrorDialogDismiss : LoginAction()

    /** The user tapped the login button. */
    data object LoginClicked : LoginAction()

    /** The user tapped the signup link. */
    data object SignupClicked : LoginAction()
}
