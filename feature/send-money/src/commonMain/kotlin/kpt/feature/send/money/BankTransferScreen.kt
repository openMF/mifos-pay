/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import mifos_pay.feature.send_money.generated.resources.Res
import mifos_pay.feature.send_money.generated.resources.feature_send_money_account_number
import mifos_pay.feature.send_money.generated.resources.feature_send_money_account_number_error
import mifos_pay.feature.send_money.generated.resources.feature_send_money_account_number_mismatch
import mifos_pay.feature.send_money.generated.resources.feature_send_money_bank_details_note
import mifos_pay.feature.send_money.generated.resources.feature_send_money_bank_transfer
import mifos_pay.feature.send_money.generated.resources.feature_send_money_bank_transfer_to_others
import mifos_pay.feature.send_money.generated.resources.feature_send_money_bank_transfer_to_self
import mifos_pay.feature.send_money.generated.resources.feature_send_money_confirm
import mifos_pay.feature.send_money.generated.resources.feature_send_money_continue
import mifos_pay.feature.send_money.generated.resources.feature_send_money_ifsc_code
import mifos_pay.feature.send_money.generated.resources.feature_send_money_ifsc_validation_error
import mifos_pay.feature.send_money.generated.resources.feature_send_money_receivers_bank_details
import mifos_pay.feature.send_money.generated.resources.feature_send_money_receivers_name
import mifos_pay.feature.send_money.generated.resources.feature_send_money_receivers_name_required
import mifos_pay.feature.send_money.generated.resources.feature_send_money_recent_transfers
import mifos_pay.feature.send_money.generated.resources.feature_send_money_reenter_account_number
import mifos_pay.feature.send_money.generated.resources.feature_send_money_search_ifsc
import mifos_pay.feature.send_money.generated.resources.feature_send_money_select_different_accounts
import mifos_pay.feature.send_money.generated.resources.feature_send_money_self_transfer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankTransferScreen(
    onBackClick: () -> Unit,
    onSearchIfscClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BankTransferViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    EventsEffect(viewModel) { event ->
        when (event) {
            BankTransferEvent.NavigateBack -> {
                onBackClick.invoke()
            }
            BankTransferEvent.ShowIfscSearch -> {
                onSearchIfscClick.invoke()
            }
            BankTransferEvent.NavigateToNext -> {
                // TODO: Navigate to next screen
            }
            BankTransferEvent.AddBankAccount -> {
                // TODO: Navigate to add bank account screen
            }
            is BankTransferEvent.IfscCodeSelected -> {
                // IFSC code has been selected and updated in the state
            }
        }
    }

    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                MifosTopBar(
                    topBarTitle = stringResource(Res.string.feature_send_money_bank_transfer),
                    backPress = {
                        viewModel.trySendAction(BankTransferAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = stringResource(Res.string.feature_send_money_bank_transfer_to_others),
                                style = KptTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = stringResource(Res.string.feature_send_money_bank_transfer_to_self),
                                style = KptTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                    )
                }

                when (selectedTabIndex) {
                    0 -> BankTransferToOthersContent(
                        state = state,
                        onAccountNumberChange = { accountNumber ->
                            viewModel.trySendAction(BankTransferAction.UpdateAccountNumber(accountNumber))
                        },
                        onIfscCodeChange = { ifscCode ->
                            viewModel.trySendAction(BankTransferAction.UpdateIfscCode(ifscCode))
                        },
                        onSearchIfscClick = {
                            viewModel.trySendAction(BankTransferAction.SearchIfsc)
                        },
                        onContinueClick = {
                            viewModel.trySendAction(BankTransferAction.Continue)
                        },
                        onReenterAccountNumberChange = { accountNumber ->
                            viewModel.trySendAction(BankTransferAction.UpdateReenterAccountNumber(accountNumber))
                        },
                        onReceiversNameChange = { name ->
                            viewModel.trySendAction(BankTransferAction.UpdateReceiversName(name))
                        },
                        onProceedWithTransferClick = {
                            viewModel.trySendAction(BankTransferAction.ProceedWithTransfer)
                        },
                        onToggleAccountNumberMask = {
                            viewModel.trySendAction(BankTransferAction.ToggleAccountNumberMask)
                        },
                        onToggleReenterAccountNumberMask = {
                            viewModel.trySendAction(BankTransferAction.ToggleReenterAccountNumberMask)
                        },
                        onSetIfscFocus = { focused ->
                            viewModel.trySendAction(BankTransferAction.SetIfscFocus(focused))
                        },
                        onSetAccountNumberFocus = { focused ->
                            viewModel.trySendAction(BankTransferAction.SetAccountNumberFocus(focused))
                        },
                        onSetReenterAccountNumberFocus = { focused ->
                            viewModel.trySendAction(BankTransferAction.SetReenterAccountNumberFocus(focused))
                        },
                        onSetReceiversNameFocus = { focused ->
                            viewModel.trySendAction(BankTransferAction.SetReceiversNameFocus(focused))
                        },
                    )
                    1 -> BankTransferToSelfContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun BankTransferToOthersContent(
    state: BankTransferState,
    onAccountNumberChange: (String) -> Unit,
    onIfscCodeChange: (String) -> Unit,
    onSearchIfscClick: () -> Unit,
    onContinueClick: () -> Unit,
    onReenterAccountNumberChange: (String) -> Unit,
    onReceiversNameChange: (String) -> Unit,
    onProceedWithTransferClick: () -> Unit,
    onToggleAccountNumberMask: () -> Unit,
    onToggleReenterAccountNumberMask: () -> Unit,
    onSetIfscFocus: (Boolean) -> Unit,
    onSetAccountNumberFocus: (Boolean) -> Unit,
    onSetReenterAccountNumberFocus: (Boolean) -> Unit,
    onSetReceiversNameFocus: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val accountNumberFocusRequester = remember { FocusRequester() }
    val reenterAccountNumberFocusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(KptTheme.spacing.lg)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.feature_send_money_receivers_bank_details),
            style = KptTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            MifosTextField(
                value = state.getDisplayAccountNumber(state.accountNumber, state.isAccountNumberMasked),
                onValueChange = { newValue ->
                    if (!state.isAccountNumberMasked) {
                        onAccountNumberChange(newValue)
                    }
                },
                label = stringResource(Res.string.feature_send_money_account_number),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = state.accountNumber.isNotEmpty() && !state.isAccountNumberValid,
                errorText = if (state.accountNumber.isNotEmpty() && !state.isAccountNumberValid) {
                    stringResource(Res.string.feature_send_money_account_number_error)
                } else {
                    null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(accountNumberFocusRequester)
                    .onFocusChanged { focusState ->
                        onSetAccountNumberFocus(focusState.isFocused)
                    },
            )

            if (state.accountNumber.isNotEmpty() && state.isAccountNumberMasked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            onToggleAccountNumberMask()
                            // Focus the field after unmasking
                            accountNumberFocusRequester.requestFocus()
                        },
                )
            }
        }

        MifosTextField(
            value = state.ifscCode,
            onValueChange = onIfscCodeChange,
            label = stringResource(Res.string.feature_send_money_ifsc_code),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters,
            ),
            isError = state.isIfscFocused && state.ifscCode.isNotEmpty() && !state.isIfscCodeValid,
            errorText = if (state.isIfscFocused && state.ifscCode.isNotEmpty() && !state.isIfscCodeValid) {
                stringResource(Res.string.feature_send_money_ifsc_validation_error)
            } else {
                null
            },
            trailingIcon = {
                TextButton(
                    onClick = onSearchIfscClick,
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_money_search_ifsc),
                        style = KptTheme.typography.bodyLarge,
                        color = KptTheme.colorScheme.primary,
                    )
                }
            },
            modifier = Modifier.onFocusChanged { focusState ->
                onSetIfscFocus(focusState.isFocused)
            },
        )

        if (!state.showAdditionalFields) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = KptTheme.colorScheme.primary,
                    )
                }
            } else {
                MifosButton(
                    text = { Text(stringResource(Res.string.feature_send_money_continue)) },
                    onClick = onContinueClick,
                    enabled = state.isFormValid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                MifosTextField(
                    value = state.getDisplayAccountNumber(state.reenterAccountNumber, state.isReenterAccountNumberMasked),
                    onValueChange = { newValue ->
                        if (!state.isReenterAccountNumberMasked) {
                            onReenterAccountNumberChange(newValue)
                        }
                    },
                    label = stringResource(Res.string.feature_send_money_reenter_account_number),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    isError = state.reenterAccountNumber.isNotEmpty() && !state.isAccountNumberMatching,
                    errorText = if (state.reenterAccountNumber.isNotEmpty() && !state.isAccountNumberMatching) {
                        stringResource(Res.string.feature_send_money_account_number_mismatch)
                    } else {
                        null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(reenterAccountNumberFocusRequester)
                        .onFocusChanged { focusState ->
                            onSetReenterAccountNumberFocus(focusState.isFocused)
                        },
                )

                if (state.reenterAccountNumber.isNotEmpty() && state.isReenterAccountNumberMasked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable {
                                onToggleReenterAccountNumberMask()
                                // Focus the field after unmasking
                                reenterAccountNumberFocusRequester.requestFocus()
                            },
                    )
                }
            }

            MifosTextField(
                value = state.receiversName,
                onValueChange = onReceiversNameChange,
                label = stringResource(Res.string.feature_send_money_receivers_name),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = state.receiversName.isNotEmpty() && !state.isReceiversNameValid,
                errorText = if (state.receiversName.isNotEmpty() && !state.isReceiversNameValid) {
                    stringResource(Res.string.feature_send_money_receivers_name_required)
                } else {
                    null
                },
                modifier = Modifier.onFocusChanged { focusState ->
                    onSetReceiversNameFocus(focusState.isFocused)
                },
            )

            MifosButton(
                text = { Text(stringResource(Res.string.feature_send_money_confirm)) },
                onClick = onProceedWithTransferClick,
                enabled = state.isAdditionalFieldsValid,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = stringResource(Res.string.feature_send_money_bank_details_note),
            style = KptTheme.typography.bodySmall,
            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = KptTheme.spacing.sm),
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

        RecentTransfersSection()
    }
}

@Composable
private fun RecentTransfersSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.feature_send_money_recent_transfers),
            style = KptTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            RecentTransferItem(
                name = "John Doe",
                onClick = { /* TODO: Handle click */ },
                modifier = Modifier.weight(1f),
            )
            RecentTransferItem(
                name = "Jane Smith",
                onClick = { /* TODO: Handle click */ },
                modifier = Modifier.weight(1f),
            )
            RecentTransferItem(
                name = "Mike Johnson",
                onClick = { /* TODO: Handle click */ },
                modifier = Modifier.weight(1f),
            )
            RecentTransferItem(
                name = "Sarah Wilson",
                onClick = { /* TODO: Handle click */ },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RecentTransferItem(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(KptTheme.spacing.sm)),
        color = KptTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = KptTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = name,
                style = KptTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = KptTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BankTransferToSelfContent(
    viewModel: BankTransferViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(KptTheme.spacing.lg)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(Res.string.feature_send_money_self_transfer),
            style = KptTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.md))

        BankAccountSelectionSection(
            viewModel = viewModel,
            isFromAccount = true,
        )

        HorizontalDivider(
            Modifier.padding(vertical = KptTheme.spacing.md),
            thickness = 1.dp,
            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        )

        BankAccountSelectionSection(
            viewModel = viewModel,
            isFromAccount = false,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

        if (!state.isSelfTransferValid && (state.selectedFromBankAccount != null || state.selectedToBankAccount != null)) {
            Text(
                text = stringResource(Res.string.feature_send_money_select_different_accounts),
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = KptTheme.spacing.sm),
            )
        }

        MifosButton(
            text = { Text(stringResource(Res.string.feature_send_money_continue)) },
            onClick = {
                viewModel.trySendAction(BankTransferAction.ContinueSelfTransfer)
            },
            enabled = state.isSelfTransferValid,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BankAccountSelectionSection(
    viewModel: BankTransferViewModel,
    isFromAccount: Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (isFromAccount) {
                        viewModel.trySendAction(BankTransferAction.ToggleFromBankAccountDropdown(!state.isFromBankAccountDropdownExpanded))
                    } else {
                        viewModel.trySendAction(BankTransferAction.ToggleToBankAccountDropdown(!state.isToBankAccountDropdownExpanded))
                    }
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isFromAccount) "Select bank account to transfer from" else "Select bank account to transfer to",
                style = KptTheme.typography.titleSmall,
                color = KptTheme.colorScheme.onSurface,
            )

            Icon(
                imageVector = MifosIcons.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = KptTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }

        if ((isFromAccount && state.isFromBankAccountDropdownExpanded) || (!isFromAccount && state.isToBankAccountDropdownExpanded)) {
            BankAccountSelectionList(
                bankAccounts = state.bankAccounts,
                selectedBankAccount = if (isFromAccount) state.selectedFromBankAccount else state.selectedToBankAccount,
                onBankAccountSelect = { bankAccount ->
                    if (isFromAccount) {
                        viewModel.trySendAction(BankTransferAction.SelectFromBankAccount(bankAccount))
                    } else {
                        viewModel.trySendAction(BankTransferAction.SelectToBankAccount(bankAccount))
                    }
                },
                onAddBankAccount = {
                    viewModel.trySendAction(BankTransferAction.AddBankAccount)
                },
            )
        } else {
            BankAccountSelectionButton(
                selectedBankAccount = if (isFromAccount) state.selectedFromBankAccount else state.selectedToBankAccount,
                onClick = {
                    if (isFromAccount) {
                        viewModel.trySendAction(BankTransferAction.ToggleFromBankAccountDropdown(true))
                    } else {
                        viewModel.trySendAction(BankTransferAction.ToggleToBankAccountDropdown(true))
                    }
                },
            )
        }
    }
}

@Composable
private fun BankAccountSelectionButton(
    selectedBankAccount: BankAccount?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        color = KptTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = KptTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MifosIcons.Bank,
                        contentDescription = "Bank Logo",
                        tint = KptTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = selectedBankAccount?.bankName ?: "Select a bank account",
                        style = KptTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = KptTheme.colorScheme.onSurface,
                    )

                    if (selectedBankAccount != null) {
                        Text(
                            text = selectedBankAccount.maskedAccountNumber,
                            style = KptTheme.typography.bodySmall,
                            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            if (selectedBankAccount != null) {
                Icon(
                    imageVector = MifosIcons.Check,
                    contentDescription = "Selected",
                    tint = KptTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun BankAccountSelectionList(
    bankAccounts: List<BankAccount>,
    selectedBankAccount: BankAccount?,
    onBankAccountSelect: (BankAccount) -> Unit,
    onAddBankAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        bankAccounts.forEach { bankAccount ->
            BankAccountSelectionItem(
                bankAccount = bankAccount,
                isSelected = selectedBankAccount?.id == bankAccount.id,
                onClick = { onBankAccountSelect(bankAccount) },
            )
        }

        BankAccountAddItem(
            onClick = onAddBankAccount,
        )
    }
}

@Composable
private fun BankAccountSelectionItem(
    bankAccount: BankAccount,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        color = if (isSelected) KptTheme.colorScheme.primaryContainer else KptTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isSelected) KptTheme.colorScheme.primary else KptTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MifosIcons.Bank,
                    contentDescription = "Bank Logo",
                    tint = if (isSelected) KptTheme.colorScheme.onPrimary else KptTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = bankAccount.bankName,
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) KptTheme.colorScheme.onPrimaryContainer else KptTheme.colorScheme.onSurface,
                )

                Text(
                    text = bankAccount.maskedAccountNumber,
                    style = KptTheme.typography.bodySmall,
                    color = if (isSelected) KptTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )

                Text(
                    text = bankAccount.accountType,
                    style = KptTheme.typography.bodySmall,
                    color = if (isSelected) KptTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else KptTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            Icon(
                imageVector = if (isSelected) MifosIcons.Check else MifosIcons.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) KptTheme.colorScheme.primary else KptTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BankAccountAddItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        color = KptTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Icon(
                imageVector = MifosIcons.Bank,
                contentDescription = "Add Bank Account",
                tint = KptTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )

            Text(
                text = "Add bank account",
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.primary,
            )
        }
    }
}
