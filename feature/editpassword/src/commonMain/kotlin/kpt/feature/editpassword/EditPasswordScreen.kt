/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.editpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.ui.MifosPasswordField
import kpt.core.ui.PasswordStrengthIndicator
import kpt.core.ui.utils.EventsEffect
import mifos_pay.feature.editpassword.generated.resources.Res
import mifos_pay.feature.editpassword.generated.resources.feature_editpassword_change_password
import mifos_pay.feature.editpassword.generated.resources.feature_editpassword_confirm_new_password
import mifos_pay.feature.editpassword.generated.resources.feature_editpassword_new_password
import mifos_pay.feature.editpassword.generated.resources.feature_editpassword_old_password
import mifos_pay.feature.editpassword.generated.resources.feature_editpassword_save
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun EditPasswordScreen(
    navigateBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPasswordViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is EditPasswordEvent.NavigateBack -> navigateBack.invoke()
            is EditPasswordEvent.OnLogoutUser -> onLogout.invoke()
            is EditPasswordEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }
        }
    }

    Box(modifier) {
        EditPasswordDialogs(
            dialogState = state.dialogState,
            onDismissRequest = remember(viewModel) {
                { viewModel.trySendAction(EditPasswordAction.ErrorDialogDismiss) }
            },
        )

        EditPasswordScreenContent(
            state = state,
            onAction = remember(viewModel) {
                { viewModel.trySendAction(it) }
            },
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
internal fun EditPasswordScreenContent(
    modifier: Modifier = Modifier,
    state: EditPasswordState,
    onAction: (EditPasswordAction) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    MifosScaffold(
        modifier = modifier,
        topBarTitle = stringResource(Res.string.feature_editpassword_change_password),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        backPress = {
            onAction(EditPasswordAction.NavigateBackClick)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            var showPassword by rememberSaveable { mutableStateOf(false) }

            MifosPasswordField(
                value = state.currentPasswordInput,
                label = stringResource(Res.string.feature_editpassword_old_password),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    onAction(EditPasswordAction.CurrentPasswordChange(it))
                },
                showPassword = showPassword,
                showPasswordChange = { showPassword = !showPassword },
            )

            MifosPasswordField(
                value = state.newPasswordInput,
                label = stringResource(Res.string.feature_editpassword_new_password),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    onAction(EditPasswordAction.NewPasswordChange(it))
                },
                showPassword = showPassword,
                showPasswordChange = { showPassword = !showPassword },
            )
            PasswordStrengthIndicator(
                modifier = Modifier,
                state = state.passwordStrengthState,
                currentCharacterCount = state.newPasswordInput.length,
            )
            MifosPasswordField(
                value = state.confirmPasswordInput,
                label = stringResource(Res.string.feature_editpassword_confirm_new_password),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    onAction(EditPasswordAction.ConfirmPasswordChange(it))
                },
                showPassword = showPassword,
                showPasswordChange = { showPassword = !showPassword },
            )

            MifosButton(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = true,
                onClick = {
                    onAction(EditPasswordAction.SubmitClick)
                },
                contentPadding = PaddingValues(KptTheme.spacing.md),
            ) {
                Text(
                    text = stringResource(Res.string.feature_editpassword_save),
                )
            }
        }
    }
}

@Composable
private fun EditPasswordDialogs(
    dialogState: EditPasswordDialog?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is EditPasswordDialog.Error -> {
            val message = if (dialogState.formatArgs.isNotEmpty()) {
                stringResource(dialogState.message, *dialogState.formatArgs.toTypedArray())
            } else {
                stringResource(dialogState.message)
            }
            MifosBasicDialog(
                visibilityState = BasicDialogState.Shown(
                    message = message,
                ),
                onDismissRequest = onDismissRequest,
            )
        }

        is EditPasswordDialog.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        is EditPasswordDialog.ApiError -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        null -> Unit
    }
}
