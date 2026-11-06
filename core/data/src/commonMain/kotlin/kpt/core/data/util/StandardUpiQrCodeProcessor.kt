/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import kpt.core.model.utils.PaymentQrData
import kpt.core.model.utils.StandardUpiQrData
/**
 * Standard UPI QR Code Processor
 * Handles parsing of standard UPI QR codes according to UPI specification
 */
object StandardUpiQrCodeProcessor {

    /**
     * Checks if the given string is a valid UPI QR code
     * @param qrData The QR code data string
     * @return true if it's a valid UPI QR code, false otherwise
     */
    fun isValidUpiQrCode(qrData: String): Boolean {
        return qrData.startsWith("upi://") || qrData.startsWith("UPI://")
    }

    /**
     * Parses a standard UPI QR code string
     * @param qrData The QR code data string
     * @return StandardUpiQrData object with parsed information
     * @throws IllegalArgumentException if the QR code is invalid
     */
    fun parseUpiQrCode(qrData: String): StandardUpiQrData {
        if (!isValidUpiQrCode(qrData)) {
            throw IllegalArgumentException("Invalid UPI QR code format")
        }

        val paramsString = qrData.substringAfter("upi://").substringAfter("UPI://")
        val parts = paramsString.split("?", limit = 2)
        val params = if (parts.size > 1) parseParams(parts[1]) else emptyMap()

        val payeeVpa = params["pa"] ?: run {
            throw IllegalArgumentException("Missing payee VPA (pa) in UPI QR code")
        }
        val payeeName = params["pn"] ?: "Unknown"

        val vpaParts = payeeVpa.split("@", limit = 2)
        val actualVpa = if (vpaParts.size == 2) payeeVpa else payeeVpa

        return StandardUpiQrData(
            payeeName = payeeName,
            payeeVpa = actualVpa,
            amount = params["am"] ?: "",
            currency = params["cu"] ?: StandardUpiQrData.DEFAULT_CURRENCY,
            transactionNote = params["tn"] ?: "",
            merchantCode = params["mc"] ?: "",
            transactionReference = params["tr"] ?: "",
            url = params["url"] ?: "",
            mode = params["mode"] ?: "02",
        )
    }

    /**
     * Parses URL parameters into a map
     * @param paramsString The parameters string
     * @return Map of parameter keys and values
     */
    private fun parseParams(paramsString: String): Map<String, String> {
        return paramsString
            .split("&")
            .associate { param ->
                val keyValue = param.split("=", limit = 2)
                if (keyValue.size == 2) {
                    keyValue[0] to keyValue[1]
                } else {
                    param to ""
                }
            }
    }

    /**
     * Converts StandardUpiQrData to PaymentQrData for compatibility with existing code
     * @param standardData Standard UPI QR data
     * @return PaymentQrData object
     * Note: clientId and accountId not available in standard UPI
     */
    // from send money pr
    fun toPaymentQrData(standardData: StandardUpiQrData): PaymentQrData {
        return PaymentQrData(
            clientId = 0,
            clientName = standardData.payeeName,
            accountNo = standardData.payeeVpa,
            amount = standardData.amount,
            accountId = 0,
            currency = standardData.currency,
            officeId = 1,
            accountTypeId = 2,
        )
    }
}
