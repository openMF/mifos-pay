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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.ui.VerifyStepHeader
import kpt.feature.upi.setup.generated.resources.Res
import kpt.feature.upi.setup.generated.resources.feature_upi_setup_enter_upi_pin
import kpt.feature.upi.setup.generated.resources.feature_upi_setup_invalid_upi_pin
import kpt.feature.upi.setup.generated.resources.feature_upi_setup_reenter_upi
import kpt.feature.upi.setup.generated.resources.feature_upi_setup_upi_pin_setup
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun UpiPinScreen(
    correctlySettingUpi: (String) -> Unit,
    modifier: Modifier = Modifier,
    verificationStatus: Boolean = false,
    contentVisibility: Boolean = false,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = KptTheme.spacing.md,
                bottom = KptTheme.spacing.md,
                start = KptTheme.spacing.sm,
                end = KptTheme.spacing.sm,
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = KptTheme.elevation.level1,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VerifyStepHeader(
                stringResource(Res.string.feature_upi_setup_upi_pin_setup),
                verificationStatus,
            )
            if (contentVisibility) UpiPinScreenContent(correctlySettingUpi)
        }
    }
}

@Composable
private fun UpiPinScreenContent(
    correctlySettingUpi: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps1 = rememberSaveable { mutableIntStateOf(0) }
    val upiPin1 = rememberSaveable { mutableStateOf("") }
    val upiPin2 = rememberSaveable { mutableStateOf("") }
    val upiPinTobeMatched1 = rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    Text(
        modifier = modifier,
        text = if (steps1.intValue == 0) {
            stringResource(Res.string.feature_upi_setup_enter_upi_pin)
        } else {
            stringResource(Res.string.feature_upi_setup_reenter_upi)
        },
        color = KptTheme.colorScheme.onSurface,
        fontSize = 18.sp,
        style = KptTheme.typography.headlineMedium,
    )

    if (steps1.intValue == 0) {
        BasicTextField(
            value = upiPin1.value,
            onValueChange = {
                upiPin1.value = it

                if (upiPin1.value.length == 4) {
                    steps1.intValue = 1
                    upiPinTobeMatched1.value = upiPin1.value
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (upiPin1.value.length == 4) {
                        steps1.intValue = 1
                        upiPinTobeMatched1.value = upiPin1.value
                    }
                },
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(4) { index ->
                        UpiPinCharView(
                            index = index,
                            text = upiPin1.value,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
        )
    } else {
        BasicTextField(
            value = upiPin2.value,
            onValueChange = {
                upiPin2.value = it
                if (upiPin2.value.length == 4) {
                    if (upiPin1.value == upiPin2.value) {
                        correctlySettingUpi(upiPin2.value)
                    }
                }
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (upiPin2.value.length == 4) {
                        if (upiPin1.value == upiPin2.value) {
                            correctlySettingUpi(upiPin2.value)
                        }
                    } else {
                        showSnackbar(
                            snackbarHostState,
                            Res.string.feature_upi_setup_invalid_upi_pin.toString(),
                        )
                    }
                },
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(4) { index ->
                        UpiPinCharView(
                            index = index,
                            text = upiPin2.value,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
        )
    }
}

/**
 * Shows [message] in the snackbar host on the main dispatcher.
 */
fun showSnackbar(snackbarHostState: SnackbarHostState, message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        snackbarHostState.showSnackbar(message)
    }
}

@Composable
private fun UpiPinCharView(
    index: Int,
    text: String,
) {
    val isFocused = text.length == index

    val char = when {
        index == text.length -> "_"
        index > text.length -> "_"
        else -> text[index].toString()
    }
    Text(
        modifier = Modifier
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

@Preview
@Composable
private fun UpiScreenPreview() {
    MifosTheme(darkTheme = true) {
        UpiPinScreen({}, verificationStatus = false, contentVisibility = true)
    }
}
