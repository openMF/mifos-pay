/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.feature.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.platform.PlatformBuildConfig
import kpt.core.base.ui.detectMultiTapGesture
import mifos_pay.feature.auth.generated.resources.Res
import mifos_pay.feature.auth.generated.resources.feature_auth_connected_to
import mifos_pay.feature.auth.generated.resources.feature_auth_login
import mifos_pay.feature.auth.generated.resources.feature_auth_password
import mifos_pay.feature.auth.generated.resources.feature_auth_sign_up
import mifos_pay.feature.auth.generated.resources.feature_auth_username
import mifos_pay.feature.auth.generated.resources.feature_auth_welcome_back
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.mifospay.core.designsystem.component.BasicDialogState
import org.mifospay.core.designsystem.component.MifosBasicDialog
import org.mifospay.core.designsystem.component.MifosButton
import org.mifospay.core.designsystem.component.MifosOutlinedTextField
import org.mifospay.core.designsystem.component.MifosScaffold
import org.mifospay.core.designsystem.theme.MifosTheme
import org.mifospay.core.designsystem.theme.styleNormal18sp
import org.mifospay.core.ui.MifosPasswordField
import org.mifospay.core.ui.MifosProgressIndicatorOverlay
import org.mifospay.core.ui.utils.EventsEffect

@Composable
internal fun LoginScreen(
    onNavigateBack: () -> Unit,
    navigateToMifosPasscodeScreen: () -> Unit,
    navigateToSignupScreen: () -> Unit,
    onShowInstanceSelector: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is LoginEvent.NavigateBack -> onNavigateBack.invoke()
            is LoginEvent.NavigateToSignup -> navigateToSignupScreen.invoke()
            is LoginEvent.NavigateToMifosPasscodeScreen -> navigateToMifosPasscodeScreen()
            is LoginEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LoginDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(LoginAction.ErrorDialogDismiss) }
        },
    )

    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        onShowInstanceSelector = onShowInstanceSelector,
    )

    if (state.dialogState is LoginState.DialogState.Loading) {
        MifosProgressIndicatorOverlay()
    }
}

@Composable
private fun LoginScreen(
    state: LoginState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (LoginAction) -> Unit,
    onShowInstanceSelector: () -> Unit,
) {
    MifosScaffold(
        snackbarHostState = snackbarHostState,
        modifier = modifier.fillMaxSize(),
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        LoginScreenContent(
            state = state,
            onAction = onAction,
            onShowInstanceSelector = onShowInstanceSelector,
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@Composable
private fun LoginDialogs(
    dialogState: LoginState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is LoginState.DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is LoginState.DialogState.Loading -> Unit

        null -> Unit
    }
}

@Composable
private fun LoginScreenContent(
    state: LoginState,
    modifier: Modifier = Modifier,
    onAction: (LoginAction) -> Unit,
    onShowInstanceSelector: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .detectMultiTapGesture(onGestureDetected = onShowInstanceSelector)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KptTheme.spacing.lg)
            .padding(top = KptTheme.spacing.xxl),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(Res.string.feature_auth_login),
            style = KptTheme.typography.titleLarge,
            color = KptTheme.colorScheme.primary,
        )
        Text(
            modifier = Modifier
                .padding(top = KptTheme.spacing.lg),
            text = stringResource(Res.string.feature_auth_welcome_back),
            style = styleNormal18sp,
        )
        Spacer(modifier = Modifier.padding(top = KptTheme.spacing.xl))
        MifosOutlinedTextField(
            label = stringResource(Res.string.feature_auth_username),
            value = state.username,
            onValueChange = {
                onAction(LoginAction.UsernameChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.padding(horizontal = KptTheme.spacing.md))
        MifosPasswordField(
            label = stringResource(Res.string.feature_auth_password),
            value = state.password,
            onValueChange = {
                onAction(LoginAction.PasswordChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            showPassword = state.isPasswordVisible,
            showPasswordChange = {
                onAction(LoginAction.TogglePasswordVisibility)
            },
        )
        val isLoginButtonEnabled = state.username.isNotEmpty() && state.password.isNotEmpty()
        MifosButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md),
            enabled = isLoginButtonEnabled,
            onClick = {
                onAction(LoginAction.LoginClicked)
            },
            contentPadding = PaddingValues(KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_auth_login).uppercase(),
                style = KptTheme.typography.labelLarge,
            )
        }

        SignupButton(
            navigateToSignupScreen = {
                onAction(LoginAction.SignupClicked)
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        // Server Instance Info at bottom - only visible in debug builds
        if (PlatformBuildConfig.isDebug) {
            ServerInstanceInfo(
                endpoint = state.selectedInstanceEndpoint ?: "",
                modifier = Modifier.padding(bottom = KptTheme.spacing.lg),
            )
        }
    }
}

@Composable
private fun SignupButton(
    navigateToSignupScreen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = KptTheme.spacing.lg),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Don’t have an account yet? ",
            style = KptTheme.typography.labelLarge,
        )
        Text(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                navigateToSignupScreen()
            },
            text = stringResource(Res.string.feature_auth_sign_up),
            fontWeight = FontWeight.Bold,
            color = KptTheme.colorScheme.primary,
            style = KptTheme.typography.labelLarge.copy(
                textDecoration = TextDecoration.Underline,
            ),
        )
    }
}

@Composable
private fun ServerInstanceInfo(
    endpoint: String,
    modifier: Modifier = Modifier,
) {
    if (endpoint.isNotEmpty()) {
        Text(
            text = stringResource(Res.string.feature_auth_connected_to, endpoint),
            style = KptTheme.typography.labelSmall,
            color = KptTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    MifosTheme {
        LoginScreen(
            state = LoginState(dialogState = null),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onShowInstanceSelector = {},
        )
    }
}
