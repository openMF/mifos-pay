/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pure digit collector — it does NOT decide correctness itself. [onOtpEntered] fires once
 * [otpCount] digits are typed (or on IME-done); the caller verifies the code against the
 * real backend (e.g. `TwoFactorAuthRepository.validateToken`) and reports the outcome back
 * via [isError]/[errorMessage]. A real OTP's correct value is never known client-side, so a
 * local string comparison (the previous `realOtp` param) can never be correct.
 */
@Composable
fun OtpTextField(
    onOtpEntered: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpCount: Int = 4,
    isError: Boolean = false,
    errorMessage: String = "Invalid OTP",
) {
    var otpText by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicTextField(
            modifier = Modifier,
            value = TextFieldValue(otpText, selection = TextRange(otpText.length)),
            onValueChange = {
                otpText = it.text
                if (otpText.length == otpCount) {
                    onOtpEntered(otpText)
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (otpText.length == otpCount) {
                        onOtpEntered(otpText)
                    }
                },
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(otpCount) { index ->
                        CharView(
                            index = index,
                            text = otpText,
                        )
                        Spacer(modifier = Modifier.width(KptTheme.spacing.sm))
                    }
                }
            },
        )
        if (isError) {
            Text(
                text = errorMessage,
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = KptTheme.spacing.sm),
            )
        }
    }
}

/** Single box showing one OTP character at [index] of [text]. */
@Composable
fun CharView(
    index: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    val isFocused = text.length == index
    val char = when {
        index == text.length -> "_"
        index > text.length -> "_"
        else -> text[index].toString()
    }
    Text(
        modifier = modifier
            .width(40.dp)
            .wrapContentHeight(align = Alignment.CenterVertically),
        text = char,
        style = KptTheme.typography.headlineSmall,
        color = if (isFocused) {
            KptTheme.colorScheme.outline
        } else {
            KptTheme.colorScheme.outlineVariant
        },
        textAlign = TextAlign.Center,
    )
}

/** Preview of the OTP text field. */
@Preview
@Composable
fun PreviewOtpTextField() {
    OtpTextField(
        onOtpEntered = {},
        otpCount = 4,
    )
}
