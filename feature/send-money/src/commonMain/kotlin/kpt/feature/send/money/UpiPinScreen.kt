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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.Serializable
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.fork.ForkTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/**
 * Data class representing payment details for UPI transactions
 * @property payeeName Name of the payee/recipient
 * @property bankName Name of the bank
 * @property accountNumber Account number of the payee
 * @property amount Payment amount
 * @property refId Reference ID for the transaction
 */
@Serializable
data class PaymentDetails(
    val payeeName: String,
    val bankName: String,
    val accountNumber: String,
    val amount: Double,
    val refId: String,
)

/** Screen where the user enters their UPI PIN to authorize a payment. */
@Composable
fun UpiPinScreen(
    onBackClick: () -> Unit,
    onUpiPinEntered: (String, String, String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpiPinViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val isDropdownExpanded = rememberSaveable { mutableStateOf(false) }

    ForkScaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = androidx.compose.ui.graphics.Color.White),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
                ) {
                    UpiPinHeader(
                        bankAccountDetails = state.paymentDetails,
                        modifier = Modifier.padding(
                            top = KptTheme.spacing.xl,
                            start = KptTheme.spacing.lg,
                            end = KptTheme.spacing.lg,
                        ),
                    )

                    PayeeTransactionDetails(
                        payeeName = state.paymentDetails.payeeName,
                        amount = state.paymentDetails.amount,
                        refId = state.paymentDetails.refId,
                        accountNumber = state.paymentDetails.accountNumber,
                        isDropdownExpanded = isDropdownExpanded,
                        modifier = Modifier.padding(horizontal = KptTheme.spacing.lg),
                    )

                    Spacer(modifier = Modifier.height(KptTheme.spacing.xl))

                    UpiPinContent(
                        bankAccountDetails = state.paymentDetails,
                        onUpiPinEntered = { pin ->
                            viewModel.trySendAction(UpiPinAction.UpiPinEntered(pin))
                            onUpiPinEntered(
                                state.paymentDetails.payeeName,
                                state.amount.toString(),
                                pin,
                                state.isUpiCode,
                            )
                        },
                        modifier = Modifier.padding(horizontal = KptTheme.spacing.lg),
                    )
                }

                if (isDropdownExpanded.value) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .offset(y = 200.dp)
                                .padding(horizontal = KptTheme.spacing.lg),
                            colors = CardDefaults.cardColors(
                                containerColor = androidx.compose.ui.graphics.Color.White,
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = KptTheme.elevation.level3,
                            ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(KptTheme.spacing.md),
                                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "REF ID:",
                                        style = KptTheme.typography.bodyMedium,
                                        color = androidx.compose.ui.graphics.Color.Gray,
                                        modifier = Modifier.width(80.dp),
                                    )
                                    Text(
                                        text = state.paymentDetails.refId,
                                        style = KptTheme.typography.bodyMedium,
                                        color = androidx.compose.ui.graphics.Color.Black,
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "ACCOUNT:",
                                        style = KptTheme.typography.bodyMedium,
                                        color = androidx.compose.ui.graphics.Color.Gray,
                                        modifier = Modifier.width(80.dp),
                                    )
                                    Text(
                                        text = maskAccountNumberForDropdown(state.paymentDetails.accountNumber),
                                        style = KptTheme.typography.bodyMedium,
                                        color = androidx.compose.ui.graphics.Color.Black,
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .offset(y = 200.dp + 100.dp)
                                .height(1000.dp)
                                .background(
                                    color = KptTheme.colorScheme.surface.copy(alpha = 0.7f),
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpiPinHeader(
    bankAccountDetails: PaymentDetails?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = bankAccountDetails?.bankName ?: "Bank Name",
                style = KptTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.Black,
            )

            Text(
                text = maskAccountNumber(bankAccountDetails?.accountNumber ?: "1234567890123456"),
                style = KptTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.Gray,
            )
        }

        Text(
            text = "UPI",
            style = KptTheme.typography.titleLarge,
            color = androidx.compose.ui.graphics.Color.Black,
        )
    }
}

@Composable
private fun UpiPinContent(
    bankAccountDetails: PaymentDetails?,
    onUpiPinEntered: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val upiPin = rememberSaveable { mutableStateOf("") }
    val errorMessage = rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val showPin = rememberSaveable { mutableStateOf(false) }
    val previousPinLength = rememberSaveable { mutableStateOf(0) }

    val pinLength = getPinLengthForBank(bankAccountDetails?.bankName)
    val expectedPin = getExpectedPinForBank(bankAccountDetails?.bankName)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(errorMessage.value) {
        if (errorMessage.value.isNotEmpty()) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(upiPin.value) {
        // Only show PIN briefly when adding characters, not when deleting
        if (upiPin.value.length > previousPinLength.value) {
            showPin.value = true
            kotlinx.coroutines.delay(200) // Show PIN for 200ms then mask
            showPin.value = false
        }
        previousPinLength.value = upiPin.value.length
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = KptTheme.elevation.level1,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
        ) {
            Text(
                text = "Enter UPI PIN",
                style = KptTheme.typography.headlineMedium,
                color = androidx.compose.ui.graphics.Color.Black,
            )

            if (errorMessage.value.isNotEmpty()) {
                Text(
                    text = errorMessage.value,
                    style = KptTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.Red,
                )
            }

            BasicTextField(
                value = upiPin.value,
                onValueChange = {
                    if (it.length <= pinLength) {
                        upiPin.value = it
                        errorMessage.value = ""
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (upiPin.value.length == pinLength) {
                            errorMessage.value = ""

                            if (upiPin.value == expectedPin) {
                                onUpiPinEntered(upiPin.value)
                            } else {
                                errorMessage.value = "Incorrect UPI PIN. Please try again."
                                upiPin.value = ""
                            }
                        }
                    },
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        repeat(pinLength) { index ->
                            UpiPinCharView(
                                index = index,
                                text = upiPin.value,
                                showPin = showPin.value,
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )

            Text(
                text = "UPI PIN will keep your account secure from unauthorized access. Do not share this PIN with anyone",
                style = KptTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun UpiPinCharView(
    index: Int,
    text: String,
    showPin: Boolean = false,
) {
    val isFocused = text.length == index

    val char = when {
        index >= text.length -> "—"
        else -> if (showPin && index == text.length - 1) text[index].toString() else "•"
    }

    Text(
        modifier = Modifier
            .width(56.dp)
            .wrapContentHeight(align = Alignment.CenterVertically),
        text = char,
        style = KptTheme.typography.headlineLarge.copy(
            fontSize = 40.sp,
        ),
        color = if (isFocused) {
            androidx.compose.ui.graphics.Color.Blue
        } else {
            androidx.compose.ui.graphics.Color.Black
        },
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PayeeTransactionDetails(
    payeeName: String,
    amount: Double,
    refId: String,
    accountNumber: String,
    isDropdownExpanded: androidx.compose.runtime.MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = KptTheme.elevation.level1,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f),
                )
                .padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "To:",
                    style = KptTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.Gray,
                )
                Text(
                    text = payeeName.uppercase(),
                    style = KptTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.Black,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "Sending:",
                    style = KptTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.Gray,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = AmountUtils.formatRupeesForUI(amount.toString()),
                        style = KptTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.Black,
                    )
                    IconButton(
                        onClick = { isDropdownExpanded.value = !isDropdownExpanded.value },
                    ) {
                        Icon(
                            imageVector = if (isDropdownExpanded.value) {
                                ForkIcons.DropUp
                            } else {
                                ForkIcons.DropDown
                            },
                            contentDescription = if (isDropdownExpanded.value) {
                                "Hide transaction details"
                            } else {
                                "Show transaction details"
                            },
                            tint = androidx.compose.ui.graphics.Color.Black,
                        )
                    }
                }
            }
        }
    }
}

private fun maskAccountNumber(accountNumber: String): String {
    return if (accountNumber.length >= 8) {
        "XXXX${accountNumber.takeLast(4)}"
    } else {
        accountNumber
    }
}

private fun maskAccountNumberForDropdown(accountNumber: String): String {
    return if (accountNumber.length >= 8) {
        "XXXXXX${accountNumber.takeLast(4)}"
    } else {
        accountNumber
    }
}

private fun getPinLengthForBank(bankName: String?): Int {
    return when (bankName) {
        "Sample Bank" -> 4
        "Test Bank" -> 6
        else -> 4
    }
}

private fun getExpectedPinForBank(bankName: String?): String {
    return when (bankName) {
        "Sample Bank" -> "1234"
        "Test Bank" -> "123456"
        else -> "1234"
    }
}

@Preview
@Composable
private fun UpiPinScreenPreview() {
    ForkTheme {
        UpiPinScreen(
            onBackClick = { },
            onUpiPinEntered = { payeeName, amount, pin, isUpiCode ->
                // Preview callback
            },
        )
    }
}
