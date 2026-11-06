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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import mifos_pay.feature.mpay_qr_scan.generated.resources.Res
import mifos_pay.feature.mpay_qr_scan.generated.resources.feature_qr_viewfinder
import org.jetbrains.compose.resources.stringResource

@Composable
fun QrViewfinder(
    modifier: Modifier = Modifier,
) {
    val primaryColor = KptTheme.colorScheme.primary
    val density = LocalDensity.current
    val viewfinderDescription = stringResource(Res.string.feature_qr_viewfinder)

    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanLineOffset",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = viewfinderDescription },
    ) {
        val screenWidth = with(density) { maxWidth.toPx() }
        val screenHeight = with(density) { maxHeight.toPx() }

        // Viewfinder dimensions
        val viewfinderSize = with(density) { 280.dp.toPx() }
        val cornerRadius = with(density) { 16.dp.toPx() }
        val cornerLength = with(density) { 40.dp.toPx() }
        val strokeWidth = with(density) { 4.dp.toPx() }
        val scanLineWidth = with(density) { 2.dp.toPx() }

        // Center the viewfinder
        val left = (screenWidth - viewfinderSize) / 2
        val top = (screenHeight - viewfinderSize) / 2 - with(density) { 40.dp.toPx() }
        val right = left + viewfinderSize
        val bottom = top + viewfinderSize

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw semi-transparent overlay
            val overlayPath = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(
                    RoundRect(
                        left = left,
                        top = top,
                        right = right,
                        bottom = bottom,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
                fillType = PathFillType.EvenOdd
            }

            drawPath(
                path = overlayPath,
                color = Color.Black.copy(alpha = 0.6f),
            )

            // Draw corner brackets with primary color
            // Top-left corner
            drawLine(
                color = primaryColor,
                start = Offset(left, top + cornerLength),
                end = Offset(left, top + cornerRadius),
                strokeWidth = strokeWidth,
            )
            drawArc(
                color = primaryColor,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(left, top),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(width = strokeWidth),
            )
            drawLine(
                color = primaryColor,
                start = Offset(left + cornerRadius, top),
                end = Offset(left + cornerLength, top),
                strokeWidth = strokeWidth,
            )

            // Top-right corner
            drawLine(
                color = primaryColor,
                start = Offset(right - cornerLength, top),
                end = Offset(right - cornerRadius, top),
                strokeWidth = strokeWidth,
            )
            drawArc(
                color = primaryColor,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(right - cornerRadius * 2, top),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(width = strokeWidth),
            )
            drawLine(
                color = primaryColor,
                start = Offset(right, top + cornerRadius),
                end = Offset(right, top + cornerLength),
                strokeWidth = strokeWidth,
            )

            // Bottom-left corner
            drawLine(
                color = primaryColor,
                start = Offset(left, bottom - cornerLength),
                end = Offset(left, bottom - cornerRadius),
                strokeWidth = strokeWidth,
            )
            drawArc(
                color = primaryColor,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(left, bottom - cornerRadius * 2),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(width = strokeWidth),
            )
            drawLine(
                color = primaryColor,
                start = Offset(left + cornerRadius, bottom),
                end = Offset(left + cornerLength, bottom),
                strokeWidth = strokeWidth,
            )

            // Bottom-right corner
            drawLine(
                color = primaryColor,
                start = Offset(right - cornerLength, bottom),
                end = Offset(right - cornerRadius, bottom),
                strokeWidth = strokeWidth,
            )
            drawArc(
                color = primaryColor,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(right - cornerRadius * 2, bottom - cornerRadius * 2),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(width = strokeWidth),
            )
            drawLine(
                color = primaryColor,
                start = Offset(right, bottom - cornerRadius),
                end = Offset(right, bottom - cornerLength),
                strokeWidth = strokeWidth,
            )

            // Draw animated scan line
            val scanLineY = top + cornerRadius + (viewfinderSize - cornerRadius * 2) * scanLineProgress
            val scanLinePadding = cornerLength / 2

            drawLine(
                color = primaryColor.copy(alpha = 0.8f),
                start = Offset(left + scanLinePadding, scanLineY),
                end = Offset(right - scanLinePadding, scanLineY),
                strokeWidth = scanLineWidth,
                blendMode = BlendMode.Screen,
            )
        }
    }
}

@Composable
@org.jetbrains.compose.ui.tooling.preview.Preview
private fun QrViewfinderPreview() {
    kpt.core.base.designsystem.KptMaterialTheme {
        QrViewfinder()
    }
}
