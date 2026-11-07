/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.editpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.UiError
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.common.toUiError
import kpt.core.data.repository.UserRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.ui.DefaultErrorMessageProvider
import kpt.core.ui.PasswordStrengthState
import kpt.core.ui.utils.BaseViewModel
import kpt.core.ui.utils.fork.PasswordChecker
import kpt.core.ui.utils.fork.PasswordStrength
import kpt.core.ui.utils.fork.PasswordStrengthResult
import kpt.feature.editpassword.EditPasswordAction.Internal.ReceivePasswordStrengthResult
import kpt.feature.editpassword.EditPasswordDialog.Error
import kpt.feature.editpassword.generated.resources.Res
import kpt.feature.editpassword.generated.resources.feature_editpassword_error_empty_current_password
import kpt.feature.editpassword.generated.resources.feature_editpassword_error_password_min_length
import kpt.feature.editpassword.generated.resources.feature_editpassword_error_password_mismatch
import kpt.feature.editpassword.generated.resources.feature_editpassword_error_password_weak
import kpt.feature.editpassword.generated.resources.feature_editpassword_error_same_password
import kpt.feature.editpassword.generated.resources.feature_editpassword_password_changed_successfully
import org.jetbrains.compose.resources.StringResource
internal class EditPasswordViewModel(
    private val userRepository: UserRepository,
    userPreferencesRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<EditPasswordState, EditPasswordEvent, EditPasswordAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: EditPasswordState(),
) {

    companion object {
        private const val KEY_STATE = "state"
        private const val MIN_PASSWORD_LENGTH = 8
    }

    private val userInfo = userPreferencesRepository.userInfo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    private var passwordStrengthJob: Job = Job().apply { complete() }

    // Template idiom (core-base/store): one-shot writes go through a SubmitHandler
    // instead of a hand-folded DataState result action. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it to drive this screen's
    // existing dialog + toast/logout events, so the Screen is unchanged.
    private val submitPassword = viewModelScope.submitHandler<Unit>()

    init {
        submitPassword.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update { it.copy(dialogState = EditPasswordDialog.Loading) }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(
                            EditPasswordEvent.ShowToast(
                                Res.string.feature_editpassword_password_changed_successfully,
                            ),
                        )
                        sendEvent(EditPasswordEvent.OnLogoutUser)
                        submitPassword.reset()
                    }

                    is SubmitState.Failed -> {
                        val uiError = submitState.error.toUiError(DefaultErrorMessageProvider)
                        mutableStateFlow.update {
                            it.copy(dialogState = EditPasswordDialog.ApiError(uiError))
                        }
                        submitPassword.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach {
                savedStateHandle.setSerialized(key = KEY_STATE, value = it)
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: EditPasswordAction) {
        when (action) {
            is EditPasswordAction.CurrentPasswordChange -> {
                mutableStateFlow.update {
                    it.copy(currentPasswordInput = action.currentPassword)
                }
            }

            is EditPasswordAction.NewPasswordChange -> handleNewPasswordInput(action)

            is EditPasswordAction.ConfirmPasswordChange -> {
                mutableStateFlow.update {
                    it.copy(confirmPasswordInput = action.confirmPassword)
                }
            }

            EditPasswordAction.ErrorDialogDismiss -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is ReceivePasswordStrengthResult -> handlePasswordStrengthResult(action)

            EditPasswordAction.SubmitClick -> handleSubmitClick()

            is EditPasswordAction.NavigateBackClick -> {
                sendEvent(EditPasswordEvent.NavigateBack)
            }
        }
    }

    private fun handleNewPasswordInput(action: EditPasswordAction.NewPasswordChange) {
        // Update input:
        mutableStateFlow.update { it.copy(newPasswordInput = action.newPassword) }
        // Update password strength:
        passwordStrengthJob.cancel()
        if (action.newPassword.isEmpty()) {
            mutableStateFlow.update {
                it.copy(passwordStrengthState = PasswordStrengthState.NONE)
            }
        } else {
            passwordStrengthJob = viewModelScope.launch {
                val result = PasswordChecker.getPasswordStrengthResult(action.newPassword)
                trySendAction(ReceivePasswordStrengthResult(result))
            }
        }
    }

    private fun handlePasswordStrengthResult(action: ReceivePasswordStrengthResult) {
        when (val result = action.result) {
            is PasswordStrengthResult.Success -> {
                val updatedState = when (result.passwordStrength) {
                    PasswordStrength.LEVEL_0 -> PasswordStrengthState.WEAK_1
                    PasswordStrength.LEVEL_1 -> PasswordStrengthState.WEAK_2
                    PasswordStrength.LEVEL_2 -> PasswordStrengthState.WEAK_3
                    PasswordStrength.LEVEL_3 -> PasswordStrengthState.GOOD
                    PasswordStrength.LEVEL_4 -> PasswordStrengthState.STRONG
                    PasswordStrength.LEVEL_5 -> PasswordStrengthState.VERY_STRONG
                }
                mutableStateFlow.update { oldState ->
                    oldState.copy(passwordStrengthState = updatedState)
                }
            }

            is PasswordStrengthResult.Error -> {}
        }
    }

    private fun handleSubmitClick() = when {
        state.currentPasswordInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error(Res.string.feature_editpassword_error_empty_current_password))
            }
        }

        state.isSamePassword -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error(Res.string.feature_editpassword_error_same_password))
            }
        }

        state.newPasswordInput.length < MIN_PASSWORD_LENGTH -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = Error(
                        Res.string.feature_editpassword_error_password_min_length,
                        listOf(MIN_PASSWORD_LENGTH),
                    ),
                )
            }
        }

        !state.isPasswordMatch -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error(Res.string.feature_editpassword_error_password_mismatch))
            }
        }

        !state.isPasswordStrong -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error(Res.string.feature_editpassword_error_password_weak))
            }
        }

        else -> initiateUpdatePassword()
    }

    private fun initiateUpdatePassword() {
        mutableStateFlow.update {
            it.copy(dialogState = EditPasswordDialog.Loading)
        }

        updatePassword(state.newPasswordInput)
    }

    private fun updatePassword(newPassword: String) {
        val userId = requireNotNull(userInfo.value?.userId)
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository call completes normally on success and throws on
        // failure, so the handler reports Failed with that exception's message.
        submitPassword.submit {
            userRepository.updateUserPassword(userId, newPassword)
        }
    }
}

@Serializable
internal data class EditPasswordState(
    val currentPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    @Transient
    val dialogState: EditPasswordDialog? = null,
    val passwordStrengthState: PasswordStrengthState = PasswordStrengthState.NONE,
) {
    val isPasswordStrong: Boolean
        get() = when (passwordStrengthState) {
            PasswordStrengthState.NONE,
            PasswordStrengthState.WEAK_1,
            PasswordStrengthState.WEAK_2,
            PasswordStrengthState.WEAK_3,
            -> false

            PasswordStrengthState.GOOD,
            PasswordStrengthState.STRONG,
            PasswordStrengthState.VERY_STRONG,
            -> true
        }

    val isPasswordMatch: Boolean
        get() = newPasswordInput == confirmPasswordInput

    val isSamePassword: Boolean
        get() = currentPasswordInput == newPasswordInput
}

internal sealed interface EditPasswordDialog {
    data object Loading : EditPasswordDialog
    data class Error(
        val message: StringResource,
        val formatArgs: List<Any> = emptyList(),
    ) : EditPasswordDialog
    data class ApiError(val message: UiError) : EditPasswordDialog
}

internal sealed interface EditPasswordEvent {
    data object NavigateBack : EditPasswordEvent
    data object OnLogoutUser : EditPasswordEvent
    data class ShowToast(val message: StringResource) : EditPasswordEvent
}

internal sealed interface EditPasswordAction {
    data class CurrentPasswordChange(val currentPassword: String) : EditPasswordAction
    data class NewPasswordChange(val newPassword: String) : EditPasswordAction
    data class ConfirmPasswordChange(val confirmPassword: String) : EditPasswordAction

    data object SubmitClick : EditPasswordAction
    data object NavigateBackClick : EditPasswordAction
    data object ErrorDialogDismiss : EditPasswordAction

    sealed class Internal : EditPasswordAction {
        data class ReceivePasswordStrengthResult(
            val result: PasswordStrengthResult,
        ) : Internal()
    }
}
