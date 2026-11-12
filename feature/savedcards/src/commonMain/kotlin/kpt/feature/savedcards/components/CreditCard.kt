/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.components

import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kpt.core.base.designsystem.theme.KptTheme
import kpt.feature.savedcards.createOrUpdate.AECardState
import kpt.feature.savedcards.utils.CardMaskStyle
import kpt.feature.savedcards.utils.CardType
import kpt.feature.savedcards.utils.creditCardColors
import org.jetbrains.compose.resources.painterResource
@Composable
internal fun CreditCard(
    card: AECardState,
    modifier: Modifier = Modifier,
    baseColor: Color = creditCardColors.first(),
    bankCardAspectRatio: Float = 1.586f,
) {
    Card(
        modifier = modifier.fillMaxWidth().aspectRatio(bankCardAspectRatio),
        elevation = CardDefaults.cardElevation(defaultElevation = KptTheme.elevation.level5),
    ) {
        Box {
            CreditCardBackground(baseColor = baseColor)
            // Position to center
            AnimatedCardNumberInput(
                cardNumber = card.cardNumber,
                maskStyle = CardMaskStyle.SHOW_ALL,
            )
            // Positioned to corner top left
            SpaceWrapper(
                modifier = Modifier.align(Alignment.TopStart),
                space = KptTheme.spacing.xl,
                top = true,
                left = true,
            ) {
                CreditCardLabelAndText(
                    label = "card holder",
                    text = card.fullName,
                )
            }
            // Positioned to corner bottom left
            SpaceWrapper(
                modifier = Modifier.align(Alignment.BottomStart),
                space = KptTheme.spacing.xl,
                bottom = true,
                left = true,
            ) {
                Row {
                    CreditCardLabelAndText(
                        label = "expires",
                        text = card.expiryDateFormatted,
                    )
                    Spacer(modifier = Modifier.width(KptTheme.spacing.md))
                    CreditCardLabelAndText(
                        label = "cvv",
                        text = card.maskedCVV,
                    )
                }
            }
            // Positioned to corner bottom right
            SpaceWrapper(
                modifier = Modifier.align(Alignment.BottomEnd),
                space = KptTheme.spacing.xl,
                bottom = true,
                right = true,
            ) {
                // Card type logo
                androidx.compose.animation.AnimatedVisibility(
                    visible = card.cardType != CardType.UNKNOWN,
                    enter = CardConfig.logoEnterTransition,
                    exit = CardConfig.logoExitTransition,
                ) {
                    Image(
                        painter = painterResource(card.cardType.cardImage),
                        contentDescription = "Card Image",
                        modifier = Modifier.size(CardConfig.logoSize)
                            .padding(top = KptTheme.spacing.sm),
                        colorFilter = CardConfig.logoTint?.let { ColorFilter.tint(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedCardNumberInput(
    cardNumber: String,
    cardType: CardType = CardType.detectCardType(cardNumber),
    maskStyle: CardMaskStyle = CardMaskStyle.ALL_EXCEPT_LAST_FOUR,
    textColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
    dotColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
    modifier: Modifier = Modifier,
) {
    val groups = remember(cardNumber, cardType) {
        val paddedNumber = cardNumber.padEnd(cardType.maxLength, ' ')
        when (cardType) {
            CardType.AMEX -> listOf(
                paddedNumber.take(4),
                paddedNumber.substring(4, 10),
                paddedNumber.substring(10, 15),
            )

            CardType.DINERS_CLUB -> listOf(
                paddedNumber.take(4),
                paddedNumber.substring(4, 10),
                paddedNumber.substring(10, 14),
            )

            else -> paddedNumber.chunked(4)
        }
    }

    val shouldShowDigit = { index: Int, total: Int ->
        when (maskStyle) {
            CardMaskStyle.ALL_EXCEPT_LAST_FOUR -> index >= total - 4
            CardMaskStyle.SHOW_FIRST_LAST_FOUR -> index < 4 || index >= total - 4
            CardMaskStyle.SHOW_NONE -> false
            CardMaskStyle.SHOW_ALL -> true
        }
    }

    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = KptTheme.spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(CardConfig.spaceBetweenGroups),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var currentIndex = 0
            groups.forEach { group ->
                GroupDisplay(
                    digits = group,
                    baseIndex = currentIndex,
                    shouldShowDigit = shouldShowDigit,
                    textColor = textColor,
                    dotColor = dotColor,
                    modifier = Modifier.width(
                        when (group.length) {
                            6 -> 96.dp
                            else -> 64.dp
                        },
                    ),
                )
                currentIndex += group.length
            }
        }
    }
}

@Composable
private fun AnimatedCVVInput(
    cvv: String,
    cardType: CardType = CardType.UNKNOWN,
    isVisible: Boolean = false,
    textColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
    dotColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
    modifier: Modifier = Modifier,
) {
    val paddedCVV = remember(cvv, cardType) {
        cvv.padEnd(cardType.cvvLength, ' ')
    }

    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "CVV",
            style = TextStyle(
                fontWeight = FontWeight.W300,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = KptTheme.colorScheme.surfaceContainerLowest,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(CardConfig.spaceBetweenDots / 2),
        ) {
            paddedCVV.forEachIndexed { index, digit ->
                DigitTransition(
                    digit = digit,
                    index = index,
                    forceHide = !isVisible && digit != ' ',
                    textColor = textColor,
                    dotColor = dotColor,
                )
            }
        }
    }
}

@Composable
private fun GroupDisplay(
    digits: String,
    baseIndex: Int,
    shouldShowDigit: (Int, Int) -> Boolean,
    textColor: Color,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(24.dp),
    ) {
        digits.forEachIndexed { index, digit ->
            val absoluteIndex = baseIndex + index
            DigitTransition(
                digit = digit,
                index = index,
                forceHide = !shouldShowDigit(absoluteIndex, digits.length) && digit != ' ',
                textColor = textColor,
                dotColor = dotColor,
            )
        }
    }
}

@Composable
private fun DigitTransition(
    digit: Char,
    index: Int,
    forceHide: Boolean = false,
    textColor: Color,
    dotColor: Color,
) {
    val isNumber = digit != ' '
    val shouldShowNumber = isNumber && !forceHide

    val transition = updateTransition(
        targetState = shouldShowNumber,
        label = "DigitTransition",
    )

    val dotAlpha by transition.animateFloat(
        label = "DotAlpha",
        transitionSpec = { tween(300) },
    ) { show -> if (show) 0f else 1f }

    val textAlpha by transition.animateFloat(
        label = "TextAlpha",
        transitionSpec = { tween(300) },
    ) { show -> if (show) 1f else 0f }

    val slideOffset by transition.animateFloat(
        label = "SlideOffset",
        transitionSpec = { tween(300, easing = EaseOutQuad) },
    ) { show -> if (show) 0f else -8f }

    Canvas(
        modifier = Modifier.offset(x = (CardConfig.dotRadius.value * 2 + CardConfig.spaceBetweenDots.value) * index.dp)
            .graphicsLayer(alpha = dotAlpha),
    ) {
        drawCircle(
            color = dotColor,
            radius = CardConfig.dotRadius.toPx(),
            center = Offset(CardConfig.dotRadius.toPx(), size.height / 2),
        )
    }

    if (isNumber) {
        Text(
            text = digit.toString(),
            color = textColor,
            style = TextStyle(fontSize = 20.sp),
            modifier = Modifier.offset(
                x = (CardConfig.dotRadius.value * 2 + CardConfig.spaceBetweenDots.value) * index.dp,
                y = slideOffset.dp,
            ).graphicsLayer(alpha = textAlpha),
        )
    }
}

@Composable
private fun CreditCardBackground(baseColor: Color) {
    val colorSaturation75 = baseColor.setSaturation(0.75f)
    val colorSaturation50 = baseColor.setSaturation(0.5f)
    // Drawing Shapes with Canvas
    Canvas(
        modifier = Modifier.fillMaxSize().background(baseColor),
    ) {
        // Drawing Circles
        drawCircle(
            color = colorSaturation75,
            center = Offset(x = size.width * 0.2f, y = size.height * 0.6f),
            radius = size.minDimension * 0.85f,
        )
        drawCircle(
            color = colorSaturation50,
            center = Offset(x = size.width * 0.1f, y = size.height * 0.3f),
            radius = size.minDimension * 0.75f,
        )
    }
}

@Composable
private fun CreditCardLabelAndText(
    label: String,
    text: String,
    labelColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
    textColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
) {
    Column(
        modifier = Modifier.wrapContentSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontWeight = FontWeight.W300,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
            ),
            color = labelColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = TextStyle(
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
            ),
            color = textColor,
        )
    }
}

@Composable
private fun SpaceWrapper(
    modifier: Modifier = Modifier,
    space: Dp,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.then(if (top) Modifier.padding(top = space) else Modifier)
            .then(if (right) Modifier.padding(end = space) else Modifier)
            .then(if (bottom) Modifier.padding(bottom = space) else Modifier)
            .then(if (left) Modifier.padding(start = space) else Modifier),
    ) {
        content()
    }
}

@Composable
internal fun CreditCard(
    cardNumber: String,
    fullName: String,
    expiryDate: String,
    cvv: String,
    maskStyle: CardMaskStyle = CardMaskStyle.SHOW_FIRST_LAST_FOUR,
    modifier: Modifier = Modifier,
    cardType: CardType = CardType.detectCardType(cardNumber),
    baseColor: Color = creditCardColors.first(),
    bankCardAspectRatio: Float = 1.586f,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().aspectRatio(bankCardAspectRatio),
        elevation = CardDefaults.cardElevation(defaultElevation = KptTheme.elevation.level5),
        onClick = onClick,
    ) {
        Box {
            CreditCardBackground(baseColor = baseColor)
            // Position to center
            DisplayCardNumberInput(
                cardNumber = cardNumber,
                maskStyle = maskStyle,
            )
            // Positioned to corner top left
            SpaceWrapper(
                modifier = Modifier.align(Alignment.TopStart),
                space = KptTheme.spacing.xl,
                top = true,
                left = true,
            ) {
                CreditCardLabelAndText(
                    label = "card holder",
                    text = fullName,
                )
            }
            // Positioned to corner bottom left
            SpaceWrapper(
                modifier = Modifier.align(Alignment.BottomStart),
                space = KptTheme.spacing.xl,
                bottom = true,
                left = true,
            ) {
                Row {
                    CreditCardLabelAndText(
                        label = "expires",
                        text = expiryDate,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    CreditCardLabelAndText(
                        label = "cvv",
                        text = cvv,
                    )
                }
            }
            // Positioned to corner bottom right
            SpaceWrapper(
                modifier = Modifier.align(Alignment.BottomEnd),
                space = KptTheme.spacing.xl,
                bottom = true,
                right = true,
            ) {
                // Card type logo
                androidx.compose.animation.AnimatedVisibility(
                    visible = cardType != CardType.UNKNOWN,
                    enter = CardConfig.logoEnterTransition,
                    exit = CardConfig.logoExitTransition,
                ) {
                    Image(
                        painter = painterResource(cardType.cardImage),
                        contentDescription = "Card Image",
                        modifier = Modifier.size(CardConfig.logoSize).padding(top = KptTheme.spacing.sm),
                        colorFilter = CardConfig.logoTint?.let { ColorFilter.tint(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplayCardNumberInput(
    cardNumber: String,
    cardType: CardType = CardType.detectCardType(cardNumber),
    maskStyle: CardMaskStyle = CardMaskStyle.ALL_EXCEPT_LAST_FOUR,
    textColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
    dotColor: Color = KptTheme.colorScheme.surfaceContainerLowest,
    modifier: Modifier = Modifier,
) {
    val groups = remember(cardNumber, cardType) {
        val paddedNumber = cardNumber.padEnd(cardType.maxLength, ' ')
        when (cardType) {
            CardType.AMEX -> listOf(
                paddedNumber.take(4),
                paddedNumber.substring(4, 10),
                paddedNumber.substring(10, 15),
            )

            CardType.DINERS_CLUB -> listOf(
                paddedNumber.take(4),
                paddedNumber.substring(4, 10),
                paddedNumber.substring(10, 14),
            )

            else -> paddedNumber.chunked(4)
        }
    }

    val totalCardLength = remember(cardType) {
        cardType.maxLength
    }

    val shouldShowDigit = { index: Int, total: Int ->
        when (maskStyle) {
            CardMaskStyle.ALL_EXCEPT_LAST_FOUR -> index >= totalCardLength - 4
            CardMaskStyle.SHOW_FIRST_LAST_FOUR -> index < 4 || index >= totalCardLength - 4
            CardMaskStyle.SHOW_NONE -> false
            CardMaskStyle.SHOW_ALL -> true
        }
    }

    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = KptTheme.spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(CardConfig.spaceBetweenGroups),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var currentIndex = 0
            groups.forEach { group ->
                DisplayCardNumber(
                    digits = group,
                    baseIndex = currentIndex,
                    shouldShowDigit = shouldShowDigit,
                    textColor = textColor,
                    dotColor = dotColor,
                    modifier = Modifier.width(
                        when (group.length) {
                            6 -> 96.dp
                            else -> 64.dp
                        },
                    ),
                )
                currentIndex += group.length
            }
        }
    }
}

@Composable
private fun DisplayCardNumber(
    digits: String,
    baseIndex: Int,
    shouldShowDigit: (Int, Int) -> Boolean,
    textColor: Color,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        digits.forEachIndexed { index, digit ->
            val actualIndex = baseIndex + index
            // Fix: Use the total card length instead of group length for masking calculation
            val totalLength = digits.length + baseIndex

            if (digit != ' ') {
                if (shouldShowDigit(actualIndex, totalLength)) {
                    // Show actual digit
                    Text(
                        text = digit.toString(),
                        color = textColor,
                        style = TextStyle(fontSize = 20.sp),
                    )
                } else {
                    // Show dot or bullet for masked digits
                    Box(
                        modifier = Modifier.size(8.dp).background(
                            color = dotColor,
                            shape = CircleShape,
                        ),
                    )
                }
            }
        }
    }
}
