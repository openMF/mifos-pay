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
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.data.fork.util.BillerValidator
import kpt.core.datastore.BillerRepository
import kpt.core.model.autopay.Biller
import kpt.core.model.autopay.BillerCategory
import kpt.core.model.autopay.BillerFormData
import kpt.core.model.autopay.BillerValidationResult
import kpt.core.ui.fork.utils.BaseViewModel
/** View model for the edit-biller form; validates input and updates the biller identified by the billerId argument. */
class EditBillerViewModel(
    savedStateHandle: SavedStateHandle,
    private val billerRepository: BillerRepository,
) : BaseViewModel<EditBillerState, EditBillerEvent, EditBillerAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: EditBillerState(),
) {

    private val billerId: String = savedStateHandle["billerId"] ?: ""

    // Template idiom (core-base/store): the one-shot update goes through a SubmitHandler
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
                        sendEvent(EditBillerEvent.BillerUpdated(submitState.result))
                        submitBiller.reset()
                    }

                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to update biller: ${submitState.error.message}",
                            )
                        }
                        submitBiller.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        loadBiller()
    }

    override fun handleAction(action: EditBillerAction) {
        when (action) {
            is EditBillerAction.UpdateBillerName -> {
                updateBillerName(action.name)
            }
            is EditBillerAction.UpdateAccountNumber -> {
                updateAccountNumber(action.accountNumber)
            }
            is EditBillerAction.UpdateContactNumber -> {
                updateContactNumber(action.contactNumber)
            }
            is EditBillerAction.UpdateEmail -> {
                updateEmail(action.email)
            }
            is EditBillerAction.UpdateCategory -> {
                updateCategory(action.category)
            }
            is EditBillerAction.UpdateAddress -> {
                updateAddress(action.address)
            }
            is EditBillerAction.UpdateBiller -> {
                updateBiller()
            }
            is EditBillerAction.ValidateForm -> {
                validateForm()
            }
            is EditBillerAction.ClearError -> {
                clearError()
            }
            is EditBillerAction.ClearValidationErrors -> {
                clearValidationErrors()
            }
        }
    }

    private fun loadBiller() {
        viewModelScope.launch {
            mutableStateFlow.update { it.copy(isLoading = true) }

            try {
                val biller = billerRepository.getBillerById(billerId)
                if (biller != null) {
                    val formData = BillerFormData(
                        name = biller.name,
                        accountNumber = biller.accountNumber,
                        contactNumber = biller.contactNumber,
                        email = biller.email ?: "",
                        category = biller.category,
                        address = biller.address ?: "",
                    )
                    mutableStateFlow.update {
                        it.copy(
                            formData = formData,
                            isLoading = false,
                            error = null,
                        )
                    }
                } else {
                    mutableStateFlow.update {
                        it.copy(
                            isLoading = false,
                            error = "Biller not found",
                        )
                    }
                }
            } catch (e: Exception) {
                mutableStateFlow.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load biller: ${e.message}",
                    )
                }
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
            it.copy(
                formData = it.formData.copy(address = address),
            )
        }
    }

    private fun validateForm(): BillerValidationResult {
        val formData = mutableStateFlow.value.formData

        val validationResult = BillerValidator.validateBillerForm(formData)

        mutableStateFlow.update {
            it.copy(validationResult = validationResult)
        }

        return validationResult
    }

    private fun updateBiller() {
        val validationResult = validateForm()
        if (!validationResult.isValid) {
            return
        }

        val formData = mutableStateFlow.value.formData
        val biller = Biller(
            id = billerId,
            name = formData.name,
            accountNumber = formData.accountNumber,
            contactNumber = formData.contactNumber,
            email = formData.email.takeIf { it.isNotBlank() },
            category = formData.category!!,
            address = formData.address.takeIf { it.isNotBlank() },
        )

        // Submit through the handler — it drives Submitting/Submitted/Failed, observed
        // in `init`. The repository returns the updated biller or throws on failure,
        // which the handler surfaces as Failed.
        submitBiller.submit {
            billerRepository.updateBiller(biller)
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

    /** Holds the saved-state key used to persist the form state. */
    companion object {
        private const val KEY_STATE = "edit_biller_state"
    }
}

/**
 * UI state of the edit-biller form.
 *
 * @property formData Biller details being edited.
 * @property validationResult Result of validating the form.
 * @property isLoading Whether a load or save is in progress.
 * @property error Error message to show, or null.
 */
@Serializable
data class EditBillerState(
    val formData: BillerFormData = BillerFormData(),
    val validationResult: BillerValidationResult = BillerValidationResult(isValid = false),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/** One-off events emitted by the edit-biller view model. */
sealed interface EditBillerEvent {
    /**
     * The biller was updated.
     *
     * @property biller The updated biller.
     */
    data class BillerUpdated(val biller: Biller) : EditBillerEvent
}

/** User actions handled by the edit-biller view model. */
sealed interface EditBillerAction {
    /**
     * The biller name changed.
     *
     * @property name New biller name.
     */
    data class UpdateBillerName(val name: String) : EditBillerAction

    /**
     * The account number changed.
     *
     * @property accountNumber New account number.
     */
    data class UpdateAccountNumber(val accountNumber: String) : EditBillerAction

    /**
     * The contact number changed.
     *
     * @property contactNumber New contact number.
     */
    data class UpdateContactNumber(val contactNumber: String) : EditBillerAction

    /**
     * The email changed.
     *
     * @property email New email address.
     */
    data class UpdateEmail(val email: String) : EditBillerAction

    /**
     * The category changed.
     *
     * @property category New biller category.
     */
    data class UpdateCategory(val category: BillerCategory) : EditBillerAction

    /**
     * The address changed.
     *
     * @property address New address.
     */
    data class UpdateAddress(val address: String) : EditBillerAction

    /** The user tapped update. */
    data object UpdateBiller : EditBillerAction

    /** Validate the form. */
    data object ValidateForm : EditBillerAction

    /** Clear the current error. */
    data object ClearError : EditBillerAction

    /** Clear field-level validation errors. */
    data object ClearValidationErrors : EditBillerAction
}
