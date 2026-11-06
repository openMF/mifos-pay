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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosTextUserImage
@Composable
fun MifosUserImage(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap? = null,
    username: String? = null,
) {
    if (bitmap == null) {
        MifosTextUserImage(
            text = username?.firstOrNull()?.toString() ?: "J",
            modifier = modifier,
        )
    } else {
        Image(
            modifier = modifier
                .clip(CircleShape)
                .background(KptTheme.colorScheme.primary),
            bitmap = bitmap,
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
        )
    }
}
