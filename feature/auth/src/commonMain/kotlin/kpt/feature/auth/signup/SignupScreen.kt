/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.signup

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopAppBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.CombinedPasswordErrorCard
import kpt.core.ui.DropdownBoxItem
import kpt.core.ui.ExposedDropdownBox
import kpt.core.ui.MifosPasswordField
import kpt.core.ui.utils.EventsEffect
import kpt.feature.auth.signup.SignUpState.DialogState
import mifos_pay.feature.auth.generated.resources.Res
import mifos_pay.feature.auth.generated.resources.feature_auth_address_line_1
import mifos_pay.feature.auth.generated.resources.feature_auth_address_line_2
import mifos_pay.feature.auth.generated.resources.feature_auth_all_fields_are_mandatory
import mifos_pay.feature.auth.generated.resources.feature_auth_complete
import mifos_pay.feature.auth.generated.resources.feature_auth_complete_your_registration
import mifos_pay.feature.auth.generated.resources.feature_auth_confirm_password
import mifos_pay.feature.auth.generated.resources.feature_auth_country
import mifos_pay.feature.auth.generated.resources.feature_auth_email
import mifos_pay.feature.auth.generated.resources.feature_auth_first_name
import mifos_pay.feature.auth.generated.resources.feature_auth_last_name
import mifos_pay.feature.auth.generated.resources.feature_auth_mobile_no
import mifos_pay.feature.auth.generated.resources.feature_auth_password
import mifos_pay.feature.auth.generated.resources.feature_auth_pin_code
import mifos_pay.feature.auth.generated.resources.feature_auth_state
import mifos_pay.feature.auth.generated.resources.feature_auth_username
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun SignupScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is SignUpEvent.NavigateBack -> onNavigateBack.invoke()
            is SignUpEvent.NavigateToLogin -> onNavigateToLogin.invoke(event.username)
            is SignUpEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    SignUpDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SignUpAction.ErrorDialogDismiss) }
        },
    )

    SignupScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupScreen(
    state: SignUpState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (SignUpAction) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    MifosScaffold(
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        containerColor = KptTheme.colorScheme.background,
        topBar = {
            MifosTopAppBar(
                title = stringResource(Res.string.feature_auth_complete_your_registration),
                subtitle = stringResource(Res.string.feature_auth_all_fields_are_mandatory),
                scrollBehavior = scrollBehavior,
                navigationIcon = MifosIcons.Back,
                navigationIconContentDescription = "Back",
                onNavigationIconClick = {
                    onAction(SignUpAction.CloseClick)
                },
            )
        },
    ) {
        SignupScreenContent(
            modifier = Modifier.padding(it),
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun SignupScreenContent(
    modifier: Modifier = Modifier,
    state: SignUpState,
    onAction: (SignUpAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        item {
            MifosOutlinedTextField(
                value = state.firstNameInput,
                label = stringResource(Res.string.feature_auth_first_name),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                ),
                onValueChange = {
                    onAction(SignUpAction.FirstNameInputChange(it))
                },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.lastNameInput,
                label = stringResource(Res.string.feature_auth_last_name),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                ),
                onValueChange = {
                    onAction(SignUpAction.LastNameInputChange(it))
                },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.userNameInput,
                label = stringResource(Res.string.feature_auth_username),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                ),
                onValueChange = {
                    onAction(SignUpAction.UserNameInputChange(it))
                },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.emailInput,
                label = stringResource(Res.string.feature_auth_email),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
                onValueChange = {
                    onAction(SignUpAction.EmailInputChange(it))
                },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.mobileNumberInput,
                label = stringResource(Res.string.feature_auth_mobile_no),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                onValueChange = {
                    onAction(SignUpAction.MobileNumberInputChange(it))
                },
            )
        }

        item {
            Column {
                var showPassword by rememberSaveable { mutableStateOf(false) }
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()

                MifosPasswordField(
                    value = state.passwordInput,
                    label = stringResource(Res.string.feature_auth_password),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {
                        onAction(SignUpAction.PasswordInputChange(it))
                    },
                    showPassword = showPassword,
                    showPasswordChange = { showPassword = !showPassword },
                    interactionSource = interactionSource,
                )
                Spacer(modifier = Modifier.height(KptTheme.spacing.xs))
                CombinedPasswordErrorCard(
                    modifier = Modifier.fillMaxWidth(),
                    errors = state.passwordFeedback,
                    passwordStrengthState = state.passwordStrengthState,
                    currentCharacterCount = state.passwordInput.length,
                    isPasswordFieldFocused = isFocused,
                )
            }
        }

        item {
            var showPassword by rememberSaveable { mutableStateOf(false) }

            MifosPasswordField(
                value = state.confirmPasswordInput,
                label = stringResource(Res.string.feature_auth_confirm_password),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    onAction(SignUpAction.ConfirmPasswordInputChange(it))
                },
                showPassword = showPassword,
                showPasswordChange = { showPassword = !showPassword },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.addressLine1Input,
                label = stringResource(Res.string.feature_auth_address_line_1),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {
                    onAction(SignUpAction.AddressLine1InputChange(it))
                },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.addressLine2Input,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.feature_auth_address_line_2),
                onValueChange = {
                    onAction(SignUpAction.AddressLine2InputChange(it))
                },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.pinCodeInput,
                label = stringResource(Res.string.feature_auth_pin_code),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                onValueChange = {
                    onAction(SignUpAction.PinCodeInputChange(it))
                },
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                var isCountryDropdownExpanded by remember { mutableStateOf(false) }
                var isStateDropdownExpanded by remember { mutableStateOf(false) }

                // Country Dropdown
                ExposedDropdownBox(
                    expanded = isCountryDropdownExpanded,
                    label = stringResource(Res.string.feature_auth_country),
                    value = state.countryInput,
                    onExpandChange = { isCountryDropdownExpanded = it },
                    modifier = Modifier.weight(1.5f),
                ) {
                    state.countriesWithStates.keys.forEach { country ->
                        DropdownBoxItem(
                            text = country,
                            onClick = {
                                onAction(SignUpAction.CountryInputChange(country))
                                isCountryDropdownExpanded = false
                            },
                        )
                    }
                }

                // State Dropdown
                if (state.statesForSelectedCountry.isNullOrEmpty()) {
                    MifosOutlinedTextField(
                        value = state.stateInput,
                        label = stringResource(Res.string.feature_auth_state),
                        onValueChange = {
                            onAction(SignUpAction.StateInputChange(it))
                        },
                        modifier = Modifier.weight(1.5f),
                    )
                } else {
                    ExposedDropdownBox(
                        expanded = isStateDropdownExpanded,
                        label = stringResource(Res.string.feature_auth_state),
                        value = state.stateInput,
                        onExpandChange = { isStateDropdownExpanded = it },
                        modifier = Modifier.weight(1.5f),
                    ) {
                        state.statesForSelectedCountry.forEach { stateName ->
                            DropdownBoxItem(
                                text = stateName,
                                onClick = {
                                    onAction(SignUpAction.StateInputChange(stateName))
                                    isStateDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        item {
            MifosButton(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = true,
                onClick = {
                    onAction(SignUpAction.SubmitClick)
                },
                contentPadding = PaddingValues(KptTheme.spacing.md),
            ) {
                Text(
                    text = stringResource(Res.string.feature_auth_complete),
                )
            }
        }
    }
}

@Composable
private fun SignUpDialogs(
    dialogState: DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        is DialogState.Error.StringMessage -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(dialogState.message),
            onDismissRequest = onDismissRequest,
        )

        is DialogState.Error.ResourceMessage -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(stringResource(dialogState.message)),
            onDismissRequest = onDismissRequest,
        )

        else -> Unit
    }
}
