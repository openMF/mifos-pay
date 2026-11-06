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
import kpt.core.model.user.UserInfo
import kpt.core.network.config.InstanceConfigManager
import kpt.core.ui.utils.BaseViewModel
import mifos_pay.feature.auth.generated.resources.Res
import mifos_pay.feature.auth.generated.resources.feature_auth_error_login_failed
import org.jetbrains.compose.resources.getString
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val instanceConfigManager: InstanceConfigManager,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LoginState, LoginEvent, LoginAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE)
        ?: LoginState(dialogState = null),
) {

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
                        sendEvent(LoginEvent.NavigateToMifosPasscodeScreen)
                        submitLogin.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = getString(Res.string.feature_auth_error_login_failed)
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = LoginState.DialogState.Error(message),
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
    sealed class DialogState {
        data class Error(val message: String) : DialogState()
        data object Loading : DialogState()
    }
}

sealed class LoginEvent {
    data object NavigateBack : LoginEvent()
    data object NavigateToSignup : LoginEvent()
    data object NavigateToMifosPasscodeScreen : LoginEvent()
    data class ShowToast(val message: String) : LoginEvent()
}

sealed class LoginAction {
    data class UsernameChanged(val username: String) : LoginAction()
    data class PasswordChanged(val password: String) : LoginAction()
    data object TogglePasswordVisibility : LoginAction()
    data object ErrorDialogDismiss : LoginAction()
    data object LoginClicked : LoginAction()
    data object SignupClicked : LoginAction()
}
