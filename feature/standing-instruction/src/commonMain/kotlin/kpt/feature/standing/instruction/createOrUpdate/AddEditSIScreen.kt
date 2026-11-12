/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction.createOrUpdate

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.BasicDialogState
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkLoadingDialog
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTextField
import kpt.core.designsystem.fork.component.LoadingDialogState
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.model.account.Account
import kpt.core.model.utils.Locale
import kpt.core.model.utils.filterLocales
import kpt.core.ui.AvatarBox
import kpt.core.ui.DropdownBox
import kpt.core.ui.DropdownBoxItem
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ExposedDropdownBox
import kpt.core.ui.ForkDivider
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.standing.instruction.generated.resources.Res
import kpt.feature.standing.instruction.generated.resources.feature_standing_instruction_error_oops
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun AddEditSIScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditSIViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val localList by viewModel.localList.collectAsStateWithLifecycle()
    val toAccounts by viewModel.toClientAccounts.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AddEditSIEvent.OnNavigateBack -> navigateBack.invoke()
            is AddEditSIEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(message = getString(event.message))
                }
            }
        }
    }

    AddEditSIDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(AddEditSIAction.DismissDialog) }
        },
    )

    AddEditSIScreen(
        state = state,
        localeList = localList,
        toAccounts = toAccounts,
        isAddMode = state.isAddMode,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun AddEditSIDialogs(
    dialogState: AddEditSIState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is AddEditSIState.DialogState.Error -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is AddEditSIState.DialogState.Loading -> ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}

@Composable
@VisibleForTesting
internal fun AddEditSIScreen(
    state: AddEditSIState,
    localeList: List<Locale>,
    toAccounts: List<Account>,
    isAddMode: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (AddEditSIAction) -> Unit,
) {
    ForkScaffold(
        topBarTitle = state.title,
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        backPress = {
            onAction(AddEditSIAction.NavigateBack)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            when (state.viewState) {
                is AddEditSIState.ViewState.Loading -> ForkProgressIndicator()

                is AddEditSIState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_standing_instruction_error_oops),
                        subTitle = state.viewState.message,
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is AddEditSIState.ViewState.Content -> {
                    AddEditSIScreenContent(
                        state = state.viewState,
                        localeList = localeList,
                        toAccounts = toAccounts,
                        isAddMode = isAddMode,
                        modifier = modifier,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditSIScreenContent(
    state: AddEditSIState.ViewState.Content,
    localeList: List<Locale>,
    toAccounts: List<Account>,
    isAddMode: Boolean,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onAction: (AddEditSIAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        state = lazyListState,
    ) {
        if (isAddMode) {
            item(key = "Note") {
                OutlinedCard(
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = Color.Transparent,
                    ),
                    shape = KptTheme.shapes.extraSmall,
                ) {
                    ListItem(
                        headlineContent = {
                            Text(text = "Currently Fixed Instruction & Periodic Recurrence are supported.")
                        },
                        leadingContent = {
                            AvatarBox(icon = ForkIcons.Info)
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                    )
                }
            }

            item(key = "FromClient&FromOffice") {
                RowBlock {
                    state.template.fromClient?.let {
                        ForkTextField(
                            value = it.displayName,
                            label = "From Client",
                            onValueChange = {},
                            readOnly = true,
                            showClearIcon = false,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    ForkTextField(
                        value = state.fromOfficeName ?: "",
                        label = "From Office",
                        onValueChange = {},
                        readOnly = true,
                        showClearIcon = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item(key = "FromAccountType") {
                state.fromAccountType?.let {
                    ForkTextField(
                        value = it,
                        label = "From Account Type",
                        onValueChange = {},
                        readOnly = true,
                        showClearIcon = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item(key = "FromAccount") {
                var accountExpanded by remember { mutableStateOf(false) }

                ExposedDropdownBox(
                    expanded = accountExpanded,
                    label = "From Account",
                    value = state.fromAccountNumber,
                    onExpandChange = {
                        accountExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.template.fromAccountOptions?.forEachIndexed { index, it ->
                        DropdownBoxItem(
                            text = it.accountNo,
                            onClick = {
                                onAction(AddEditSIAction.FromAccountChanged(it.id.toString()))
                                accountExpanded = false
                            },
                        )

                        if (state.template.fromAccountOptions?.let { index < it.size - 1 } == true) {
                            ForkDivider()
                        }
                    }
                }
            }

            item(key = "ToOffice&ToAccountType") {
                RowBlock {
                    ForkTextField(
                        value = state.toOfficeName ?: "",
                        label = "To Office",
                        onValueChange = {},
                        readOnly = true,
                        showClearIcon = false,
                        modifier = Modifier.weight(1f),
                    )

                    state.toAccountType?.let {
                        ForkTextField(
                            value = it,
                            label = "To Account Type",
                            onValueChange = {},
                            readOnly = true,
                            showClearIcon = false,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item(key = "ToClient") {
                var toClientExpanded by remember { mutableStateOf(false) }

                ExposedDropdownBox(
                    expanded = toClientExpanded,
                    label = "To Client",
                    value = state.toClientName,
                    onExpandChange = {
                        toClientExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.toClientOptions?.forEachIndexed { index, it ->
                        DropdownBoxItem(
                            text = it.displayName,
                            onClick = {
                                onAction(AddEditSIAction.ToClientChanged(it.id.toString()))
                                toClientExpanded = false
                            },
                        )

                        if (index < state.toClientOptions.size - 1) {
                            ForkDivider()
                        }
                    }

                    if (state.toClientOptions?.isEmpty() == true) {
                        DropdownBoxItem(
                            text = "No Clients Found",
                            onClick = {
                                toClientExpanded = false
                            },
                        )
                    }
                }
            }

            item(key = "ToAccount") {
                var accountExpanded by remember { mutableStateOf(false) }

                val accountNumber = remember(state.payload.toAccountId, toAccounts) {
                    toAccounts.find { it.id == state.payload.toAccountId }?.number ?: ""
                }

                ExposedDropdownBox(
                    expanded = accountExpanded,
                    label = "To Account",
                    value = accountNumber,
                    onExpandChange = {
                        accountExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    toAccounts.forEachIndexed { index, it ->
                        DropdownBoxItem(
                            text = it.number,
                            onClick = {
                                onAction(AddEditSIAction.ToAccountChanged(it.id.toString()))
                                accountExpanded = false
                            },
                        )

                        if (index < toAccounts.size - 1) {
                            ForkDivider()
                        }
                    }

                    if (toAccounts.isEmpty()) {
                        DropdownBoxItem(
                            text = "No Accounts Found",
                            onClick = {
                                accountExpanded = false
                            },
                        )
                    }
                }
            }

            item(key = "Name") {
                ForkTextField(
                    label = "Name",
                    value = state.payload.name,
                    onValueChange = {
                        onAction(AddEditSIAction.NameChanged(it))
                    },
                )
            }

            item(key = "Amount") {
                ForkTextField(
                    label = "Amount",
                    value = state.payload.amount,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    onValueChange = {
                        onAction(AddEditSIAction.AmountChanged(it))
                    },
                )
            }

            item(key = "TransferType") {
                var transferType by remember { mutableStateOf(false) }

                ExposedDropdownBox(
                    expanded = transferType,
                    label = "Transfer Type",
                    value = state.transferType,
                    onExpandChange = {
                        transferType = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.template.transferTypeOptions?.forEachIndexed { index, it ->
                        DropdownBoxItem(
                            text = it.value,
                            onClick = {
                                onAction(AddEditSIAction.TransferTypeChanged(it.id.toString()))
                                transferType = false
                            },
                        )

                        if (state.template.transferTypeOptions?.let { index < it.size - 1 } == true) {
                            ForkDivider()
                        }
                    }
                }
            }

            item(key = "InstructionType") {
                var instructionTypeExpanded by remember { mutableStateOf(false) }

                ExposedDropdownBox(
                    expanded = instructionTypeExpanded,
                    label = "Instruction Type",
                    value = state.instructionType,
                    onExpandChange = {
                        instructionTypeExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.template.instructionTypeOptions?.forEachIndexed { index, it ->
                        DropdownBoxItem(
                            text = it.value,
                            onClick = {
                                onAction(AddEditSIAction.InstructionTypeChanged(it.id.toString()))
                                instructionTypeExpanded = false
                            },
                        )

                        if (state.template.instructionTypeOptions?.let { index < it.size - 1 } == true) {
                            ForkDivider()
                        }
                    }
                }
            }
        }

        item(key = "Priority") {
            var priorityExpanded by remember { mutableStateOf(false) }

            ExposedDropdownBox(
                expanded = priorityExpanded,
                label = "Priority",
                value = state.priority,
                onExpandChange = {
                    priorityExpanded = it
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                state.template.priorityOptions?.forEachIndexed { index, it ->
                    DropdownBoxItem(
                        text = it.value,
                        onClick = {
                            onAction(AddEditSIAction.PriorityChanged(it.id.toString()))
                            priorityExpanded = false
                        },
                    )

                    if (state.template.priorityOptions?.let { index < it.size - 1 } == true) {
                        ForkDivider()
                    }
                }
            }
        }

        item(key = "Status") {
            var statusExpanded by remember { mutableStateOf(false) }

            ExposedDropdownBox(
                expanded = statusExpanded,
                label = "Status",
                value = state.status,
                onExpandChange = {
                    statusExpanded = it
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                state.template.statusOptions?.forEachIndexed { index, it ->
                    DropdownBoxItem(
                        text = it.value,
                        onClick = {
                            onAction(AddEditSIAction.StatusChanged(it.id.toString()))
                            statusExpanded = false
                        },
                    )

                    if (state.template.statusOptions?.let { index < it.size - 1 } == true) {
                        ForkDivider()
                    }
                }
            }
        }

        if (isAddMode) {
            item(key = "RecurrenceType") {
                var recTypeExpanded by remember { mutableStateOf(false) }

                ExposedDropdownBox(
                    expanded = recTypeExpanded,
                    label = "Recurrence Type",
                    value = state.recurrenceType,
                    onExpandChange = {
                        recTypeExpanded = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.template.recurrenceTypeOptions?.forEachIndexed { index, it ->
                        DropdownBoxItem(
                            text = it.value,
                            onClick = {
                                onAction(AddEditSIAction.RecurrenceTypeChanged(it.id.toString()))
                                recTypeExpanded = false
                            },
                        )

                        if (state.template.recurrenceTypeOptions?.let { index < it.size - 1 } == true) {
                            ForkDivider()
                        }
                    }
                }
            }

            if (state.requiredRecurrenceFrequency) {
                item(key = "Recurrence Frequency") {
                    var recFreqExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownBox(
                        expanded = recFreqExpanded,
                        label = "Recurrence Frequency",
                        value = state.recurrenceFrequency,
                        onExpandChange = {
                            recFreqExpanded = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        state.template.recurrenceFrequencyOptions?.forEachIndexed { index, it ->
                            DropdownBoxItem(
                                text = it.value,
                                onClick = {
                                    onAction(AddEditSIAction.RecurrenceFrequencyChanged(it.id.toString()))
                                    recFreqExpanded = false
                                },
                            )

                            if (state.template.recurrenceFrequencyOptions?.let { index < it.size - 1 } == true) {
                                ForkDivider()
                            }
                        }
                    }
                }

                item(key = "Recurrence Interval") {
                    ForkTextField(
                        label = "Recurrence Interval",
                        value = state.payload.recurrenceInterval,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                        ),
                        onValueChange = {
                            onAction(AddEditSIAction.RecurrenceIntervalChanged(it))
                        },
                    )
                }
            }
        }

        item(key = "Locale") {
            val filteredLocalList by remember(localeList, state.payload.locale) {
                derivedStateOf {
                    localeList.filterLocales(state.payload.locale)
                }
            }

            var localeToggled by remember { mutableStateOf(false) }

            DropdownBox(
                expanded = localeToggled,
                label = "Locale",
                value = state.payload.locale,
                readOnly = false,
                showClearIcon = true,
                onValueChange = {
                    onAction(AddEditSIAction.LocaleChanged(it))
                },
                onExpandChange = {
                    localeToggled = it
                },
            ) {
                filteredLocalList.forEachIndexed { index, locale ->
                    DropdownMenuItem(
                        text = { Text(locale.countryName) },
                        onClick = {
                            onAction(AddEditSIAction.LocaleChanged(locale.localName))
                            localeToggled = false
                        },
                    )

                    if (index != filteredLocalList.size - 1) {
                        ForkDivider()
                    }
                }
            }
        }

        item(key = "ValidFrom") {
            var showDialog by remember { mutableStateOf(false) }

            val dateState = rememberDatePickerState(
                initialSelectedDateMillis = state.initialDate,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= state.initialDate
                    }
                },
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
                                onAction(
                                    AddEditSIAction.ValidFromChanged(
                                        dateState.selectedDateMillis ?: state.initialDate,
                                    ),
                                )
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

            ForkTextField(
                label = "Valid From",
                value = state.payload.validFrom,
                readOnly = true,
                showClearIcon = false,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showDialog = true
                        },
                    ) {
                        Icon(
                            imageVector = ForkIcons.CalenderMonth,
                            contentDescription = "Choose Date",
                        )
                    }
                },
                onValueChange = {},
            )
        }

        item(key = "ValidTill") {
            var showDialog by remember { mutableStateOf(false) }

            val dateState = rememberDatePickerState(
                initialSelectedDateMillis = state.initialDate,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= state.initialDate
                    }
                },
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
                                onAction(
                                    AddEditSIAction.ValidTillChanged(
                                        dateState.selectedDateMillis ?: state.initialDate,
                                    ),
                                )
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

            ForkTextField(
                label = "Valid Until",
                value = state.payload.validTill,
                readOnly = true,
                showClearIcon = false,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showDialog = true
                        },
                    ) {
                        Icon(
                            imageVector = ForkIcons.CalenderMonth,
                            contentDescription = "Choose Date",
                        )
                    }
                },
                onValueChange = {},
            )
        }

        if (state.requiredRecurrenceOnMonth && isAddMode) {
            item(key = "RecurrenceOnMonthDay") {
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
                                    onAction(
                                        AddEditSIAction.RecurrenceOnMonthDayChanged(
                                            dateState.selectedDateMillis
                                                ?: state.initialDate,
                                        ),
                                    )
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

                ForkTextField(
                    label = "Recurrence On Month Day",
                    value = state.payload.recurrenceOnMonthDay,
                    readOnly = true,
                    showClearIcon = false,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                showDialog = true
                            },
                        ) {
                            Icon(
                                imageVector = ForkIcons.CalenderMonth,
                                contentDescription = "Choose Date",
                            )
                        }
                    },
                    onValueChange = {},
                )
            }
        }

        item(key = "SubmitBtn") {
            ForkButton(
                onClick = {
                    onAction(AddEditSIAction.SubmitClicked)
                },
                text = {
                    Text(text = "Submit")
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Horizontally arranged, full-width row used to lay out form fields side by side. */
@Composable
inline fun RowBlock(
    crossinline content: @Composable (RowScope.() -> Unit),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
