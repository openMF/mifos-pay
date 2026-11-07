/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.designsystem.fork.component.BasicDialogState
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkLoadingDialog
import kpt.core.designsystem.fork.component.ForkOutlinedButton
import kpt.core.designsystem.fork.component.ForkOutlinedTextField
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTopBar
import kpt.core.designsystem.fork.component.LoadingDialogState
import kpt.core.model.autopay.BillerCategory
import kpt.core.ui.DropdownBox
import kpt.core.ui.DropdownBoxItem
import kpt.core.ui.fork.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
/** Screen with a form for editing an existing biller. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillerScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditBillerViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var showCategoryDropdown by remember { mutableStateOf(false) }

    EventsEffect(viewModel) { event ->
        when (event) {
            is EditBillerEvent.BillerUpdated -> {
                onNavigateBack()
            }
        }
    }

    ForkScaffold(
        topBar = {
            ForkTopBar(
                topBarTitle = "Edit Biller",
                backPress = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Update biller details",
                modifier = Modifier.padding(bottom = 8.dp),
            )

            ForkOutlinedTextField(
                label = "Biller Name *",
                value = state.formData.name,
                onValueChange = { viewModel.trySendAction(EditBillerAction.UpdateBillerName(it)) },
                isError = state.validationResult.nameError != null,
                errorMessage = state.validationResult.nameError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
            )

            ForkOutlinedTextField(
                label = "Account Number *",
                value = state.formData.accountNumber,
                onValueChange = { viewModel.trySendAction(EditBillerAction.UpdateAccountNumber(it)) },
                isError = state.validationResult.accountNumberError != null,
                errorMessage = state.validationResult.accountNumberError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            )

            ForkOutlinedTextField(
                label = "Contact Number *",
                value = state.formData.contactNumber,
                onValueChange = { viewModel.trySendAction(EditBillerAction.UpdateContactNumber(it)) },
                isError = state.validationResult.contactNumberError != null,
                errorMessage = state.validationResult.contactNumberError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                ),
            )

            ForkOutlinedTextField(
                label = "Email (Optional)",
                value = state.formData.email,
                onValueChange = { viewModel.trySendAction(EditBillerAction.UpdateEmail(it)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            DropdownBox(
                expanded = showCategoryDropdown,
                label = "Biller Category *",
                value = state.formData.category?.displayName ?: "",
                isError = state.validationResult.categoryError != null,
                errorText = state.validationResult.categoryError,
                onExpandChange = { showCategoryDropdown = it },
            ) {
                BillerCategory.entries.forEach { category ->
                    DropdownBoxItem(
                        text = category.displayName,
                        onClick = {
                            viewModel.trySendAction(EditBillerAction.UpdateCategory(category))
                            showCategoryDropdown = false
                        },
                    )
                }
            }

            ForkOutlinedTextField(
                label = "Address (Optional)",
                value = state.formData.address,
                onValueChange = { viewModel.trySendAction(EditBillerAction.UpdateAddress(it)) },
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ForkOutlinedButton(
                    text = { Text("Cancel") },
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                )
                ForkButton(
                    text = { Text("Update Biller") },
                    onClick = { viewModel.trySendAction(EditBillerAction.UpdateBiller) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading,
                )
            }
        }
    }

    if (state.isLoading) {
        ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )
    }

    state.error?.let { error ->
        ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = "Error",
                message = error,
            ),
            onDismissRequest = { viewModel.trySendAction(EditBillerAction.ClearError) },
        )
    }
}
