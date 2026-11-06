/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.ImmutableListSerializer
import kpt.core.common.ScreenState
import kpt.core.common.StringProvider
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.common.utils.formatAsBulletPoints
import kpt.core.common.utils.isValidEmail
import kpt.core.data.repository.AssetRepository
import kpt.core.data.repository.ClientRepository
import kpt.core.data.repository.SearchRepository
import kpt.core.data.repository.UserRepository
import kpt.core.data.util.Constants
import kpt.core.model.client.ClientAddress
import kpt.core.model.client.NewClient
import kpt.core.model.user.NewUser
import kpt.core.ui.PasswordStrengthState
import kpt.core.ui.utils.BaseViewModel
import kpt.core.ui.utils.fork.PasswordChecker
import kpt.core.ui.utils.fork.PasswordStrength
import kpt.core.ui.utils.fork.PasswordStrengthResult
import kpt.feature.auth.signup.SignUpAction.Internal.ReceivePasswordStrengthResult
import kpt.feature.auth.signup.SignUpState.DialogState
import mifos_pay.feature.auth.generated.resources.Res
import mifos_pay.feature.auth.generated.resources.feature_auth_error_address_line1_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_check_uniqueness_failed
import mifos_pay.feature.auth.generated.resources.feature_auth_error_confirm_password_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_country_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_email_invalid
import mifos_pay.feature.auth.generated.resources.feature_auth_error_email_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_field_already_exists
import mifos_pay.feature.auth.generated.resources.feature_auth_error_first_name_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_last_name_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_mobile_invalid
import mifos_pay.feature.auth.generated.resources.feature_auth_error_mobile_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_password_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_passwords_mismatch
import mifos_pay.feature.auth.generated.resources.feature_auth_error_pincode_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_select_savings_account
import mifos_pay.feature.auth.generated.resources.feature_auth_error_state_required
import mifos_pay.feature.auth.generated.resources.feature_auth_error_username_required
import mifos_pay.feature.auth.generated.resources.feature_auth_registration_successful
import org.jetbrains.compose.resources.StringResource
class SignupViewModel(
    private val userRepository: UserRepository,
    private val searchRepository: SearchRepository,
    private val clientRepository: ClientRepository,
    private val assetRepository: AssetRepository,
    private val stringProvider: StringProvider,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<SignUpState, SignUpEvent, SignUpAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: SignUpState(),
) {

    companion object {
        private const val KEY_STATE = "signup_state"
    }

    private var passwordStrengthJob: Job = Job().apply { complete() }

    // Template idiom (core-base/store): the multi-step register write (uniqueness check →
    // create user → create client → assign client, with rollback on failure) goes through a
    // SubmitHandler instead of a hand-folded DataState result action. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it below to drive this screen's existing
    // loading/error dialog, success toast and login navigation, so the Screen is unchanged.
    // The success result is the new user's username.
    private val submitRegister = viewModelScope.submitHandler<String>()

    init {
        submitRegister.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update { it.copy(dialogState = DialogState.Loading) }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(
                            SignUpEvent.ShowToast(
                                stringProvider.get(Res.string.feature_auth_registration_successful),
                            ),
                        )
                        sendEvent(SignUpEvent.NavigateToLogin(submitState.result))
                        submitRegister.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = DialogState.Error.StringMessage(
                                    submitState.error.message.toString(),
                                ),
                            )
                        }
                        submitRegister.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow.onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        savedStateHandle.get<String>("mobileNumber")?.let {
            viewModelScope.launch {
                trySendAction(SignUpAction.MobileNumberInputChange(it))
            }
        }

        savedStateHandle.get<Int>("savingsProductId")?.let {
            viewModelScope.launch {
                trySendAction(SignUpAction.SavingsAccountNoInputChange(it))
            }
        }

        savedStateHandle.get<String>("businessName")?.let {
            viewModelScope.launch {
                trySendAction(SignUpAction.BusinessNameInputChange(it))
            }
        }

        loadCountriesFromJson()
    }

    override fun handleAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.FirstNameInputChange -> {
                mutableStateFlow.update {
                    it.copy(firstNameInput = action.firstName)
                }
            }

            is SignUpAction.LastNameInputChange -> {
                mutableStateFlow.update {
                    it.copy(lastNameInput = action.lastName)
                }
            }

            is SignUpAction.UserNameInputChange -> {
                mutableStateFlow.update {
                    it.copy(userNameInput = action.username)
                }
            }

            is SignUpAction.PasswordInputChange -> handlePasswordInput(action)

            is SignUpAction.ConfirmPasswordInputChange -> {
                mutableStateFlow.update {
                    it.copy(confirmPasswordInput = action.confirmPassword)
                }
            }

            is SignUpAction.EmailInputChange -> {
                mutableStateFlow.update {
                    it.copy(emailInput = action.email)
                }
            }

            is SignUpAction.AddressLine1InputChange -> {
                mutableStateFlow.update {
                    it.copy(addressLine1Input = action.addressLineOne)
                }
            }

            is SignUpAction.AddressLine2InputChange -> {
                mutableStateFlow.update {
                    it.copy(addressLine2Input = action.addressLineTwo)
                }
            }

            is SignUpAction.PinCodeInputChange -> {
                mutableStateFlow.update {
                    it.copy(pinCodeInput = action.pincode)
                }
            }

            is SignUpAction.BusinessNameInputChange -> {
                mutableStateFlow.update {
                    it.copy(businessNameInput = action.businessName)
                }
            }

            is SignUpAction.MobileNumberInputChange -> {
                mutableStateFlow.update {
                    it.copy(mobileNumberInput = action.mobileNumber)
                }
            }

            is SignUpAction.SavingsAccountNoInputChange -> {
                mutableStateFlow.update {
                    it.copy(savingsProductId = action.savingsAccountNo)
                }
            }

            is SignUpAction.StateInputChange -> {
                mutableStateFlow.update {
                    it.copy(stateInput = action.state)
                }
            }

            is SignUpAction.CountryInputChange -> {
                mutableStateFlow.update {
                    it.copy(
                        countryInput = action.country,
                        // reset state when country changes
                        stateInput = "",
                    )
                }
            }

            is SignUpAction.CloseClick -> {
                sendEvent(SignUpEvent.NavigateBack)
            }

            is SignUpAction.ErrorDialogDismiss -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is ReceivePasswordStrengthResult -> handlePasswordStrengthResult(action)

            is SignUpAction.SubmitClick -> handleSubmitClick()

            is SignUpAction.LoadCountries -> loadCountriesFromJson()
        }
    }

    private fun handlePasswordInput(action: SignUpAction.PasswordInputChange) {
        // Update input:
        mutableStateFlow.update {
            it.copy(
                passwordInput = action.password,
                passwordFeedback = PasswordChecker.getPasswordFeedback(action.password)
                    .toPersistentList(),
            )
        }
        // Update password strength:
        passwordStrengthJob.cancel()
        if (action.password.isEmpty()) {
            mutableStateFlow.update {
                it.copy(passwordStrengthState = PasswordStrengthState.NONE)
            }
        } else {
            passwordStrengthJob = viewModelScope.launch {
                val result = PasswordChecker.getPasswordStrengthResult(action.password)
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
        state.savingsProductId == 0 -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_select_savings_account),
                )
            }
        }

        state.firstNameInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_first_name_required),
                )
            }
        }

        state.lastNameInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_last_name_required),
                )
            }
        }

        state.userNameInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_username_required),
                )
            }
        }

        state.emailInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_email_required),
                )
            }
        }

        !state.emailInput.isValidEmail() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_email_invalid),
                )
            }
        }

        state.mobileNumberInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_mobile_required),
                )
            }
        }

        state.mobileNumberInput.length < 10 -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_mobile_invalid),
                )
            }
        }

        state.passwordInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_password_required),
                )
            }
        }

        state.passwordFeedback.isNotEmpty() -> {
            val bulletListPasswordFeedback = formatAsBulletPoints(state.passwordFeedback)
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.StringMessage(bulletListPasswordFeedback),
                )
            }
        }

        state.confirmPasswordInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_confirm_password_required),
                )
            }
        }

        state.passwordInput != state.confirmPasswordInput -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_passwords_mismatch),
                )
            }
        }

        state.addressLine1Input.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_address_line1_required),
                )
            }
        }

        state.pinCodeInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_pincode_required),
                )
            }
        }

        state.countryInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_country_required),
                )
            }
        }

        state.stateInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(
                    dialogState = DialogState.Error.ResourceMessage(Res.string.feature_auth_error_state_required),
                )
            }
        }

        else -> initiateSignUp()
    }

    /*
        Enhancement: Move the following code in to a Use Case
     */
    private fun initiateSignUp() {
        // Submit through the handler — it drives Submitting/Submitted/Failed, observed in
        // `init` (the observer shows the loading dialog while in-flight, the success toast +
        // login navigation on Submitted, and the error dialog on Failed). The block runs the
        // full register orchestration, throwing on any step's failure so the handler reports
        // Failed with that step's exact message.
        submitRegister.submit { registerUser() }
    }

    private suspend fun registerUser(): String {
        // 1. Uniqueness check (username + mobile number).
        val fieldsToCheck = mapOf(
            "Username" to state.userNameInput,
            "Mobile Number" to state.mobileNumberInput,
        )
        val uniquenessErrors = checkUniqueFields(fieldsToCheck)
        if (uniquenessErrors.isNotEmpty()) {
            throw Exception(uniquenessErrors.joinToString("\n"))
        }

        // 2. Create the user account.
        val newUser = NewUser(
            state.userNameInput,
            state.firstNameInput,
            state.lastNameInput,
            state.emailInput,
            state.passwordInput,
        )
        val userId = userRepository.createUser(newUser)

        // 3. Create the client (rollback: delete the user on failure).
        val newClient = NewClient(
            firstname = state.firstNameInput,
            lastname = state.lastNameInput,
            externalId = state.userNameInput.plus("_client"),
            mobileNo = state.mobileNumberInput,
            savingsProductId = state.savingsProductId,
            address = ClientAddress(
                addressLine1 = state.addressLine1Input,
                addressLine2 = state.addressLine2Input,
                postalCode = state.pinCodeInput,
                stateProvinceId = state.stateInput,
                countryId = state.countryInput,
            ),
        )
        val clientId = try {
            clientRepository.createClient(newClient)
        } catch (e: Exception) {
            userRepository.deleteUser(userId)
            throw Exception(e.message.toString())
        }

        // 4. Assign the client to the user (rollback: delete both on failure).
        try {
            userRepository.assignClientToUser(userId, clientId)
        } catch (e: Exception) {
            userRepository.deleteUser(userId)
            runCatching { clientRepository.deleteClient(clientId) }
            throw Exception(e.toString())
        }

        // 5. Success — the observer surfaces the toast + login navigation.
        return state.userNameInput
    }

    private suspend fun checkUniqueFields(fields: Map<String, String>): List<String> {
        val results = coroutineScope {
            fields.map { (label, value) ->
                async {
                    val result = runCatching {
                        searchRepository.searchResources(value, Constants.CLIENTS, false)
                    }
                    label to result
                }
            }.awaitAll()
        }

        return results.mapNotNull { (label, result) ->
            result.fold(
                onSuccess = { matches ->
                    if (matches.isNotEmpty()) {
                        stringProvider.get(
                            resource = Res.string.feature_auth_error_field_already_exists,
                            label,
                        )
                    } else {
                        null
                    }
                },
                onFailure = {
                    stringProvider.get(
                        Res.string.feature_auth_error_check_uniqueness_failed,
                        label,
                    )
                },
            )
        }
    }

    private fun loadCountriesFromJson() {
        assetRepository.getCountriesWithStates()
            .onEach { screenState ->
                when (screenState) {
                    is ScreenState.Content -> mutableStateFlow.update {
                        it.copy(countriesWithStates = screenState.data)
                    }

                    is ScreenState.Error ->
                        Logger.d("Failed to load countries.json: ${screenState.error.message}")

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }
}

@Serializable
data class SignUpState(
    val savingsProductId: Int = 0,
    val firstNameInput: String = "",
    val lastNameInput: String = "",
    val emailInput: String = "",
    val userNameInput: String = "",
    val addressLine1Input: String = "",
    val addressLine2Input: String = "",
    val pinCodeInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val mobileNumberInput: String = "",
    val stateInput: String = "",
    val countryInput: String = "",
    val businessNameInput: String = "",
    @Transient val dialogState: DialogState? = null,
    val passwordStrengthState: PasswordStrengthState = PasswordStrengthState.NONE,
    @Serializable(with = ImmutableListSerializer::class)
    val passwordFeedback: ImmutableList<String> = persistentListOf(),
    val countriesWithStates: Map<String, List<String>> = emptyMap(),
) {
    @Transient
    val statesForSelectedCountry = countriesWithStates[countryInput]

    sealed interface DialogState {
        sealed interface Error : DialogState {
            data class StringMessage(val message: String) : Error
            data class ResourceMessage(val message: StringResource) : Error
        }

        data object Loading : DialogState
    }
}

sealed interface SignUpEvent {
    data object NavigateBack : SignUpEvent
    data class ShowToast(val message: String) : SignUpEvent
    data class NavigateToLogin(val username: String) : SignUpEvent
}

sealed interface SignUpAction {
    data class FirstNameInputChange(val firstName: String) : SignUpAction
    data class LastNameInputChange(val lastName: String) : SignUpAction
    data class EmailInputChange(val email: String) : SignUpAction
    data class UserNameInputChange(val username: String) : SignUpAction
    data class AddressLine1InputChange(val addressLineOne: String) : SignUpAction
    data class AddressLine2InputChange(val addressLineTwo: String) : SignUpAction
    data class PinCodeInputChange(val pincode: String) : SignUpAction
    data class BusinessNameInputChange(val businessName: String) : SignUpAction
    data class PasswordInputChange(val password: String) : SignUpAction
    data class ConfirmPasswordInputChange(val confirmPassword: String) : SignUpAction
    data class MobileNumberInputChange(val mobileNumber: String) : SignUpAction
    data class SavingsAccountNoInputChange(val savingsAccountNo: Int) : SignUpAction
    data class StateInputChange(val state: String) : SignUpAction
    data class CountryInputChange(val country: String) : SignUpAction

    data object SubmitClick : SignUpAction
    data object CloseClick : SignUpAction
    data object ErrorDialogDismiss : SignUpAction
    data object LoadCountries : SignUpAction

    sealed class Internal : SignUpAction {
        data class ReceivePasswordStrengthResult(
            val result: PasswordStrengthResult,
        ) : Internal()
    }
}
