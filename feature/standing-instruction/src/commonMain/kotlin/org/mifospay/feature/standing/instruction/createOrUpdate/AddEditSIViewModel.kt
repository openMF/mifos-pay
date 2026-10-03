/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.feature.standing.instruction.createOrUpdate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import mifos_pay.feature.standing_instruction.generated.resources.Res
import mifos_pay.feature.standing_instruction.generated.resources.feature_standing_instruction_created_successfully
import mifos_pay.feature.standing_instruction.generated.resources.feature_standing_instruction_updated_successfully
import org.jetbrains.compose.resources.StringResource
import org.mifospay.core.common.DateHelper
import org.mifospay.core.common.ScreenState
import org.mifospay.core.common.getSerialized
import org.mifospay.core.common.setSerialized
import org.mifospay.core.data.repository.ClientRepository
import org.mifospay.core.data.repository.LocalAssetRepository
import org.mifospay.core.data.repository.StandingInstructionRepository
import org.mifospay.core.datastore.UserPreferencesRepository
import org.mifospay.core.model.account.Account
import org.mifospay.core.model.client.Client
import org.mifospay.core.model.standinginstruction.SITemplate
import org.mifospay.core.model.standinginstruction.StandingInstruction
import org.mifospay.core.model.standinginstruction.StandingInstructionPayload
import org.mifospay.core.model.standinginstruction.toSIUploadPayload
import org.mifospay.core.ui.utils.BaseViewModel
import org.mifospay.feature.standing.instruction.createOrUpdate.AddEditSIAction.Internal.HandleTemplateResult
import org.mifospay.feature.standing.instruction.createOrUpdate.AddEditSIAction.Internal.LoadClientAccount
import org.mifospay.feature.standing.instruction.createOrUpdate.AddEditSIState.DialogState.Error
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal class AddEditSIViewModel(
    private val repository: StandingInstructionRepository,
    private val userRepository: UserPreferencesRepository,
    private val clientRepository: ClientRepository,
    localRepository: LocalAssetRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AddEditSIState, AddEditSIEvent, AddEditSIAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: run {
        val client = requireNotNull(userRepository.client.value)
        val type = SIAddEditArgs(savedStateHandle).addEditType

        AddEditSIState(
            client = client,
            type = type,
            viewState = AddEditSIState.ViewState.Loading,
        )
    },
) {

    companion object {
        private const val KEY_STATE = "add_edit_si_state"
    }

    val localList = localRepository.localeList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _toClientAccounts = MutableStateFlow<List<Account>>(emptyList())
    val toClientAccounts = _toClientAccounts.asStateFlow()

    // Template idiom (core-base/store): the create/update WRITE goes through a
    // SubmitHandler instead of a hand-folded DataState result action. The handler
    // owns the Submitting/Submitted/Failed lifecycle; we observe it to drive this
    // screen's existing loading/error dialog + success toast + navigate-back.
    private val submitSI = viewModelScope.submitHandler<Unit>()

    init {
        submitSI.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = AddEditSIState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        val successMessage = when (state.type) {
                            is SIAddEditType.AddItem ->
                                Res.string.feature_standing_instruction_created_successfully

                            is SIAddEditType.EditItem ->
                                Res.string.feature_standing_instruction_updated_successfully
                        }
                        sendEvent(AddEditSIEvent.ShowToast(successMessage))
                        sendEvent(AddEditSIEvent.OnNavigateBack)
                        submitSI.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(dialogState = Error(message))
                        }
                        submitSI.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)

        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        repository.getStandingInstructionTemplate(
            fromOfficeId = state.client.officeId,
            fromClientId = state.client.id,
            fromAccountType = state.client.savingsProductName.toLong(),
        ).onEach {
            sendAction(HandleTemplateResult(it))
        }.launchIn(viewModelScope)
    }

    override fun handleAction(action: AddEditSIAction) {
        when (action) {
            is AddEditSIAction.FromAccountChanged -> updatePayload {
                it.copy(fromAccountId = action.accountId.toLong())
            }

            is AddEditSIAction.FromAccountTypeChanged -> updatePayload {
                it.copy(fromAccountType = action.accountType)
            }

            is AddEditSIAction.FromClientChanged -> updatePayload {
                it.copy(fromClientId = action.clientId.toLong())
            }

            is AddEditSIAction.FromOfficeChanged -> updatePayload {
                it.copy(fromOfficeId = action.officeId.toLong())
            }

            is AddEditSIAction.ToAccountChanged -> updatePayload {
                it.copy(toAccountId = action.accountId.toLong())
            }

            is AddEditSIAction.ToAccountTypeChanged -> updatePayload {
                it.copy(toAccountType = action.accountType)
            }

            is AddEditSIAction.ToClientChanged -> {
                updatePayload {
                    it.copy(toClientId = action.clientId.toLong())
                }

                trySendAction(LoadClientAccount(action.clientId.toLong()))
            }

            is AddEditSIAction.ToOfficeChanged -> updatePayload {
                it.copy(toOfficeId = action.officeId.toLong())
            }

            is AddEditSIAction.NameChanged -> updatePayload {
                it.copy(name = action.name)
            }

            is AddEditSIAction.AmountChanged -> updatePayload {
                it.copy(amount = action.amount)
            }

            is AddEditSIAction.TransferTypeChanged -> updatePayload {
                it.copy(transferType = action.transferType.toLong())
            }

            is AddEditSIAction.InstructionTypeChanged -> updatePayload {
                it.copy(instructionType = action.instructionType.toLong())
            }

            is AddEditSIAction.PriorityChanged -> updatePayload {
                it.copy(priority = action.priority.toLong())
            }

            is AddEditSIAction.StatusChanged -> updatePayload {
                it.copy(status = action.status.toLong())
            }

            is AddEditSIAction.ValidFromChanged -> updatePayload {
                val date = DateHelper.getDateAsStringFromLong(action.validFrom)
                it.copy(validFrom = date)
            }

            is AddEditSIAction.ValidTillChanged -> updatePayload {
                val date = DateHelper.getDateAsStringFromLong(action.validTill)
                it.copy(validTill = date)
            }

            is AddEditSIAction.RecurrenceTypeChanged -> updatePayload {
                it.copy(recurrenceType = action.recurrenceType.toLong())
            }

            is AddEditSIAction.RecurrenceFrequencyChanged -> updatePayload {
                it.copy(recurrenceFrequency = action.recurrenceFrequency.toLong())
            }

            is AddEditSIAction.RecurrenceIntervalChanged -> updatePayload {
                it.copy(recurrenceInterval = action.recurrenceInterval)
            }

            is AddEditSIAction.RecurrenceOnMonthDayChanged -> updatePayload {
                val date = DateHelper.getMonthAsStringFromLong(action.recurrenceOnMonthDay)
                it.copy(recurrenceOnMonthDay = date)
            }

            is AddEditSIAction.LocaleChanged -> updatePayload {
                it.copy(locale = action.locale)
            }

            is AddEditSIAction.MonthDayFormatChanged -> updatePayload {
                it.copy(monthDayFormat = action.monthDayFormat)
            }

            is AddEditSIAction.DateFormatChanged -> updatePayload {
                it.copy(dateFormat = action.dateFormat)
            }

            AddEditSIAction.NavigateBack -> {
                sendEvent(AddEditSIEvent.OnNavigateBack)
            }

            AddEditSIAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            AddEditSIAction.SubmitClicked -> initiateAddEditSI()

            is HandleTemplateResult -> handleTemplateResult(action)

            is LoadClientAccount -> handleClientAccountResult(action)

            is AddEditSIAction.Internal.HandleSIResult -> handleSIResult(action)
        }
    }

    private fun initiateAddEditSI() = onContent {
        onPayload { data ->
            when {
                data.fromOfficeId == 0L && state.isAddMode -> showError("From Office is Required")
                data.fromClientId == 0L && state.isAddMode -> showError("From Client is Required")
                data.fromAccountType.isEmpty() && state.isAddMode -> showError("From Account Type is Required")
                data.fromAccountId == 0L && state.isAddMode -> showError("From Account is Required")

                data.toOfficeId == 0L && state.isAddMode -> showError("To Office is Required")
                data.toClientId == 0L && state.isAddMode -> showError("To Client is Required")
                data.toAccountType.isEmpty() && state.isAddMode -> showError("To Account Type is Required")
                data.toAccountId == 0L && state.isAddMode -> showError("To Account is Required")

                data.name.isBlank() && state.isAddMode -> showError("Name is Required")

                data.amount.isBlank() && state.isAddMode -> showError("Amount is Required")
                data.amount.toDoubleOrNull() == null && state.isAddMode -> showError("Amount is Invalid")
                data.amount.toDouble() <= 0 && state.isAddMode -> showError("Amount must be greater than 0")
                data.transferType == 0L && state.isAddMode -> showError("Transfer Type is Required")

                data.instructionType == 0L && state.isAddMode -> showError("Instruction Type is Required")
                data.priority == 0L -> showError("Priority is Required")
                data.status == 0L -> showError("Status is Required")
                data.validFrom.isBlank() -> showError("Valid From is Required")

                data.recurrenceType == 0L && state.isAddMode -> showError("Recurrence Type is Required")

                data.recurrenceFrequency == 0L &&
                    it.requiredRecurrenceFrequency && state.isAddMode -> {
                    showError("Recurrence Frequency is Required")
                }

                data.recurrenceInterval.isBlank() &&
                    it.requiredRecurrenceFrequency && state.isAddMode -> {
                    showError("Recurrence Interval is Required")
                }

                data.recurrenceInterval.any { !it.isDigit() } &&
                    it.requiredRecurrenceFrequency && state.isAddMode -> {
                    showError("Recurrence Interval is Invalid")
                }

                data.recurrenceOnMonthDay.isBlank() &&
                    it.requiredRecurrenceOnMonth && state.isAddMode -> {
                    showError("Recurrence On Month Day is Required")
                }

                data.locale.isBlank() -> showError("Locale is Required")

                data.dateFormat.isBlank() -> showError("Date Format is Required")

                data.monthDayFormat.isBlank() && state.isAddMode -> {
                    showError("Month Day Format is Required")
                }

                else -> initiateSubmitSI()
            }
        }
    }

    private fun initiateSubmitSI() = onContent { content ->
        // Submit through the handler — it drives Submitting/Submitted/Failed,
        // observed in `init`. The repository write completes normally on success
        // and throws on failure; the handler maps that to Submitted/Failed.
        submitSI.submit {
            when (state.type) {
                is SIAddEditType.AddItem -> {
                    repository.createStandingInstruction(content.payload)
                }

                is SIAddEditType.EditItem -> {
                    val insId = requireNotNull(state.type.standingInsId)
                    val payload = content.payload.toSIUploadPayload()

                    repository.updateStandingInstruction(insId, payload)
                }
            }
        }
    }

    private fun handleTemplateResult(action: HandleTemplateResult) {
        // `getStandingInstructionTemplate` was migrated to
        // `Flow<ScreenState<SITemplate>>` in Phase-3. Content builds the
        // initial payload; Empty is defensively surfaced as an error (a
        // template endpoint shouldn't emit Empty). NoNetwork / Unauthenticated
        // fold into the existing Error surface until Phase-4 differentiates.
        when (val result = action.result) {
            is ScreenState.Loading -> {
                mutableStateFlow.update {
                    it.copy(viewState = AddEditSIState.ViewState.Loading)
                }
            }

            is ScreenState.Empty -> {
                mutableStateFlow.update {
                    it.copy(viewState = AddEditSIState.ViewState.Error("Template not available."))
                }
            }

            is ScreenState.Error -> {
                val message = result.error.message.toString()
                mutableStateFlow.update {
                    it.copy(viewState = AddEditSIState.ViewState.Error(message))
                }
            }

            is ScreenState.NoNetwork -> {
                mutableStateFlow.update {
                    it.copy(
                        viewState = AddEditSIState.ViewState.Error(
                            "No network. Please check your connection.",
                        ),
                    )
                }
            }

            is ScreenState.Unauthenticated -> {
                mutableStateFlow.update {
                    it.copy(
                        viewState = AddEditSIState.ViewState.Error(
                            "Session expired. Please log in again.",
                        ),
                    )
                }
            }

            is ScreenState.Content -> {
                val template = result.data
                mutableStateFlow.update {
                    it.copy(
                        viewState = AddEditSIState.ViewState.Content(
                            template = template,
                            payload = if (state.isAddMode) {
                                StandingInstructionPayload(
                                    fromClientId = template.fromClient?.id ?: 0L,
                                    fromOfficeId = template.fromOffice?.id ?: 0L,
                                    fromAccountType = template.fromAccountType ?: "",
                                    fromAccountId = template.fromAccountOptions?.firstOrNull()?.id ?: -1,

                                    toOfficeId = template.toOfficeOptions?.firstOrNull()?.id ?: -1,
                                    toAccountType = template.fromAccountType ?: "",
                                    toClientId = 0,
                                    toAccountId = 0,

                                    name = "",
                                    amount = "",
                                    transferType = template.transferTypeOptions?.firstOrNull()?.id ?: -1,
                                    instructionType = template.instructionTypeOptions?.firstOrNull()?.id ?: -1,
                                    priority = template.priorityOptions?.firstOrNull()?.id,
                                    status = template.statusOptions?.firstOrNull()?.id,
                                    recurrenceType = template.recurrenceTypeOptions?.firstOrNull()?.id ?: -1,
                                    recurrenceFrequency = template.recurrenceFrequencyOptions?.firstOrNull()?.id ?: -1,
                                    recurrenceInterval = "",

                                    locale = "en_IN",
                                    validFrom = DateHelper.formattedShortDate,
                                    validTill = DateHelper.formattedShortDate,
                                    dateFormat = DateHelper.SHORT_MONTH,
                                    monthDayFormat = DateHelper.MONTH_FORMAT,
                                    recurrenceOnMonthDay = "",
                                )
                            } else {
                                StandingInstructionPayload()
                            },
                        ),
                    )
                }

                if (!state.isAddMode) {
                    val insId = requireNotNull(state.type.standingInsId)

                    repository.getStandingInstruction(insId).onEach { screenState ->
                        sendAction(AddEditSIAction.Internal.HandleSIResult(screenState))
                    }.launchIn(viewModelScope)
                }
            }
        }
    }

    private fun handleClientAccountResult(action: LoadClientAccount) {
        mutableStateFlow.update {
            it.copy(dialogState = AddEditSIState.DialogState.Loading)
        }

        // `clientRepository.getAccounts` was migrated to
        // `Flow<ScreenState<List<Account>>>` in Phase-3. Content updates the
        // to-client account picker; Empty explicitly emits an empty list AND
        // clears the loading dialog (semantically "no savings accounts").
        // NoNetwork / Unauthenticated fold into the existing error dialog.
        viewModelScope.launch {
            clientRepository.getAccounts(
                clientId = action.clientId,
                accountType = "savingsAccounts",
            ).collectLatest { result ->
                when (result) {
                    is ScreenState.Loading -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = AddEditSIState.DialogState.Loading)
                        }
                    }

                    is ScreenState.Empty -> {
                        sendAction(AddEditSIAction.DismissDialog)
                        _toClientAccounts.update { emptyList() }
                    }

                    is ScreenState.Error -> showError(result.error.message.toString())

                    is ScreenState.NoNetwork ->
                        showError("No network. Please check your connection.")

                    is ScreenState.Unauthenticated ->
                        showError("Session expired. Please log in again.")

                    is ScreenState.Content -> {
                        sendAction(AddEditSIAction.DismissDialog)
                        _toClientAccounts.update { result.data }
                    }
                }
            }
        }
    }

    private fun handleSIResult(action: AddEditSIAction.Internal.HandleSIResult) {
        // `getStandingInstruction` was migrated to
        // `Flow<ScreenState<StandingInstruction>>` in Phase-3. Content
        // hydrates the edit-mode form; Empty is defensively surfaced as an
        // error (single-record endpoint shouldn't emit Empty). NoNetwork /
        // Unauthenticated fold into the existing error dialog.
        when (val result = action.result) {
            is ScreenState.Loading -> {
                mutableStateFlow.update {
                    it.copy(dialogState = AddEditSIState.DialogState.Loading)
                }
            }

            is ScreenState.Empty -> {
                mutableStateFlow.update {
                    it.copy(dialogState = Error("Standing instruction not found."))
                }
            }

            is ScreenState.Error -> {
                val message = result.error.message.toString()
                mutableStateFlow.update {
                    it.copy(dialogState = Error(message))
                }
            }

            is ScreenState.NoNetwork -> {
                mutableStateFlow.update {
                    it.copy(dialogState = Error("No network. Please check your connection."))
                }
            }

            is ScreenState.Unauthenticated -> {
                mutableStateFlow.update {
                    it.copy(dialogState = Error("Session expired. Please log in again."))
                }
            }

            is ScreenState.Content -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }

                updateSIResult(result.data)
            }
        }
    }

    private fun updateSIResult(result: StandingInstruction) {
        viewModelScope.launch {
            updatePayload { payload ->
                payload.copy(
                    priority = result.priority?.id,
                    status = result.status?.id,
                    locale = "en_IN",
                    validFrom = result.validFrom ?: "",
                    validTill = result.validTill ?: "",
                    dateFormat = DateHelper.SHORT_MONTH,
                )
            }
        }
    }

    private inline fun onContent(
        crossinline block: (AddEditSIState.ViewState.Content) -> Unit,
    ) {
        (state.viewState as? AddEditSIState.ViewState.Content)?.let(block)
    }

    private inline fun onPayload(
        crossinline block: (StandingInstructionPayload) -> Unit,
    ) {
        (state.viewState as? AddEditSIState.ViewState.Content)?.let { content ->
            block(content.payload)
        }
    }

    private inline fun updateContent(
        crossinline block: (
            AddEditSIState.ViewState.Content,
        ) -> AddEditSIState.ViewState.Content?,
    ) {
        val currentViewState = state.viewState
        val updatedContent = (currentViewState as? AddEditSIState.ViewState.Content)
            ?.let(block)
            ?: return
        mutableStateFlow.update { it.copy(viewState = updatedContent) }
    }

    private inline fun updatePayload(
        crossinline block: (StandingInstructionPayload) -> StandingInstructionPayload,
    ) {
        updateContent { content ->
            content.copy(payload = block(content.payload))
        }
    }

    private fun showError(message: String) {
        mutableStateFlow.update {
            it.copy(dialogState = Error(message))
        }
    }
}

@Serializable
internal data class AddEditSIState(
    val client: Client,
    val type: SIAddEditType,
    val viewState: ViewState,
    @Transient
    val dialogState: DialogState? = null,
) {

    @Transient
    val isAddMode = type is SIAddEditType.AddItem

    val title: String get() = if (isAddMode) "Create New Instruction" else "Update Instruction"

    @Serializable
    sealed interface ViewState {
        @Serializable
        data object Loading : ViewState

        @Serializable
        data class Error(val message: String) : ViewState

        @Serializable
        data class Content(
            val template: SITemplate,
            val payload: StandingInstructionPayload,
        ) : ViewState {

            @Transient
            val fromOfficeName = template.fromOffice?.name

            @Transient
            val fromAccountType = template.fromAccountType

            @Transient
            val fromAccountNumber = template
                .fromAccountOptions?.firstOrNull { it.id == payload.fromAccountId }?.accountNo ?: ""

            @Transient
            val toClientOptions = template.fromClientOptions
                ?.filter { it.id != payload.fromClientId }

            @Transient
            val toClientName = template
                .fromClientOptions?.firstOrNull { it.id == payload.toClientId }?.displayName ?: ""

            @Transient
            val toOfficeName = template.fromOffice?.name

            @Transient
            val toAccountType = template.fromAccountType

            @Transient
            val transferType = template.transferTypeOptions?.firstOrNull {
                it.id == payload.transferType
            }?.value ?: ""

            @Transient
            val instructionType = template.instructionTypeOptions?.firstOrNull {
                it.id == payload.instructionType
            }?.value ?: ""

            @Transient
            val priority = template.priorityOptions?.firstOrNull {
                it.id == payload.priority
            }?.value ?: ""

            @Transient
            val status = template.statusOptions?.firstOrNull {
                it.id == payload.status
            }?.value ?: ""

            @Transient
            val recurrenceType = template.recurrenceTypeOptions?.firstOrNull {
                it.id == payload.recurrenceType
            }?.value ?: ""

            @Transient
            val recurrenceFrequency = template.recurrenceFrequencyOptions?.firstOrNull {
                it.id == payload.recurrenceFrequency
            }?.value ?: ""

            @OptIn(ExperimentalTime::class)
            @Transient
            val initialDate = Clock.System.now().toEpochMilliseconds()

            @Transient
            val requiredRecurrenceFrequency = recurrenceType == "Periodic Recurrence"

            @Transient
            val requiredRecurrenceOnMonth =
                recurrenceFrequency == "Months" || recurrenceFrequency == "Years"
        }
    }

    sealed interface DialogState {
        data object Loading : DialogState
        data class Error(val message: String) : DialogState
    }
}

sealed interface AddEditSIEvent {
    data class ShowToast(val message: StringResource) : AddEditSIEvent
    data object OnNavigateBack : AddEditSIEvent
}

sealed interface AddEditSIAction {

    data class FromOfficeChanged(val officeId: String) : AddEditSIAction
    data class FromClientChanged(val clientId: String) : AddEditSIAction
    data class FromAccountTypeChanged(val accountType: String) : AddEditSIAction
    data class FromAccountChanged(val accountId: String) : AddEditSIAction

    data class ToOfficeChanged(val officeId: String) : AddEditSIAction
    data class ToClientChanged(val clientId: String) : AddEditSIAction
    data class ToAccountTypeChanged(val accountType: String) : AddEditSIAction
    data class ToAccountChanged(val accountId: String) : AddEditSIAction

    data class NameChanged(val name: String) : AddEditSIAction
    data class AmountChanged(val amount: String) : AddEditSIAction
    data class TransferTypeChanged(val transferType: String) : AddEditSIAction
    data class InstructionTypeChanged(val instructionType: String) : AddEditSIAction
    data class PriorityChanged(val priority: String) : AddEditSIAction
    data class StatusChanged(val status: String) : AddEditSIAction
    data class RecurrenceTypeChanged(val recurrenceType: String) : AddEditSIAction
    data class RecurrenceIntervalChanged(val recurrenceInterval: String) : AddEditSIAction
    data class RecurrenceFrequencyChanged(val recurrenceFrequency: String) : AddEditSIAction

    data class LocaleChanged(val locale: String) : AddEditSIAction
    data class DateFormatChanged(val dateFormat: String) : AddEditSIAction
    data class ValidFromChanged(val validFrom: Long) : AddEditSIAction
    data class ValidTillChanged(val validTill: Long) : AddEditSIAction
    data class MonthDayFormatChanged(val monthDayFormat: String) : AddEditSIAction
    data class RecurrenceOnMonthDayChanged(val recurrenceOnMonthDay: Long) : AddEditSIAction

    data object DismissDialog : AddEditSIAction
    data object NavigateBack : AddEditSIAction
    data object SubmitClicked : AddEditSIAction

    sealed interface Internal : AddEditSIAction {
        /**
         * Template load result. Uses [ScreenState] — Phase-3 migrated
         * `getStandingInstructionTemplate` to a ScreenState stream.
         */
        data class HandleTemplateResult(val result: ScreenState<SITemplate>) : Internal
        data class LoadClientAccount(val clientId: Long) : Internal

        /**
         * Existing-SI load result (edit mode). Uses [ScreenState] — Phase-3
         * migrated `getStandingInstruction` to a ScreenState stream.
         */
        data class HandleSIResult(val result: ScreenState<StandingInstruction>) : Internal
    }
}
