/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.kyc

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.DocumentRepository
import kpt.core.data.util.Constants
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.kyc.KycLevel2State.DialogState.Error
import mifos_pay.feature.kyc.generated.resources.Res
import mifos_pay.feature.kyc.generated.resources.feature_kyc_successkyc2
import org.jetbrains.compose.resources.StringResource
internal class KYCLevel2ViewModel(
    private val repository: DocumentRepository,
    private val userRepository: ForkPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<KycLevel2State, KycLevel2Event, KycLevel2Action>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)

        KycLevel2State(entityId = clientId)
    },
) {

    companion object {
        private const val KEY_STATE = "kyc_level_2_state"
    }

    // Template idiom (core-base/store): the one-shot document upload write goes through a
    // SubmitHandler instead of a hand-folded result action. The handler owns the
    // Submitting/Submitted/Failed lifecycle; we observe it below to drive this screen's
    // existing Loading dialog + toast + navigate-to-level-3 UX, so the Screen is unchanged.
    // Writes return Unit; the success toast is a feature StringResource.
    private val submitUpload = viewModelScope.submitHandler<Unit>()

    init {
        submitUpload.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = KycLevel2State.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        sendEvent(KycLevel2Event.ShowToast(Res.string.feature_kyc_successkyc2))
                        sendEvent(KycLevel2Event.OnNavigateToLevel3)
                        submitUpload.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(dialogState = Error(message))
                        }
                        submitUpload.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: KycLevel2Action) {
        when (action) {
            is KycLevel2Action.PickFile -> {
                viewModelScope.launch {
                    val file = FileKit.openFilePicker(mode = FileKitMode.Single)
                    file?.let {
                        mutableStateFlow.update { state ->
                            state.copy(
                                uploadedFile = it.readBytes(),
                                name = it.name,
                                extension = it.extension,
                            )
                        }
                    }
                }
            }

            is KycLevel2Action.NameChanged -> {
                mutableStateFlow.update {
                    it.copy(name = action.name)
                }
            }

            is KycLevel2Action.DescriptionChanged -> {
                mutableStateFlow.update {
                    it.copy(description = action.desc)
                }
            }

            KycLevel2Action.NavigateBack -> {
                sendEvent(KycLevel2Event.OnNavigateBack)
            }

            KycLevel2Action.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            KycLevel2Action.SubmitClicked -> initiateUploadDocument()
        }
    }

    private fun initiateUploadDocument() = when {
        state.uploadedFile == null -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Upload an image or pdf"))
            }
        }

        state.name.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Name is required"))
            }
        }

        state.description.isEmpty() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error("Description is required"))
            }
        }

        else -> uploadDocument()
    }

    /**
     * API call to upload document fails with the following error:
     * Unable to create parent directories
     * of /.fineract/VENUS/documents/clients/2/iwqyn/abc.png
     * This is a server side error, the client side code is correct.
     */
    private fun uploadDocument() {
        mutableStateFlow.update {
            it.copy(dialogState = KycLevel2State.DialogState.Loading)
        }

        val file = state.uploadedFile ?: return

        // Submit through the handler — it drives Submitting/Submitted/Failed, observed in
        // `init`. The write returns Unit and throws on error, so the handler reports Failed.
        submitUpload.submit {
            repository.createDocument(
                entityType = state.entityType,
                entityId = state.entityId,
                name = state.fileName,
                description = state.description,
                file = file,
            )
        }
    }
}

@Serializable
internal data class KycLevel2State(
    val entityId: Long,
    val description: String = "",
    @Transient val uploadedFile: ByteArray? = null,
    val name: String = "",
    val extension: String = "",
    val entityType: String = Constants.ENTITY_TYPE_CLIENTS,
    @Transient
    val dialogState: DialogState? = null,
) {
    @Transient
    val fileName = "$name.$extension"

    sealed interface DialogState {
        data object Loading : DialogState
        data class Error(val message: String) : DialogState
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KycLevel2State

        if (entityId != other.entityId) return false
        if (description != other.description) return false
        if (!uploadedFile.contentEquals(other.uploadedFile)) return false
        if (name != other.name) return false
        if (extension != other.extension) return false
        if (entityType != other.entityType) return false
        if (dialogState != other.dialogState) return false
        if (fileName != other.fileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityId.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (uploadedFile?.contentHashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + entityType.hashCode()
        result = 31 * result + (dialogState?.hashCode() ?: 0)
        result = 31 * result + fileName.hashCode()
        return result
    }
}

internal sealed interface KycLevel2Event {
    data object OnNavigateBack : KycLevel2Event
    data object OnNavigateToLevel3 : KycLevel2Event
    data class ShowToast(val message: StringResource) : KycLevel2Event
}

internal sealed interface KycLevel2Action {
    data class DescriptionChanged(val desc: String) : KycLevel2Action
    data class NameChanged(val name: String) : KycLevel2Action

    data object SubmitClicked : KycLevel2Action

    data object NavigateBack : KycLevel2Action
    data object DismissDialog : KycLevel2Action
    data object PickFile : KycLevel2Action
}
