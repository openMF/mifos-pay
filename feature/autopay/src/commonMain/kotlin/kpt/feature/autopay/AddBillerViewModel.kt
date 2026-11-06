/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.util.BillerValidator
import kpt.core.datastore.BillerRepository
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
import kpt.core.model.autopay.BillerFormData
import kpt.core.model.autopay.BillerValidationResult
import kpt.core.ui.utils.BaseViewModel
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AddBillerViewModel(
    savedStateHandle: SavedStateHandle,
    private val billerRepository: BillerRepository,
) : BaseViewModel<AddBillerState, AddBillerEvent, AddBillerAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: AddBillerState(),
) {

    companion object {
        private const val KEY_STATE = "add_biller_state"
        private const val SOURCE_ARG = "source"
    }

    private val source: String = savedStateHandle.get<String>(SOURCE_ARG) ?: "direct"

    fun getSource(): String = source

    // Template idiom (core-base/store): the one-shot create goes through a SubmitHandler
    // instead of a hand-folded DataState result. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it to drive this screen's
    // existing loading/success/error UX, so the Screen is unchanged.
    private val submitBiller = viewModelScope.submitHandler<Biller>()

    init {
        submitBiller.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update { it.copy(isLoading = true) }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update {
                            it.copy(isLoading = false, isSuccess = true)
                        }
                        sendEvent(AddBillerEvent.BillerSaved(submitState.result))
                        submitBiller.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(isLoading = false, error = submitState.error.message)
                        }
                        submitBiller.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: AddBillerAction) {
        when (action) {
            is AddBillerAction.UpdateBillerName -> {
                updateBillerName(action.name)
            }
            is AddBillerAction.UpdateAccountNumber -> {
                updateAccountNumber(action.accountNumber)
            }
            is AddBillerAction.UpdateContactNumber -> {
                updateContactNumber(action.contactNumber)
            }
            is AddBillerAction.UpdateEmail -> {
                updateEmail(action.email)
            }
            is AddBillerAction.UpdateCategory -> {
                updateCategory(action.category)
            }
            is AddBillerAction.UpdateAddress -> {
                updateAddress(action.address)
            }
            is AddBillerAction.SaveBiller -> {
                saveBiller()
            }
            is AddBillerAction.ValidateForm -> {
                validateForm()
            }
            is AddBillerAction.ClearError -> {
                clearError()
            }
            is AddBillerAction.ClearValidationErrors -> {
                clearValidationErrors()
            }
        }
    }

    private fun updateBillerName(name: String) {
        val nameError = BillerValidator.validateNameField(name)
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(name = name),
                validationResult = it.validationResult.copy(nameError = nameError),
            )
        }
    }

    private fun updateAccountNumber(accountNumber: String) {
        val accountNumberError = BillerValidator.validateAccountNumberField(accountNumber)
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(accountNumber = accountNumber),
                validationResult = it.validationResult.copy(accountNumberError = accountNumberError),
            )
        }
    }

    private fun updateContactNumber(contactNumber: String) {
        val contactNumberError = BillerValidator.validateContactNumberField(contactNumber)
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(contactNumber = contactNumber),
                validationResult = it.validationResult.copy(contactNumberError = contactNumberError),
            )
        }
    }

    private fun updateEmail(email: String) {
        val emailError = BillerValidator.validateEmailField(email)
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(email = email),
                validationResult = it.validationResult.copy(emailError = emailError),
            )
        }
    }

    private fun updateCategory(category: BillerCategory) {
        mutableStateFlow.update {
            it.copy(
                formData = it.formData.copy(category = category),
                validationResult = it.validationResult.copy(categoryError = null),
            )
        }
    }

    private fun updateAddress(address: String) {
        mutableStateFlow.update {
            it.copy(formData = it.formData.copy(address = address))
        }
    }

    private fun validateForm(): BillerValidationResult {
        val currentState = stateFlow.value
        val formData = currentState.formData

        val validationResult = BillerValidator.validateBillerForm(formData)

        mutableStateFlow.update { it.copy(validationResult = validationResult) }
        return validationResult
    }

    @OptIn(ExperimentalTime::class)
    private fun generateUniqueId(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextInt(100000, 999999)
        return "$timestamp-$random"
    }

    private fun saveBiller() {
        val validationResult = validateForm()

        if (!validationResult.isValid) {
            return
        }

        val formData = stateFlow.value.formData

        val biller = Biller(
            id = generateUniqueId(),
            name = formData.name.trim(),
            accountNumber = formData.accountNumber.trim(),
            contactNumber = formData.contactNumber.trim(),
            email = formData.email.takeIf { it.isNotBlank() },
            category = formData.category!!,
            address = formData.address.takeIf { it.isNotBlank() },
        )

        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository returns the saved biller or throws on failure,
        // which the handler surfaces as Failed.
        submitBiller.submit {
            billerRepository.saveBiller(biller)
        }
    }

    private fun clearError() {
        mutableStateFlow.update { it.copy(error = null) }
    }

    private fun clearValidationErrors() {
        mutableStateFlow.update {
            it.copy(
                validationResult = BillerValidationResult(isValid = false),
            )
        }
    }
}

@Serializable
data class AddBillerState(
    val formData: BillerFormData = BillerFormData(),
    val validationResult: BillerValidationResult = BillerValidationResult(isValid = false),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

sealed interface AddBillerEvent {
    data class BillerSaved(val biller: Biller) : AddBillerEvent
}

sealed interface AddBillerAction {
    data class UpdateBillerName(val name: String) : AddBillerAction
    data class UpdateAccountNumber(val accountNumber: String) : AddBillerAction
    data class UpdateContactNumber(val contactNumber: String) : AddBillerAction
    data class UpdateEmail(val email: String) : AddBillerAction
    data class UpdateCategory(val category: BillerCategory) : AddBillerAction
    data class UpdateAddress(val address: String) : AddBillerAction
    data object SaveBiller : AddBillerAction
    data object ValidateForm : AddBillerAction
    data object ClearError : AddBillerAction
    data object ClearValidationErrors : AddBillerAction
}
