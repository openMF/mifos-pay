/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Icon-over-label action button used on the scanner screen. */
@Composable
fun QrActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier.semantics {
            role = Role.Button
            contentDescription = label
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (enabled) 0.2f else 0.1f))
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = label,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
        )
    }
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun QrActionButtonEnabledPreview() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.background(Color.Black).size(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        QrActionButton(
            icon = kpt.core.designsystem.icon.MifosIcons.FlashOn,
            label = "Torch",
            onClick = {},
            enabled = true,
        )
    }
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun QrActionButtonDisabledPreview() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.background(Color.Black).size(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        QrActionButton(
            icon = kpt.core.designsystem.icon.MifosIcons.FlashOff,
            label = "Torch",
            onClick = {},
            enabled = false,
        )
    }
}
