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

import co.touchlab.kermit.Logger
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
/**
 * Routes QR code scans to appropriate transfer flow based on:
 * 1. FSP ID comparison (same bank vs different bank)
 * 2. QR code type (explicit INTER_BANK vs INTRA_BANK)
 *
 * Routing Logic:
 * - If QR's fspId matches scanner's fspId → Intra-bank transfer
 * - If QR's fspId differs from scanner's fspId → Inter-bank transfer
 * - If QR type is explicitly INTER_BANK → Inter-bank transfer
 * - Fallback: Use internal IDs presence to determine type
 */
class QrTransferRouter(
    private val userPreferencesRepository: ForkPreferencesRepository,
) {

    /**
     * Determines the appropriate transfer route for a scanned QR code.
     *
     * @param qrData The decoded QR code data
     * @return The routing result indicating which transfer flow to use
     */
    fun routeQrScan(qrData: QrCodeData): QrRouteResult {
        val currentFspId = userPreferencesRepository.selectedInstance.value?.platformTenantId
        val qrFspId = qrData.fspId

        Logger.d { "QR Route - currentFspId: $currentFspId, qrFspId: $qrFspId, qrType: ${qrData.type}" }

        // Step 1: Determine if same bank or different bank
        val isSameBank = determineIsSameBank(qrData, currentFspId, qrFspId)

        Logger.d { "QR Route - isSameBank: $isSameBank" }

        // Step 2: Route accordingly
        return if (isSameBank) {
            routeIntraBank(qrData)
        } else {
            routeInterBank(qrData)
        }
    }

    /**
     * Determines if the QR code belongs to the same bank as the scanner.
     */
    private fun determineIsSameBank(
        qrData: QrCodeData,
        currentFspId: String?,
        qrFspId: String?,
    ): Boolean {
        return when {
            // If QR type is explicitly INTER_BANK, always route to inter-bank
            qrData.type == QrCodeType.INTER_BANK -> false

            // If both fspIds are present, compare them directly
            qrFspId != null && currentFspId != null -> {
                qrFspId.equals(currentFspId, ignoreCase = true)
            }

            // If QR has internal IDs (clientId > 0, accountId > 0), assume same bank
            // This is for legacy QR codes without fspId
            qrData.clientId > 0 && qrData.accountId > 0 -> true

            // If only accountExternalId is present (no internal IDs), assume inter-bank
            qrData.accountExternalId != null && qrData.clientId == 0L -> false

            // Default: assume same bank for backward compatibility
            else -> true
        }
    }

    /**
     * Routes to intra-bank transfer flow.
     */
    private fun routeIntraBank(qrData: QrCodeData): QrRouteResult {
        // Validate required fields for intra-bank transfer
        if (qrData.clientId <= 0 || qrData.accountId <= 0) {
            Logger.w { "QR Route - Intra-bank but missing internal IDs, falling back to inter-bank" }
            return routeInterBank(qrData)
        }

        Logger.d { "QR Route - Intra-bank transfer to clientId: ${qrData.clientId}, accountId: ${qrData.accountId}" }

        return QrRouteResult.IntraBank(qrData)
    }

    /**
     * Routes to inter-bank transfer flow.
     */
    private fun routeInterBank(qrData: QrCodeData): QrRouteResult {
        val accountExternalId = qrData.accountExternalId

        if (accountExternalId.isNullOrBlank()) {
            Logger.w { "QR Route - Inter-bank but no accountExternalId" }
            return QrRouteResult.Error("Inter-bank QR missing account identifier")
        }

        Logger.d { "QR Route - Inter-bank transfer to accountExternalId: $accountExternalId" }

        return QrRouteResult.InterBank(
            accountExternalId = accountExternalId,
            recipientName = qrData.clientName.takeIf { it.isNotBlank() },
            amount = qrData.amount.takeIf { it.isNotBlank() },
            currency = qrData.currency,
        )
    }
}

/**
 * Result of QR code routing decision.
 */
sealed interface QrRouteResult {

    /**
     * Intra-bank transfer (same bank).
     * Use internal Fineract API for direct transfer.
     *
     * @property qrData The full QR code data with internal IDs
     */
    data class IntraBank(
        val qrData: QrCodeData,
    ) : QrRouteResult

    /**
     * Inter-bank transfer (different bank).
     * Use payment hub (Mojaloop) for cross-bank transfer.
     *
     * @property accountExternalId External account ID for participant lookup
     * @property recipientName Optional recipient name from QR
     * @property amount Optional pre-filled amount from QR
     * @property currency Currency code
     */
    data class InterBank(
        val accountExternalId: String,
        val recipientName: String?,
        val amount: String?,
        val currency: String,
    ) : QrRouteResult

    /**
     * Error during routing.
     *
     * @property message Error description
     */
    data class Error(val message: String) : QrRouteResult
}
