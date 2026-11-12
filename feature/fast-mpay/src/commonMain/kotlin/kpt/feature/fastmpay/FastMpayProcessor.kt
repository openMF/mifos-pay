/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.fastmpay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kpt.core.common.ScreenState
import kpt.core.data.fork.util.toForkScreenStateFlow
import kpt.core.data.repository.OfficeRepository
import kpt.core.data.repository.SelfServiceRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.utils.QrCodeData
import kpt.core.model.utils.QrCodeType
import kpt.feature.fastmpay.model.QrProcessResult
/**
 * Central processor for QR code data.
 *
 * Receives [QrCodeData] and determines the appropriate navigation target
 * based on [QrCodeData.type].
 *
 * Phase-5 Batch-3 rewiring:
 * - The beneficiary read used to walk `BeneficiaryRepository.getBeneficiaryList()`
 *   (a non-store transitional shim). It now walks
 *   [SelfServiceRepository.getBeneficiaryListStream] — the Phase-5 Batch-1
 *   store-backed reader (offline-first via the `wallet_beneficiaries` Room SoT,
 *   CACHE_FIRST_SWR band) — so the beneficiary lookup is offline-first + cheap
 *   on repeated QR scans within the freshness band. No new BeneficiaryStore is
 *   emitted for the fast-mpay surface; the batch-1 store is REUSED.
 * - The office read used to walk `OfficeRepository.getOffices()` (a non-store
 *   transitional shim). It now walks [OfficeRepository.getOfficesStream] — the
 *   Phase-5 Batch-3 store-backed reader (offline-first via `wallet_offices`
 *   Room SoT, 60-minute TTL because office reference data is effectively
 *   static). One cache slot per app install (singleton-keyed on `OfficesKey`).
 *
 * Both reads take the caller's `scope` so Store5 can subscribe its refresh
 * trigger to the caller's lifecycle. `processQrCode(qrData, scope)` therefore
 * exposes a `scope` parameter, and the caller ([FastMpayViewModel]) passes its
 * `viewModelScope`.
 *
 * @param selfServiceRepository Repository for reading the beneficiary list via
 *   the store-backed screen stream (batch-1 `AppStoreRegistry.Beneficiary`).
 * @param userPreferencesRepository Repository for user preferences including
 *   current FSP and the current logged-in clientId (used as the beneficiary /
 *   offices store cache key).
 * @param officeRepository Repository for resolving office names from IDs, via
 *   the store-backed screen stream (batch-3 `AppStoreRegistry.Offices`).
 */
class FastMpayProcessor(
    private val selfServiceRepository: SelfServiceRepository,
    private val userPreferencesRepository: ForkPreferencesRepository,
    private val officeRepository: OfficeRepository,
) {

    /** Constants for [FastMpayProcessor]. */
    companion object {
        /** Office name used when no specific office is resolved. */
        const val DEFAULT_OFFICE_NAME = "Head Office"
    }

    /**
     * Process QR code data and determine navigation target.
     *
     * For INTRA_BANK type, this checks if the beneficiary already exists
     * by matching accountNumber. If found, navigates to MakeTransfer,
     * otherwise navigates to AddBeneficiary.
     *
     * @param qrData The decoded QR code data.
     * @param scope The caller's [CoroutineScope] (typically `viewModelScope`).
     *   Store5's [SelfServiceRepository.getBeneficiaryListStream] and
     *   [OfficeRepository.getOfficesStream] subscribe their internal refresh
     *   triggers to this scope.
     * @return [QrProcessResult] indicating where to navigate.
     */
    suspend fun processQrCode(qrData: QrCodeData, scope: CoroutineScope): QrProcessResult {
        return when (qrData.type) {
            QrCodeType.INTRA_BANK -> {
                processIntraBankQr(qrData, scope)
            }

            QrCodeType.INTER_BANK -> {
                // Inter-bank: Has accountExternalId -> Go to inter-bank transfer
                val accountExternalId = qrData.accountExternalId
                if (accountExternalId.isNullOrBlank()) {
                    return QrProcessResult.Error("Inter-bank QR missing account external ID")
                }
                QrProcessResult.NavigateToInterbankTransfer(
                    accountExternalId = accountExternalId,
                    recipientName = qrData.clientName.takeIf { it.isNotBlank() },
                    amount = qrData.amount.takeIf { it.isNotBlank() },
                )
            }

            QrCodeType.BENEFICIARY -> {
                // Beneficiary: Pre-fill beneficiary form
                val officeName = resolveOfficeName(qrData, scope)
                val beneficiaryJson = convertToBeneficiaryJson(qrData, officeName)
                val qrDataJson = Json.encodeToString(QrCodeData.serializer(), qrData)
                QrProcessResult.NavigateToAddBeneficiary(
                    beneficiaryData = beneficiaryJson,
                    sourceQrType = QrCodeType.BENEFICIARY,
                    sourceQrData = qrDataJson,
                )
            }

            QrCodeType.MERCHANT -> {
                // Merchant: Navigate to merchant payment
                QrProcessResult.NavigateToMerchantPayment(qrData)
            }
        }
    }

    /**
     * Process Intra-bank QR code.
     *
     * First checks if the QR code belongs to the same bank (FSP).
     * If different bank, returns BankMismatch result.
     * If same bank, checks if beneficiary already exists by matching accountNumber
     * (reading through the batch-1 `AppStoreRegistry.Beneficiary` store-backed
     * `SelfServiceRepository.getBeneficiaryListStream(clientId, scope)`).
     * If found, navigates to MakeTransfer with the existing beneficiary.
     * If not found, navigates to AddBeneficiary with pre-filled data.
     */
    private suspend fun processIntraBankQr(
        qrData: QrCodeData,
        scope: CoroutineScope,
    ): QrProcessResult {
        // Check for bank mismatch first
        val currentFspId = userPreferencesRepository.selectedInstance.value?.platformTenantId
        val qrFspId = qrData.fspId

        if (qrFspId != null && currentFspId != null &&
            !qrFspId.equals(currentFspId, ignoreCase = true)
        ) {
            return QrProcessResult.BankMismatch(
                qrData = qrData,
                currentBankId = currentFspId,
                qrBankId = qrFspId,
            )
        }

        // Resolve office name early for beneficiary creation
        val officeName = resolveOfficeName(qrData, scope)

        return try {
            // Read the beneficiary list through the batch-1 store-backed reader
            // (SelfServiceRepository.getBeneficiaryListStream). ScreenState
            // discipline: skip Loading; treat Empty as "no existing beneficiary"
            // (fall through to AddBeneficiary); Content carries the list.
            // Error/NoNetwork/Unauthenticated fall back to AddBeneficiary — the
            // pre-store shape did the same on any non-Success emission.
            val clientId = userPreferencesRepository.clientId.value

            if (clientId == null) {
                // No logged-in clientId available — cannot address the
                // per-client beneficiary cache. Fall through to AddBeneficiary.
                buildAddBeneficiaryResult(qrData, officeName, QrCodeType.INTRA_BANK)
            } else {
                val beneficiaryResult = selfServiceRepository
                    .getBeneficiaryListStream(clientId = clientId, scope = scope)
                    .state.toForkScreenStateFlow()
                    .first { it !is ScreenState.Loading }

                when (beneficiaryResult) {
                    is ScreenState.Content -> {
                        val existingBeneficiary = beneficiaryResult.data.find {
                            it.accountNumber == qrData.accountNo
                        }

                        if (existingBeneficiary != null) {
                            // Beneficiary exists -> Navigate to MakeTransfer directly
                            QrProcessResult.NavigateToMakeTransfer(
                                qrData = qrData,
                                beneficiaryName = existingBeneficiary.clientName,
                            )
                        } else {
                            // Beneficiary doesn't exist -> Navigate to AddBeneficiary
                            buildAddBeneficiaryResult(qrData, officeName, QrCodeType.INTRA_BANK)
                        }
                    }

                    // Empty page = "no beneficiaries yet" — proceed to
                    // AddBeneficiary just like the pre-store shape treated
                    // an empty Success list.
                    is ScreenState.Empty,
                    is ScreenState.Error,
                    is ScreenState.NoNetwork,
                    is ScreenState.Unauthenticated,
                    is ScreenState.Loading, // unreachable — filtered above
                    -> buildAddBeneficiaryResult(qrData, officeName, QrCodeType.INTRA_BANK)
                }
            }
        } catch (e: Exception) {
            // On exception, fallback to add beneficiary flow
            buildAddBeneficiaryResult(qrData, officeName, QrCodeType.INTRA_BANK)
        }
    }

    /**
     * Resolves office name from QR data.
     *
     * Uses officeName directly from QR if available (new QR format).
     * Falls back to looking up by officeId for backward compatibility (old QR format),
     * consulting the batch-3 store-backed `OfficeRepository.getOfficesStream(scope)`.
     *
     * @param qrData The QR code data containing office info.
     * @param scope The caller's [CoroutineScope] — threaded to the store-backed
     *   offices reader.
     * @return The office name from QR, or resolved from officeId, or [DEFAULT_OFFICE_NAME].
     */
    private suspend fun resolveOfficeName(
        qrData: QrCodeData,
        scope: CoroutineScope,
    ): String {
        // Use officeName directly if available (new QR format)
        if (qrData.officeName.isNotBlank()) {
            return qrData.officeName
        }

        // Fallback: resolve from officeId (backward compatibility with old QR codes),
        // reading through the batch-3 offices store.
        return try {
            val result = officeRepository.getOfficesStream(scope)
                .state.toForkScreenStateFlow()
                .first { it !is ScreenState.Loading }
            when (result) {
                is ScreenState.Content -> {
                    result.data.find { it.id == qrData.officeId }?.name
                        ?: DEFAULT_OFFICE_NAME
                }
                // Empty / Error / NoNetwork / Unauthenticated / Loading (unreachable)
                // all fall through to the default name — same as the pre-store shape.
                else -> DEFAULT_OFFICE_NAME
            }
        } catch (e: Exception) {
            DEFAULT_OFFICE_NAME
        }
    }

    /**
     * Builds a NavigateToAddBeneficiary result from the QR data + resolved office
     * name. Extracted so the multiple fallback paths in [processIntraBankQr]
     * (empty list, error, exception) share the same construction.
     */
    private fun buildAddBeneficiaryResult(
        qrData: QrCodeData,
        officeName: String,
        sourceType: QrCodeType,
    ): QrProcessResult.NavigateToAddBeneficiary {
        val beneficiaryJson = convertToBeneficiaryJson(qrData, officeName)
        val qrDataJson = Json.encodeToString(QrCodeData.serializer(), qrData)
        return QrProcessResult.NavigateToAddBeneficiary(
            beneficiaryData = beneficiaryJson,
            sourceQrType = sourceType,
            sourceQrData = qrDataJson,
        )
    }

    /**
     * Converts QrCodeData to Beneficiary JSON string.
     *
     * @param qrData The QR code data containing beneficiary info
     * @param officeName The resolved office name (from API lookup)
     */
    private fun convertToBeneficiaryJson(qrData: QrCodeData, officeName: String): String {
        val beneficiary = Beneficiary(
            name = qrData.clientName,
            clientName = qrData.clientName,
            accountNumber = qrData.accountNo,
            accountType = Beneficiary.AccountType(
                id = qrData.accountTypeId.toInt(),
                code = "savings",
                value = "Savings",
            ),
            officeName = officeName,
            officeId = qrData.officeId,
            transferLimit = 0,
        )
        return Json.encodeToString(Beneficiary.serializer(), beneficiary)
    }
}
