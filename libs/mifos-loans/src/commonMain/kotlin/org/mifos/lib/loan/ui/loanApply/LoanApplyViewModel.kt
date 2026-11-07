/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.loanApply

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kpt.core.common.DateHelper
import kpt.core.common.ScreenState
import kpt.core.data.fork.util.NetworkMonitor
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.client.Client
import kpt.core.ui.fork.utils.BaseViewModel
import org.jetbrains.compose.resources.StringResource
import org.mifos.lib.loan.core.model.LoanTemplate
import org.mifos.lib.loan.generated.resources.Res
import org.mifos.lib.loan.generated.resources.feature_apply_loan_error_amount_empty
import org.mifos.lib.loan.generated.resources.feature_apply_loan_error_amount_invalid_decimal
import org.mifos.lib.loan.generated.resources.feature_apply_loan_error_amount_invalid_format
import org.mifos.lib.loan.generated.resources.feature_apply_loan_error_amount_multiple
import org.mifos.lib.loan.generated.resources.feature_apply_loan_error_amount_too_large
import org.mifos.lib.loan.generated.resources.feature_apply_loan_error_amount_too_small
import org.mifos.lib.loan.repository.LoansRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * `ViewModel` for the Loan Application form screen.
 *
 * Combines the applicant's client record, the client-wide loan template (for currency), and the
 * product-specific loan template (for principal bounds and loan-purpose options) to populate the
 * application form, then validates and forwards the entered data to the document-upload step.
 *
 * @param savedStateHandle Handle used to read the [LoanApplyRoute] navigation arguments.
 * @param loansRepository Facade resolving the loan provider chosen for this application.
 * @param selfServiceRepository Used to fetch the applicant's client record (name, activation date).
 * @param userPreferencesRepository Fallback source for the current client id if the navigation
 * argument was not supplied.
 * @param networkMonitor Monitors network connectivity so the form can be reloaded on reconnect.
 */
@OptIn(ExperimentalTime::class)
internal class LoanApplyViewModel(
    savedStateHandle: SavedStateHandle,
    private val loansRepository: LoansRepository,
    private val selfServiceRepository: SelfServiceRepository,
    private val userPreferencesRepository: ForkPreferencesRepository,
    private val networkMonitor: NetworkMonitor,
) : BaseViewModel<LoanApplyState, LoanApplyEvent, LoanApplyAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<LoanApplyRoute>()
        val resolvedClientId = route.clientId.takeIf { it > 0 }
            ?: requireNotNull(userPreferencesRepository.clientId.value)
        LoanApplyState(
            clientId = resolvedClientId,
            productId = route.productId,
            providerId = route.providerId,
        )
    },
) {

    init {
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.distinctUntilChanged().collect { isOnline ->
                sendAction(LoanApplyAction.ReceiveNetworkStatus(isOnline))
            }
        }
    }

    override fun handleAction(action: LoanApplyAction) {
        when (action) {
            LoanApplyAction.NavigateBack -> sendEvent(LoanApplyEvent.NavigateBack)

            LoanApplyAction.Retry -> loadData()

            is LoanApplyAction.PrincipalAmountChanged -> onPrincipalAmountChanged(action.value)

            is LoanApplyAction.LoanPurposeSelected -> {
                mutableStateFlow.update { it.copy(selectedLoanPurpose = action.purpose) }
            }

            LoanApplyAction.SubmitClicked -> onSubmitClicked()

            is LoanApplyAction.ReceiveNetworkStatus -> handleNetworkStatus(action.isOnline)

            is LoanApplyAction.Internal.DataLoaded -> {
                handleDataLoaded(action.client, action.template, action.productTemplate)
            }
        }
    }

    private fun handleNetworkStatus(isOnline: Boolean) {
        mutableStateFlow.update { it.copy(networkStatus = isOnline) }
        if (!isOnline) {
            mutableStateFlow.update { current ->
                if (current.viewState !is LoanApplyState.ViewState.Content) {
                    current.copy(viewState = LoanApplyState.ViewState.NetworkError)
                } else {
                    current
                }
            }
        } else {
            loadData()
        }
    }

    private fun loadData() {
        mutableStateFlow.update { it.copy(viewState = LoanApplyState.ViewState.Loading) }
        viewModelScope.launch {
            combine(
                selfServiceRepository.getSelfClientDetails(state.clientId),
                loansRepository.getLoanTemplate(state.providerId, state.clientId),
                loansRepository.getLoanTemplateByProduct(
                    state.providerId,
                    state.clientId,
                    state.productId,
                ),
            ) { client, template, productTemplate ->
                Triple(client, template, productTemplate)
            }.collect { (client, template, productTemplate) ->
                sendAction(LoanApplyAction.Internal.DataLoaded(client, template, productTemplate))
            }
        }
    }

    private fun handleDataLoaded(
        client: ScreenState<Client>,
        template: ScreenState<LoanTemplate>,
        productTemplate: ScreenState<LoanTemplate>,
    ) {
        val loanStates = listOf(template, productTemplate)
        when {
            client is ScreenState.Loading || loanStates.any { it is ScreenState.Loading } -> {
                mutableStateFlow.update { it.copy(viewState = LoanApplyState.ViewState.Loading) }
            }

            client is ScreenState.Content &&
                template is ScreenState.Content &&
                productTemplate is ScreenState.Content -> {
                populateForm(client.data, template.data, productTemplate.data)
            }

            else -> {
                val loanMessage = loanStates
                    .filterIsInstance<ScreenState.Error>()
                    .firstOrNull()
                    ?.error
                    ?.message
                val clientMessage = (client as? ScreenState.Error)
                    ?.error
                    ?.message
                val message = loanMessage
                    ?: clientMessage
                    ?: "Something went wrong. Please try again."
                mutableStateFlow.update {
                    it.copy(viewState = LoanApplyState.ViewState.Error(message))
                }
            }
        }
    }

    private fun populateForm(client: Client, template: LoanTemplate, productTemplate: LoanTemplate) {
        val product = productTemplate.product
        val currency = template.currency

        val minPrincipal = product?.minPrincipal ?: productTemplate.principal ?: 0.0
        val maxPrincipal = product?.maxPrincipal ?: productTemplate.principal ?: Double.MAX_VALUE

        val purposeOptions = productTemplate.loanPurposeOptions
            .mapNotNull { option ->
                val id = option.id?.toLong()
                val name = option.name
                if (id != null && name != null) id to name else null
            }
            .toMap()

        val initialPrincipal = if (minPrincipal % 1.0 == 0.0) {
            minPrincipal.toLong().toString()
        } else {
            minPrincipal.toString()
        }

        mutableStateFlow.update {
            it.copy(
                applicantName = client.displayName,
                loanProductName = productTemplate.loanProductName ?: product?.name.orEmpty(),
                activationDate = client.activationDate,
                loanPurposeOptions = purposeOptions,
                minPrincipal = minPrincipal,
                maxPrincipal = maxPrincipal,
                principalAmount = initialPrincipal,
                principalAmountError = null,
                disbursementDate = computeDefaultDisbursementDate(client.activationDate),
                currencyDisplaySymbol = currency?.displaySymbol.orEmpty(),
                currencyDecimalPlaces = currency?.decimalPlaces?.toInt() ?: DEFAULT_DECIMAL_PLACES,
                currencyInMultiplesOf = currency?.inMultiplesOf?.toDouble() ?: 0.0,
                viewState = LoanApplyState.ViewState.Content,
            )
        }
    }

    /**
     * The disbursement date defaults to the later of today or the client's activation date,
     * mirroring the source app's behaviour of never allowing disbursement before a client is
     * active.
     */
    private fun computeDefaultDisbursementDate(activationDate: List<Long>): String {
        val todayMillis = Clock.System.now().toEpochMilliseconds()
        val activationMillis = runCatching {
            if (activationDate.size >= 3) {
                LocalDate(
                    year = activationDate[0].toInt(),
                    monthNumber = activationDate[1].toInt(),
                    dayOfMonth = activationDate[2].toInt(),
                ).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            } else {
                null
            }
        }.getOrNull()

        val effectiveMillis = maxOf(todayMillis, activationMillis ?: todayMillis)
        return DateHelper.getDateAsStringFromLong(effectiveMillis)
    }

    private fun onPrincipalAmountChanged(newValue: String) {
        mutableStateFlow.update {
            it.copy(
                principalAmount = newValue,
                principalAmountError = validatePrincipal(newValue),
            )
        }
    }

    private fun onSubmitClicked() {
        val error = validatePrincipal(state.principalAmount)
        mutableStateFlow.update { it.copy(principalAmountError = error) }

        if (error == null && state.isSubmitEnabled) {
            sendEvent(
                LoanApplyEvent.NavigateToUploadDocs(
                    clientId = state.clientId,
                    productId = state.productId,
                    applicantName = state.applicantName,
                    loanProductName = state.loanProductName,
                    loanPurpose = state.selectedLoanPurpose,
                    disbursementDate = state.disbursementDate,
                    principalAmount = state.principalAmount,
                    providerId = state.providerId,
                ),
            )
        }
    }

    /**
     * Validates the principal amount against the selected product's bounds and the loan
     * currency's decimal/step configuration. There is no shared `ValidationHelper` in this app,
     * so the rules are implemented directly here.
     */
    private fun validatePrincipal(amount: String): StringResource? {
        if (amount.isBlank()) return Res.string.feature_apply_loan_error_amount_empty

        val cleaned = amount.trim().replace(",", "")
        if (!AMOUNT_FORMAT_REGEX.matches(cleaned)) {
            return Res.string.feature_apply_loan_error_amount_invalid_format
        }

        val value = cleaned.toDoubleOrNull()
            ?: return Res.string.feature_apply_loan_error_amount_invalid_format

        val dotIndex = cleaned.indexOf('.')
        if (dotIndex != -1 && cleaned.length - dotIndex - 1 > state.currencyDecimalPlaces) {
            return Res.string.feature_apply_loan_error_amount_invalid_decimal
        }

        if (value < state.minPrincipal) return Res.string.feature_apply_loan_error_amount_too_small
        if (value > state.maxPrincipal) return Res.string.feature_apply_loan_error_amount_too_large

        val multiplesOf = state.currencyInMultiplesOf
        if (multiplesOf > 0) {
            val remainder = value % multiplesOf
            val isMultiple = remainder < AMOUNT_EPSILON || (multiplesOf - remainder) < AMOUNT_EPSILON
            if (!isMultiple) return Res.string.feature_apply_loan_error_amount_multiple
        }

        return null
    }

    private companion object {
        const val DEFAULT_DECIMAL_PLACES = 2
        const val AMOUNT_EPSILON = 0.0001
        val AMOUNT_FORMAT_REGEX = Regex("^\\d+(\\.\\d+)?$")
    }
}

/**
 * UI state for the Loan Application form screen.
 *
 * @property clientId The id of the client applying for a loan.
 * @property productId The id of the loan product being applied for.
 * @property providerId The id of the loan provider chosen on the Select Loan Provider screen.
 * @property applicantName The applicant's display name (read-only, sourced from the client record).
 * @property loanProductName The selected loan product's name (read-only).
 * @property activationDate The client's activation date, as `[year, month, day]`.
 * @property loanPurposeOptions Map of loan-purpose id to display name, sourced from the product template.
 * @property selectedLoanPurpose The currently selected loan purpose name.
 * @property principalAmount The requested principal amount, as entered by the user.
 * @property disbursementDate The computed disbursement date (`dd-MM-yyyy`), read-only.
 * @property currencyDisplaySymbol The currency symbol to prefix amounts with.
 * @property currencyDecimalPlaces Allowed decimal places for the currency.
 * @property currencyInMultiplesOf The step the principal amount must be a multiple of, if any.
 * @property minPrincipal The minimum principal allowed for the selected product.
 * @property maxPrincipal The maximum principal allowed for the selected product.
 * @property principalAmountError Validation error for the principal amount field, if any.
 * @property networkStatus Whether the device currently has network connectivity.
 * @property viewState The current content/loading/error state of the screen.
 */
internal data class LoanApplyState(
    val clientId: Long,
    val productId: Long,
    val providerId: String,
    val applicantName: String = "",
    val loanProductName: String = "",
    val activationDate: List<Long> = emptyList(),
    val loanPurposeOptions: Map<Long, String> = emptyMap(),
    val selectedLoanPurpose: String = "",
    val principalAmount: String = "",
    val disbursementDate: String = "",
    val currencyDisplaySymbol: String = "",
    val currencyDecimalPlaces: Int = 2,
    val currencyInMultiplesOf: Double = 0.0,
    val minPrincipal: Double = 0.0,
    val maxPrincipal: Double = Double.MAX_VALUE,
    val principalAmountError: StringResource? = null,
    val networkStatus: Boolean = true,
    val viewState: ViewState = ViewState.Loading,
) {
    val isSubmitEnabled: Boolean
        get() = viewState is ViewState.Content &&
            principalAmountError == null &&
            principalAmount.isNotBlank() &&
            selectedLoanPurpose.isNotBlank() &&
            disbursementDate.isNotBlank()

    sealed interface ViewState {
        data object Loading : ViewState
        data object Content : ViewState
        data class Error(val message: String) : ViewState
        data object NetworkError : ViewState
    }
}

/**
 * One-shot navigation events emitted by [LoanApplyViewModel].
 */
internal sealed interface LoanApplyEvent {
    data object NavigateBack : LoanApplyEvent

    /**
     * Requests navigation to the document-upload step with the fully-populated application
     * form data. See [loanApplyScreen] for the expected shape of the destination this feeds.
     */
    data class NavigateToUploadDocs(
        val clientId: Long,
        val productId: Long,
        val applicantName: String,
        val loanProductName: String,
        val loanPurpose: String,
        val disbursementDate: String,
        val principalAmount: String,
        val providerId: String,
    ) : LoanApplyEvent
}

/**
 * User actions and internal events handled by [LoanApplyViewModel].
 */
internal sealed interface LoanApplyAction {
    data object NavigateBack : LoanApplyAction

    data object Retry : LoanApplyAction

    data class PrincipalAmountChanged(val value: String) : LoanApplyAction

    data class LoanPurposeSelected(val purpose: String) : LoanApplyAction

    data object SubmitClicked : LoanApplyAction

    data class ReceiveNetworkStatus(val isOnline: Boolean) : LoanApplyAction

    sealed interface Internal : LoanApplyAction {
        data class DataLoaded(
            val client: ScreenState<Client>,
            val template: ScreenState<LoanTemplate>,
            val productTemplate: ScreenState<LoanTemplate>,
        ) : Internal
    }
}
