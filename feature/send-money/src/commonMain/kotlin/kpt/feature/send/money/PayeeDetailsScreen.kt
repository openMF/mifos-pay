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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import mifos_pay.feature.send_money.generated.resources.Res
import mifos_pay.feature.send_money.generated.resources.feature_send_money_add_bank_account
import mifos_pay.feature.send_money.generated.resources.feature_send_money_add_bank_account_desc
import mifos_pay.feature.send_money.generated.resources.feature_send_money_amount_below_minimum
import mifos_pay.feature.send_money.generated.resources.feature_send_money_amount_exceeds_limit
import mifos_pay.feature.send_money.generated.resources.feature_send_money_balance
import mifos_pay.feature.send_money.generated.resources.feature_send_money_bank_icon
import mifos_pay.feature.send_money.generated.resources.feature_send_money_change_account
import mifos_pay.feature.send_money.generated.resources.feature_send_money_check_now
import mifos_pay.feature.send_money.generated.resources.feature_send_money_choose_account
import mifos_pay.feature.send_money.generated.resources.feature_send_money_pay_amount
import mifos_pay.feature.send_money.generated.resources.feature_send_money_payee_details_title
import mifos_pay.feature.send_money.generated.resources.feature_send_money_payee_profile
import mifos_pay.feature.send_money.generated.resources.feature_send_money_paying
import mifos_pay.feature.send_money.generated.resources.feature_send_money_rupee_icon
import mifos_pay.feature.send_money.generated.resources.feature_send_money_selected
import mifos_pay.feature.send_money.generated.resources.feature_send_money_upi_id
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun PayeeDetailsScreen(
    onBackClick: () -> Unit,
    onNavigateToPaymentProcessing: (PayeeDetailsState) -> Unit,
    onNavigateToUpiPayment: (PayeeDetailsState) -> Unit,
    onNavigateToFineractPayment: (PayeeDetailsState) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PayeeDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            PayeeDetailsEvent.NavigateBack -> onBackClick.invoke()
            is PayeeDetailsEvent.NavigateToUpiPayment -> onNavigateToPaymentProcessing.invoke(event.state)
            is PayeeDetailsEvent.NavigateToFineractPayment -> onNavigateToPaymentProcessing.invoke(event.state)
        }
    }

    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                MifosTopBar(
                    topBarTitle = stringResource(Res.string.feature_send_money_payee_details_title),
                    backPress = {
                        viewModel.trySendAction(PayeeDetailsAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KptTheme.spacing.lg)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
                ) {
                    Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                    PayeeProfileSection(state)

                    Spacer(modifier = Modifier.height(KptTheme.spacing.xs))

                    PaymentDetailsSection(
                        state = state,
                        onAmountChange = { amount ->
                            viewModel.trySendAction(PayeeDetailsAction.UpdateInputAmount(amount))
//                            viewModel.trySendAction(PayeeDetailsAction.UpdateAmount(amount))
                        },
                        onNoteChange = { note ->
                            viewModel.trySendAction(PayeeDetailsAction.UpdateNote(note))
                        },
                        onNoteFieldFocused = {
                            viewModel.trySendAction(PayeeDetailsAction.NoteFieldFocused)
                        },
                        onAmountFieldFocused = {
                            viewModel.trySendAction(PayeeDetailsAction.AmountFieldFocused)
                        },
                    )

                    Spacer(modifier = Modifier.height(KptTheme.spacing.xl))
                }

                if (!state.showAccountSelectionSheet) {
                    if (state.selectedAccount != null) {
                        // Show selected account and pay button at bottom
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(KptTheme.spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                        ) {
                            SelectedAccountSection(
                                account = state.selectedAccount!!,
                                onChangeAccount = {
                                    viewModel.trySendAction(PayeeDetailsAction.ProceedToPayment)
                                },
                            )

                            ProceedButton(
                                state = state,
                                onProceedClick = {
                                    viewModel.trySendAction(PayeeDetailsAction.ConfirmPayment)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        ProceedButton(
                            state = state,
                            onProceedClick = {
                                viewModel.trySendAction(PayeeDetailsAction.ProceedToPayment)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    end = KptTheme.spacing.lg,
                                    bottom = KptTheme.spacing.lg,
                                ),
                        )
                    }
                }
            }
        }

        if (state.showAccountSelectionSheet) {
            AccountSelectionBottomSheet(
                state = state,
                onAccountSelected = { account ->
                    viewModel.trySendAction(PayeeDetailsAction.SelectAccount(account))
                },
                onDismiss = {
                    viewModel.trySendAction(PayeeDetailsAction.DismissAccountSelection)
                },
                onConfirmPayment = {
                    viewModel.trySendAction(PayeeDetailsAction.ConfirmPayment)
                },
            )
        }
    }
}

@Composable
private fun PayeeProfileSection(
    state: PayeeDetailsState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.md),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = KptTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.payeeName.isNotEmpty() && state.payeeName != "UNKNOWN") {
                    val firstLetter = state.payeeName
                        .replace("%20", " ")
                        .trim()
                        .firstOrNull()
                        ?.uppercase()

                    if (firstLetter != null) {
                        Text(
                            text = firstLetter,
                            style = KptTheme.typography.headlineLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = KptTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Icon(
                            imageVector = MifosIcons.Person,
                            contentDescription = stringResource(Res.string.feature_send_money_payee_profile),
                            modifier = Modifier.size(40.dp),
                            tint = KptTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                } else {
                    Icon(
                        imageVector = MifosIcons.Person,
                        contentDescription = "Payee Profile",
                        modifier = Modifier.size(40.dp),
                        tint = KptTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            if (state.payeeName.isNotEmpty() && state.payeeName != "UNKNOWN") {
                val decodedName = state.payeeName
                    .replace("%20", " ")
                    .trim()

                Text(
                    text = stringResource(Res.string.feature_send_money_paying, decodedName.uppercase()),
                    style = KptTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = KptTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }

            val contactInfo = if (state.isUpiCode) {
                stringResource(Res.string.feature_send_money_upi_id, state.upiId)
            } else {
                state.phoneNumber
            }

            if (contactInfo.isNotEmpty()) {
                Text(
                    text = contactInfo,
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDetailsSection(
    state: PayeeDetailsState,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onNoteFieldFocused: () -> Unit,
    onAmountFieldFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
    ) {
        ExpandableAmountInput(
            value = state.formattedAmount,
            onValueChange = onAmountChange,
            enabled = state.isAmountEditable,
            modifier = Modifier.wrapContentWidth(),
            onFieldFocused = onAmountFieldFocused,
        )

        AnimatedVisibility(
            visible = state.showMaxAmountMessage || state.showMinAmountMessage,
//            visible = state.showMaxAmountMessage,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
        ) {
            val isVisible = state.showMaxAmountMessage || state.showMinAmountMessage
            val vibrationOffset by animateFloatAsState(
                targetValue = if (state.showMaxAmountMessage) 1f else 0f,
                animationSpec = repeatable(
                    iterations = 3,
                    animation = tween(100, delayMillis = 0),
                ),
                label = "vibration",
            )

            Text(
                text = when {
                    state.showMaxAmountMessage -> stringResource(Res.string.feature_send_money_amount_exceeds_limit)
                    state.showMinAmountMessage -> stringResource(Res.string.feature_send_money_amount_below_minimum)
                    else -> ""
                },
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = KptTheme.spacing.xs)
                    .graphicsLayer {
                        translationX = if (isVisible) {
                            (vibrationOffset * 10f * (if (vibrationOffset % 2 == 0f) 1f else -1f))
                        } else {
                            0f
                        }
                    },
            )
        }

        ExpandableNoteInput(
            value = state.note,
            onValueChange = onNoteChange,
            onFieldFocused = onNoteFieldFocused,
            modifier = Modifier.wrapContentWidth(),
        )
    }
}

// TODO improve amount validation and UI/UX
@Composable
private fun ExpandableAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onFieldFocused: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val displayValue = value.ifEmpty { "0" }

    /**
     * Calculate width based on the display value
     * When showing "0" (single digit), use minimal width
     * When user enters decimal or additional digits, expand dynamically
     * Maximum amount is ₹5,00,000 (6 digits + decimal + up to 2 decimal places = max 9 characters)
     */
    val textFieldWidth = when {
        displayValue == "0" -> 24.dp
        displayValue.length == 2 -> 32.dp
        displayValue.length == 3 -> 48.dp
        displayValue.length == 4 -> 64.dp
        displayValue.length == 5 -> 80.dp
        displayValue.length == 6 -> 96.dp
        displayValue.length == 7 -> 112.dp
        displayValue.length == 8 -> 128.dp
        displayValue.length == 9 -> 144.dp
        else -> 144.dp // Maximum width for ₹5,00,000.00
    }

    LaunchedEffect(enabled) {
        if (enabled) {
            focusRequester.requestFocus()
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(KptTheme.spacing.sm))
                .background(
                    color = KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(KptTheme.spacing.sm),
                )
                .padding(
                    horizontal = KptTheme.spacing.md,
                    vertical = KptTheme.spacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = MifosIcons.CurrencyRupee,
                contentDescription = stringResource(Res.string.feature_send_money_rupee_icon),
                tint = KptTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

            BasicTextField(
                value = displayValue,
                onValueChange = { newValue ->
                    val cleanValue = newValue.replace(",", "")
                    if (cleanValue.isEmpty() || cleanValue.toDoubleOrNull() != null) {
                        val amount = cleanValue.toDoubleOrNull() ?: 0.0

                        /**
                         * Allow the input to be processed by ViewModel for error handling
                         * The ViewModel will show error message briefly for invalid amounts
                         */
                        onValueChange(cleanValue)
                    }
                },
                enabled = enabled,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .width(textFieldWidth)
                    .focusRequester(focusRequester),
                singleLine = true,
            )
        }
    }
}

// TODO improve add note UI/UX
@Composable
private fun ExpandableNoteInput(
    value: String,
    onValueChange: (String) -> Unit,
    onFieldFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(KptTheme.spacing.sm))
                .background(
                    color = KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(KptTheme.spacing.sm),
                )
                .padding(
                    horizontal = KptTheme.spacing.md,
                    vertical = KptTheme.spacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.length <= 50) {
                        onValueChange(newValue)
                    }
                },
                enabled = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (value.isEmpty()) KptTheme.colorScheme.onSurfaceVariant else KptTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .width(
                        when {
                            value.length <= 7 -> 7 * 12.dp
                            value.length <= 28 -> (value.length + 1) * 12.dp
                            else -> 28 * 12.dp
                        },
                    )
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !isFocused) {
                            isFocused = true
                            onFieldFocused()
                        }
                    },
                singleLine = value.length <= 28,
                maxLines = if (value.length > 28) 2 else 1,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Add note",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            ),
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}

// TODO improve UI/UX of proceed button
@Composable
private fun ProceedButton(
    state: PayeeDetailsState,
    onProceedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isAmountValid = if (state.isUpiCode) {
        state.amount.isNotEmpty() &&
            AmountUtils.isValidPaise(state.amount) &&
            AmountUtils.paiseToRupees(state.amount).toDoubleOrNull()?.let { it >= 0 } == true &&
            !state.isAmountExceedingMax &&
            !state.isAmountBelowMin
    } else {
        state.amount.isNotEmpty() &&
            AmountUtils.isValidPaise(state.amount) &&
            AmountUtils.paiseToRupees(state.amount).toDoubleOrNull()?.let { it >= 1 } == true &&
            !state.isAmountExceedingMax
    }
    val isContactValid = state.upiId.isNotEmpty() || state.phoneNumber.isNotEmpty()
    val isAmountPrefilled = !state.isAmountEditable
    val showCheckMark = isAmountValid && isContactValid && (isAmountPrefilled || state.hasNoteFieldBeenFocused)
    val hasSelectedAccount = state.selectedAccount != null

    val isButtonEnabled = if (hasSelectedAccount) {
        isAmountValid && isContactValid
    } else {
        isAmountValid && isContactValid && state.amount.isNotEmpty()
    }

    Button(
        onClick = {
            focusManager.clearFocus()
            onProceedClick()
        },
        enabled = isButtonEnabled,
        modifier = if (hasSelectedAccount) {
            modifier
                .fillMaxWidth()
                .height(56.dp)
        } else {
            modifier.size(56.dp)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isButtonEnabled) {
                KptTheme.colorScheme.primary
            } else {
                KptTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isButtonEnabled) {
                KptTheme.colorScheme.onPrimary
            } else {
                KptTheme.colorScheme.onSurfaceVariant
            },
        ),
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        contentPadding = if (hasSelectedAccount) {
            PaddingValues(horizontal = KptTheme.spacing.lg)
        } else {
            PaddingValues(0.dp)
        },
    ) {
        if (hasSelectedAccount) {
            Text(
                text = stringResource(Res.string.feature_send_money_pay_amount, AmountUtils.formatPaiseForUI(state.amount)),
                style = KptTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        } else {
            Icon(
                imageVector = when {
                    showCheckMark -> MifosIcons.CheckRounded
                    else -> MifosIcons.ArrowForward
                },
                contentDescription = when {
                    showCheckMark -> "Proceed"
                    else -> "Next"
                },
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

// TODO improve bottomsheet UI/UX
@Composable
private fun AccountSelectionBottomSheet(
    state: PayeeDetailsState,
    onAccountSelected: (BankAccount) -> Unit,
    onDismiss: () -> Unit,
    onConfirmPayment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dummyAccounts = listOf(
        BankAccount(
            id = "1",
            bankName = "State Bank of India",
            accountNumber = "****1234",
            isDefault = true,
            accountType = "",
        ),
        BankAccount(
            id = "2",
            bankName = "HDFC Bank",
            accountNumber = "****5678",
            isDefault = false,
            accountType = "",
        ),
    )

    MifosBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(KptTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.feature_send_money_choose_account),
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    color = KptTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    dummyAccounts.forEach { account ->
                        AccountItem(
                            account = account,
                            isSelected = state.selectedAccount?.id == account.id,
                            onAccountClick = { onAccountSelected(account) },
                        )
                    }

                    AddBankAccountItem()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.md),
            ) {
                val focusManager = LocalFocusManager.current
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onConfirmPayment()
                    },
                    enabled = state.selectedAccount != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedAccount != null) {
                            KptTheme.colorScheme.primary
                        } else {
                            KptTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (state.selectedAccount != null) {
                            KptTheme.colorScheme.onPrimary
                        } else {
                            KptTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                    shape = RoundedCornerShape(KptTheme.spacing.sm),
                    contentPadding = PaddingValues(horizontal = KptTheme.spacing.lg),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_money_pay_amount, AmountUtils.formatPaiseForUI(state.amount)),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountItem(
    account: BankAccount,
    isSelected: Boolean,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAccountClick() },
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Bank,
                contentDescription = stringResource(Res.string.feature_send_money_bank_icon),
                modifier = Modifier.size(32.dp),
                tint = KptTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = account.bankName,
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = account.accountNumber,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                // TODO implement check now for balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_money_balance),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(Res.string.feature_send_money_check_now),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.primary,
                        modifier = Modifier.clickable { },
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = MifosIcons.CheckCircle,
                    contentDescription = stringResource(Res.string.feature_send_money_selected),
                    modifier = Modifier.size(24.dp),
                    tint = KptTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun SelectedAccountSection(
    account: BankAccount,
    onChangeAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(KptTheme.spacing.md),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg)
                .clickable { onChangeAccount() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Bank,
                contentDescription = stringResource(Res.string.feature_send_money_bank_icon),
                modifier = Modifier.size(32.dp),
                tint = KptTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = account.bankName,
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = account.accountNumber,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_money_balance),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(Res.string.feature_send_money_check_now),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.primary,
                        modifier = Modifier.clickable { },
                    )
                }
            }

            Icon(
                imageVector = MifosIcons.KeyboardArrowDown,
                contentDescription = stringResource(Res.string.feature_send_money_change_account),
                modifier = Modifier.size(24.dp),
                tint = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddBankAccountItem(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
        shape = RoundedCornerShape(KptTheme.spacing.sm),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Add,
                contentDescription = stringResource(Res.string.feature_send_money_add_bank_account_desc),
                modifier = Modifier.size(24.dp),
                tint = KptTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.md))

            Text(
                text = stringResource(Res.string.feature_send_money_add_bank_account),
                style = KptTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.primary,
            )
        }
    }
}

@Preview
@Composable
fun PayeeDetailsScreenPreview() {
    PayeeDetailsScreen(
        onBackClick = {},
        onNavigateToPaymentProcessing = {
            it
        },
        modifier = Modifier,
        onNavigateToUpiPayment = {},
        onNavigateToFineractPayment = {},
        // TODO: Figure out how to instantiate 'PayeeDetailsViewModel'
        // viewModel = koinViewModel(),
    )
}

@Preview
@Composable
fun PayeeProfileSectionPreview() {
    val state = PayeeDetailsState(
        payeeName = "John Doe",
        upiId = "john.doe@upi",
        phoneNumber = "1234567890",
        amount = "100.00",
        note = "Test payment",
        isAmountEditable = true,
        isUpiCode = true,
        isLoading = false,
        showMaxAmountMessage = false,
        hasNoteFieldBeenFocused = false,
        showAccountSelectionSheet = false,
        selectedAccount = null,
    )
    PayeeProfileSection(state = state, modifier = Modifier)
}

@Preview
@Composable
fun PaymentDetailsSectionPreview() {
    val state = PayeeDetailsState(
        payeeName = "John Doe",
        upiId = "john.doe@upi",
        phoneNumber = "1234567890",
        amount = "100.00",
        note = "Test payment",
        isAmountEditable = true,
        isUpiCode = true,
        isLoading = false,
        showMaxAmountMessage = false,
        hasNoteFieldBeenFocused = false,
        showAccountSelectionSheet = false,
        selectedAccount = null,
    )
    PaymentDetailsSection(
        state = state,
        onAmountChange = {},
        onNoteChange = {},
        onNoteFieldFocused = {},
        onAmountFieldFocused = {},
        modifier = Modifier,
    )
}

@Preview
@Composable
fun ExpandableAmountInputPreview() {
    ExpandableAmountInput(
        value = "100.00",
        onValueChange = {},
        enabled = true,
        modifier = Modifier,
        onFieldFocused = {},
    )
}

@Preview
@Composable
fun ExpandableNoteInputPreview() {
    ExpandableNoteInput(
        value = "Test note",
        onValueChange = {},
        onFieldFocused = {},
        modifier = Modifier,
    )
}

@Preview
@Composable
fun ProceedButtonPreview() {
    val state = PayeeDetailsState(
        payeeName = "John Doe",
        upiId = "john.doe@upi",
        phoneNumber = "1234567890",
        amount = "100.00",
        note = "Test payment",
        isAmountEditable = true,
        isUpiCode = true,
        isLoading = false,
        showMaxAmountMessage = false,
        hasNoteFieldBeenFocused = false,
        showAccountSelectionSheet = false,
        selectedAccount = null,
    )
    ProceedButton(
        state = state,
        onProceedClick = {},
        modifier = Modifier,
    )
}

@Preview
@Composable
fun AccountSelectionBottomSheetPreview() {
    val state = PayeeDetailsState(
        payeeName = "John Doe",
        upiId = "john.doe@upi",
        phoneNumber = "1234567890",
        amount = "100.00",
        note = "Test payment",
        isAmountEditable = true,
        isUpiCode = true,
        isLoading = false,
        showMaxAmountMessage = false,
        hasNoteFieldBeenFocused = false,
        showAccountSelectionSheet = false,
        selectedAccount = null,
    )
    AccountSelectionBottomSheet(
        state = state,
        onAccountSelected = {},
        onDismiss = {},
        onConfirmPayment = {},
        modifier = Modifier,
    )
}

@Preview
@Composable
fun AccountItemPreview() {
    val account = BankAccount(
        id = "1",
        bankName = "State Bank of India",
        accountNumber = "****1234",
        isDefault = true,
        accountType = "",

    )
    AccountItem(
        account = account,
        isSelected = true,
        onAccountClick = {},
        modifier = Modifier,
    )
}

@Preview
@Composable
fun SelectedAccountSectionPreview() {
    val account = BankAccount(
        id = "1",
        bankName = "State Bank of India",
        accountNumber = "****1234",
        isDefault = true,
        accountType = "",
    )
    SelectedAccountSection(
        account = account,
        onChangeAccount = {},
        modifier = Modifier,
    )
}

@Preview
@Composable
fun AddBankAccountItemPreview() {
    AddBankAccountItem(modifier = Modifier)
}
