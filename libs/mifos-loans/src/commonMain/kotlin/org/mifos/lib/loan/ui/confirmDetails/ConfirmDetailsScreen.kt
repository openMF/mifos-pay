/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.confirmDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.BasicDialogState
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTopBar
import kpt.core.ui.ForkProgressIndicatorOverlay
import kpt.core.ui.fork.utils.EventsEffect
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifos.lib.loan.component.ConfirmDetailsCard
import org.mifos.lib.loan.generated.resources.Res
import org.mifos.lib.loan.generated.resources.feature_apply_loan_info_confirm_details
import org.mifos.lib.loan.generated.resources.feature_apply_loan_status_success
import org.mifos.lib.loan.generated.resources.feature_apply_loan_title
/**
 * Entry point for the Confirm Details screen — the final step of the loan-application wizard.
 *
 * Owns the **passcode/biometric auth-gate plumbing**, mirroring `TransferConfirmScreen`:
 *  - Observes [entryStateHandle] for [LOAN_APPLICATION_VERIFICATION_KEY] (written by
 *    `internalPasscodeScreen` on success/failure) and re-dispatches it as
 *    [ConfirmDetailsAction.UpdateUserVerificationResult] so the suspended
 *    `handleUserVerification` in [ConfirmDetailsViewModel] resumes.
 *  - Cancellation guard: if the user backs out of the passcode screen without verifying, the
 *    `repeatOnLifecycle` block dispatches `UpdateUserVerificationResult(false)` on RESUMED so
 *    the VM doesn't hang forever.
 *  - Routes [ConfirmDetailsEvent.NavigateForPasscodeVerification] to
 *    [navigateForPasscodeVerification] with [LOAN_APPLICATION_VERIFICATION_KEY].
 *
 * @param navigateBack Callback to return to the previous screen.
 * @param navigateForPasscodeVerification `(verificationKey) -> Unit` — caller binds to
 * `navController::navigateToInternalPasscodeScreen`.
 * @param onSubmitSuccess Callback invoked once the loan application has been submitted
 * successfully and the user has acknowledged the success dialog.
 * @param entryStateHandle This destination's own `SavedStateHandle`, hoisted from the nav-graph
 * builder so the round-trip channel works.
 * @param viewModel The state holder managing the submission flow.
 */
@Composable
internal fun ConfirmDetailsScreen(
    navigateBack: () -> Unit,
    navigateForPasscodeVerification: (String) -> Unit,
    onSubmitSuccess: () -> Unit,
    entryStateHandle: SavedStateHandle,
    modifier: Modifier = Modifier,
    viewModel: ConfirmDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val authResult by entryStateHandle
        .getStateFlow<Boolean?>(LOAN_APPLICATION_VERIFICATION_KEY, null)
        .collectAsStateWithLifecycle()

    LaunchedEffect(authResult) {
        authResult?.let { result ->
            entryStateHandle.remove<Boolean>(LOAN_APPLICATION_VERIFICATION_KEY)
            viewModel.trySendAction(ConfirmDetailsAction.UpdateUserVerificationResult(result))
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (authResult == null && state.isAwaitingPasscodeVerification) {
                viewModel.trySendAction(
                    ConfirmDetailsAction.UpdateUserVerificationResult(false),
                )
            }
        }
    }

    EventsEffect(viewModel) { event ->
        when (event) {
            ConfirmDetailsEvent.NavigateBack -> navigateBack()

            ConfirmDetailsEvent.NavigateForPasscodeVerification -> {
                navigateForPasscodeVerification(LOAN_APPLICATION_VERIFICATION_KEY)
            }

            ConfirmDetailsEvent.SubmitSucceeded -> onSubmitSuccess()
        }
    }

    ConfirmDetailsDialogs(
        dialogState = state.dialogState,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )

    ConfirmDetailsScreenContent(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

/**
 * Displays modal dialogs for the submission result: a success acknowledgement, or an error with
 * a retry option, mirroring `TransferConfirmScreen`'s dialog pattern.
 */
@Composable
internal fun ConfirmDetailsDialogs(
    dialogState: ConfirmDetailsState.DialogState?,
    onAction: (ConfirmDetailsAction) -> Unit,
) {
    when (dialogState) {
        is ConfirmDetailsState.DialogState.Success -> {
            ForkBasicDialog(
                visibilityState = BasicDialogState.Shown(
                    title = stringResource(Res.string.feature_apply_loan_status_success),
                    message = dialogState.message,
                ),
                onDismissRequest = { onAction(ConfirmDetailsAction.AcknowledgeSuccess) },
            )
        }

        is ConfirmDetailsState.DialogState.Error -> {
            ForkBasicDialog(
                visibilityState = BasicDialogState.Shown(message = dialogState.message),
                onConfirm = { onAction(ConfirmDetailsAction.RetrySubmit) },
                onDismissRequest = { onAction(ConfirmDetailsAction.DismissDialog) },
            )
        }

        null -> Unit
    }
}

/**
 * Renders the confirmation summary card and the final submit button.
 *
 * @param state The current UI state containing the application summary and submission status.
 * @param onAction Callback to handle user intent (e.g. clicking "Apply Loan" or navigating back).
 */
@Composable
internal fun ConfirmDetailsScreenContent(
    state: ConfirmDetailsState,
    onAction: (ConfirmDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        topBar = {
            ForkTopBar(
                topBarTitle = stringResource(Res.string.feature_apply_loan_info_confirm_details),
                backPress = { onAction(ConfirmDetailsAction.OnNavigateBack) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(KptTheme.spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
        ) {
            ConfirmDetailsCard(keyValuePairs = state.details)

            ForkButton(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                onClick = { onAction(ConfirmDetailsAction.SubmitClicked) },
                enabled = !state.isSubmitting,
                shape = KptTheme.shapes.medium,
            ) {
                Text(text = stringResource(Res.string.feature_apply_loan_title))
            }
        }

        if (state.isSubmitting) {
            ForkProgressIndicatorOverlay()
        }
    }
}
