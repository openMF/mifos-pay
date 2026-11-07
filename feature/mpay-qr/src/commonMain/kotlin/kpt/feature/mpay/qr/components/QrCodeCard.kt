/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.feature.mpay.qr.generated.resources.Res
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_inter_bank
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_intra_bank
import kpt.feature.mpay.qr.generated.resources.feature_mpay_qr_scan_instruction
import kpt.feature.mpay.qr.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * QR type identifier for visual distinction.
 */
enum class QrType {
    /** QR for a transfer between accounts within the same bank. */
    INTRA_BANK,

    /** QR for a transfer to an account at a different bank. */
    INTER_BANK,
}

private val QrCodeSize = 260.dp
private val LogoOverlaySize = 52.dp
private val LogoImageSize = 40.dp

/**
 * Enhanced QR card with:
 * - Type badge at top ("Intra-bank" / "Inter-bank") with color distinction
 * - Large QR code (260dp)
 * - Mifos logo centered with circular background and border
 * - "Scan with any payment app" instruction text
 * - Elevated card with rounded corners
 */
@Composable
internal fun QrCodeCard(
    data: String,
    options: QrOptions,
    qrType: QrType,
    modifier: Modifier = Modifier,
) {
    val painter = rememberQrCodePainter(
        data = data,
        options = options,
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = KptTheme.spacing.xs,
        ),
        shape = KptTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            QrTypeBadge(qrType = qrType)

            // QR code with explicit light background for scannability in dark mode
            // Standard QR codes require dark data on light background for reliable scanning
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(KptTheme.spacing.md))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(KptTheme.spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(QrCodeSize),
                )

                LogoOverlay()
            }

            Text(
                text = stringResource(Res.string.feature_mpay_qr_scan_instruction),
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QrTypeBadge(
    qrType: QrType,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (qrType) {
        QrType.INTRA_BANK -> KptTheme.colorScheme.primaryContainer
        QrType.INTER_BANK -> KptTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (qrType) {
        QrType.INTRA_BANK -> KptTheme.colorScheme.onPrimaryContainer
        QrType.INTER_BANK -> KptTheme.colorScheme.onSecondaryContainer
    }
    val text = when (qrType) {
        QrType.INTRA_BANK -> stringResource(Res.string.feature_mpay_qr_intra_bank)
        QrType.INTER_BANK -> stringResource(Res.string.feature_mpay_qr_inter_bank)
    }

    Surface(
        modifier = modifier,
        shape = KptTheme.shapes.small,
        color = backgroundColor,
    ) {
        Text(
            text = text,
            style = KptTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(
                horizontal = KptTheme.spacing.md,
                vertical = KptTheme.spacing.sm,
            ),
        )
    }
}

@Composable
private fun LogoOverlay(
    modifier: Modifier = Modifier,
) {
    // Use QR background color for logo to match QR code background
    // This ensures consistent appearance in both light and dark modes
    Box(
        modifier = modifier
            .size(LogoOverlaySize)
            .clip(CircleShape)
            .background(androidx.compose.ui.graphics.Color.White)
            .border(
                width = KptTheme.spacing.xs / 2,
                color = KptTheme.colorScheme.primary,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(LogoImageSize)
                .clip(CircleShape),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview
@Composable
private fun QrCodeCardIntraBankPreview() {
    KptMaterialTheme {
        QrCodeCard(
            data = "sample-qr-data",
            options = QrOptions(),
            qrType = QrType.INTRA_BANK,
        )
    }
}

@Preview
@Composable
private fun QrCodeCardInterBankPreview() {
    KptMaterialTheme {
        QrCodeCard(
            data = "sample-qr-data",
            options = QrOptions(),
            qrType = QrType.INTER_BANK,
        )
    }
}
