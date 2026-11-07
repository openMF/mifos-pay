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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.model.client.Client
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile_email
import kpt.feature.profile.generated.resources.feature_profile_mobile
import kpt.feature.profile.generated.resources.feature_profile_username
import kpt.feature.profile.generated.resources.feature_profile_vpa
import org.jetbrains.compose.resources.stringResource
/** Card listing the client profile details such as username, email and mobile number. */
@Composable
fun ProfileDetailsCard(
    client: Client,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(),
        shape = KptTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primaryContainer,
            contentColor = KptTheme.colorScheme.onPrimary,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            ProfileItem(
                label = stringResource(Res.string.feature_profile_username),
                value = client.displayName,
            )
            ProfileItem(
                label = stringResource(Res.string.feature_profile_email),
                value = client.emailAddress,
            )
            ProfileItem(
                label = stringResource(Res.string.feature_profile_vpa),
                value = client.externalId,
            )
            ProfileItem(
                label = stringResource(Res.string.feature_profile_mobile),
                value = client.mobileNo,
            )
        }
    }
}

/** A labeled profile field showing a label above its value. */
@Composable
fun ProfileItem(
    label: String,
    value: String,
    labelColor: Color = KptTheme.colorScheme.primary,
    textColor: Color = KptTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = labelColor,
            style = KptTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            color = textColor,
            style = KptTheme.typography.labelLarge,
            fontWeight = FontWeight(400),
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}
