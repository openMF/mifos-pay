/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrCodeShape
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogo
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.QrShapes
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.solid
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.SubmitState
import kpt.core.base.store.submit.submitHandler
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.data.fork.util.MpayQrCodeProcessor
import kpt.core.data.repository.AccountRepository
import kpt.core.data.repository.LocalAssetRepository
import kpt.core.data.repository.PocketRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.account.Account
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.AccountStatus
import kpt.core.model.pocket.DetailedPocketAccount
import kpt.core.model.pocket.PocketAccount
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
import kpt.core.ui.fork.utils.BaseViewModel
import kpt.core.ui.fork.utils.MimeType
import kpt.core.ui.fork.utils.ShareFileModel
import kpt.core.ui.fork.utils.ShareUtils
import kpt.core.ui.generated.resources.core_ui_error_msg_generic
import kpt.core.ui.generated.resources.core_ui_pocket_add_failed
import kpt.core.ui.generated.resources.core_ui_pocket_added_successfully
import kpt.feature.mpay.qr.generated.resources.Res
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_external_id_required
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_failed_to_generate
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_no_default_account
import kpt.feature.mpay.qr.generated.resources.logo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import kpt.core.ui.generated.resources.Res as UiRes
import kpt.feature.mpay.qr.generated.resources.Res as MpayQrRes

/** ViewModel of the QR screen; builds intra-bank and inter-bank QR codes for the selected account. */
class MpayQrViewModel(
    localRepository: LocalAssetRepository,
    repository: ForkPreferencesRepository,
    private val accountRepository: AccountRepository,
    private val pocketRepository: PocketRepository,
    savedStateHandle: SavedStateHandle,
    private val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel<MpayQrState, MpayQrEvent, MpayQrAction>(
    initialState = run {
        // Always get fresh client and account data to avoid stale savedStateHandle values
        /** Current client from preferences, or an empty placeholder when none is stored. */
        val client = repository.client.value ?: Client(
            id = 0,
            accountNo = "",
            externalId = "",
            active = false,
            activationDate = emptyList(),
            firstname = "",
            lastname = "",
            displayName = "",
            mobileNo = "",
            emailAddress = "",
            dateOfBirth = emptyList(),
            isStaff = false,
            officeId = 0,
            officeName = "",
            savingsProductName = "",
        )

        /** Default account from preferences, or the built-in default. */
        val defaultAccount = repository.defaultAccount.value ?: DefaultAccount.DEFAULT
        // Get the account external ID for inter-bank QR
        /** External id of the default account, used for inter-bank QR codes. */
        val accountExternalId = repository.getAccountExternalId(defaultAccount.accountId)
        // Get the FSP ID (bank/tenant identifier) for routing
        /** Bank/tenant identifier of the selected instance, used for routing. */
        val fspId = repository.selectedInstance.value?.platformTenantId ?: ""

        // Try to restore saved state but only use amount/currency, not client data
        /** Previously saved state, of which only amount and currency are reused. */
        val savedState = savedStateHandle.getSerialized<MpayQrState>(KEY_STATE)

        Logger.d { "MpayQrViewModel init - fspId: $fspId, client.id: ${client.id}, client.officeId: ${client.officeId}, defaultAccount.accountId: ${defaultAccount.accountId}" }

        MpayQrState(
            client = client,
            defaultAccount = defaultAccount,
            fspId = fspId,
            accountExternalId = accountExternalId ?: "",
            viewState = MpayQrState.ViewState.Loading,
            selectedPage = savedState?.selectedPage ?: 0,
        )
    },
) {

    private companion object {
        private const val KEY_STATE = "mpay_qr_state"
    }

    /** Currencies the amount dialog offers. */
    val currencyList = localRepository.currencyList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val submitLink = viewModelScope.submitHandler<Unit>()

    init {
        observeLinkSubmit()

        stateFlow.onEach {
            savedStateHandle.setSerialized(key = KEY_STATE, value = it)
        }.launchIn(viewModelScope)

        loadAccounts()
    }

    private fun observeLinkSubmit() {
        submitLink.state.onEach { submitState ->
            when (submitState) {
                is SubmitState.Submitting -> {
                    Unit
                }
                is SubmitState.Submitted -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = MpayQrState.DialogState.Success(
                                UiRes.string.core_ui_pocket_added_successfully,
                            ),
                        )
                    }
                    submitLink.reset()
                }
                is SubmitState.Failed -> {
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = MpayQrState.DialogState.Error(
                                UiRes.string.core_ui_pocket_add_failed,
                            ),
                        )
                    }
                    submitLink.reset()
                }
                SubmitState.Idle -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun loadAccounts() {
        // Phase-5 Batch-3 cutover: switched from the transitional
        // `getSelfAccounts(clientId)` shim to the store-backed
        // `getSelfAccountsScreen(clientId, scope)` (GOAL D13,
        // `AppStoreRegistry.SelfAccounts`, offline-first via
        // `wallet_self_accounts` Room SoT, CACHE_FIRST_SWR band). Fold
        // semantics are identical: Content carries the accounts and triggers
        // QR generation; Empty still falls back to the default account
        // (existing "Still generate QR" behavior on failure); Error /
        // NoNetwork / Unauthenticated all log + regenerate with the default
        // account until Phase-4 differentiates them.
        viewModelScope.launch {
            combine(
                accountRepository.getSelfAccountsStream(
                    clientId = state.client.id,
                    scope = viewModelScope,
                ).state,
                pocketRepository.observeLinkedPocketAccounts(state.client.id),
            ) { result, pocketAccounts ->
                val pocketAccountIds = pocketAccounts
                    .filter { it.accountType == kpt.core.model.enums.AccountType.SAVINGS }
                    .map { it.accountId }
                    .toSet()

                mutableStateFlow.update { it.copy(pocketAccountIds = pocketAccountIds) }
                result
            }.collect { result ->
                when (result) {
                    is ScreenState.Content<*> -> {
                        val unsortedAccounts = result.data as List<Account>
                        /*
                         * Pocket Account Suggestion Feature:
                         * Sorts the accounts so that linked pocket accounts appear at the top.
                         */
                        val accounts = unsortedAccounts.sortedByDescending { it.id in state.pocketAccountIds }
                        val defaultAcc = state.selectedAccount ?: accounts.firstOrNull()

                        mutableStateFlow.update {
                            it.copy(
                                accounts = accounts,
                                selectedAccount = defaultAcc,
                                accountExternalId = defaultAcc?.externalId ?: "",
                                qrData = it.qrData.copy(
                                    currency = defaultAcc?.currency?.code ?: QrCodeData.DEFAULT_CURRENCY,
                                    accountNo = defaultAcc?.number ?: "",
                                    accountId = defaultAcc?.id ?: 0L,
                                    accountExternalId = defaultAcc?.externalId ?: "",
                                    officeId = defaultAcc?.officeId?.toLong() ?: it.qrData.officeId,
                                    officeName = defaultAcc?.officeName ?: it.qrData.officeName,
                                ),
                            )
                        }
                        sendAction(MpayQrAction.Internal.GenerateQr)
                    }

                    is ScreenState.Empty -> {
                        // No accounts returned — fall back to default and generate QR.
                        sendAction(MpayQrAction.Internal.GenerateQr)
                    }

                    is ScreenState.Error -> {
                        Logger.e { "Failed to load accounts: ${result.error.message}" }
                        // Still generate QR with default account
                        sendAction(MpayQrAction.Internal.GenerateQr)
                    }

                    is ScreenState.NoNetwork -> {
                        Logger.e { "Failed to load accounts: no network" }
                        sendAction(MpayQrAction.Internal.GenerateQr)
                    }

                    is ScreenState.Unauthenticated -> {
                        Logger.e { "Failed to load accounts: unauthenticated" }
                        sendAction(MpayQrAction.Internal.GenerateQr)
                    }

                    is ScreenState.Loading -> {
                        // Loading state handled by viewState
                    }
                }
            }
        }
    }

    override fun handleAction(action: MpayQrAction) {
        when (action) {
            is MpayQrAction.NavigateBack -> sendEvent(MpayQrEvent.OnNavigateBack)
            is MpayQrAction.AmountChanged -> updateAmount(action.amount)
            is MpayQrAction.CurrencyChanged -> updateCurrency(action.currency)
            is MpayQrAction.PageChanged -> updatePage(action.page)
            is MpayQrAction.ConfirmSetAmount -> confirmSetAmount()
            is MpayQrAction.DismissDialog -> dismissDialog()
            is MpayQrAction.ShowSetAmountDialog -> showSetAmountDialog()
            is MpayQrAction.ShareQrCode -> shareQrCode(action.data)
            is MpayQrAction.DownloadQrCode -> downloadQrCode(action.bytes)
            is MpayQrAction.CopyToClipboard -> copyToClipboard(action.text)
            is MpayQrAction.ShowAccountPicker -> showAccountPicker(true)
            is MpayQrAction.DismissAccountPicker -> showAccountPicker(false)
            is MpayQrAction.SelectAccount -> selectAccount(action)
            is MpayQrAction.ConfirmAddToPocket -> confirmAddToPocket(action.add)
            is MpayQrAction.Internal.GenerateQr -> generateQr()
        }
    }

    private fun updateAmount(amount: String) = updateQrData { it.copy(amount = amount) }

    private fun updateCurrency(currency: String) = updateQrData { it.copy(currency = currency) }

    private fun updatePage(page: Int) = mutableStateFlow.update { it.copy(selectedPage = page) }

    private fun confirmSetAmount() {
        mutableStateFlow.update { it.copy(dialogState = MpayQrState.DialogState.Loading) }
        initiateSetAmount()
    }

    private fun dismissDialog() = mutableStateFlow.update { it.copy(dialogState = null) }

    private fun showSetAmountDialog() = mutableStateFlow.update {
        it.copy(dialogState = MpayQrState.DialogState.ShowSetAmountDialog)
    }

    private fun shareQrCode(data: ByteArray) {
        viewModelScope.launch {
            ShareUtils.shareFile(
                file = ShareFileModel(
                    fileName = "qr_code.png",
                    bytes = data,
                    mime = MimeType.IMAGE,
                ),
            )
        }
    }

    private fun downloadQrCode(bytes: ByteArray) {
        viewModelScope.launch {
            ShareUtils.shareFile(
                file = ShareFileModel(
                    fileName = "mpay_qr_code.png",
                    bytes = bytes,
                    mime = MimeType.IMAGE,
                ),
            )
            sendEvent(MpayQrEvent.QrDownloaded)
        }
    }

    private fun copyToClipboard(text: String) = sendEvent(MpayQrEvent.ShowSnackbar(text))

    private fun showAccountPicker(show: Boolean) = mutableStateFlow.update { it.copy(isAccountPickerVisible = show) }

    private fun selectAccount(action: MpayQrAction.SelectAccount) {
        mutableStateFlow.update {
            it.copy(
                selectedAccount = action.account,
                accountExternalId = action.account.externalId ?: "",
                qrData = it.qrData.copy(
                    accountNo = action.account.number,
                    accountId = action.account.id,
                    accountExternalId = action.account.externalId ?: "",
                    officeId = action.account.officeId?.toLong() ?: it.qrData.officeId,
                    officeName = action.account.officeName ?: it.qrData.officeName,
                ),
                isAccountPickerVisible = false,
            )
        }
        if (action.account.id !in state.pocketAccountIds) {
            mutableStateFlow.update { it.copy(dialogState = MpayQrState.DialogState.AddToPocketConfirmation) }
        }
        generateQr() // Regenerate QR for new account
    }

    private fun generateQr() {
        viewModelScope.launch {
            // Check if default account is properly set
            if (state.defaultAccount.accountNo.isBlank()) {
                mutableStateFlow.update {
                    it.copy(
                        viewState = MpayQrState.ViewState.Error(
                            MpayQrRes.string.feature_mpay_qr_no_default_account,
                        ),
                    )
                }
                return@launch
            }

            Logger.d { "QR Generating QR code with data: ${state.selectedAccount}" }
            Logger.d { "QR Generate - client.id: ${state.client.id}, defaultAccount.accountId: ${state.defaultAccount.accountId}, qrData.clientId: ${state.qrData.clientId}, qrData.accountId: ${state.qrData.accountId}" }

            // Generate Intra-Bank QR (always works with internal IDs)
            val intraBankData = withContext(ioDispatcher) {
                MpayQrCodeProcessor.encodeMpayString(state.qrData)
            }

            // Try to generate Inter-Bank QR (requires external ID)
            val (interBankData, interBankReason) = withContext(ioDispatcher) {
                if (state.accountExternalId.isBlank()) {
                    // No external ID - return null with reason
                    null to MpayQrRes.string.feature_mpay_qr_external_id_required
                } else {
                    try {
                        MpayQrCodeProcessor.encodeMpayString(state.interBankQrData) to null
                    } catch (e: IllegalArgumentException) {
                        Logger.w(e) { "Inter-bank QR could not be encoded" }
                        null to MpayQrRes.string.feature_mpay_qr_failed_to_generate
                    }
                }
            }

            mutableStateFlow.update {
                it.copy(
                    viewState = MpayQrState.ViewState.Content(
                        intraBankData = intraBankData,
                        interBankData = interBankData,
                        interBankUnavailableReason = interBankReason,
                    ),
                )
            }
        }
    }

    private fun initiateSetAmount() {
        viewModelScope.launch {
            val intraBankData = withContext(ioDispatcher) {
                MpayQrCodeProcessor.encodeMpayString(state.qrData)
            }

            val interBankData = if (state.accountExternalId.isNotBlank()) {
                withContext(ioDispatcher) {
                    MpayQrCodeProcessor.encodeMpayString(state.interBankQrData)
                }
            } else {
                null
            }

            updateContent {
                it.copy(
                    intraBankData = intraBankData,
                    interBankData = interBankData,
                )
            }

            mutableStateFlow.update { it.copy(dialogState = null) }
        }
    }

    /**
     * Handles the user's decision to add the selected account to their Pocket.
     * If true, it initiates a link submission using the PocketRepository.
     * If false, it simply proceeds to the next step.
     */
    private fun confirmAddToPocket(add: Boolean) {
        mutableStateFlow.update { it.copy(dialogState = null) }
        if (add) {
            val accountId = state.selectedAccount?.id
            val accountNumber = state.selectedAccount?.number
            if (accountId == null || accountNumber.isNullOrBlank()) {
                return
            }
            val detailedAccount = DetailedPocketAccount(
                pocket = PocketAccount(
                    pocketId = 0,
                    id = 0,
                    accountId = accountId,
                    accountType = AccountType.SAVINGS,
                    accountNumber = accountNumber,
                ),
                productName = state.selectedAccount?.productName ?: state.selectedAccount?.name,
                balance = state.selectedAccount?.balance,
                currencyCode = state.selectedAccount?.currency?.code,
                decimalPlaces = state.selectedAccount?.currency?.decimalPlaces,
                status = state.selectedAccount?.status?.let {
                    when {
                        it.active -> AccountStatus.ACTIVE
                        it.approved -> AccountStatus.APPROVED
                        it.rejected -> AccountStatus.REJECTED
                        it.closed -> AccountStatus.CLOSED
                        it.submittedAndPendingApproval -> AccountStatus.PENDING
                        else -> null
                    }
                },
                currencyDisplaySymbol = state.selectedAccount?.currency?.displaySymbol,
            )
            submitLink.submit {
                pocketRepository.linkAccounts(listOf(detailedAccount), state.client.id)
            }
        }
    }

    private inline fun updateQrData(
        crossinline block: (QrCodeData) -> QrCodeData,
    ) {
        mutableStateFlow.update {
            it.copy(qrData = block(it.qrData))
        }
    }

    private inline fun updateContent(
        crossinline block: (
            MpayQrState.ViewState.Content,
        ) -> MpayQrState.ViewState.Content?,
    ) {
        val currentViewState = state.viewState
        val updatedContent = (currentViewState as? MpayQrState.ViewState.Content)
            ?.let(block)
            ?: return
        mutableStateFlow.update { it.copy(viewState = updatedContent) }
    }
}

/**
 * Persisted UI state of the QR screen.
 *
 * @property client The current client.
 * @property defaultAccount The default account.
 * @property pocketAccountIds Ids of accounts already added to the pocket.
 * @property viewState Loading, error or content state of the QR screen.
 * @property selectedPage Selected tab: 0 for intra-bank, 1 for inter-bank.
 * @property qrData Data encoded in the QR code.
 * @property dialogState Dialog currently shown, or null.
 */
@Serializable
data class MpayQrState(
    val client: Client,

    val defaultAccount: DefaultAccount,

    /**
     * All accounts available for the user. Loaded from AccountRepository.
     */
    @Transient
    val accounts: List<Account> = emptyList(),

    /**
     * Currently selected account for QR generation.
     * Defaults to the default account on initial load.
     */
    @Transient
    val selectedAccount: Account? = null,

    /**
     * Whether the account picker bottom sheet is visible.
     */
    @Transient
    val isAccountPickerVisible: Boolean = false,
    val pocketAccountIds: Set<Long> = emptySet(),

    /**
     * The FSP ID (bank/tenant identifier) used for routing.
     * When scanning, if QR's fspId matches scanner's fspId → intra-bank transfer.
     */
    val fspId: String = "",

    /**
     * The external ID of the selected account, used for inter-bank QR codes.
     * Updated when a new account is selected.
     */
    val accountExternalId: String = "",

    @Transient
    val viewState: ViewState = ViewState.Loading,

    // 0=Intra-bank, 1=Inter-bank
    val selectedPage: Int = 0,

    val qrData: QrCodeData = QrCodeData(
        fspId = fspId,
        clientId = client.id,
        clientName = client.displayName,
        accountNo = selectedAccount?.number ?: defaultAccount.accountNo,
        accountId = selectedAccount?.id ?: defaultAccount.accountId,
        officeId = selectedAccount?.officeId?.toLong() ?: client.officeId.toLong(),
        officeName = selectedAccount?.officeName ?: client.officeName,
        accountTypeId = QrCodeData.ACCOUNT_TYPE_ID,
        accountExternalId = selectedAccount?.externalId ?: accountExternalId,
        currency = selectedAccount?.currency?.code ?: QrCodeData.DEFAULT_CURRENCY,
        amount = "",
    ),
    @Transient val dialogState: DialogState? = null,
) {
    /**
     * The currently active external ID, prioritizing selectedAccount over the stored accountExternalId.
     */
    val currentExternalId: String
        get() = selectedAccount?.externalId ?: accountExternalId

    /**
     * Computed property for inter-bank QR data.
     * Simplified format: fspId + accountExternalId for participant lookup.
     */
    val interBankQrData: QrCodeData
        get() = QrCodeData(
            type = QrCodeType.INTER_BANK,
            fspId = fspId,
            clientId = 0L,
            clientName = client.displayName,
            accountNo = "",
            amount = qrData.amount,
            accountId = 0L,
            currency = qrData.currency,
            accountExternalId = currentExternalId,
        )

    /** Display state of the QR screen. */
    sealed interface ViewState {
        /** QR codes are being generated. */
        data object Loading : ViewState

        /**
         * QR generation failed.
         *
         * @property message Error message to display, resolved by the screen.
         */
        data class Error(val message: StringResource) : ViewState

        /**
         * QR codes are ready.
         *
         * @property intraBankData Encoded payload of the intra-bank QR code.
         * @property interBankData Encoded payload of the inter-bank QR code, or null if unavailable.
         * @property interBankUnavailableReason Why the inter-bank QR is unavailable, if it is.
         */
        data class Content(
            val intraBankData: String,
            val interBankData: String?,
            val interBankUnavailableReason: StringResource? = null,
        ) : ViewState {

            private val logo: QrLogo
                @Composable
                get() = QrLogo(
                    painter = painterResource(Res.drawable.logo),
                    padding = QrLogoPadding.Natural(.1f),
                    shape = QrLogoShape.circle(),
                    size = 0.2f,
                )

            private val shapes: QrShapes
                get() = QrShapes(
                    code = QrCodeShape.Default,
                    lightPixel = QrPixelShape.Default,
                    darkPixel = QrPixelShape.Default,
                    ball = QrBallShape.Default,
                    frame = QrFrameShape.Default,
                )

            /**
             * QR code colors optimized for maximum scannability.
             * Uses pure black on white for all elements to ensure
             * fastest and most reliable scanning across all devices.
             *
             * NOTE (fork-migration 2026-08-01): the pre-migration KptTheme.colorScheme
             * carried fork-specific `qrBackground` / `qrForeground` extension roles that
             * the template's M3-native KptColorScheme does not expose. Reverting to
             * hardcoded Color.White / Color.Black — matches the "pure black on white"
             * intent already documented above. Restore branded roles by re-adding
             * them to `core/base/designsystem/theme/KptColorScheme` if needed.
             */
            private val colors: QrColors
                @Composable
                get() = QrColors(
                    light = QrBrush.solid(androidx.compose.ui.graphics.Color.White),
                    dark = QrBrush.solid(androidx.compose.ui.graphics.Color.Black),
                    ball = QrBrush.solid(androidx.compose.ui.graphics.Color.Black),
                    frame = QrBrush.solid(androidx.compose.ui.graphics.Color.Black),
                )

            /** QR rendering options (shapes, colors, logo, error correction). */
            val options: QrOptions
                @Composable
                get() = QrOptions(
                    shapes = shapes,
                    colors = colors,
                    logo = logo,
                    errorCorrectionLevel = QrErrorCorrectionLevel.Medium,
                )
        }
    }

    /** Dialogs shown over the QR screen. */
    sealed interface DialogState {
        /** Asks whether to add the account to the pocket. */
        data object AddToPocketConfirmation : DialogState

        /**
         * Error dialog.
         *
         * @property messageRes String resource of the error message.
         */
        data class Error(
            val messageRes: StringResource =
                UiRes.string.core_ui_error_msg_generic,
        ) : DialogState

        /** Progress dialog. */
        data object Loading : DialogState

        /**
         * Success dialog.
         *
         * @property messageRes String resource of the success message.
         */
        data class Success(
            val messageRes: StringResource =
                UiRes.string.core_ui_pocket_added_successfully,
        ) : DialogState

        /** Dialog to set the QR amount. */
        data object ShowSetAmountDialog : DialogState
    }
}

/** One-off events of the QR screen. */
sealed interface MpayQrEvent {
    /** Navigate back. */
    data object OnNavigateBack : MpayQrEvent

    /** The QR image was downloaded. */
    data object QrDownloaded : MpayQrEvent

    /**
     * Show a snackbar.
     *
     * @property message Text to display.
     */
    data class ShowSnackbar(val message: String) : MpayQrEvent
}

/** User and internal actions of the QR screen. */
sealed interface MpayQrAction {

    /** The user navigated back. */
    data object NavigateBack : MpayQrAction

    /** Open the set-amount dialog. */
    data object ShowSetAmountDialog : MpayQrAction

    /** Dismiss the current dialog. */
    data object DismissDialog : MpayQrAction

    /**
     * Answer to the add-to-pocket confirmation.
     *
     * @property add True to add the account to the pocket.
     */
    data class ConfirmAddToPocket(val add: Boolean) : MpayQrAction

    // Account picker actions
    /** Open the account picker. */
    data object ShowAccountPicker : MpayQrAction

    /** Close the account picker. */
    data object DismissAccountPicker : MpayQrAction

    /**
     * Select an account for QR generation.
     *
     * @property account The chosen account.
     */
    data class SelectAccount(val account: Account) : MpayQrAction

    /**
     * The entered amount changed.
     *
     * @property amount The new amount text.
     */
    data class AmountChanged(val amount: String) : MpayQrAction

    /**
     * The selected currency changed.
     *
     * @property currency The new currency code.
     */
    data class CurrencyChanged(val currency: String) : MpayQrAction

    /**
     * The selected QR tab changed.
     *
     * @property page New page index: 0 intra-bank, 1 inter-bank.
     */
    data class PageChanged(val page: Int) : MpayQrAction

    /** Confirm the amount entered in the set-amount dialog. */
    data object ConfirmSetAmount : MpayQrAction

    /**
     * Share the QR image.
     *
     * @property data Encoded image bytes.
     */
    data class ShareQrCode(val data: ByteArray) : MpayQrAction {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ShareQrCode

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    /**
     * Save the QR image.
     *
     * @property bytes Encoded image bytes.
     */
    data class DownloadQrCode(val bytes: ByteArray) : MpayQrAction {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as DownloadQrCode

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    /**
     * Copy text to the clipboard.
     *
     * @property text Text to copy.
     */
    data class CopyToClipboard(val text: String) : MpayQrAction

    /** Actions dispatched by the ViewModel itself. */
    sealed interface Internal : MpayQrAction {
        /** Generate the QR codes. */
        data object GenerateQr : Internal
    }
}
