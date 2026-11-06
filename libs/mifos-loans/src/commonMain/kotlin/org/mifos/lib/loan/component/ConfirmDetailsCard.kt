/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosCard
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
/**
 * Displays a summary card containing a list of labeled details, typically used for confirmation screens.
 *
 * @param keyValuePairs A map where the key is the label resource and the value is the detail text to display.
 */
@Composable
fun ConfirmDetailsCard(
    keyValuePairs: Map<StringResource, String>,
    modifier: Modifier = Modifier,
) {
    MifosCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = KptTheme.colorScheme.outlineVariant,
                shape = KptTheme.shapes.medium,
            ),
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(KptTheme.spacing.md)) {
            keyValuePairs.forEach { (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = KptTheme.spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${stringResource(key)} :",
                        style = KptTheme.typography.labelMedium,
                    )
                    Text(
                        text = value,
                        style = KptTheme.typography.labelMedium,
                        textAlign = TextAlign.Right,
                    )
                }
            }
        }
    }
}
