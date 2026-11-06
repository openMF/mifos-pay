/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.utils

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.toLocalDateTime
import mifos_pay.feature.savedcards.generated.resources.Res
import mifos_pay.feature.savedcards.generated.resources.amex_logo
import mifos_pay.feature.savedcards.generated.resources.diner_clubs
import mifos_pay.feature.savedcards.generated.resources.ic_mastercard
import mifos_pay.feature.savedcards.generated.resources.ic_visa
import mifos_pay.feature.savedcards.generated.resources.maestro
import mifos_pay.feature.savedcards.generated.resources.rupay_logo
import org.jetbrains.compose.resources.DrawableResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object CreditCardUtils {
    // Extension function for card type detection
    fun String.detectCardType(): CardType = CardType.detectCardType(this)

    // Extension function for card number validation
    fun String.isValidCreditCardNumber(): Boolean {
        // Remove spaces and non-digit characters
        val cleanNumber = this.replace("\\s".toRegex(), "")

        // Check if the string contains only digits
        if (!cleanNumber.matches("\\d+".toRegex())) {
            return false
        }

        val cardType = detectCardType()

        // Validate card number length based on type
        if (cleanNumber.length != cardType.maxLength) {
            return false
        }

        // Luhn algorithm implementation
        var sum = 0
        var isEven = false

        for (i in cleanNumber.length - 1 downTo 0) {
            var digit = cleanNumber[i].toString().toInt()

            if (isEven) {
                digit *= 2
                if (digit > 9) {
                    digit -= 9
                }
            }

            sum += digit
            isEven = !isEven
        }

        return sum % 10 == 0
    }

    // Extension function for CVV validation
    fun String.isValidCVV(cardType: CardType): Boolean {
        val cleanCVV = this.replace("\\s".toRegex(), "")

        if (!cleanCVV.matches("\\d+".toRegex())) {
            return false
        }

        return cleanCVV.length == cardType.cvvLength
    }

    // Data class to hold expiry date information
    data class ExpiryDate(val month: Int, val year: Int) {
        companion object {
            fun parse(input: String): ExpiryDate? {
                return try {
                    when {
                        // Format: MM/YY
                        input.matches("\\d{2}/\\d{2}".toRegex()) -> {
                            val (month, year) = input.split("/")
                            ExpiryDate(
                                month = month.toInt(),
                                year = 2000 + year.toInt(),
                            )
                        }
                        // Format: MM/YYYY
                        input.matches("\\d{2}/\\d{4}".toRegex()) -> {
                            val (month, year) = input.split("/")
                            ExpiryDate(
                                month = month.toInt(),
                                year = year.toInt(),
                            )
                        }

                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }

        @OptIn(ExperimentalTime::class)
        fun isValid(): Boolean {
            if (month !in 1..12) return false

            val now = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())

            val expiryDate = LocalDate(
                year = year,
                month = Month(month),
                dayOfMonth = 1,
            )

            // Check if card is not expired
            // Add 1 month because cards typically expire at the end of the month
            return now.date.monthsUntil(expiryDate) >= -1
        }
    }

    // Expiry date validation extension function
    fun String.isValidExpiryDate(): Boolean {
        val expiryDate = ExpiryDate.parse(this) ?: return false
        return expiryDate.isValid()
    }

    // Extension function to format card number based on card type
    fun String.formatCreditCardNumber(): String {
        val cleanNumber = this.replace("\\s".toRegex(), "")
        val cardType = detectCardType()

        return when (cardType) {
            CardType.AMEX -> {
                // Format: XXXX XXXXXX XXXXX
                "${cleanNumber.take(4)} ${cleanNumber.substring(4, 10)} ${cleanNumber.takeLast(5)}"
            }

            CardType.DINERS_CLUB -> {
                // Format: XXXX XXXXXX XXXX
                "${cleanNumber.take(4)} ${cleanNumber.substring(4, 10)} ${cleanNumber.takeLast(4)}"
            }

            else -> {
                // Format: XXXX XXXX XXXX XXXX
                cleanNumber.chunked(4).joinToString(" ")
            }
        }.trim()
    }

    // Helper function to format card number based on card type
    fun formatCardNumber(number: String, cardType: CardType): String {
        if (number.isEmpty()) return ""

        return when (cardType) {
            CardType.AMEX -> number.take(15).chunked(4).mapIndexed { index, chunk ->
                when (index) {
                    1 -> chunk.take(6)
                    2 -> chunk.take(5)
                    else -> chunk
                }
            }.joinToString(" ")
            CardType.DINERS_CLUB -> number.take(14).chunked(4).mapIndexed { index, chunk ->
                when (index) {
                    1 -> chunk.take(6)
                    2 -> chunk.take(4)
                    else -> chunk
                }
            }.joinToString(" ")
            else -> number.take(16).chunked(4).joinToString(" ")
        }
    }

    // Extension function to mask card number
    fun String.maskCreditCardNumber(): String {
        val cleanNumber = this.replace("\\s".toRegex(), "")
        val cardType = detectCardType()

        val maskedNumber = when (cardType) {
            CardType.AMEX -> {
                // Show last 5 digits for AMEX
                "*".repeat(10) + cleanNumber.takeLast(5)
            }

            else -> {
                // Show last 4 digits for other cards
                "*".repeat(cleanNumber.length - 4) + cleanNumber.takeLast(4)
            }
        }

        // Format the masked number according to card type
        return when (cardType) {
            CardType.AMEX -> {
                "${maskedNumber.take(4)} ${
                    maskedNumber.substring(
                        4,
                        10,
                    )
                } ${maskedNumber.takeLast(5)}"
            }

            CardType.DINERS_CLUB -> {
                "${maskedNumber.take(4)} ${
                    maskedNumber.substring(
                        4,
                        10,
                    )
                } ${maskedNumber.takeLast(4)}"
            }

            else -> {
                maskedNumber.chunked(4).joinToString(" ")
            }
        }.trim()
    }
}

enum class CardMaskStyle {
    ALL_EXCEPT_LAST_FOUR,
    SHOW_FIRST_LAST_FOUR,
    SHOW_NONE,
    SHOW_ALL,
}

enum class CardType(
    val maxLength: Int,
    val cvvLength: Int,
    val pattern: Regex,
    val cardImage: DrawableResource,
) {
    VISA(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^4[0-9]*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),
    RUPAY(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^6[0-9]{15}$".toRegex(),
        cardImage = Res.drawable.rupay_logo,
    ),
    MASTERCARD(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^5[1-5][0-9]*".toRegex(),
        cardImage = Res.drawable.ic_mastercard,
    ),
    AMEX(
        maxLength = 15,
        cvvLength = 4,
        pattern = "^3[47][0-9]*".toRegex(),
        cardImage = Res.drawable.amex_logo,
    ),
    DISCOVER(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^6(?:011|5[0-9]{2})[0-9]*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),
    DINERS_CLUB(
        maxLength = 14,
        cvvLength = 3,
        pattern = "^3(?:0[0-5]|[68][0-9])[0-9]*".toRegex(),
        cardImage = Res.drawable.diner_clubs,
    ),
    JCB(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^(?:2131|1800|35\\d{3})\\d*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),
    MAESTRO(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^(5018|5020|5038|6304|6759|6761|6763)[0-9]{8,15}$".toRegex(),
        cardImage = Res.drawable.maestro,
    ),
    UNKNOWN(
        maxLength = 16,
        cvvLength = 3,
        pattern = ".*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),
    ;

    companion object {
        fun detectCardType(number: String): CardType {
            if (number.isEmpty()) return UNKNOWN
            return values().find { it.pattern.matches(number) } ?: UNKNOWN
        }
    }
}

val creditCardColors: List<Color> = listOf(
    Color(0xFF1252C8),
    Color(0xFFC0C0C0),
    Color(0xFFB76E79),
    Color(0xFF4169E1),
    Color(0xFF50C878),
    Color(0xFF483D8B),
    Color(0xFF191970),
    Color(0xFFE0115F),
)
