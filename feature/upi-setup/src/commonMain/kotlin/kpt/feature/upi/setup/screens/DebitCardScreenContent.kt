/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.upi.setup.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.ui.ExpiryDateInput
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun DebitCardScreenContent(
    modifier: Modifier = Modifier,
    onDone: (String, String, String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var cardNumber by rememberSaveable { mutableStateOf("") }
    var expiryDate by rememberSaveable { mutableStateOf("") }

    Column(modifier) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            label = {
                Text(
                    text = "Debit Card Number",
                    style = TextStyle(color = KptTheme.colorScheme.onSurface),
                )
            },
            value = cardNumber,
            onValueChange = {
                cardNumber = it
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.moveFocus(FocusDirection.Down)
                },
            ),
            visualTransformation = ::formatCardNumber,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KptTheme.colorScheme.outline,
                unfocusedBorderColor = KptTheme.colorScheme.outlineVariant,
                cursorColor = KptTheme.colorScheme.onSurface,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExpiryDateInput(
            date = expiryDate,
            onDateChange = { expiryDate = it },
            onDone = {
                onDone(cardNumber, "month, year", expiryDate)
            },
        )
    }
}

@Suppress("ReturnCount")
private fun formatCardNumber(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
    var out = ""
    for (i in trimmed.indices) {
        out += trimmed[i]
        if (i % 4 == 3 && i != 15) {
            out += "-"
        }
    }
    val creditCardOffsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) {
                return offset
            }
            if (offset <= 7) {
                return offset + 1
            }
            if (offset <= 11) {
                return offset + 2
            }
            if (offset <= 16) {
                return offset + 3
            }
            return 19
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) {
                return offset
            }
            if (offset <= 9) {
                return offset - 1
            }
            if (offset <= 14) {
                return offset - 2
            }
            if (offset <= 19) {
                return offset - 3
            }
            return 16
        }
    }
    return TransformedText(AnnotatedString(out), creditCardOffsetTranslator)
}

@Preview
@Composable
private fun DebitCardScreenContentsPreview() {
    ForkTheme {
        DebitCardScreenContent(
            onDone = { _, _, _ -> },
        )
    }
}
