/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.feature.kyc

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import mifos_pay.feature.kyc.generated.resources.Res
import mifos_pay.feature.kyc.generated.resources.feature_kyc_address_line_1
import mifos_pay.feature.kyc.generated.resources.feature_kyc_address_line_2
import mifos_pay.feature.kyc.generated.resources.feature_kyc_first_name
import mifos_pay.feature.kyc.generated.resources.feature_kyc_last_name
import mifos_pay.feature.kyc.generated.resources.feature_kyc_phone_number
import mifos_pay.feature.kyc.generated.resources.feature_kyc_select_dob
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.mifospay.core.designsystem.component.BasicDialogState
import org.mifospay.core.designsystem.component.LoadingDialogState
import org.mifospay.core.designsystem.component.MifosBasicDialog
import org.mifospay.core.designsystem.component.MifosButton
import org.mifospay.core.designsystem.component.MifosLoadingDialog
import org.mifospay.core.designsystem.component.MifosScaffold
import org.mifospay.core.designsystem.component.MifosTextField
import org.mifospay.core.designsystem.icon.MifosIcons
import org.mifospay.core.ui.utils.EventsEffect

@Composable
internal fun KYCLevel1Screen(
    navigateBack: () -> Unit,
    navigateToKycLevel2: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KYCLevel1ViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is KycLevel1Event.NavigateToKycLevel2 -> navigateToKycLevel2.invoke()
            is KycLevel1Event.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }

            is KycLevel1Event.OnNavigateBack -> navigateBack.invoke()
        }
    }

    KycLevel1Dialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(KycLevel1Action.DismissDialog) }
        },
    )

    KYCLevel1ScreenContent(
        state = state,
        onAction = remember(viewModel) {
            { action -> viewModel.trySendAction(action) }
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KYCLevel1ScreenContent(
    state: KycLevel1State,
    onAction: (KycLevel1Action) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        topBarTitle = state.title,
        backPress = {
            onAction(KycLevel1Action.NavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_kyc_first_name),
                    value = state.firstNameInput,
                    onValueChange = {
                        onAction(KycLevel1Action.FirstNameChanged(it))
                    },
                )
            }

            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_kyc_last_name),
                    value = state.lastNameInput,
                    onValueChange = {
                        onAction(KycLevel1Action.LastNameChanged(it))
                    },
                )
            }

            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_kyc_phone_number),
                    value = state.mobileNoInput,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        onAction(KycLevel1Action.MobileNoChanged(it))
                    },
                )
            }

            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_kyc_address_line_1),
                    value = state.addressLine1Input,
                    onValueChange = {
                        onAction(KycLevel1Action.AddressLine1Changed(it))
                    },
                )
            }

            item {
                MifosTextField(
                    label = stringResource(Res.string.feature_kyc_address_line_2),
                    value = state.addressLine2Input,
                    onValueChange = {
                        onAction(KycLevel1Action.AddressLine2Changed(it))
                    },
                )
            }

            item {
                var showDialog by remember { mutableStateOf(false) }

                val dateState = rememberDatePickerState(
                    initialSelectedDateMillis = state.initialDate,
                )

                val confirmEnabled = remember {
                    derivedStateOf { dateState.selectedDateMillis != null }
                }

                AnimatedVisibility(showDialog) {
                    DatePickerDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    dateState.selectedDateMillis?.let { millis ->
                                        onAction(KycLevel1Action.DobChanged(millis))
                                    }
                                },
                                enabled = confirmEnabled.value,
                            ) {
                                Text(text = "Ok")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                },
                            ) { Text("Cancel") }
                        },
                        content = {
                            DatePicker(state = dateState)
                        },
                    )
                }

                MifosTextField(
                    label = stringResource(Res.string.feature_kyc_select_dob),
                    value = state.dobInput,
                    readOnly = true,
                    showClearIcon = false,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                showDialog = true
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = KptTheme.colorScheme.tertiary,
                                contentColor = KptTheme.colorScheme.tertiaryContainer,
                            ),
                        ) {
                            Icon(
                                imageVector = MifosIcons.CalenderMonth,
                                contentDescription = "Choose Date",
                            )
                        }
                    },
                    onValueChange = {},
                )
            }

            item {
                MifosButton(
                    onClick = {
                        onAction(KycLevel1Action.SubmitClicked)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = state.submitButtonText)
                }
            }
        }
    }
}

@Composable
private fun KycLevel1Dialogs(
    dialogState: KycLevel1State.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is KycLevel1State.DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is KycLevel1State.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}
