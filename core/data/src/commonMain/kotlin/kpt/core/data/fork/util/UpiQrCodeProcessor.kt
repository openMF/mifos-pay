/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fork.util

import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64
import kpt.core.model.utils.PaymentQrData
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * UPI QR Code Encoder and Decoder with validation
 */
@OptIn(ExperimentalEncodingApi::class)
object UpiQrCodeProcessor {
    // Regex patterns for validation
    private val VPA_PATTERN = Regex("^[a-zA-Z0-9.-]+@[a-zA-Z0-9]+$")
    private val CURRENCY_PATTERN = Regex("^[A-Z]{3}$")

    // Maximum allowed amount
    private const val MAX_AMOUNT = 100000.0

    /**
     * Encodes UPI payment details into a Base64 encoded string
     * @param paymentQrData UPI payment request details
     * @return Base64 encoded UPI payment string
     * @throws IllegalArgumentException for invalid input
     */
    fun encodeUpiString(paymentQrData: PaymentQrData): String {
        // Validate input data
        validate(paymentQrData)

        // Build UPI string
        val requestPaymentString = buildString {
            append("upi://pay")
            append("?ci=${paymentQrData.clientId}")
            append("&am=${paymentQrData.amount}")
            append("&cn=${paymentQrData.clientName}")
            append("&an=${paymentQrData.accountNo}")
            append("&ai=${paymentQrData.accountId}")
            append("&cu=${paymentQrData.currency}")
            append("&oi=${paymentQrData.officeId}")
            append("&pi=${paymentQrData.accountTypeId}")
            append("&mode=02")
            append("&s=000000")
        }

        return requestPaymentString.encodeBase64()
    }

    /**
     * Decodes a Base64 encoded UPI payment string
     * @param encodedString Base64 encoded UPI payment string
     * @return Decoded PaymentQrData object
     * @throws IllegalArgumentException for invalid encoded string
     */
    fun decodeUpiString(encodedString: String): PaymentQrData {
        // Decode the Base64 string
        val decodedString = encodedString.decodeBase64String()

        // Extract parameters
        val params = parseUpiString(decodedString)

        // Create PaymentQrData
        val requestQrData = PaymentQrData(
            clientId = params["ci"]?.toLongOrNull()
                ?: throw IllegalArgumentException("Missing client ID"),
            clientName = params["cn"]
                ?: throw IllegalArgumentException("Missing client name"),
            accountNo = params["an"]
                ?: throw IllegalArgumentException("Missing account number"),
            amount = params["am"] ?: "",
            accountId = params["ai"]?.toLongOrNull()
                ?: throw IllegalArgumentException("Missing account ID"),
            currency = params["cu"]
                ?: PaymentQrData.DEFAULT_CURRENCY,
            officeId = params["oi"]?.toLongOrNull()
                ?: PaymentQrData.OFFICE_ID,
            accountTypeId = params["pi"]?.toLongOrNull()
                ?: PaymentQrData.ACCOUNT_TYPE_ID,
        )

        // Validate the created object
        validate(requestQrData)

        return requestQrData
    }

    /**
     * Validates the PaymentQrData
     * @param data UPI payment request details to validate
     * @throws IllegalArgumentException for any validation failures
     */
    private fun validate(data: PaymentQrData) {
        // Name validation
        require(data.clientName.isNotBlank()) {
            "Client name cannot be empty"
        }

        require(data.clientName.length <= 50) {
            "Client name too long (max 50 characters)"
        }

        // Account number validation
        require(data.accountNo.isNotBlank()) {
            "Account number cannot be empty"
        }

        // Optional amount validation (if not empty)
        if (data.amount.isNotEmpty()) {
            val amountValue = data.amount.toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid amount format")

            require(amountValue > 0) {
                "Amount must be positive"
            }
            require(amountValue <= MAX_AMOUNT) {
                "Amount exceeds maximum limit of ₹$MAX_AMOUNT"
            }
        }

        // Currency validation
        require(CURRENCY_PATTERN.matches(data.currency)) {
            "Invalid currency code. Must be 3 uppercase letters"
        }
    }

    /**
     * Parses the UPI payment string into a map of parameters
     * @param upiString Decoded UPI payment string
     * @return Map of parameter keys and values
     */
    private fun parseUpiString(upiString: String): Map<String, String> {
        return upiString
            .substringAfter("upi://pay?")
            .split("&")
            .associate {
                val (key, value) = it.split("=")
                key to value
            }
    }
}
