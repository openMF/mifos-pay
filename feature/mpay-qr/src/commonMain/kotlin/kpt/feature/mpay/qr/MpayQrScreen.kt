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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.alexzhirkevich.qrose.ImageFormat
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.alexzhirkevich.qrose.toByteArray
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosDialogBox
import kpt.core.designsystem.component.MifosOutlinedButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.model.account.DefaultAccount
import kpt.core.model.client.Client
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.MifosProgressIndicatorOverlay
import kpt.core.ui.generated.resources.core_ui_add_to_pocket_message
import kpt.core.ui.generated.resources.core_ui_add_to_pocket_title
import kpt.core.ui.generated.resources.core_ui_error
import kpt.core.ui.generated.resources.core_ui_no
import kpt.core.ui.generated.resources.core_ui_ok
import kpt.core.ui.generated.resources.core_ui_success
import kpt.core.ui.generated.resources.core_ui_yes
import kpt.core.ui.utils.EventsEffect
import kpt.feature.mpay.qr.components.AccountIdSection
import kpt.feature.mpay.qr.components.AccountPickerBottomSheet
import kpt.feature.mpay.qr.components.AccountSelectorCard
import kpt.feature.mpay.qr.components.InterBankPlaceholder
import kpt.feature.mpay.qr.components.QrActionButtons
import kpt.feature.mpay.qr.components.QrCodeCard
import kpt.feature.mpay.qr.components.QrType
import kpt.feature.mpay.qr.generated.resources.Res
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_copied
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_downloaded
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_external_id_required
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_go_back
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_receive_money
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_scan_to_pay
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_unable_to_generate
import kpt.feature.mpay.qr.generated.resources.feature_request_money_set_amount
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kpt.core.ui.generated.resources.Res as UiRes

@Composable
internal fun MpayQrScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MpayQrViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val currencyList by viewModel.currencyList.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    val copiedMessage = stringResource(Res.string.feature_mpay_qr_copied)
    val downloadedMessage = stringResource(Res.string.feature_mpay_qr_downloaded)

    EventsEffect(viewModel) { event ->
        when (event) {
            MpayQrEvent.OnNavigateBack -> navigateBack.invoke()
            MpayQrEvent.QrDownloaded -> snackbarHostState.showSnackbar(downloadedMessage)
            is MpayQrEvent.ShowSnackbar -> {
                clipboardManager.setText(AnnotatedString(event.message))
                snackbarHostState.showSnackbar(copiedMessage)
            }
        }
    }

    MpayQrDialogs(
        dialogState = state.dialogState,
        showAmountDialog = {
            SetAmountDialog(
                amount = state.qrData.amount,
                currency = state.qrData.currency,
                currencyList = currencyList,
                onAction = viewModel::trySendAction,
            )
        },
        onAction = viewModel::trySendAction,
    )

    MpayQrScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun MpayQrDialogs(
    dialogState: MpayQrState.DialogState?,
    showAmountDialog: @Composable () -> Unit,
    onAction: (MpayQrAction) -> Unit,
) {
    when (dialogState) {
        is MpayQrState.DialogState.ShowSetAmountDialog -> showAmountDialog.invoke()

        is MpayQrState.DialogState.Loading -> MifosProgressIndicatorOverlay()

        is MpayQrState.DialogState.AddToPocketConfirmation -> {
            MifosDialogBox(
                showDialogState = true,
                onDismiss = { onAction(MpayQrAction.ConfirmAddToPocket(false)) },
                title = stringResource(UiRes.string.core_ui_add_to_pocket_title),
                message = stringResource(UiRes.string.core_ui_add_to_pocket_message),
                confirmButtonText = stringResource(UiRes.string.core_ui_yes),
                onConfirm = { onAction(MpayQrAction.ConfirmAddToPocket(true)) },
                dismissButtonText = stringResource(UiRes.string.core_ui_no),
            )
        }

        is MpayQrState.DialogState.Error -> {
            MifosDialogBox(
                showDialogState = true,
                onDismiss = { onAction(MpayQrAction.DismissDialog) },
                title = stringResource(UiRes.string.core_ui_error),
                message = stringResource(dialogState.messageRes),
                confirmButtonText = stringResource(UiRes.string.core_ui_ok),
                onConfirm = { onAction(MpayQrAction.DismissDialog) },
            )
        }
        is MpayQrState.DialogState.Success -> {
            MifosDialogBox(
                showDialogState = true,
                onDismiss = { onAction(MpayQrAction.DismissDialog) },
                title = stringResource(UiRes.string.core_ui_success),
                message = stringResource(dialogState.messageRes),
                confirmButtonText = stringResource(UiRes.string.core_ui_ok),
                onConfirm = { onAction(MpayQrAction.DismissDialog) },
            )
        }

        null -> Unit
    }
}

@Composable
internal fun MpayQrScreen(
    state: MpayQrState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (MpayQrAction) -> Unit,
) {
    MifosScaffold(
        modifier = modifier,
        topBarTitle = stringResource(Res.string.feature_mpay_qr_receive_money),
        backPress = {
            onAction(MpayQrAction.NavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        AnimatedContent(
            targetState = state.viewState,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            contentAlignment = Alignment.Center,
            label = "QrScreenContent",
        ) { viewState ->
            when (viewState) {
                is MpayQrState.ViewState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MifosProgressIndicator()
                    }
                }

                is MpayQrState.ViewState.Error -> {
                    MpayQrErrorContent(
                        message = stringResource(viewState.message),
                        onNavigateBack = { onAction(MpayQrAction.NavigateBack) },
                    )
                }

                is MpayQrState.ViewState.Content -> {
                    MpayQrScreenContent(
                        state = state,
                        contentState = viewState,
                        selectedPage = state.selectedPage,
                        modifier = Modifier,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun MpayQrScreenContent(
    state: MpayQrState,
    contentState: MpayQrState.ViewState.Content,
    selectedPage: Int,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onAction: (MpayQrAction) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { 2 },
    )

    // Sync pager state with viewmodel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onAction(MpayQrAction.PageChanged(page))
        }
    }

    // Account Picker Bottom Sheet
    if (state.isAccountPickerVisible) {
        AccountPickerBottomSheet(
            accounts = state.accounts,
            selectedAccount = state.selectedAccount,
            onAccountSelected = { account ->
                onAction(MpayQrAction.SelectAccount(account))
            },
            onDismiss = {
                onAction(MpayQrAction.DismissAccountPicker)
            },
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        // Header text
        item {
            Text(
                text = stringResource(Res.string.feature_mpay_qr_scan_to_pay),
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Account selector card
        item {
            AccountSelectorCard(
                client = state.client,
                account = state.selectedAccount?.let { account ->
                    DefaultAccount(
                        accountId = account.id,
                        accountNo = account.number,
                    )
                } ?: state.defaultAccount,
                isPrimary = state.selectedAccount?.id == state.defaultAccount.accountId ||
                    state.selectedAccount == null,
                hasMultipleAccounts = state.accounts.size > 1,
                onClick = {
                    onAction(MpayQrAction.ShowAccountPicker)
                },
            )
        }

        // Swipeable QR codes with QrCodeCard component
        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                key = { it },
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    when (page) {
                        0 -> {
                            // Intra-Bank - always show QR
                            QrCodeCard(
                                data = contentState.intraBankData,
                                options = contentState.options,
                                qrType = QrType.INTRA_BANK,
                                modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
                            )
                        }
                        1 -> {
                            // Inter-Bank - show QR or placeholder
                            if (contentState.interBankData != null) {
                                QrCodeCard(
                                    data = contentState.interBankData,
                                    options = contentState.options,
                                    qrType = QrType.INTER_BANK,
                                    modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
                                )
                            } else {
                                InterBankPlaceholder(
                                    reason = stringResource(
                                        contentState.interBankUnavailableReason
                                            ?: Res.string.feature_mpay_qr_external_id_required,
                                    ),
                                    modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Page indicator dots
        item {
            HorizontalPagerIndicator(
                pageCount = 2,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
            )
        }

        // Set Amount button
        item {
            MifosButton(
                onClick = {
                    onAction(MpayQrAction.ShowSetAmountDialog)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.feature_request_money_set_amount))
            }
        }

        // Share and Download buttons
        item {
            val currentData = when {
                pagerState.currentPage == 0 -> contentState.intraBankData
                contentState.interBankData != null -> contentState.interBankData
                else -> null
            }

            if (currentData != null) {
                val qrPainter = rememberQrCodePainter(
                    data = currentData,
                    options = contentState.options,
                )

                QrActionButtons(
                    onShareClick = {
                        val bytes = qrPainter.toByteArray(1024, 1024, ImageFormat.PNG)
                        onAction(MpayQrAction.ShareQrCode(bytes))
                    },
                    onDownloadClick = {
                        val bytes = qrPainter.toByteArray(1024, 1024, ImageFormat.PNG)
                        onAction(MpayQrAction.DownloadQrCode(bytes))
                    },
                )
            }
        }

        // Account IDs section
        item {
            val accountNo = state.selectedAccount?.number ?: state.defaultAccount.accountNo
            val externalId = state.selectedAccount?.externalId ?: state.accountExternalId

            AccountIdSection(
                accountNumber = accountNo,
                externalId = externalId.ifBlank { null },
                onCopyAccountNumber = {
                    onAction(MpayQrAction.CopyToClipboard(accountNo))
                },
                onCopyExternalId = {
                    if (externalId.isNotBlank()) {
                        onAction(MpayQrAction.CopyToClipboard(externalId))
                    }
                },
            )
        }
    }
}

@Composable
private fun MpayQrErrorContent(
    message: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            Text(
                text = stringResource(Res.string.feature_mpay_qr_unable_to_generate),
                style = KptTheme.typography.titleMedium,
                color = KptTheme.colorScheme.error,
            )
        }

        item {
            Text(
                text = message,
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = KptTheme.spacing.md),
            )
        }

        item {
            MifosOutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KptTheme.spacing.md),
            ) {
                Text(text = stringResource(Res.string.feature_mpay_qr_go_back))
            }
        }
    }
}

@Composable
private fun HorizontalPagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(
                        if (index == currentPage) {
                            KptTheme.spacing.sm + KptTheme.spacing.xs / 2
                        } else {
                            KptTheme.spacing.sm
                        },
                    )
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) {
                            KptTheme.colorScheme.primary
                        } else {
                            KptTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun MpayQrScreenLoadingPreview() {
    KptMaterialTheme {
        MpayQrScreen(
            state = MpayQrState(
                client = Client(
                    id = 1,
                    accountNo = "000000001",
                    externalId = "",
                    active = true,
                    activationDate = emptyList(),
                    firstname = "John",
                    lastname = "Doe",
                    displayName = "John Doe",
                    mobileNo = "",
                    emailAddress = "",
                    dateOfBirth = emptyList(),
                    isStaff = false,
                    officeId = 1,
                    officeName = "Head Office",
                    savingsProductName = "",
                ),
                defaultAccount = DefaultAccount(
                    accountId = 1,
                    accountNo = "000000001",
                ),
                viewState = MpayQrState.ViewState.Loading,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun MpayQrScreenErrorPreview() {
    KptMaterialTheme {
        MpayQrErrorContent(
            message = "No default account set",
            onNavigateBack = {},
        )
    }
}

@Preview
@Composable
private fun HorizontalPagerIndicatorPreview() {
    KptMaterialTheme {
        HorizontalPagerIndicator(
            pageCount = 3,
            currentPage = 1,
        )
    }
}
