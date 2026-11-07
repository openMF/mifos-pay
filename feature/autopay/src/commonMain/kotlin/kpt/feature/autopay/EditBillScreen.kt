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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosOutlinedButton
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.utils.fork.onClick
import kpt.core.model.autopay.RecurrencePattern
import kpt.core.ui.DropdownBox
import kpt.core.ui.DropdownBoxItem
import kpt.core.ui.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Screen with a form for editing an existing bill, including recurrence and optional AutoPay settings. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddBiller: () -> Unit,
    viewModel: EditBillViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var showRecurrenceDropdown by remember { mutableStateOf(false) }
    var showBillerDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    EventsEffect(viewModel) { event ->
        when (event) {
            is EditBillEvent.BillUpdated -> {
                onNavigateBack()
            }
        }
    }

    MifosScaffold(
        topBar = {
            MifosTopBar(
                topBarTitle = "Edit Bill",
                backPress = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Edit bill details",
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                MifosOutlinedTextField(
                    label = "Bill Name *",
                    value = state.formData.name,
                    onValueChange = { viewModel.trySendAction(EditBillAction.UpdateBillName(it)) },
                    isError = state.validationResult.nameError != null,
                    errorMessage = state.validationResult.nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                )

                DropdownBox(
                    expanded = showBillerDropdown,
                    label = "Select Biller *",
                    value = state.formData.billerName ?: "Select a biller",
                    readOnly = true,
                    isError = state.validationResult.billerError != null,
                    errorText = state.validationResult.billerError,
                    onExpandChange = { showBillerDropdown = it },
                ) {
                    state.availableBillers.forEach { biller ->
                        DropdownBoxItem(
                            text = biller.name,
                            onClick = {
                                viewModel.trySendAction(EditBillAction.SelectBiller(biller))
                                showBillerDropdown = false
                            },
                        )
                    }
                    DropdownBoxItem(
                        text = "+ Add New Biller",
                        onClick = {
                            showBillerDropdown = false
                            onNavigateToAddBiller()
                        },
                    )
                }

                MifosOutlinedTextField(
                    label = "Amount *",
                    value = state.formData.amount,
                    onValueChange = { viewModel.trySendAction(EditBillAction.UpdateAmount(it)) },
                    isError = state.validationResult.amountError != null,
                    errorMessage = state.validationResult.amountError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                )

                Box(
                    modifier = Modifier.onClick { showDatePicker = true },
                ) {
                    MifosTextField(
                        label = "Due Date *",
                        value = if (state.formData.dueDate > 0L) {
                            formatDateForDisplay(state.formData.dueDate)
                        } else {
                            ""
                        },
                        onValueChange = { },
                        isError = state.validationResult.dueDateError != null,
                        errorText = state.validationResult.dueDateError,
                        singleLine = true,
                        readOnly = true,
                        showClearIcon = false,
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
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
                    )
                }

                DropdownBox(
                    expanded = showRecurrenceDropdown,
                    label = "Recurrence Pattern *",
                    value = state.formData.recurrencePattern.displayName,
                    readOnly = true,
                    isError = state.validationResult.recurrencePatternError != null,
                    errorText = state.validationResult.recurrencePatternError,
                    onExpandChange = { showRecurrenceDropdown = it },
                ) {
                    RecurrencePattern.entries.forEach { pattern ->
                        DropdownBoxItem(
                            text = pattern.displayName,
                            onClick = {
                                viewModel.trySendAction(EditBillAction.UpdateRecurrencePattern(pattern))
                                showRecurrenceDropdown = false
                            },
                        )
                    }
                }

                MifosOutlinedTextField(
                    label = "Description (Optional)",
                    value = state.formData.description,
                    onValueChange = { viewModel.trySendAction(EditBillAction.UpdateDescription(it)) },
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                )

                AutoPaySection(
                    enableAutoPay = state.formData.enableAutoPay,
                    paymentMethod = state.formData.autoPayPaymentMethod,
                    sourceAccount = state.formData.autoPaySourceAccount,
                    maxAmount = state.formData.autoPayMaxAmount,
                    paymentMethodError = state.validationResult.autoPayPaymentMethodError,
                    sourceAccountError = state.validationResult.autoPaySourceAccountError,
                    maxAmountError = state.validationResult.autoPayMaxAmountError,
                    onEnableAutoPayChanged = { enabled ->
                        viewModel.trySendAction(EditBillAction.UpdateAutoPayEnabled(enabled))
                    },
                    onPaymentMethodChanged = { paymentMethod ->
                        viewModel.trySendAction(EditBillAction.UpdateAutoPayPaymentMethod(paymentMethod))
                    },
                    onSourceAccountChanged = { sourceAccount ->
                        viewModel.trySendAction(EditBillAction.UpdateAutoPaySourceAccount(sourceAccount))
                    },
                    onMaxAmountChanged = { maxAmount ->
                        viewModel.trySendAction(EditBillAction.UpdateAutoPayMaxAmount(maxAmount))
                    },
                )

                if (state.nextPaymentDates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Next Payment Dates:",
                        style = KptTheme.typography.titleMedium,
                    )

                    state.nextPaymentDates.forEach { nextDate ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = nextDate.formattedDate,
                                style = KptTheme.typography.bodyMedium,
                            )
                            if (nextDate.isOverdue) {
                                Text(
                                    text = "Overdue",
                                    style = KptTheme.typography.bodySmall,
                                    color = KptTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MifosOutlinedButton(
                    text = { Text("Cancel") },
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                )
                MifosButton(
                    text = { Text("Update Bill") },
                    onClick = { viewModel.trySendAction(EditBillAction.UpdateBill) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading,
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    AnimatedVisibility(showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = if (state.formData.dueDate > 0L) {
                state.formData.dueDate
            } else {
                Clock.System.now().toEpochMilliseconds()
            },
        )

        val confirmEnabled = remember {
            derivedStateOf { dateState.selectedDateMillis != null }
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { timestamp ->
                            viewModel.trySendAction(EditBillAction.UpdateDueDate(timestamp))
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value,
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                ) {
                    Text("Cancel")
                }
            },
            content = {
                DatePicker(state = dateState)
            },
        )
    }

    if (state.isLoading) {
        MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )
    }

    state.error?.let { error ->
        MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = "Error",
                message = error,
            ),
            onDismissRequest = { viewModel.trySendAction(EditBillAction.ClearError) },
        )
    }
}

@Composable
private fun AutoPaySection(
    enableAutoPay: Boolean,
    paymentMethod: String,
    sourceAccount: String,
    maxAmount: String,
    paymentMethodError: String?,
    sourceAccountError: String?,
    maxAmountError: String?,
    onEnableAutoPayChanged: (Boolean) -> Unit,
    onPaymentMethodChanged: (String) -> Unit,
    onSourceAccountChanged: (String) -> Unit,
    onMaxAmountChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "AutoPay Settings",
                        style = KptTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Automatically pay this bill when due",
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Checkbox(
                    checked = enableAutoPay,
                    onCheckedChange = onEnableAutoPayChanged,
                )
            }

            if (enableAutoPay) {
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                var showPaymentMethodDropdown by remember { mutableStateOf(false) }
                DropdownBox(
                    expanded = showPaymentMethodDropdown,
                    label = "Payment Method *",
                    value = paymentMethod.ifBlank { "Select payment method" },
                    readOnly = true,
                    isError = paymentMethodError != null,
                    errorText = paymentMethodError,
                    onExpandChange = { showPaymentMethodDropdown = it },
                ) {
                    listOf("Bank Account", "Credit Card", "UPI").forEach { method ->
                        DropdownBoxItem(
                            text = method,
                            onClick = {
                                onPaymentMethodChanged(method)
                                showPaymentMethodDropdown = false
                            },
                        )
                    }
                }

                MifosOutlinedTextField(
                    label = "Source Account *",
                    value = sourceAccount,
                    onValueChange = onSourceAccountChanged,
                    isError = sourceAccountError != null,
                    errorMessage = sourceAccountError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                )

                MifosOutlinedTextField(
                    label = "Maximum Amount Limit (Optional)",
                    value = maxAmount,
                    onValueChange = onMaxAmountChanged,
                    isError = maxAmountError != null,
                    errorMessage = maxAmountError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                )

                Text(
                    text = "This amount will be used as a safety limit for automatic payments",
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
