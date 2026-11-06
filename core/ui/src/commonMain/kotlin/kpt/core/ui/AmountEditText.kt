/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
/**
 * AmountEditText - Generic amount input component with validation
 *
 * @param value Current amount value as String (formatted display)
 * @param onValueChange Callback when amount changes, receives unformatted Double value
 * @param modifier Modifier for the component
 * @param currencyCode Currency code to display (e.g., "MXN")
 * @param availableBalance Available balance to display
 * @param maxAmount Maximum allowed amount (for validation)
 * @param errorMessage Error message to display if validation fails
 * @param onAmountValidation Callback for amount validation with error message
 * @param enabled Whether the input is enabled
 * @param isError Whether the field is in error state
 * @param backgroundColor Background color of the input box
 * @param errorBorderColor Border color when in error state
 * @param successBorderColor Border color when valid
 * @param borderWidth Border width
 * @param cornerRadius Corner radius of the input box
 * @param contentPadding Padding inside the input box
 */
@Composable
fun AmountEditText(
    value: String,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    currencyCode: String = "MXN",
    availableBalance: Double? = null,
    maxAmount: Double? = null,
    errorMessage: String? = null,
    onAmountValidation: ((Double, String?) -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    backgroundColor: Color = KptTheme.colorScheme.background,
    errorBorderColor: Color = KptTheme.colorScheme.error,
    successBorderColor: Color = Color(0xFFE0E0E0),
    borderWidth: Dp = 2.dp,
    cornerRadius: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    currencyTextStyle: TextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = KptTheme.colorScheme.primary,
    ),
    amountTextStyle: TextStyle = TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = KptTheme.colorScheme.primary,
    ),
    balanceTextStyle: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFF388E3C),
    ),
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Amount Input Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(cornerRadius),
                )
                .border(
                    width = borderWidth,
                    color = when {
                        isError -> errorBorderColor
                        isFocused -> MaterialTheme.colorScheme.primary
                        else -> successBorderColor
                    },
                    shape = RoundedCornerShape(cornerRadius),
                )
                .padding(contentPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Currency Symbol
                Text(
                    text = currencyCode,
                    style = currencyTextStyle,
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Divider
                VerticalDivider(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp),
                    thickness = 2.dp,
                    color = KptTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Amount Input
                BasicTextField(
                    value = value,
                    onValueChange = { newValue ->
                        val formattedValue = formatAmount(newValue)
                        val doubleValue = formattedValue.replace(",", "").toDoubleOrNull() ?: 0.0

                        // Validate amount
                        val validationError = validateAmount(doubleValue, maxAmount)
                        onAmountValidation?.invoke(doubleValue, validationError)

                        // Always call onValueChange with Double value
                        onValueChange(doubleValue)
                    },
                    textStyle = amountTextStyle,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                        },
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = amountTextStyle.copy(
                                    color = amountTextStyle.color.copy(alpha = 0.3f),
                                ),
                            )
                        }
                        innerTextField()
                    },
                )
            }
        }

        // Error Message Display
        if (isError && errorMessage != null) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(8.dp),
            ) {
                Text(
                    text = "⚠️",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }

        // Available Balance
        if (availableBalance != null && availableBalance > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Icon(
                    imageVector = MifosIcons.CheckCircle,
                    contentDescription = null,
                    tint = balanceTextStyle.color,
                    modifier = Modifier.size(16.dp),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Available Balance: $availableBalance $currencyCode",
                    style = balanceTextStyle,
                )
            }
        }
    }
}

/**
 * Validates the amount based on constraints
 * @return Error message if validation fails, null if valid
 */
private fun validateAmount(amount: Double, maxAmount: Double?): String? {
    return when {
        amount <= 0 -> "Amount must be greater than 0"
        maxAmount != null && amount > maxAmount -> "Amount exceeds available balance"
        else -> null
    }
}

/**
 * Formats the amount input with thousand separators
 */
private fun formatAmount(input: String): String {
    // Remove non-digit characters except decimal point
    val cleaned = input.replace(Regex("[^\\d.]"), "")

    // Split by decimal point
    val parts = cleaned.split(".")

    // Format integer part with thousand separators
    val integerPart = parts.getOrNull(0)?.let { part ->
        if (part.isNotEmpty()) {
            part.reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()
        } else {
            ""
        }
    } ?: ""

    // Combine with decimal part if exists (limit to 2 decimal places)
    return if (parts.size > 1) {
        val decimalPart = parts[1].take(2)
        "$integerPart.$decimalPart"
    } else if (cleaned.endsWith(".")) {
        "$integerPart."
    } else {
        integerPart
    }
}

@Preview
@Composable
private fun AmountEditTextPreview() {
    MifosTheme {
        var amount by remember { mutableStateOf("1234.50") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var isError by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            AmountEditText(
                value = amount,
                onValueChange = { doubleAmount ->
                    amount = doubleAmount.toString()
                },
                currencyCode = "MXN",
                availableBalance = 5000.0,
                maxAmount = 5000.0,
                errorMessage = errorMsg,
                onAmountValidation = { doubleAmount, error ->
                    errorMsg = error
                    isError = error != null
                },
                isError = isError,
            )
        }
    }
}

@Preview
@Composable
private fun AmountEditTextEmptyPreview() {
    MifosTheme {
        var amount by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var isError by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            AmountEditText(
                value = amount,
                onValueChange = { doubleAmount ->
                    amount = doubleAmount.toString()
                },
                currencyCode = "MXN",
                availableBalance = 5000.0,
                maxAmount = 5000.0,
                errorMessage = errorMsg,
                onAmountValidation = { doubleAmount, error ->
                    errorMsg = error
                    isError = error != null
                },
                isError = isError,
            )
        }
    }
}

@Preview
@Composable
private fun AmountEditTextErrorPreview() {
    MifosTheme {
        var amount by remember { mutableStateOf("6000.00") }
        var errorMsg by remember { mutableStateOf("Amount exceeds available balance") }
        var isError by remember { mutableStateOf(true) }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            AmountEditText(
                value = amount,
                onValueChange = { doubleAmount ->
                    amount = doubleAmount.toString()
                },
                currencyCode = "MXN",
                availableBalance = 5000.0,
                maxAmount = 5000.0,
                errorMessage = errorMsg,
                onAmountValidation = { doubleAmount, error ->
                    errorMsg = error ?: ""
                    isError = error != null
                },
                isError = isError,
            )
        }
    }
}
