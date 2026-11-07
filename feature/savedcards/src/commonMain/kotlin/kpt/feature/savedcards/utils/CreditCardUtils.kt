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
import kpt.feature.savedcards.generated.resources.Res
import kpt.feature.savedcards.generated.resources.amex_logo
import kpt.feature.savedcards.generated.resources.diner_clubs
import kpt.feature.savedcards.generated.resources.ic_mastercard
import kpt.feature.savedcards.generated.resources.ic_visa
import kpt.feature.savedcards.generated.resources.maestro
import kpt.feature.savedcards.generated.resources.rupay_logo
import org.jetbrains.compose.resources.DrawableResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Card number, CVV and expiry helpers: detection, Luhn validation, formatting and masking.
 */
object CreditCardUtils {
    /**
     * Detects the [CardType] of this card number.
     */
    // Extension function for card type detection
    fun String.detectCardType(): CardType = CardType.detectCardType(this)

    /**
     * Returns true if this is a digits-only number of the right length for its card type that passes the Luhn
     * check.
     */
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

    /**
     * Returns true if this is a digits-only CVV of the length required by [cardType].
     */
    // Extension function for CVV validation
    fun String.isValidCVV(cardType: CardType): Boolean {
        val cleanCVV = this.replace("\\s".toRegex(), "")

        if (!cleanCVV.matches("\\d+".toRegex())) {
            return false
        }

        return cleanCVV.length == cardType.cvvLength
    }

    /**
     * Card expiry as month and year.
     * @property month Month of expiry, 1 to 12.
     * @property year Four-digit year of expiry.
     */
    // Data class to hold expiry date information
    data class ExpiryDate(val month: Int, val year: Int) {
        /**
         * Holds the [ExpiryDate] parser.
         */
        companion object {
            /**
             * Parses `MM/YY` or `MM/YYYY` into an [ExpiryDate], or returns null if the format is invalid.
             */
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

        /**
         * Returns true if the month is valid and the card has not expired yet (one month of grace).
         */
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

    /**
     * Returns true if this string parses as an expiry date that is still valid.
     */
    // Expiry date validation extension function
    fun String.isValidExpiryDate(): Boolean {
        val expiryDate = ExpiryDate.parse(this) ?: return false
        return expiryDate.isValid()
    }

    /**
     * Formats this card number into groups according to its detected card type.
     */
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

    /**
     * Formats [number] into groups for [cardType], truncating to the maximum digits of that type.
     */
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

    /**
     * Masks all but the last digits of this card number and groups it by card type.
     */
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

/**
 * How much of a card number is revealed when displayed.
 */
enum class CardMaskStyle {
    /**
     * Mask everything except the last four digits.
     */
    ALL_EXCEPT_LAST_FOUR,

    /**
     * Show only the first four and last four digits.
     */
    SHOW_FIRST_LAST_FOUR,

    /**
     * Mask every digit.
     */
    SHOW_NONE,

    /**
     * Show every digit.
     */
    SHOW_ALL,
}

/**
 * Supported card networks with their number rules.
 * @property maxLength Number of digits in a card number.
 * @property cvvLength Number of digits in the CVV.
 * @property pattern Regex matching numbers of this network.
 * @property cardImage Logo drawable of the network.
 */
enum class CardType(
    val maxLength: Int,
    val cvvLength: Int,
    val pattern: Regex,
    val cardImage: DrawableResource,
) {
    /**
     * Visa.
     */
    VISA(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^4[0-9]*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),

    /**
     * RuPay.
     */
    RUPAY(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^6[0-9]{15}$".toRegex(),
        cardImage = Res.drawable.rupay_logo,
    ),

    /**
     * Mastercard.
     */
    MASTERCARD(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^5[1-5][0-9]*".toRegex(),
        cardImage = Res.drawable.ic_mastercard,
    ),

    /**
     * American Express.
     */
    AMEX(
        maxLength = 15,
        cvvLength = 4,
        pattern = "^3[47][0-9]*".toRegex(),
        cardImage = Res.drawable.amex_logo,
    ),

    /**
     * Discover.
     */
    DISCOVER(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^6(?:011|5[0-9]{2})[0-9]*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),

    /**
     * Diners Club.
     */
    DINERS_CLUB(
        maxLength = 14,
        cvvLength = 3,
        pattern = "^3(?:0[0-5]|[68][0-9])[0-9]*".toRegex(),
        cardImage = Res.drawable.diner_clubs,
    ),

    /**
     * JCB.
     */
    JCB(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^(?:2131|1800|35\\d{3})\\d*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),

    /**
     * Maestro.
     */
    MAESTRO(
        maxLength = 16,
        cvvLength = 3,
        pattern = "^(5018|5020|5038|6304|6759|6761|6763)[0-9]{8,15}$".toRegex(),
        cardImage = Res.drawable.maestro,
    ),

    /**
     * Fallback when no network pattern matches.
     */
    UNKNOWN(
        maxLength = 16,
        cvvLength = 3,
        pattern = ".*".toRegex(),
        cardImage = Res.drawable.ic_visa,
    ),
    ;

    /**
     * Holds the card type lookup.
     */
    companion object {
        /**
         * Returns the [CardType] whose pattern matches [number], or [UNKNOWN].
         */
        fun detectCardType(number: String): CardType {
            if (number.isEmpty()) return UNKNOWN
            return values().find { it.pattern.matches(number) } ?: UNKNOWN
        }
    }
}

/**
 * Palette of colors a user can pick for a saved card.
 */
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
