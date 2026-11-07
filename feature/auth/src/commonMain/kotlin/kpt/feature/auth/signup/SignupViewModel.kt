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
import kpt.feature.auth.generated.resources.Res
import kpt.feature.auth.generated.resources.feature_auth_error_address_line1_required
import kpt.feature.auth.generated.resources.feature_auth_error_check_uniqueness_failed
import kpt.feature.auth.generated.resources.feature_auth_error_confirm_password_required
import kpt.feature.auth.generated.resources.feature_auth_error_country_required
import kpt.feature.auth.generated.resources.feature_auth_error_email_invalid
import kpt.feature.auth.generated.resources.feature_auth_error_email_required
import kpt.feature.auth.generated.resources.feature_auth_error_field_already_exists
import kpt.feature.auth.generated.resources.feature_auth_error_first_name_required
import kpt.feature.auth.generated.resources.feature_auth_error_last_name_required
import kpt.feature.auth.generated.resources.feature_auth_error_mobile_invalid
import kpt.feature.auth.generated.resources.feature_auth_error_mobile_required
import kpt.feature.auth.generated.resources.feature_auth_error_password_required
import kpt.feature.auth.generated.resources.feature_auth_error_passwords_mismatch
import kpt.feature.auth.generated.resources.feature_auth_error_pincode_required
import kpt.feature.auth.generated.resources.feature_auth_error_select_savings_account
import kpt.feature.auth.generated.resources.feature_auth_error_state_required
import kpt.feature.auth.generated.resources.feature_auth_error_username_required
import kpt.feature.auth.generated.resources.feature_auth_registration_successful
import kpt.feature.auth.signup.SignUpAction.Internal.ReceivePasswordStrengthResult
import kpt.feature.auth.signup.SignUpState.DialogState
import org.jetbrains.compose.resources.StringResource
/** View model for the signup screen; validates the form and registers a new user and client. */
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

    /** Holds the saved-state key used to persist the signup state. */
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

/**
 * UI state of the signup form.
 *
 * @property savingsProductId Savings product to open for the new client.
 * @property firstNameInput First name typed by the user.
 * @property lastNameInput Last name typed by the user.
 * @property emailInput Email typed by the user.
 * @property userNameInput Username typed by the user.
 * @property addressLine1Input First address line typed by the user.
 * @property addressLine2Input Second address line typed by the user.
 * @property pinCodeInput Postal code typed by the user.
 * @property passwordInput Password typed by the user.
 * @property confirmPasswordInput Password confirmation typed by the user.
 * @property mobileNumberInput Mobile number of the user.
 * @property stateInput Selected state or province.
 * @property countryInput Selected country.
 * @property businessNameInput Business name typed by the user.
 * @property dialogState Loading or error dialog currently shown, or null.
 * @property passwordStrengthState Evaluated strength of the password.
 * @property passwordFeedback Hints for improving the password.
 * @property countriesWithStates Map of country name to its states.
 */
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
    /** States available for the currently selected country, or null if none. */
    @Transient
    val statesForSelectedCountry = countriesWithStates[countryInput]

    /** Dialog shown over the signup screen. */
    sealed interface DialogState {
        /** Error dialog, with either a plain or a resource-based message. */
        sealed interface Error : DialogState {
            /**
             * Error dialog with a plain text message.
             *
             * @property message Error text to show.
             */
            data class StringMessage(val message: String) : Error

            /**
             * Error dialog with a string-resource message.
             *
             * @property message Resource of the error text.
             */
            data class ResourceMessage(val message: StringResource) : Error
        }

        /** Blocking progress dialog shown while signup is in flight. */
        data object Loading : DialogState
    }
}

/** One-off events emitted by the signup view model. */
sealed interface SignUpEvent {
    /** Navigate back from the signup screen. */
    data object NavigateBack : SignUpEvent

    /**
     * Show a toast.
     *
     * @property message Text of the toast.
     */
    data class ShowToast(val message: String) : SignUpEvent

    /**
     * Navigate to login after successful signup.
     *
     * @property username Username of the newly registered user.
     */
    data class NavigateToLogin(val username: String) : SignUpEvent
}

/** User actions handled by the signup view model. */
sealed interface SignUpAction {
    /**
     * The first name field changed.
     *
     * @property firstName New first name value.
     */
    data class FirstNameInputChange(val firstName: String) : SignUpAction

    /**
     * The last name field changed.
     *
     * @property lastName New last name value.
     */
    data class LastNameInputChange(val lastName: String) : SignUpAction

    /**
     * The email field changed.
     *
     * @property email New email value.
     */
    data class EmailInputChange(val email: String) : SignUpAction

    /**
     * The username field changed.
     *
     * @property username New username value.
     */
    data class UserNameInputChange(val username: String) : SignUpAction

    /**
     * The first address line changed.
     *
     * @property addressLineOne New address line value.
     */
    data class AddressLine1InputChange(val addressLineOne: String) : SignUpAction

    /**
     * The second address line changed.
     *
     * @property addressLineTwo New address line value.
     */
    data class AddressLine2InputChange(val addressLineTwo: String) : SignUpAction

    /**
     * The postal code changed.
     *
     * @property pincode New postal code value.
     */
    data class PinCodeInputChange(val pincode: String) : SignUpAction

    /**
     * The business name changed.
     *
     * @property businessName New business name value.
     */
    data class BusinessNameInputChange(val businessName: String) : SignUpAction

    /**
     * The password field changed.
     *
     * @property password New password value.
     */
    data class PasswordInputChange(val password: String) : SignUpAction

    /**
     * The confirm-password field changed.
     *
     * @property confirmPassword New confirmation value.
     */
    data class ConfirmPasswordInputChange(val confirmPassword: String) : SignUpAction

    /**
     * The mobile number field changed.
     *
     * @property mobileNumber New mobile number value.
     */
    data class MobileNumberInputChange(val mobileNumber: String) : SignUpAction

    /**
     * The savings product selection changed.
     *
     * @property savingsAccountNo Selected savings product id.
     */
    data class SavingsAccountNoInputChange(val savingsAccountNo: Int) : SignUpAction

    /**
     * The state selection changed.
     *
     * @property state Selected state.
     */
    data class StateInputChange(val state: String) : SignUpAction

    /**
     * The country selection changed.
     *
     * @property country Selected country.
     */
    data class CountryInputChange(val country: String) : SignUpAction

    /** The user tapped the submit button. */
    data object SubmitClick : SignUpAction

    /** The user tapped the close button. */
    data object CloseClick : SignUpAction

    /** The user dismissed the error dialog. */
    data object ErrorDialogDismiss : SignUpAction

    /** Load the list of countries and their states. */
    data object LoadCountries : SignUpAction

    /** Actions dispatched internally by the view model rather than by the UI. */
    sealed class Internal : SignUpAction {
        /**
         * Delivers the evaluated password strength.
         *
         * @property result Result of the password strength check.
         */
        data class ReceivePasswordStrengthResult(
            val result: PasswordStrengthResult,
        ) : Internal()
    }
}
