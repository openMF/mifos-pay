/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.accounts.savingsaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.DateHelper
import kpt.core.common.ScreenState
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.repository.LocalAssetRepository
import kpt.core.data.repository.SavingsAccountRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.savingsaccount.CreateNewSavingEntity
import kpt.core.model.savingsaccount.SavingAccountTemplate
import kpt.core.model.savingsaccount.UpdateSavingAccountEntity
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.accounts.generated.resources.Res
import kpt.feature.accounts.generated.resources.feature_accounts_error_account_id_required
import kpt.feature.accounts.generated.resources.feature_accounts_error_client_id_required
import kpt.feature.accounts.generated.resources.feature_accounts_error_date_format_required
import kpt.feature.accounts.generated.resources.feature_accounts_error_external_id_length
import kpt.feature.accounts.generated.resources.feature_accounts_error_external_id_required
import kpt.feature.accounts.generated.resources.feature_accounts_error_locale_required
import kpt.feature.accounts.generated.resources.feature_accounts_error_min_opening_balance_required
import kpt.feature.accounts.generated.resources.feature_accounts_error_overdraft_limit_required
import kpt.feature.accounts.generated.resources.feature_accounts_error_select_saving_product
import kpt.feature.accounts.generated.resources.feature_accounts_error_submitted_date_required
import kpt.feature.accounts.generated.resources.feature_accounts_saving_button_save
import kpt.feature.accounts.generated.resources.feature_accounts_saving_button_update
import kpt.feature.accounts.generated.resources.feature_accounts_saving_created_successfully
import kpt.feature.accounts.generated.resources.feature_accounts_saving_title_create
import kpt.feature.accounts.generated.resources.feature_accounts_saving_title_update
import kpt.feature.accounts.generated.resources.feature_accounts_saving_updated_successfully
import kpt.feature.accounts.savingsaccount.AESAction.CreateOrUpdateSavingAccount
import kpt.feature.accounts.savingsaccount.AESAction.Internal.HandleSavingTemplateResult
import kpt.feature.accounts.savingsaccount.AESState.ViewState.Error
import org.jetbrains.compose.resources.StringResource
import kpt.feature.accounts.savingsaccount.AESState.DialogState.Error as DialogStateError
internal class AddEditSavingViewModel(
    private val repository: SavingsAccountRepository,
    private val userRepository: ForkPreferencesRepository,
    localAssetRepository: LocalAssetRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AESState, AESEvent, AESAction>(
    initialState = savedStateHandle.getSerialized(ADD_EDIT_SAVING_STATE_KEY) ?: run {
        val clientId = requireNotNull(userRepository.clientId.value)
        val type = SavingAccountAddEditArgs(savedStateHandle).savingsAddEditType

        AESState(
            clientId = clientId,
            type = type,
            viewState = AESState.ViewState.Loading,
            dialogState = null,
        )
    },
) {

    companion object {
        private const val ADD_EDIT_SAVING_STATE_KEY = "add_edit_saving_state"
    }

    val localeList = localAssetRepository.localeList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    // Template idiom (core-base/store): the create/update savings-account write goes
    // through a SubmitHandler instead of a hand-folded DataState result action. The
    // handler owns the Submitting/Submitted/Failed lifecycle; we observe it here to
    // drive this screen's existing loading/error dialog + toast + back-navigation,
    // so the Screen is unchanged.
    private val submitSavingAccount = viewModelScope.submitHandler<Unit>()

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = ADD_EDIT_SAVING_STATE_KEY, value = it) }
            .launchIn(viewModelScope)

        repository.getSavingAccountTemplate(state.clientId).onEach {
            sendAction(HandleSavingTemplateResult(it))
        }.launchIn(viewModelScope)

        submitSavingAccount.state
            .onEach { submitState ->
                when (submitState) {
                    is SubmitState.Submitting -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = AESState.DialogState.Loading)
                        }
                    }

                    is SubmitState.Submitted -> {
                        mutableStateFlow.update { it.copy(dialogState = null) }
                        val successMessage = when (state.type) {
                            is SavingsAddEditType.AddItem ->
                                Res.string.feature_accounts_saving_created_successfully

                            is SavingsAddEditType.EditItem ->
                                Res.string.feature_accounts_saving_updated_successfully
                        }
                        sendEvent(AESEvent.ShowToast(successMessage))
                        sendEvent(AESEvent.OnNavigateBack)
                        submitSavingAccount.reset()
                    }

                    is SubmitState.Failed -> {
                        val message = submitState.error.message.toString()
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.StringMessage(message))
                        }
                        submitSavingAccount.reset()
                    }

                    SubmitState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: AESAction) {
        when (action) {
            is AESAction.ExternalIdChanged -> {
                updateContent { content ->
                    content.copy(externalId = action.externalId)
                }
            }

            is AESAction.ProductChanged -> {
                updateContent { content ->
                    content.copy(productId = action.productId)
                }
            }

            is AESAction.LocaleChanged -> {
                updateContent { content ->
                    content.copy(locale = action.locale)
                }
            }

            is AESAction.AllowOverdraftChanged -> {
                updateContent { content ->
                    content.copy(allowOverdraft = !content.allowOverdraft)
                }
            }

            is AESAction.EnforceMinRequiredBalanceChanged -> {
                updateContent { content ->
                    content.copy(enforceMinRequiredBalance = !content.enforceMinRequiredBalance)
                }
            }

            is AESAction.MinRequiredOpeningBalanceChanged -> {
                updateContent { content ->
                    val balance = action.balance.toLongOrNull() ?: 0L
                    content.copy(minRequiredOpeningBalance = balance)
                }
            }

            is AESAction.NominalAnnualInterestRateChanged -> {
                updateContent { content ->
                    val rate = action.rate.toDoubleOrNull() ?: 0.0
                    content.copy(
                        nominalAnnualInterestRate = if (rate.isFinite()) rate else 0.0,
                    )
                }
            }

            is AESAction.OverdraftLimitChanged -> {
                updateContent { content ->
                    content.copy(overdraftLimit = action.overdraftLimit)
                }
            }

            is AESAction.WithHoldTaxChanged -> {
                updateContent { content ->
                    content.copy(withHoldTax = !content.withHoldTax)
                }
            }

            is AESAction.WithdrawalFeeForTransfersChanged -> {
                updateContent { content ->
                    content.copy(withdrawalFeeForTransfers = !content.withdrawalFeeForTransfers)
                }
            }

            is AESAction.NavigateBack -> {
                sendEvent(AESEvent.OnNavigateBack)
            }

            is AESAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is CreateOrUpdateSavingAccount -> initiateCreateOrUpdateSavingAccount()

            is HandleSavingTemplateResult -> handleSavingTemplateResult(action)
        }
    }

    private fun initiateCreateOrUpdateSavingAccount() {
        onContent { content ->
            when (state.type) {
                is SavingsAddEditType.AddItem -> when {
                    content.productId == 0L -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_select_saving_product))
                        }
                    }

                    content.clientId.isEmpty() -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_client_id_required))
                        }
                    }

                    content.externalId.isEmpty() -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_external_id_required))
                        }
                    }

                    content.externalId.length < 8 -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_external_id_length))
                        }
                    }

                    content.enforceMinRequiredBalance && content.minRequiredOpeningBalance == 0L -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_min_opening_balance_required))
                        }
                    }

                    content.allowOverdraft && content.overdraftLimit.isEmpty() -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_overdraft_limit_required))
                        }
                    }

                    content.locale.isEmpty() -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_locale_required))
                        }
                    }

                    content.submittedOnDate.isEmpty() -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_submitted_date_required))
                        }
                    }

                    content.dateFormat.isEmpty() -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_date_format_required))
                        }
                    }

                    else -> initiateCreateSavingAccount()
                }

                is SavingsAddEditType.EditItem -> when {
                    content.productId == 0L -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_select_saving_product))
                        }
                    }

                    content.clientId.isEmpty() -> {
                        mutableStateFlow.update {
                            it.copy(dialogState = DialogStateError.ResourceMessage(Res.string.feature_accounts_error_client_id_required))
                        }
                    }

                    else -> initiateUpdateSavingAccount()
                }
            }
        }
    }

    private fun initiateCreateSavingAccount() {
        onContent { content ->
            // Submit through the handler — it drives Submitting/Submitted/Failed,
            // observed in `init`. The repository write returns Unit and throws on
            // failure; the handler maps that to Submitted/Failed.
            submitSavingAccount.submit {
                repository.createSavingsAccount(content.createSavingEntity)
            }
        }
    }

    private fun initiateUpdateSavingAccount() {
        onContent { content ->
            val accountId = requireNotNull(state.type.savingsAccountId) {
                Res.string.feature_accounts_error_account_id_required
            }

            submitSavingAccount.submit {
                repository.updateSavingsAccount(accountId, content.updateSavingEntity)
            }
        }
    }

    private fun handleSavingTemplateResult(action: HandleSavingTemplateResult) {
        // `getSavingAccountTemplate` was migrated to `ScreenStateStream`.
        // Content builds the form; Empty is defensively surfaced as an error
        // (a template endpoint shouldn't emit Empty). NoNetwork /
        // Unauthenticated fold into the existing Error surface until Phase-4
        // wires per-branch messaging.
        when (val result = action.result) {
            is ScreenState.Loading -> {
                mutableStateFlow.update {
                    it.copy(viewState = AESState.ViewState.Loading)
                }
            }

            is ScreenState.Empty -> {
                mutableStateFlow.update {
                    it.copy(viewState = Error("Template not available."))
                }
            }

            is ScreenState.Error -> {
                mutableStateFlow.update {
                    it.copy(viewState = Error(result.error.message.toString()))
                }
            }

            is ScreenState.NoNetwork -> {
                mutableStateFlow.update {
                    it.copy(viewState = Error("No network. Please check your connection."))
                }
            }

            is ScreenState.Unauthenticated -> {
                mutableStateFlow.update {
                    it.copy(viewState = Error("Session expired. Please log in again."))
                }
            }

            is ScreenState.Content -> {
                mutableStateFlow.update {
                    it.copy(viewState = AESState.ViewState.Content(result.data))
                }
            }
        }
    }

    private inline fun onContent(
        crossinline block: (AESState.ViewState.Content) -> Unit,
    ) {
        (state.viewState as? AESState.ViewState.Content)?.let(block)
    }

    private inline fun updateContent(
        crossinline block: (
            AESState.ViewState.Content,
        ) -> AESState.ViewState.Content?,
    ) {
        val currentViewState = state.viewState
        val updatedContent = (currentViewState as? AESState.ViewState.Content)
            ?.let(block)
            ?: return
        mutableStateFlow.update { it.copy(viewState = updatedContent) }
    }
}

@Serializable
internal data class AESState(
    val clientId: Long,
    val type: SavingsAddEditType,
    val viewState: ViewState,
    @Transient
    val dialogState: DialogState? = null,
) {

    val isInEditMode: Boolean
        get() = type is SavingsAddEditType.EditItem

    val btnText: StringResource
        get() = if (!isInEditMode) {
            Res.string.feature_accounts_saving_button_save
        } else {
            Res.string.feature_accounts_saving_button_update
        }

    val title: StringResource
        get() = if (!isInEditMode) {
            Res.string.feature_accounts_saving_title_create
        } else {
            Res.string.feature_accounts_saving_title_update
        }

    @Serializable
    sealed interface ViewState {
        @Serializable
        data object Loading : ViewState

        @Serializable
        data class Error(val message: String) : ViewState

        @Serializable
        data class Content(
            val template: SavingAccountTemplate,
            val minRequiredOpeningBalance: Long = 0,
            val overdraftLimit: String = "",
            val externalId: String = "",
            val locale: String = "en_IN",
            val submittedOnDate: String = DateHelper.formattedShortDate,
            val dateFormat: String = DateHelper.SHORT_MONTH,
            val clientId: String = template.clientId,
            val productId: Long = template.productOptions.first().id,
            val nominalAnnualInterestRate: Double = template.nominalAnnualInterestRate,
            val withdrawalFeeForTransfers: Boolean = template.withdrawalFeeForTransfers,
            val allowOverdraft: Boolean = template.allowOverdraft,
            val enforceMinRequiredBalance: Boolean = template.enforceMinRequiredBalance,
            val withHoldTax: Boolean = template.withHoldTax,
        ) : ViewState {

            val createSavingEntity: CreateNewSavingEntity
                get() = CreateNewSavingEntity(
                    clientId = clientId,
                    productId = productId,
                    nominalAnnualInterestRate = nominalAnnualInterestRate,
                    minRequiredOpeningBalance = minRequiredOpeningBalance,
                    withdrawalFeeForTransfers = withdrawalFeeForTransfers,
                    allowOverdraft = allowOverdraft,
                    overdraftLimit = overdraftLimit,
                    enforceMinRequiredBalance = enforceMinRequiredBalance,
                    withHoldTax = withHoldTax,
                    externalId = externalId,
                    submittedOnDate = submittedOnDate,
                    locale = locale,
                    dateFormat = dateFormat,
                )

            val updateSavingEntity: UpdateSavingAccountEntity
                get() = UpdateSavingAccountEntity(
                    clientId = clientId,
                    productId = productId,
                )
        }
    }

    sealed interface DialogState {
        data object Loading : DialogState
        sealed interface Error : DialogState {
            data class StringMessage(val message: String) : Error
            data class ResourceMessage(val message: StringResource) : Error
        }
    }
}

internal sealed interface AESEvent {
    data object OnNavigateBack : AESEvent
    data class ShowToast(val message: StringResource) : AESEvent
}

internal sealed interface AESAction {
    data class ProductChanged(val productId: Long) : AESAction
    data class ExternalIdChanged(val externalId: String) : AESAction
    data class LocaleChanged(val locale: String) : AESAction

    data class NominalAnnualInterestRateChanged(val rate: String) : AESAction

    data class MinRequiredOpeningBalanceChanged(val balance: String) : AESAction
    data object EnforceMinRequiredBalanceChanged : AESAction

    data object AllowOverdraftChanged : AESAction
    data class OverdraftLimitChanged(val overdraftLimit: String) : AESAction

    data object WithdrawalFeeForTransfersChanged : AESAction
    data object WithHoldTaxChanged : AESAction

    data object DismissDialog : AESAction
    data object NavigateBack : AESAction

    data object CreateOrUpdateSavingAccount : AESAction

    sealed interface Internal : AESAction {
        /**
         * Template load result. Uses [ScreenState] — Phase-3 migrated
         * `getSavingAccountTemplate` to a `ScreenStateStream`.
         */
        data class HandleSavingTemplateResult(val result: ScreenState<SavingAccountTemplate>) :
            Internal
    }
}
