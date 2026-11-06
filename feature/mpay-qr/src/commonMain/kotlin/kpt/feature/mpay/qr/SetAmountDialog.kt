/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosCustomDialog
import kpt.core.designsystem.component.MifosOutlinedButton
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.utils.fork.onClick
import kpt.core.model.utils.CurrencyCode
import kpt.core.ui.AvatarBox
import mifos_pay.feature.mpay_qr.generated.resources.Res
import mifos_pay.feature.mpay_qr.generated.resources.feature_request_money_cancel
import mifos_pay.feature.mpay_qr.generated.resources.feature_request_money_confirm
import mifos_pay.feature.mpay_qr.generated.resources.feature_request_money_enter_valid_amount
import mifos_pay.feature.mpay_qr.generated.resources.feature_request_money_set_amount
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun SetAmountDialog(
    currency: String,
    amount: String,
    currencyList: List<CurrencyCode>,
    modifier: Modifier = Modifier,
    onAction: (MpayQrAction) -> Unit,
) {
    MifosCustomDialog(
        onDismiss = {
            onAction(MpayQrAction.DismissDialog)
        },
        content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(KptTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_request_money_set_amount),
                        style = KptTheme.typography.titleMedium,
                    )

                    val validAmountError = stringResource(Res.string.feature_request_money_enter_valid_amount)
                    val amountValidator by remember(amount, validAmountError) {
                        derivedStateOf {
                            when {
                                amount.trim() == "" -> null

                                amount.trim().any { it.isLetter() } -> validAmountError

                                amount.trim().toDoubleOrNull() == null -> validAmountError

                                amount.trim().toDouble() <= 0.0 -> validAmountError

                                else -> null
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                    ) {
                        Text(
                            textAlign = TextAlign.Start,
                            text = currency,
                            style = KptTheme.typography.bodyMedium,
                        )

                        MifosTextField(
                            value = amount,
                            label = stringResource(Res.string.feature_request_money_set_amount),
                            onValueChange = {
                                onAction(MpayQrAction.AmountChanged(it))
                            },
                            isError = amountValidator != null,
                            errorText = amountValidator,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                            ),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        MifosOutlinedButton(
                            onClick = {
                                onAction(MpayQrAction.DismissDialog)
                            },
                        ) {
                            Text(text = stringResource(Res.string.feature_request_money_cancel))
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        MifosButton(
                            onClick = {
                                if (amountValidator == null && amount.isNotBlank()) {
                                    onAction(MpayQrAction.ConfirmSetAmount)
                                }
                            },
                            enabled = amountValidator == null && amount.isNotBlank(),
                        ) {
                            Text(text = stringResource(Res.string.feature_request_money_confirm))
                        }
                    }
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun CurrencyDropdownItem(
    currency: CurrencyCode,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = currency.countryName,
            )
        },
        leadingContent = {
            AvatarBox(
                name = currency.currencySymbol,
            )
        },
        trailingContent = {
            Text(
                text = currency.currencyCode,
                color = KptTheme.colorScheme.onSurface,
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .onClick { onClick(currency.currencyCode) },
    )
}

@Preview
@Composable
private fun SetAmountDialogPreview() {
    SetAmountDialog(
        amount = "",
        currency = "",
        currencyList = emptyList(),
        onAction = { },
    )
}
