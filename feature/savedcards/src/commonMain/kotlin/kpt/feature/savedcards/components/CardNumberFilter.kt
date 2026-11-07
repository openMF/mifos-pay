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

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kpt.feature.savedcards.utils.CardType
/**
 * Visual transformation that formats a card number into groups for the given card type, optionally masking it.
 * @property cardType Card network that decides the grouping.
 * @property isMasked Whether digits are hidden when displayed.
 */
class CreditCardVisualTransformation(
    private val cardType: CardType = CardType.UNKNOWN,
    private val isMasked: Boolean = false,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = when (cardType) {
            CardType.AMEX -> {
                if (text.text.length >= 15) text.text.substring(0..14) else text.text
            }

            CardType.DINERS_CLUB -> {
                if (text.text.length >= 14) text.text.substring(0..13) else text.text
            }

            else -> {
                if (text.text.length >= 16) text.text.substring(0..15) else text.text
            }
        }

        val formattedText = if (isMasked) {
            maskCardNumber(trimmed)
        } else {
            formatCardNumber(trimmed)
        }

        return TransformedText(
            AnnotatedString(formattedText),
            getOffsetMappingForCardType(cardType),
        )
    }

    private fun formatCardNumber(number: String): String {
        return when (cardType) {
            CardType.AMEX -> {
                // Format: XXXX XXXXXX XXXXX
                buildString {
                    number.forEachIndexed { index, char ->
                        append(char)
                        if (index == 3 || index == 9) append(" ")
                    }
                }
            }

            CardType.DINERS_CLUB -> {
                // Format: XXXX XXXXXX XXXX
                buildString {
                    number.forEachIndexed { index, char ->
                        append(char)
                        if (index == 3 || index == 9) append(" ")
                    }
                }
            }

            else -> {
                // Format: XXXX XXXX XXXX XXXX
                buildString {
                    number.forEachIndexed { index, char ->
                        append(char)
                        if (index % 4 == 3 && index != 15) append(" ")
                    }
                }
            }
        }
    }

    private fun maskCardNumber(number: String): String {
        return when (cardType) {
            CardType.AMEX -> {
                buildString {
                    number.forEachIndexed { index, char ->
                        append(if (index < number.length - 5) '*' else char)
                        if (index == 3 || index == 9) append(" ")
                    }
                }
            }

            CardType.DINERS_CLUB -> {
                // Format: XXXX XXXXXX XXXX
                buildString {
                    number.forEachIndexed { index, char ->
                        append(if (index < number.length - 4) '*' else char)
                        if (index == 3 || index == 9) append(" ")
                    }
                }
            }

            else -> {
                buildString {
                    number.forEachIndexed { index, char ->
                        append(if (index < number.length - 4) '*' else char)
                        if (index % 4 == 3 && index != 15) append(" ")
                    }
                }
            }
        }
    }

    private fun getOffsetMappingForCardType(cardType: CardType): OffsetMapping {
        return when (cardType) {
            CardType.AMEX -> AmexOffsetMapping
            CardType.DINERS_CLUB -> DinersClubOffsetMapping
            else -> DefaultCardOffsetMapping
        }
    }

    private object DefaultCardOffsetMapping : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 7) return offset + 1
            if (offset <= 11) return offset + 2
            if (offset <= 16) return offset + 3
            return 19
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) return offset
            if (offset <= 9) return offset - 1
            if (offset <= 14) return offset - 2
            if (offset <= 19) return offset - 3
            return 16
        }
    }

    private object AmexOffsetMapping : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 9) return offset + 1
            if (offset <= 15) return offset + 2
            return 17
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) return offset
            if (offset <= 11) return offset - 1
            if (offset <= 17) return offset - 2
            return 15
        }
    }

    private object DinersClubOffsetMapping : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 3) return offset
            if (offset <= 9) return offset + 1
            if (offset <= 14) return offset + 2
            return 16
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 4) return offset
            if (offset <= 11) return offset - 1
            if (offset <= 16) return offset - 2
            return 14
        }
    }
}

/**
 * Returns a [VisualTransformation] for [cardNumber], detecting its card type and honoring [isMasked].
 */
// Extension function to detect card type and apply appropriate transformation
fun getCardNumberTransformation(cardNumber: String, isMasked: Boolean): VisualTransformation {
    val cardType = CardType.detectCardType(cardNumber)
    return CreditCardVisualTransformation(cardType, isMasked)
}
