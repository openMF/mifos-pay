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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kpt.core.designsystem.theme.spacing

/**
 * Big-and-bold currency presentation used at the top of dashboards and detail screens.
 * Layout: optional label (small, dimmed) → large amount → optional supporting metadata row.
 *
 * The amount color is locked to [LocalContentColor] so this works inside a [HeroCard]
 * (white on gradient) AND inside an [AppCard] (onSurface on tint) without per-call
 * overrides. Use [MoneyText] when you specifically want sign-based finance coloring.
 */
@Composable
fun AmountDisplay(
    amountText: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    supporting: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = LocalContentColor.current.copy(alpha = 0.75f),
            )
        }
        Text(
            text = amountText,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = LocalContentColor.current,
        )
        if (supporting != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                supporting()
            }
        }
    }
}
