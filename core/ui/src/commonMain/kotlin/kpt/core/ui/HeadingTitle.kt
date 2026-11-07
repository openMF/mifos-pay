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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Step heading with a verified/unverified status icon next to [text]. */
@Composable
fun VerifyStepHeader(
    text: String,
    isVerified: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = text,
            color = KptTheme.colorScheme.onSurface,
            fontSize = 17.sp,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(KptTheme.spacing.md)
                .weight(1f),
            textAlign = TextAlign.Start,
            style = TextStyle(fontWeight = FontWeight.Bold),
        )
        IconButton(
            onClick = { },
        ) {
            if (isVerified) {
                Icon(
                    imageVector = MifosIcons.Check,
                    contentDescription = null,
                    tint = if (isVerified) KptTheme.colorScheme.onSurface else KptTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/** Preview of [VerifyStepHeader] in the verified state. */
@Preview
@Composable
fun VerifyStepHeaderVerifiedPreview() {
    MifosTheme {
        VerifyStepHeader(text = "Debit card Details ", isVerified = true)
    }
}

/** Preview of [VerifyStepHeader] in the unverified state. */
@Preview
@Composable
fun VerifyStepHeaderUnverifiedPreview() {
    MifosTheme {
        VerifyStepHeader(text = "Enter OTP ", isVerified = false)
    }
}
