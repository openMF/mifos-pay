/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.uploadDocs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.BasicDialogState
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTopBar
import kpt.core.ui.fork.utils.EventsEffect
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.lib.loan.component.UploadDocumentsSection
import org.mifos.lib.loan.generated.resources.Res
import org.mifos.lib.loan.generated.resources.feature_apply_loan_section_fill_details
import org.mifos.lib.loan.generated.resources.feature_button_next
import org.mifos.lib.loan.ui.uploadDocs.component.BottomSheetContent
/**
 * Entry point for the Document Upload screen.
 *
 * Orchestrates the UI state, handles navigation side effects, and manages the display of
 * dialogs and bottom sheets for uploading the bank statement, property document, and
 * signature required by the loan application.
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateToConfirmDetails Callback to proceed to the Confirm Details step once all
 * required documents have been uploaded.
 * @param viewModel The state holder managing document selection and upload logic.
 */
@Composable
internal fun UploadDocsScreen(
    navigateBack: () -> Unit,
    navigateToConfirmDetails: (
        clientId: Long,
        productId: Long,
        applicantName: String,
        loanProductName: String,
        loanPurpose: String,
        disbursementDate: String,
        principalAmount: String,
        providerId: String,
    ) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UploadDocsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            UploadDocsEvent.NavigateBack -> navigateBack()

            is UploadDocsEvent.NavigateToConfirmDetails -> {
                navigateToConfirmDetails(
                    event.clientId,
                    event.productId,
                    event.applicantName,
                    event.loanProductName,
                    event.loanPurpose,
                    event.disbursementDate,
                    event.principalAmount,
                    event.providerId,
                )
            }
        }
    }

    UploadDocsDialogs(
        dialogState = state.dialogState,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )

    UploadDocsScreenContent(
        modifier = modifier,
        state = state,
        onAction = {
            viewModel.trySendAction(it)
        },
    )
}

/**
 * Renders overlay UIs such as error alerts or the signature capture bottom sheet based on
 * the active dialog state.
 *
 * @param dialogState The current state determining which dialog or sheet to show.
 * @param onAction Callback to handle dialog interactions (dismissal, mode switching).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UploadDocsDialogs(
    dialogState: UploadDocumentDialog?,
    onAction: (UploadDocsAction) -> Unit,
) {
    when (dialogState) {
        is UploadDocumentDialog.Error -> {
            ForkBasicDialog(
                visibilityState = BasicDialogState.Shown(
                    message = stringResource(dialogState.error),
                ),
                onDismissRequest = {
                    onAction(UploadDocsAction.DismissDialog)
                },
            )
        }

        is UploadDocumentDialog.ShowSignaturePicker -> {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = dialogState.isSignatureMode,
            )

            // Animate the sheet expansion when transitioning to signature mode
            LaunchedEffect(dialogState.isSignatureMode) {
                if (dialogState.isSignatureMode) {
                    sheetState.expand()
                }
            }

            ModalBottomSheet(
                onDismissRequest = {
                    onAction(UploadDocsAction.DismissDialog)
                },
                sheetState = sheetState,
                containerColor = Color.White,
                contentWindowInsets = {
                    if (dialogState.isSignatureMode) {
                        WindowInsets(0)
                    } else {
                        BottomSheetDefaults.windowInsets
                    }
                },
                modifier = if (dialogState.isSignatureMode) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.wrapContentHeight()
                },
                dragHandle = {
                    if (dialogState.isSignatureMode) {
                        null
                    } else {
                        BottomSheetDefaults.DragHandle()
                    }
                },
            ) {
                BottomSheetContent(
                    onAction = onAction,
                    isSignatureMode = dialogState.isSignatureMode,
                )
            }
        }

        null -> Unit
    }
}

/**
 * Displays the main layout containing the document list and the "Next" button.
 *
 * @param state The current UI state containing the list of required and uploaded documents.
 * @param onAction Callback to handle user interactions like clicking upload or continue.
 */
@Composable
internal fun UploadDocsScreenContent(
    state: UploadDocsState,
    modifier: Modifier = Modifier,
    onAction: (UploadDocsAction) -> Unit,
) {
    ForkScaffold(
        modifier = modifier,
        topBar = {
            ForkTopBar(
                topBarTitle = stringResource(Res.string.feature_apply_loan_section_fill_details),
                backPress = { onAction(UploadDocsAction.OnNavigateBack) },
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            UploadDocumentsSection(
                state = state,
                onAction = onAction,
            )

            ForkButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = {
                    onAction(UploadDocsAction.NavigateToNextScreen)
                },
                enabled = state.isSubmitEnabled,
            ) {
                Text(
                    text = stringResource(Res.string.feature_button_next),
                )
            }
        }
    }
}
