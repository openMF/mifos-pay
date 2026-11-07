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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkCard
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
/**
 * A compact loan summary card showing title/amount/interest as plain text on a solid
 * background — no background image, since a generic illustration stretched to fill the
 * card looked like a stray diagonal shimmer artifact.
 *
 * @param title The loan product title (e.g., "Home Loan").
 * @param amount The formatted loan amount string.
 * @param interestRate The interest rate string to display.
 * @param onClick Optional callback invoked when the card is clicked.
 */
@Composable
fun LoanCard(
    title: String,
    amount: String,
    interestRate: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    ForkCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                },
            ),
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primaryContainer,
        ),
        elevation = KptTheme.elevation.level2,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg),
        ) {
            Text(
                text = title,
                style = KptTheme.typography.titleMedium,
                color = KptTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

            Text(
                text = amount,
                style = KptTheme.typography.headlineSmall,
                color = KptTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(KptTheme.spacing.xs))

            Text(
                text = interestRate,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

/**
 * An expanded loan card featuring a top-section image followed by a colored content area.
 *
 * @param cardImage The hero image displayed at the top of the card.
 * @param title The loan product title.
 * @param amount The formatted loan amount string.
 * @param interestRate The interest rate string.
 * @param backgroundColor The background color for the text content area.
 * @param contentColor The color used for text elements.
 * @param onClick Optional callback invoked when the card is clicked.
 */
@Composable
fun LoanCardCustom(
    cardImage: DrawableResource,
    title: String,
    amount: String,
    interestRate: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = KptTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
) {
    ForkCard(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                },
            ),
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = KptTheme.elevation.level3,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Image section
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(KptTheme.shapes.medium),
                painter = painterResource(cardImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.lg),
            ) {
                Text(
                    text = title,
                    style = KptTheme.typography.bodySmall,
                    color = contentColor,
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

                Text(
                    text = amount,
                    style = KptTheme.typography.titleMedium,
                    color = contentColor,
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                Text(
                    text = interestRate,
                    style = KptTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}
