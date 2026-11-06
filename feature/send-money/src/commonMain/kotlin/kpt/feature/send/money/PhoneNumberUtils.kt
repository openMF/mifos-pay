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

object PhoneNumberUtils {

    /**
     * Formats an Indian mobile number to the format +91 88888 88888
     */
    fun formatIndianMobileNumber(phoneNumber: String): String {
        // Remove all non-digit characters
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")

        // Check if it's a valid Indian mobile number
        if (!isValidIndianMobileNumber(digitsOnly)) {
            return phoneNumber // Return original if not valid
        }

        // Handle different formats
        return when {
            // If it starts with +91, format the remaining part
            digitsOnly.startsWith("91") && digitsOnly.length == 12 -> {
                val number = digitsOnly.substring(2)
                "+91 ${number.substring(0, 5)} ${number.substring(5)}"
            }
            // If it's a 10-digit number, add +91
            digitsOnly.length == 10 -> {
                "+91 ${digitsOnly.substring(0, 5)} ${digitsOnly.substring(5)}"
            }
            // If it's already 12 digits (with country code), format it
            digitsOnly.length == 12 -> {
                "+${digitsOnly.substring(0, 2)} ${digitsOnly.substring(2, 7)} ${digitsOnly.substring(7)}"
            }
            else -> phoneNumber // Return original if format is not recognized
        }
    }

    /**
     * Checks if a phone number is a valid Indian mobile number
     */
    fun isValidIndianMobileNumber(phoneNumber: String): Boolean {
        // Remove all non-digit characters
        val digitsOnly = phoneNumber.replace(Regex("[^0-9]"), "")

        // Check for common non-mobile patterns
        if (isNonMobileNumber(digitsOnly)) {
            return false
        }

        // Valid Indian mobile number patterns:
        // 1. 10 digits starting with 6, 7, 8, 9
        // 2. 12 digits starting with 91 followed by 6, 7, 8, 9
        return when {
            digitsOnly.length == 10 -> {
                digitsOnly.startsWith("6") || digitsOnly.startsWith("7") ||
                    digitsOnly.startsWith("8") || digitsOnly.startsWith("9")
            }
            digitsOnly.length == 12 && digitsOnly.startsWith("91") -> {
                val number = digitsOnly.substring(2)
                number.startsWith("6") || number.startsWith("7") ||
                    number.startsWith("8") || number.startsWith("9")
            }
            else -> false
        }
    }

    /**
     * Checks if a phone number is a non-mobile number (landline, toll-free, etc.)
     */
    private fun isNonMobileNumber(digitsOnly: String): Boolean {
        // Remove country code if present for checking
        val numberToCheck = if (digitsOnly.length == 12 && digitsOnly.startsWith("91")) {
            digitsOnly.substring(2)
        } else {
            digitsOnly
        }

        // Landline numbers (start with 0 or 1-5)
        if (numberToCheck.startsWith("0") ||
            numberToCheck.startsWith("1") ||
            numberToCheck.startsWith("2") ||
            numberToCheck.startsWith("3") ||
            numberToCheck.startsWith("4") ||
            numberToCheck.startsWith("5")
        ) {
            return true
        }

        // Toll-free numbers (1800, 1860, etc.)
        if (numberToCheck.startsWith("1800") ||
            numberToCheck.startsWith("1860") ||
            numberToCheck.startsWith("1861") ||
            numberToCheck.startsWith("1862") ||
            numberToCheck.startsWith("1863") ||
            numberToCheck.startsWith("1864") ||
            numberToCheck.startsWith("1865") ||
            numberToCheck.startsWith("1866") ||
            numberToCheck.startsWith("1867") ||
            numberToCheck.startsWith("1868") ||
            numberToCheck.startsWith("1869")
        ) {
            return true
        }

        // Special service numbers (100, 101, 102, etc.)
        if (numberToCheck.startsWith("100") ||
            numberToCheck.startsWith("101") ||
            numberToCheck.startsWith("102") ||
            numberToCheck.startsWith("103") ||
            numberToCheck.startsWith("104") ||
            numberToCheck.startsWith("105") ||
            numberToCheck.startsWith("106") ||
            numberToCheck.startsWith("107") ||
            numberToCheck.startsWith("108") ||
            numberToCheck.startsWith("109") ||
            numberToCheck.startsWith("110") ||
            numberToCheck.startsWith("112") ||
            numberToCheck.startsWith("113") ||
            numberToCheck.startsWith("114") ||
            numberToCheck.startsWith("115") ||
            numberToCheck.startsWith("116") ||
            numberToCheck.startsWith("117") ||
            numberToCheck.startsWith("118") ||
            numberToCheck.startsWith("119")
        ) {
            return true
        }

        // USSD codes (*123#, etc.)
        if (numberToCheck.contains("*") || numberToCheck.contains("#")) {
            return true
        }

        // Numbers that are too short or too long
        if (numberToCheck.length < 10 || numberToCheck.length > 10) {
            return true
        }

        return false
    }

    /**
     * Filters and formats a list of contacts to only include valid mobile numbers
     */
    fun filterAndFormatContacts(contacts: List<Contact>): List<Contact> {
        return contacts
            .filter { contact -> isValidIndianMobileNumber(contact.phoneNumber) }
            .map { contact ->
                contact.copy(
                    phoneNumber = formatIndianMobileNumber(contact.phoneNumber),
                )
            }
            .distinctBy { contact -> contact.phoneNumber }
    }

    /**
     * Normalizes a phone number for search purposes by removing country code and spaces
     * This allows searching with or without +91 prefix
     */
    fun normalizePhoneNumberForSearch(phoneNumber: String): String {
        return phoneNumber
            .replace("+91", "")
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .trim()
    }

    /**
     * Normalizes a search query for phone number matching
     * Removes +91 prefix and spaces to match against normalized phone numbers
     */
    fun normalizeSearchQuery(query: String): String {
        return query
            .replace("+91", "")
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .trim()
    }
}
