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

import co.touchlab.kermit.Logger
import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * MPay QR Code Encoder and Decoder with validation.
 *
 * MPay string format:
 * ```
 * mpay://pay?qt=0&fsp=mifos-bank-1&ci=123&am=100&cn=John&an=ACC001&ai=456&cu=USD&oi=1&pi=2&ae=EXT123
 * ```
 *
 * Type values (qt):
 * - qt=0 → INTRA_BANK
 * - qt=1 → INTER_BANK
 * - qt=2 → BENEFICIARY
 * - qt=3 → MERCHANT
 *
 * The fsp (FSP ID) field enables automatic routing:
 * - If scanner's fspId == QR's fspId → Intra-bank transfer
 * - If scanner's fspId != QR's fspId → Inter-bank transfer
 */
@OptIn(ExperimentalEncodingApi::class)
object MpayQrCodeProcessor {
    // Regex patterns for validation
    private val CURRENCY_PATTERN = Regex("^[A-Z]{3}$")

    // Maximum allowed amount
    private const val MAX_AMOUNT = 100000.0

    // Protocol constants
    private const val MPAY_PROTOCOL = "mpay://pay"
    private const val UPI_PROTOCOL = "upi://pay" // For backwards compatibility

    /**
     * Encodes MPay payment details into a Base64 encoded string.
     *
     * Supports multiple QR formats based on [QrCodeData.type]:
     * - INTRA_BANK: Contains internal Mifos IDs (clientId, accountId > 0)
     * - INTER_BANK: Contains only accountExternalId (simplified format)
     * - BENEFICIARY: For adding beneficiaries
     * - MERCHANT: For merchant payments
     *
     * @param qrCodeData MPay payment request details
     * @return Base64 encoded MPay payment string
     * @throws IllegalArgumentException for invalid input
     */
    fun encodeMpayString(qrCodeData: QrCodeData): String {
        // Validate input data
        validate(qrCodeData)

        Logger.d { "QR Encode - type: ${qrCodeData.type}, fspId: ${qrCodeData.fspId}, clientId: ${qrCodeData.clientId}, accountId: ${qrCodeData.accountId}, officeId: ${qrCodeData.officeId}" }

        // Build MPay string based on type
        val requestPaymentString = when (qrCodeData.type) {
            QrCodeType.INTER_BANK -> {
                // Simplified inter-bank format: fspId + accountExternalId + optional amount/currency
                buildString {
                    append(MPAY_PROTOCOL)
                    append("?qt=${qrCodeData.type.ordinal}")
                    append("&fsp=${qrCodeData.fspId ?: ""}")
                    append("&ae=${qrCodeData.accountExternalId ?: ""}")
                    append("&cn=${qrCodeData.clientName}")
                    append("&am=${qrCodeData.amount}")
                    append("&cu=${qrCodeData.currency}")
                }
            }

            else -> {
                // Full format for intra-bank and other types
                buildString {
                    append(MPAY_PROTOCOL)
                    append("?qt=${qrCodeData.type.ordinal}")
                    append("&fsp=${qrCodeData.fspId ?: ""}")
                    append("&ci=${qrCodeData.clientId}")
                    append("&am=${qrCodeData.amount}")
                    append("&cn=${qrCodeData.clientName}")
                    append("&an=${qrCodeData.accountNo}")
                    append("&ai=${qrCodeData.accountId}")
                    append("&cu=${qrCodeData.currency}")
                    append("&oi=${qrCodeData.officeId}")
                    append("&on=${qrCodeData.officeName}")
                    append("&pi=${qrCodeData.accountTypeId}")
                    append("&ae=${qrCodeData.accountExternalId ?: ""}")
                }
            }
        }

        return requestPaymentString.encodeBase64()
    }

    /**
     * Decodes a Base64 encoded MPay or UPI payment string.
     *
     * Supports both new mpay:// and legacy upi:// formats for backwards compatibility.
     *
     * @param encodedString Base64 encoded payment string
     * @return Decoded QrCodeData object
     * @throws IllegalArgumentException for invalid encoded string
     */
    fun decodeMpayString(encodedString: String): QrCodeData {
        Logger.d { "QR Decode START - raw input length: ${encodedString.length}" }
        Logger.d { "QR Decode - raw input (first 80): ${encodedString.take(80)}" }

        // Normalize the input: trim whitespace and handle platform differences
        // Some QR scanners may add whitespace or newlines
        val normalizedInput = encodedString.trim()
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "")

        Logger.d { "QR Decode - normalized length: ${normalizedInput.length}" }
        Logger.d { "QR Decode - normalized (first 80): ${normalizedInput.take(80)}" }

        // Decode the Base64 string
        val decodedString = try {
            normalizedInput.decodeBase64String()
        } catch (e: Exception) {
            Logger.e { "QR Decode - Base64 decode FAILED: ${e.message}" }
            Logger.e { "QR Decode - Attempting without normalization..." }
            // Try original input as fallback
            encodedString.trim().decodeBase64String()
        }
        Logger.d { "QR Decode - decodedString: $decodedString" }

        // Parse based on protocol (support both mpay:// and upi:// for backwards compat)
        val params = when {
            decodedString.startsWith(MPAY_PROTOCOL) -> parseMpayString(decodedString, MPAY_PROTOCOL)
            decodedString.startsWith(UPI_PROTOCOL) -> parseMpayString(decodedString, UPI_PROTOCOL)
            else -> throw IllegalArgumentException("Invalid QR code format: unknown protocol")
        }

        // Get FSP ID for bank routing
        val fspId = params["fsp"]?.takeIf { it.isNotBlank() }

        // Get accountExternalId for inter-bank QR support
        val accountExternalId = params["ae"]?.takeIf { it.isNotBlank() }

        // Get phoneNumber for legacy inter-bank QR support
        val phoneNumber = params["pn"]?.takeIf { it.isNotBlank() }

        // For inter-bank QR codes, clientId and accountId may be 0
        val clientId = params["ci"]?.toLongOrNull() ?: 0L
        val accountId = params["ai"]?.toLongOrNull() ?: 0L
        Logger.d { "QR Decode - fspId: $fspId, clientId: $clientId, accountId: $accountId" }

        // Determine QR type - check qt field first, then infer from fields
        val type = params["qt"]?.toIntOrNull()?.let { QrCodeType.fromOrdinal(it) }
            ?: inferTypeFromFields(clientId, accountId, accountExternalId, fspId)

        // Validate: must have either internal IDs or accountExternalId for inter-bank
        val hasInternalIds = clientId > 0 && accountId > 0
        val hasAccountExternalId = accountExternalId != null

        if (!hasInternalIds && !hasAccountExternalId) {
            throw IllegalArgumentException("QR code missing required data: needs internal IDs or accountExternalId")
        }

        // Create QrCodeData
        val requestQrData = QrCodeData(
            type = type,
            fspId = fspId,
            clientId = clientId,
            clientName = params["cn"] ?: "",
            accountNo = params["an"] ?: "",
            amount = params["am"] ?: "",
            accountId = accountId,
            currency = params["cu"] ?: QrCodeData.DEFAULT_CURRENCY,
            officeId = params["oi"]?.toLongOrNull() ?: QrCodeData.OFFICE_ID,
            officeName = params["on"] ?: "",
            accountTypeId = params["pi"]?.toLongOrNull() ?: QrCodeData.ACCOUNT_TYPE_ID,
            phoneNumber = phoneNumber,
            accountExternalId = accountExternalId,
        )

        // Validate the created object (only for intra-bank with internal IDs)
        if (hasInternalIds) {
            validate(requestQrData)
        }

        return requestQrData
    }

    /**
     * Infers the QR type from fields when qt parameter is not present (legacy QR codes).
     * Note: The actual intra/inter bank routing is determined by comparing fspId,
     * not by this inferred type. This is only for legacy QR code support.
     */
    private fun inferTypeFromFields(
        clientId: Long,
        accountId: Long,
        accountExternalId: String?,
        fspId: String?,
    ): QrCodeType {
        return when {
            // If has internal IDs, likely intra-bank
            clientId > 0 && accountId > 0 -> QrCodeType.INTRA_BANK
            // If only has accountExternalId (no internal IDs), likely inter-bank
            accountExternalId != null && clientId == 0L -> QrCodeType.INTER_BANK
            // Default to intra-bank
            else -> QrCodeType.INTRA_BANK
        }
    }

    /**
     * Validates the QrCodeData
     * @param data MPay payment request details to validate
     * @throws IllegalArgumentException for any validation failures
     */
    private fun validate(data: QrCodeData) {
        // Validation depends on QR type
        when (data.type) {
            QrCodeType.INTER_BANK -> {
                // Inter-bank QR requires accountExternalId
                require(!data.accountExternalId.isNullOrBlank()) {
                    "Account external ID is required for inter-bank QR"
                }
            }

            else -> {
                // Intra-bank and other types require account details
                require(data.clientName.isNotBlank()) {
                    "Client name cannot be empty"
                }

                require(data.clientName.length <= 50) {
                    "Client name too long (max 50 characters)"
                }

                require(data.accountNo.isNotBlank()) {
                    "Account number cannot be empty"
                }
            }
        }

        // Optional amount validation (if not empty)
        if (data.amount.isNotEmpty()) {
            val amountValue = data.amount.toDoubleOrNull()
                ?: throw IllegalArgumentException("Invalid amount format")

            require(amountValue > 0) {
                "Amount must be positive"
            }
            require(amountValue <= MAX_AMOUNT) {
                "Amount exceeds maximum limit of $MAX_AMOUNT"
            }
        }

        // Currency validation
        require(CURRENCY_PATTERN.matches(data.currency)) {
            "Invalid currency code. Must be 3 uppercase letters"
        }
    }

    /**
     * Parses the MPay/UPI payment string into a map of parameters
     * @param paymentString Decoded payment string
     * @param protocol The protocol prefix to strip (mpay://pay or upi://pay)
     * @return Map of parameter keys and values
     */
    private fun parseMpayString(paymentString: String, protocol: String): Map<String, String> {
        return paymentString
            .substringAfter("$protocol?")
            .split("&")
            .mapNotNull { param ->
                val separatorIndex = param.indexOf('=')
                if (separatorIndex > 0) {
                    val key = param.substring(0, separatorIndex)
                    val value = param.substring(separatorIndex + 1)
                    key to value
                } else {
                    null
                }
            }
            .toMap()
    }
}
