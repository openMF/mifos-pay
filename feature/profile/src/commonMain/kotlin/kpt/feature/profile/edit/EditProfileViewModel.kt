/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.ScreenState
import kpt.core.common.StringResourceSerializer
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.common.utils.isValidEmail
import kpt.core.data.repository.ClientRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.client.UpdatedClient
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.profile.edit.EditProfileAction.Internal.HandleLoadClientImageResult
import kpt.feature.profile.edit.EditProfileAction.Internal.LoadClientImage
import kpt.feature.profile.edit.EditProfileState.DialogState.Error
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile_error_empty_email
import kpt.feature.profile.generated.resources.feature_profile_error_empty_firstname
import kpt.feature.profile.generated.resources.feature_profile_error_empty_lastname
import kpt.feature.profile.generated.resources.feature_profile_error_empty_phone
import kpt.feature.profile.generated.resources.feature_profile_error_invalid_email
import kpt.feature.profile.generated.resources.feature_profile_error_invalid_phone_length
import kpt.feature.profile.generated.resources.feature_profile_profile_image_updated_successfully
import kpt.feature.profile.generated.resources.feature_profile_profile_updated_successfully
import org.jetbrains.compose.resources.StringResource
internal class EditProfileViewModel(
    private val preferencesRepository: ForkPreferencesRepository,
    private val clientRepository: ClientRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<EditProfileState, EditProfileEvent, EditProfileAction>(
    initialState = savedStateHandle.getSerialized(KEY) ?: run {
        val client = requireNotNull(preferencesRepository.client.value)

        EditProfileState(
            clientId = client.id,
            firstNameInput = client.firstname,
            lastNameInput = client.lastname,
            emailInput = client.emailAddress,
            phoneNumberInput = client.mobileNo,
            externalIdInput = client.externalId,
        )
    },
) {

    companion object {
        private const val KEY = "edit_profile_state"
    }

    // Template idiom (core-base/store): the one-shot profile writes go through
    // SubmitHandlers instead of hand-folded DataState result actions. Each handler
    // owns its own Submitting/Submitted/Failed lifecycle; we observe them in `init`
    // to drive this screen's existing dialog + toast + navigate-back UX, so the
    // Screen is unchanged. Three logically-distinct writes → three handlers:
    //  - submitUpdateProfile  → clientRepository.updateClient (server profile)
    //  - submitUpdateImage    → clientRepository.updateClientImage (only when an image is picked)
    //  - submitClientProfile  → preferencesRepository.updateClientProfile (local prefs)
    // The original chain (updateClient success → image [if any] → local prefs →
    // toast + navigate-back) is reproduced by kicking the next write off the prior
    // handler's terminal state.
    private val submitUpdateProfile = viewModelScope.submitHandler<Unit>()
    private val submitUpdateImage = viewModelScope.submitHandler<Unit>()
    private val submitClientProfile = viewModelScope.submitHandler<Unit>()

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY, value = it) }
            .launchIn(viewModelScope)

        // updateClient: on failure surface the error dialog (raw exception message,
        // matching the prior fold); on success continue to the image + local-prefs
        // chain. No loading dialog — the prior fold's Loading branch never fired for
        // this one-shot suspend call, so runtime UX is unchanged.
        submitUpdateProfile.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = Error.StringMessage(submitState.error.message ?: ""),
                            )
                        }
                        submitUpdateProfile.reset()
                    }

                    is SubmitState.Submitted -> {
                        onProfileUpdated()
                        submitUpdateProfile.reset()
                    }

                    is SubmitState.Submitting,
                    SubmitState.Idle,
                    -> Unit
                }
            }
            .launchIn(viewModelScope)

        // updateClientImage: on success show the image toast; on failure surface the
        // error dialog. Either way, continue to the local-prefs write — the prior
        // code ran updateClientProfile regardless of the image result.
        submitUpdateImage.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Failed -> {
                        mutableStateFlow.update {
                            it.copy(
                                dialogState = Error.StringMessage(submitState.error.message ?: ""),
                            )
                        }
                        submitClientProfileUpdate()
                        submitUpdateImage.reset()
                    }

                    is SubmitState.Submitted -> {
                        sendEvent(
                            EditProfileEvent.ShowToast(
                                Res.string.feature_profile_profile_image_updated_successfully,
                            ),
                        )
                        submitClientProfileUpdate()
                        submitUpdateImage.reset()
                    }

                    is SubmitState.Submitting,
                    SubmitState.Idle,
                    -> Unit
                }
            }
            .launchIn(viewModelScope)

        // updateClientProfile (local prefs): on success show the profile toast and
        // navigate back; on failure stay silent (the prior fold's `else -> {}`).
        submitClientProfile.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitted -> {
                        sendEvent(
                            EditProfileEvent.ShowToast(
                                Res.string.feature_profile_profile_updated_successfully,
                            ),
                        )
                        sendEvent(EditProfileEvent.NavigateBack)
                        submitClientProfile.reset()
                    }

                    is SubmitState.Failed -> submitClientProfile.reset()

                    is SubmitState.Submitting,
                    SubmitState.Idle,
                    -> Unit
                }
            }
            .launchIn(viewModelScope)

        trySendAction(LoadClientImage(state.clientId))
    }

    override fun handleAction(action: EditProfileAction) {
        when (action) {
            is EditProfileAction.FirstNameInputChange -> {
                mutableStateFlow.update {
                    it.copy(firstNameInput = action.firstName)
                }
            }

            is EditProfileAction.LastNameInputChange -> {
                mutableStateFlow.update {
                    it.copy(lastNameInput = action.lastName)
                }
            }

            is EditProfileAction.EmailInputChange -> {
                mutableStateFlow.update {
                    it.copy(emailInput = action.email)
                }
            }

            is EditProfileAction.ExternalIdInputChange -> {
                mutableStateFlow.update {
                    it.copy(externalIdInput = action.externalId)
                }
            }

            is EditProfileAction.PhoneNumberInputChange -> {
                mutableStateFlow.update {
                    it.copy(phoneNumberInput = action.phoneNumber)
                }
            }

            is EditProfileAction.DismissErrorDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is EditProfileAction.NavigateBack -> {
                sendEvent(EditProfileEvent.NavigateBack)
            }

            is EditProfileAction.PickProfileImage -> handlePickProfileImage(action)

            is HandleLoadClientImageResult -> handleLoadClientImageResult(action)

            is LoadClientImage -> loadClientImage(action)

            is EditProfileAction.UpdateProfile -> handleUpdateProfile()
        }
    }

    private fun handleLoadClientImageResult(action: HandleLoadClientImageResult) {
        // `getClientImage` now returns a `ScreenState<String>` stream. Fold
        // the 6 branches back onto the existing dialog/state surface. The
        // Success (Content) branch decodes to bytes for the profile image;
        // Error variants surface a dialog matching the prior behaviour.
        // Empty is defensively surfaced as an error (single-record endpoint
        // shouldn't emit Empty). NoNetwork / Unauthenticated fold into the
        // existing Error dialog until Phase-4 differentiates.
        when (val result = action.result) {
            is ScreenState.Content -> {
                mutableStateFlow.update {
                    it.copy(profileImage = result.data.encodeToByteArray())
                }
            }

            is ScreenState.Empty -> {
                mutableStateFlow.update {
                    it.copy(dialogState = Error.StringMessage("Profile image not available."))
                }
            }

            is ScreenState.Error -> {
                mutableStateFlow.update {
                    it.copy(dialogState = Error.StringMessage(result.error.message ?: ""))
                }
            }

            is ScreenState.NoNetwork -> {
                mutableStateFlow.update {
                    it.copy(dialogState = Error.StringMessage("No network. Please check your connection."))
                }
            }

            is ScreenState.Unauthenticated -> {
                mutableStateFlow.update {
                    it.copy(dialogState = Error.StringMessage("Session expired. Please log in again."))
                }
            }

            is ScreenState.Loading -> {
                mutableStateFlow.update {
                    it.copy(dialogState = EditProfileState.DialogState.Loading)
                }
            }
        }
    }

    private fun loadClientImage(action: LoadClientImage) {
        clientRepository.getClientImage(action.clientId).onEach {
            sendAction(HandleLoadClientImageResult(it))
        }.launchIn(viewModelScope)
    }

    private fun handlePickProfileImage(action: EditProfileAction.PickProfileImage) {
        viewModelScope.launch {
            val file = FileKit.openFilePicker(FileKitType.Image)
            file?.let {
                mutableStateFlow.update { state ->
                    state.copy(profileImage = it.readBytes())
                }
            }
        }
    }

    private fun handleUpdateProfile() = when {
        state.firstNameInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_profile_error_empty_firstname))
            }
        }

        state.lastNameInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_profile_error_empty_lastname))
            }
        }

        state.emailInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_profile_error_empty_email))
            }
        }

        !state.emailInput.isValidEmail() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_profile_error_invalid_email))
            }
        }

        state.phoneNumberInput.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_profile_error_empty_phone))
            }
        }

        state.phoneNumberInput.length < 10 -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_profile_error_invalid_phone_length))
            }
        }

        else -> initiateUpdateProfile()
    }

    private fun initiateUpdateProfile() {
        // updateClient write → SubmitHandler. The repository write returns Unit and throws
        // on failure; the handler maps that to Submitted/Failed. The chain to the image +
        // local-prefs writes is driven from the handler's terminal state.
        submitUpdateProfile.submit {
            clientRepository.updateClient(state.clientId, state.updatedClient)
        }
    }

    /**
     * Runs after [submitUpdateProfile] succeeds. Mirrors the prior success branch:
     * upload the picked image first (when present), then persist the local profile.
     * When no image was picked, go straight to the local-prefs write.
     */
    private fun onProfileUpdated() {
        val image = state.profileImage
        if (image != null) {
            submitUpdateImage.submit {
                clientRepository.updateClientImage(state.clientId, image.decodeToString())
            }
        } else {
            submitClientProfileUpdate()
        }
    }

    private fun submitClientProfileUpdate() {
        // The local-prefs write returns Unit and throws on failure; the handler maps
        // that to Submitted/Failed (observed in `init`).
        submitClientProfile.submit {
            preferencesRepository.updateClientProfile(state.updatedClient)
        }
    }
}

@Serializable
internal data class EditProfileState(
    val clientId: Long,
    val firstNameInput: String,
    val lastNameInput: String,
    val phoneNumberInput: String,
    val emailInput: String,
    val externalIdInput: String,
    val profileImage: ByteArray? = null,
    val dialogState: DialogState? = null,
) {
    @Transient
    internal val updatedClient = UpdatedClient(
        firstname = this.firstNameInput,
        lastname = this.lastNameInput,
        emailAddress = this.emailInput,
        mobileNo = this.phoneNumberInput,
        externalId = this.externalIdInput,
    )

    @Serializable
    sealed interface DialogState {
        @Serializable
        data object Loading : DialogState

        @Serializable
        sealed class Error : DialogState {
            @Serializable
            data class StringMessage(val message: String) : Error()

            @Serializable
            data class ResourceMessage(
                @Serializable(with = StringResourceSerializer::class)
                val message: StringResource,
            ) : Error()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EditProfileState

        if (clientId != other.clientId) return false
        if (firstNameInput != other.firstNameInput) return false
        if (lastNameInput != other.lastNameInput) return false
        if (phoneNumberInput != other.phoneNumberInput) return false
        if (emailInput != other.emailInput) return false
        if (externalIdInput != other.externalIdInput) return false
        if (!profileImage.contentEquals(other.profileImage)) return false
        if (dialogState != other.dialogState) return false
        if (updatedClient != other.updatedClient) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientId.hashCode()
        result = 31 * result + firstNameInput.hashCode()
        result = 31 * result + lastNameInput.hashCode()
        result = 31 * result + phoneNumberInput.hashCode()
        result = 31 * result + emailInput.hashCode()
        result = 31 * result + externalIdInput.hashCode()
        result = 31 * result + (profileImage?.contentHashCode() ?: 0)
        result = 31 * result + (dialogState?.hashCode() ?: 0)
        result = 31 * result + updatedClient.hashCode()
        return result
    }
}

/** One-shot events emitted by the edit-profile ViewModel. */
sealed interface EditProfileEvent {
    /** Event requesting navigation back. */
    data object NavigateBack : EditProfileEvent

    /**
     * Event requesting a toast message to be shown.
     *
     * @property message String resource of the toast message.
     */
    data class ShowToast(val message: StringResource) : EditProfileEvent
}

/** Actions dispatched to the edit-profile ViewModel. */
sealed interface EditProfileAction {
    /**
     * User edited the first name field.
     *
     * @property firstName New first name input.
     */
    data class FirstNameInputChange(val firstName: String) : EditProfileAction

    /**
     * User edited the last name field.
     *
     * @property lastName New last name input.
     */
    data class LastNameInputChange(val lastName: String) : EditProfileAction

    /**
     * User edited the phone number field.
     *
     * @property phoneNumber New phone number input.
     */
    data class PhoneNumberInputChange(val phoneNumber: String) : EditProfileAction

    /**
     * User edited the email field.
     *
     * @property email New email input.
     */
    data class EmailInputChange(val email: String) : EditProfileAction

    /**
     * User edited the external id field.
     *
     * @property externalId New external id input.
     */
    data class ExternalIdInputChange(val externalId: String) : EditProfileAction

    /** User dismissed the error dialog. */
    data object DismissErrorDialog : EditProfileAction

    /** User requested to navigate back. */
    data object NavigateBack : EditProfileAction

    /** User confirmed saving the profile changes. */
    data object UpdateProfile : EditProfileAction

    /** User tapped to pick a new profile image. */
    data object PickProfileImage : EditProfileAction

    /** Actions dispatched internally by the ViewModel rather than the UI. */
    sealed interface Internal : EditProfileAction {
        /**
         * Requests loading the image of a client.
         *
         * @property clientId Id of the client whose image is loaded.
         */
        data class LoadClientImage(val clientId: Long) : Internal

        /**
         * Client-image load result. Uses [ScreenState] (not the legacy DataState) — the
         * `getClientImage` read path was migrated to `Flow<ScreenState<String>>`
         * during the Phase-3 cutover. The one-shot profile writes no longer have
         * result actions here: `updateClient` / `updateClientImage` /
         * `updateClientProfile` now go through SubmitHandlers in the ViewModel.
         *
         * @property result Screen state carrying the loaded client image.
         */
        data class HandleLoadClientImageResult(val result: ScreenState<String>) : Internal
    }
}
