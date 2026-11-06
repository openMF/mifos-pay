/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.ui.utils.EventsEffect
import kpt.feature.profile.components.EditableProfileImage
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile_edit_profile
import kpt.feature.profile.generated.resources.feature_profile_email
import kpt.feature.profile.generated.resources.feature_profile_firstname
import kpt.feature.profile.generated.resources.feature_profile_lastname
import kpt.feature.profile.generated.resources.feature_profile_mobile
import kpt.feature.profile.generated.resources.feature_profile_save
import kpt.feature.profile.generated.resources.feature_profile_vpa
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun EditProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is EditProfileEvent.NavigateBack -> onBackClick.invoke()
            is EditProfileEvent.ShowToast -> {
                scope.launch {
                    val message = getString(event.message)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Box(modifier) {
        EditProfileScreenContent(
            state = state,
            onAction = remember(viewModel) {
                { action -> viewModel.trySendAction(action) }
            },
            snackbarHostState = snackbarHostState,
        )

        EditProfileDialogs(
            dialogState = state.dialogState,
            onDismissRequest = remember(viewModel) {
                { viewModel.trySendAction(EditProfileAction.DismissErrorDialog) }
            },
        )
    }
}

@Composable
private fun EditProfileScreenContent(
    modifier: Modifier = Modifier,
    state: EditProfileState,
    onAction: (EditProfileAction) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    MifosScaffold(
        topBarTitle = stringResource(Res.string.feature_profile_edit_profile),
        backPress = {
            onAction(EditProfileAction.NavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                EditableProfileImage(
                    profileImage = state.profileImage,
                    onPickImage = {
                        onAction(EditProfileAction.PickProfileImage)
                    },
                )
            }

            item {
                MifosTextField(
                    value = state.firstNameInput,
                    onValueChange = {
                        onAction(EditProfileAction.FirstNameInputChange(it))
                    },
                    label = stringResource(Res.string.feature_profile_firstname),
                )
            }

            item {
                MifosTextField(
                    value = state.lastNameInput,
                    onValueChange = {
                        onAction(EditProfileAction.LastNameInputChange(it))
                    },
                    label = stringResource(Res.string.feature_profile_lastname),
                )
            }

            item {
                MifosTextField(
                    value = state.emailInput,
                    onValueChange = {
                        onAction(EditProfileAction.EmailInputChange(it))
                    },
                    label = stringResource(Res.string.feature_profile_email),
                )
            }

            item {
                MifosTextField(
                    value = state.externalIdInput,
                    onValueChange = {
                        onAction(EditProfileAction.ExternalIdInputChange(it))
                    },
                    label = stringResource(Res.string.feature_profile_vpa),
                )
            }

            item {
                MifosTextField(
                    value = state.phoneNumberInput,
                    onValueChange = {
                        onAction(EditProfileAction.PhoneNumberInputChange(it))
                    },
                    label = stringResource(Res.string.feature_profile_mobile),
                )
            }

            item {
                Spacer(modifier.height(16.dp))

                MifosButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    text = {
                        Text(
                            text = stringResource(Res.string.feature_profile_save),
                        )
                    },
                    onClick = {
                        onAction(EditProfileAction.UpdateProfile)
                    },
                )
            }
        }
    }
}

@Composable
private fun EditProfileDialogs(
    dialogState: EditProfileState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is EditProfileState.DialogState.Error -> {
            val message = when (dialogState) {
                is EditProfileState.DialogState.Error.StringMessage -> dialogState.message
                is EditProfileState.DialogState.Error.ResourceMessage -> stringResource(dialogState.message)
            }
            MifosBasicDialog(
                visibilityState = BasicDialogState.Shown(
                    message = message,
                ),
                onDismissRequest = onDismissRequest,
            )
        }

        is EditProfileState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}
