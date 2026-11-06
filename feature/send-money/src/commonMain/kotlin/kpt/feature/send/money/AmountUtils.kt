/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import kpt.core.common.CurrencyFormatter
/**
 * Utility functions for converting between paise and rupees
 * Navigation parameters use paise (stored as string) for precision
 * UI displays use rupees with 2 decimal places
 */
object AmountUtils {

    /**
     * Converts rupees (as string) to paise (as string)
     * @param rupees Amount in rupees as string (e.g., "100.50")
     * @return Amount in paise as string (e.g., "10050")
     */
    fun rupeesToPaise(rupees: String): String {
        return try {
            val amount = rupees.toDoubleOrNull() ?: 0.0
            (amount * 100).toLong().toString()
        } catch (e: NumberFormatException) {
            "0"
        }
    }

    /**
     * Converts paise (as string) to rupees (as string) with 2 decimal places
     * @param paise Amount in paise as string (e.g., "10050")
     * @return Amount in rupees as string with 2 decimal places (e.g., "100.50")
     */
    fun paiseToRupees(paise: String): String {
        return try {
            val amount = paise.toLongOrNull() ?: 0L
            val rupees = amount / 100.0
            val formatted = CurrencyFormatter.format(
                balance = rupees,
                currencyCode = "INR",
                maximumFractionDigits = 2,
            )
            formatted.replace("₹", "").trim()
        } catch (e: NumberFormatException) {
            "0.00"
        }
    }

    /**
     * Formats rupees amount for UI display with proper formatting
     * @param rupees Amount in rupees as string (e.g., "100.50")
     * @return Formatted amount for UI (e.g., "₹100.50")
     */
    fun formatRupeesForUI(rupees: String): String {
        return try {
            val amount = rupees.toDoubleOrNull() ?: 0.0
            return CurrencyFormatter.format(
                balance = amount,
                currencyCode = "INR",
                maximumFractionDigits = 2,
            )
        } catch (e: NumberFormatException) {
            "₹0.00"
        }
    }

    /**
     * Formats paise amount for UI display by converting to rupees first
     * @param paise Amount in paise as string (e.g., "10050")
     * @return Formatted amount for UI (e.g., "₹100.50")
     */
    fun formatPaiseForUI(paise: String): String {
        val rupees = paiseToRupees(paise)
        return formatRupeesForUI(rupees)
    }

    /**
     * Validates if a paise amount is valid
     * @param paise Amount in paise as string
     * @return true if valid, false otherwise
     */
    fun isValidPaise(paise: String): Boolean {
        return try {
            val amount = paise.toLongOrNull()
            amount != null && amount >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Validates if a rupees amount is valid
     * @param rupees Amount in rupees as string
     * @return true if valid, false otherwise
     */
    fun isValidRupees(rupees: String): Boolean {
        return try {
            val amount = rupees.toDoubleOrNull()
            amount != null && amount >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    /**
     * Validates and formats amount input for the new input system
     * Ensures amount starts with single digit, allows only one decimal point
     * @param input Raw input string from user
     * @return Validated and formatted amount string
     */
    fun validateAndFormatAmountInput(input: String): String {
        if (input.isEmpty()) return ""

        val cleanInput = input.replace(",", "")

        if (cleanInput == ".") return "0."
        if (cleanInput.startsWith(".")) return "0$cleanInput"

        val parts = cleanInput.split(".")
        if (parts.size > 2) return parts[0] + "." + parts[1]

        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else ""

        if (integerPart.isEmpty()) return "0."
        if (integerPart.length > 6) return integerPart.take(6) + (if (parts.size > 1) ".$decimalPart" else "")
        if (decimalPart.length > 2) return "$integerPart.${decimalPart.take(2)}"

        return cleanInput
    }

    /**
     * Checks if the amount input is valid
     * @param input Raw input string from user
     * @return true if input is valid, false otherwise
     */
    fun isValidAmountInput(input: String): Boolean {
        if (input.isEmpty()) return true

        val cleanInput = input.replace(",", "")

        if (cleanInput == "." || cleanInput == "0.") return true
        if (cleanInput.startsWith(".")) return false

        val parts = cleanInput.split(".")
        if (parts.size > 2) return false

        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else ""

        if (integerPart.isEmpty()) return false
        if (integerPart.length > 6) return false
        if (decimalPart.length > 2) return false

        val validIntegerPart = integerPart.all { it.isDigit() }
        val validDecimalPart = decimalPart.isEmpty() || decimalPart.all { it.isDigit() }

        return validIntegerPart && validDecimalPart
    }

    /**
     * Formats input amount for display
     * Shows only the numeric part without currency symbol
     * @param amount Amount in rupees as string
     * @return Formatted amount for display
     */

    // TODO handle edge cases for example decimal point is entered first.
    fun formatAmountForInput(amount: String): String {
        if (amount.isEmpty()) return ""

        val cleanAmount = amount.replace(",", "")
        return try {
            val amountValue = cleanAmount.toDoubleOrNull() ?: 0.0
            if (amountValue == 0.0) return ""

            val parts = amountValue.toString().split(".")
            val integerPart = parts[0]
            val decimalPart = if (parts.size > 1) parts[1] else ""

            if (decimalPart.isEmpty() || decimalPart == "0") {
                integerPart
            } else if (decimalPart == "00") {
                integerPart
            } else if (decimalPart.endsWith("0")) {
                "$integerPart.${decimalPart.dropLast(1)}"
            } else {
                "$integerPart.$decimalPart"
            }
        } catch (e: NumberFormatException) {
            amount
        }
    }
}
