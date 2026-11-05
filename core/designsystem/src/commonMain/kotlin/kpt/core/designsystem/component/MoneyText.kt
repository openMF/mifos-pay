/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kpt.core.designsystem.theme.finance

/**
 * Currency text that picks its color from [MaterialTheme.finance] based on the amount's
 * sign (or a forced [MoneyTone]). Use everywhere a monetary value is rendered so the app
 * has a single visual grammar for money.
 *
 * Formatting is the caller's job — pass already-formatted strings (e.g. "₹12,500" or
 * "-$120.00"). The [amount] parameter is only used for sign-based tone selection when
 * [tone] == [MoneyTone.AutoFromSign].
 */
@Composable
fun MoneyText(
    text: String,
    modifier: Modifier = Modifier,
    amount: Double = 0.0,
    tone: MoneyTone = MoneyTone.AutoFromSign,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    weight: FontWeight = FontWeight.SemiBold,
) {
    val color = resolveMoneyColor(tone, amount)
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style.copy(fontWeight = weight),
    )
}

@Composable
private fun resolveMoneyColor(tone: MoneyTone, amount: Double): Color {
    val f = MaterialTheme.finance
    return when (tone) {
        MoneyTone.Positive -> f.moneyPositive
        MoneyTone.Negative -> f.moneyNegative
        MoneyTone.Neutral -> f.moneyNeutral
        MoneyTone.Inherit -> LocalContentColor.current
        MoneyTone.AutoFromSign -> when {
            amount > 0 -> f.moneyPositive
            amount < 0 -> f.moneyNegative
            else -> f.moneyNeutral
        }
    }
}
