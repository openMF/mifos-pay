/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.addupdatebeneficiary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.LocalAssetRepository
import kpt.core.data.repository.OfficeRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.beneficiary.BeneficiaryPayload
import kpt.core.model.beneficiary.BeneficiaryUpdatePayload
import kpt.core.model.office.Office
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.beneficiary.addupdatebeneficiary.AEBState.DialogState.Error
import mifos_pay.feature.beneficiary.generated.resources.Res
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_account_type_other
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_account_type_wallet
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_added
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_button_save
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_button_update
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_error_empty_account_number
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_error_empty_beneficiary_name
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_error_empty_office_name
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_error_invalid_transfer_limit
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_error_select_account_type
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_error_select_locale
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_title_add
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_title_update
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_updated
import org.jetbrains.compose.resources.StringResource
@OptIn(ExperimentalCoroutinesApi::class)
internal class AddEditBeneficiaryViewModel(
    private val localAssetRepository: LocalAssetRepository,
    private val repository: SelfServiceRepository,
    private val officeRepository: OfficeRepository,
    private val json: Json,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AEBState, AEBEvent, AEBAction>(
    initialState = savedStateHandle.getSerialized(ADD_EDIT_BENEFICIARY_KEY) ?: run {
        when (val addEditType = BeneficiaryAddEditArgs(savedStateHandle).addEditType) {
            is BeneficiaryAddEditType.AddItem -> {
                // Parse sourceQrData if available (for post-add navigation)
                val sourceQrData = addEditType.sourceQrData?.let { qrDataJson ->
                    try {
                        json.decodeFromString(QrCodeData.serializer(), qrDataJson)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (addEditType.beneficiary != null) {
                    // Pre-filled from QR scan
                    val beneficiary = json.decodeFromString(
                        Beneficiary.serializer(),
                        addEditType.beneficiary,
                    )
                    AEBState(
                        name = beneficiary.name,
                        accountNumber = beneficiary.accountNumber,
                        transferLimit = beneficiary.transferLimit,
                        officeName = beneficiary.officeName,
                        officeId = beneficiary.officeId,
                        addEditType = addEditType,
                        sourceQrType = addEditType.sourceQrType,
                        sourceQrData = sourceQrData,
                    )
                } else {
                    // Empty form for manual add
                    AEBState(
                        name = "",
                        accountNumber = "",
                        transferLimit = 0,
                        addEditType = addEditType,
                        sourceQrType = addEditType.sourceQrType,
                        sourceQrData = sourceQrData,
                    )
                }
            }

            is BeneficiaryAddEditType.EditItem -> {
                val beneficiary = json.decodeFromString(
                    Beneficiary.serializer(),
                    addEditType.beneficiary,
                )

                AEBState(
                    name = beneficiary.name,
                    accountNumber = beneficiary.accountNumber,
                    transferLimit = beneficiary.transferLimit,
                    officeName = beneficiary.officeName,
                    beneficiaryId = beneficiary.id,
                    addEditType = addEditType,
                )
            }
        }
    },
) {

    companion object {
        private const val ADD_EDIT_BENEFICIARY_KEY = "AddEditBeneficiaryViewModel"
    }

    val filteredLocalList = localAssetRepository.localeList.stateIn(
        scope = viewModelScope,
        initialValue = emptyList(),
        started = SharingStarted.WhileSubscribed(5_000),
    )

    // `getOffices` was migrated to a ScreenState stream in Phase-3. This VM
    // only needs the successful list projection — every non-Content branch
    // collapses to an empty list (same behaviour as the prior DataState.else
    // fallthrough).
    val officeList = officeRepository.getOffices()
        .mapLatest { screenState ->
            when (screenState) {
                is ScreenState.Content -> screenState.data
                else -> emptyList()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    // Template idiom (core-base/store): the create/update write goes through a
    // SubmitHandler instead of a hand-folded DataState result action. The handler
    // owns the Submitting/Submitted/Failed lifecycle; we observe it in `init` to
    // drive this screen's existing loading/error dialog + toast + QR navigation,
    // so the Screen is unchanged.
    private val submitBeneficiary = viewModelScope.submitHandler<Unit>()

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = ADD_EDIT_BENEFICIARY_KEY, value = it) }
            .launchIn(viewModelScope)

        submitBeneficiary.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = AEBState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        val successMessage = when (state.addEditType) {
                            is BeneficiaryAddEditType.AddItem ->
                                Res.string.feature_beneficiary_added

                            is BeneficiaryAddEditType.EditItem ->
                                Res.string.feature_beneficiary_updated
                        }
                        sendEvent(AEBEvent.ShowToast(successMessage))
                        navigateAfterSave()
                        submitBeneficiary.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = extractMifosErrorMessage(submitState.error)
                        mutableStateFlow.update {
                            it.copy(dialogState = Error.StringMessage(message))
                        }
                        submitBeneficiary.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        // Resolve office name from officeId when office list is loaded
        officeList
            .onEach { offices ->
                val currentState = state

                if (
                    currentState.officeName.isBlank() &&
                    currentState.officeId == null &&
                    offices.isNotEmpty()
                ) {
                    val firstOffice = offices.first()

                    mutableStateFlow.update {
                        it.copy(
                            officeName = firstOffice.name,
                            officeId = firstOffice.id,
                        )
                    }
                    return@onEach
                }

                val officeId = currentState.officeId
                if (officeId != null) {
                    offices.find { it.id == officeId }?.let { office ->
                        mutableStateFlow.update {
                            it.copy(officeName = office.name)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: AEBAction) {
        when (action) {
            is AEBAction.ChangeName -> {
                mutableStateFlow.update {
                    it.copy(name = action.name)
                }
            }

            is AEBAction.ChangeOffice -> {
                mutableStateFlow.update {
                    it.copy(
                        officeName = action.office.name,
                        officeId = action.office.id,
                    )
                }
            }

            is AEBAction.ChangeTransferLimit -> {
                mutableStateFlow.update {
                    it.copy(transferLimit = action.transferLimit.toIntOrNull() ?: 0)
                }
            }

            is AEBAction.ChangeAccountNumber -> {
                mutableStateFlow.update {
                    it.copy(accountNumber = action.accountNumber)
                }
            }

            is AEBAction.ChangeAccountType -> {
                mutableStateFlow.update {
                    it.copy(accountType = action.accountType)
                }
            }

            is AEBAction.ChangeLocale -> {
                mutableStateFlow.update {
                    it.copy(locale = action.locale)
                }
            }

            is AEBAction.ChangeOfficeName -> {
                mutableStateFlow.update {
                    it.copy(officeName = action.officeName)
                }
            }

            AEBAction.NavigateBack -> {
                sendEvent(AEBEvent.NavigateBack)
            }

            AEBAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            AEBAction.SaveBeneficiary -> initiateSaveBeneficiary()

            AEBAction.OnQrScanClicked -> sendEvent(AEBEvent.NavigateToQr)
        }
    }

    private fun initiateSaveBeneficiary() = when {
        state.name.isBlank() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_beneficiary_error_empty_beneficiary_name))
            }
        }

        state.accountNumber.isBlank() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_beneficiary_error_empty_account_number))
            }
        }

        state.transferLimit <= 0 -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_beneficiary_error_invalid_transfer_limit))
            }
        }

        state.locale.isBlank() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_beneficiary_error_select_locale))
            }
        }

        state.officeName.isBlank() -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_beneficiary_error_empty_office_name))
            }
        }

        state.accountType == 0 -> {
            mutableStateFlow.update {
                it.copy(dialogState = Error.ResourceMessage(Res.string.feature_beneficiary_error_select_account_type))
            }
        }

        else -> handleSaveBeneficiary()
    }

    private fun handleSaveBeneficiary() {
        mutableStateFlow.update {
            it.copy(dialogState = AEBState.DialogState.Loading)
        }

        when (val addEditType = state.addEditType) {
            is BeneficiaryAddEditType.AddItem -> {
                val payload = BeneficiaryPayload(
                    name = state.name,
                    accountNumber = state.accountNumber,
                    transferLimit = state.transferLimit,
                    locale = state.locale,
                    officeName = state.officeName,
                    accountType = state.accountType,
                )

                // Submit through the handler — it drives Submitting/Submitted/Failed,
                // observed in `init`. The repository write returns Unit and throws on
                // failure; the handler maps that to Submitted/Failed.
                submitBeneficiary.submit {
                    repository.createBeneficiary(payload)
                }
            }

            is BeneficiaryAddEditType.EditItem -> {
                val beneficiaryId = state.beneficiaryId ?: return
                val payload = BeneficiaryUpdatePayload(
                    name = state.name,
                    transferLimit = state.transferLimit,
                )

                submitBeneficiary.submit {
                    repository.updateBeneficiary(beneficiaryId, payload)
                }
            }
        }
    }

    /**
     * Reproduces the post-save navigation the DataState.Success fold used to drive:
     * QR-source-aware routing to intra/inter-bank transfer, or back otherwise.
     */
    private fun navigateAfterSave() {
        val qrData = state.sourceQrData
        when (state.sourceQrType) {
            QrCodeType.INTRA_BANK -> {
                if (qrData != null) {
                    // Use full QR data for navigation to TransferConfirm
                    sendEvent(
                        AEBEvent.NavigateToIntraBankTransfer(
                            officeId = qrData.officeId.toInt(),
                            clientId = qrData.clientId,
                            accountTypeId = qrData.accountTypeId.toInt(),
                            accountId = qrData.accountId.toInt(),
                            amount = qrData.amount.toIntOrNull() ?: 0,
                            accountName = qrData.clientName,
                            accountNo = qrData.accountNo,
                        ),
                    )
                } else {
                    // Fallback: no QR data available, go back
                    sendEvent(AEBEvent.NavigateBack)
                }
            }
            QrCodeType.INTER_BANK -> {
                sendEvent(
                    AEBEvent.NavigateToInterbankTransfer(
                        accountNumber = state.accountNumber,
                        recipientName = state.name,
                    ),
                )
            }
            else -> {
                // No QR source or other types - just go back
                sendEvent(AEBEvent.NavigateBack)
            }
        }
    }

    /**
     * Extract readable error message from MifosError JSON structure.
     *
     * Server errors come as JSON: {"defaultUserMessage":"...", ...}
     * This extracts the user-friendly message instead of showing raw JSON.
     */
    private fun extractMifosErrorMessage(exception: Throwable): String {
        val rawMessage = exception.message ?: return "Unknown error"

        // Try to parse MifosError JSON structure
        return try {
            // Pattern: {"defaultUserMessage":"...", ...}
            val regex = """"defaultUserMessage"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(rawMessage)?.groupValues?.get(1) ?: rawMessage
        } catch (e: Exception) {
            rawMessage
        }
    }
}

@Serializable
internal data class AEBState(
    val addEditType: BeneficiaryAddEditType,
    val name: String,
    val accountNumber: String,
    val transferLimit: Int,
    val locale: String = "en_US",
    val officeName: String = "",
    val officeId: Long? = null,
    val accountType: Int = SAVINGS_ACC_ID,
    val beneficiaryId: Long? = null,
    val sourceQrType: QrCodeType? = null,
    val sourceQrData: QrCodeData? = null,
    @Transient
    val dialogState: DialogState? = null,
) {
    private val isAddMode: Boolean
        get() = addEditType is BeneficiaryAddEditType.AddItem

    val btnText: StringResource
        get() = if (isAddMode) {
            Res.string.feature_beneficiary_button_save
        } else {
            Res.string.feature_beneficiary_button_update
        }

    val title: StringResource
        get() = if (isAddMode) {
            Res.string.feature_beneficiary_title_add
        } else {
            Res.string.feature_beneficiary_title_update
        }

    val accountTypeName: StringResource
        get() = if (accountType == SAVINGS_ACC_ID) {
            Res.string.feature_beneficiary_account_type_wallet
        } else {
            Res.string.feature_beneficiary_account_type_other
        }

    sealed interface DialogState {
        data object Loading : DialogState
        sealed interface Error : DialogState {
            data class StringMessage(val message: String) : Error
            data class ResourceMessage(val message: StringResource) : Error
        }
    }

    companion object {
        const val SAVINGS_ACC_ID = 2
        val DEFAULT_OFFICE = Office(id = 1, name = "Head Office", nameDecorated = "Head Office")
    }
}

internal sealed interface AEBEvent {
    data object NavigateBack : AEBEvent
    data object NavigateToQr : AEBEvent
    data class ShowToast(val message: StringResource) : AEBEvent

    /**
     * Navigate to intra-bank transfer screen after successfully adding beneficiary from QR scan.
     * Contains all QR data needed for navigating to TransferConfirm screen.
     */
    data class NavigateToIntraBankTransfer(
        val officeId: Int,
        val clientId: Long,
        val accountTypeId: Int,
        val accountId: Int,
        val amount: Int,
        val accountName: String,
        val accountNo: String,
    ) : AEBEvent

    /**
     * Navigate to inter-bank transfer screen after successfully adding beneficiary from QR scan.
     * Contains phone number/external ID for participant lookup.
     */
    data class NavigateToInterbankTransfer(
        val accountNumber: String,
        val recipientName: String,
    ) : AEBEvent
}

internal sealed interface AEBAction {
    data class ChangeLocale(val locale: String) : AEBAction
    data class ChangeName(val name: String) : AEBAction

    data class ChangeOffice(val office: Office) : AEBAction
    data class ChangeOfficeName(val officeName: String) : AEBAction
    data class ChangeAccountNumber(val accountNumber: String) : AEBAction
    data class ChangeAccountType(val accountType: Int) : AEBAction
    data class ChangeTransferLimit(val transferLimit: String) : AEBAction
    data object OnQrScanClicked : AEBAction

    data object DismissDialog : AEBAction
    data object NavigateBack : AEBAction
    data object SaveBeneficiary : AEBAction
}
