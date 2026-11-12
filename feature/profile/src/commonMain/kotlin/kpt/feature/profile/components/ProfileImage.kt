/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile_profile_image_description
import kpt.feature.profile.generated.resources.placeholder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Displays the client profile image decoded from a base64 string. */
@OptIn(ExperimentalEncodingApi::class)
@Composable
fun ProfileImage(
    modifier: Modifier = Modifier,
    bitmap: String? = null,
) {
    val context = LocalPlatformContext.current

    Box(
        modifier = modifier
            .size(150.dp),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap?.let {
            try {
                Base64.decode(it)
            } catch (e: Exception) {
                byteArrayOf()
            }
        }

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(image)
                .build(),
            error = painterResource(Res.drawable.placeholder),
            fallback = painterResource(Res.drawable.placeholder),
            imageLoader = ImageLoader(context),
            contentDescription = "Profile Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(4.dp, KptTheme.colorScheme.primary, CircleShape),
        )
    }
}

/** Profile image that can be tapped to pick a new picture. */
@OptIn(ExperimentalEncodingApi::class)
@Composable
fun EditableProfileImage(
    modifier: Modifier = Modifier,
    profileImage: ByteArray? = null,
    onPickImage: () -> Unit,
) {
    val context = LocalPlatformContext.current

    Box(
        modifier = modifier
            .size(150.dp),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = profileImage,
            error = painterResource(Res.drawable.placeholder),
            fallback = painterResource(Res.drawable.placeholder),
            imageLoader = ImageLoader(context),
            contentDescription = stringResource(Res.string.feature_profile_profile_image_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(4.dp, KptTheme.colorScheme.primary, CircleShape),
        )

        IconButton(
            onClick = onPickImage,
            modifier = Modifier
                .offset(y = 12.dp)
                .size(36.dp)
                .clip(CircleShape)
                .align(Alignment.BottomCenter),
            colors = IconButtonDefaults.iconButtonColors(KptTheme.colorScheme.surface),
        ) {
            Icon(
                imageVector = ForkIcons.Edit2,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp),
            )
        }
    }
}
