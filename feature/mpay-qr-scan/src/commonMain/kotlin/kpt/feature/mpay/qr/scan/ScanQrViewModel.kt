/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.mpay.qr.scan

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kpt.core.data.util.MpayQrCodeProcessor
import kpt.core.data.util.QrRouteResult
import kpt.core.data.util.QrTransferRouter
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.model.utils.QrCodeData
import kpt.core.ui.utils.BaseViewModel
/**
 * ViewModel for QR code scanning screen.
 *
 * Uses [QrTransferRouter] for smart routing:
 * - Compares FSP IDs to determine intra-bank vs inter-bank
 * - Routes to appropriate transfer flow based on the comparison
 *
 * One-shot navigation/toast signals are emitted through the MVI [ScanQrEvent] channel
 * (consumed via `EventsEffect`). The simple camera-UI toggles (torch, help dialog, image
 * processing) remain plain [StateFlow] surfaces the screen binds directly.
 */
class ScanQrViewModel(
    private val qrTransferRouter: QrTransferRouter,
) : BaseViewModel<ScanQrState, ScanQrEvent, ScanQrAction>(
    initialState = ScanQrState,
) {

    private val _isTorchEnabled = MutableStateFlow(false)

    /** Whether the camera torch is switched on. */
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _showHelpDialog = MutableStateFlow(false)

    /** Whether the QR help dialog is visible. */
    val showHelpDialog: StateFlow<Boolean> = _showHelpDialog.asStateFlow()

    private val _isProcessingImage = MutableStateFlow(false)

    /** Whether an imported QR image is currently being decoded. */
    val isProcessingImage: StateFlow<Boolean> = _isProcessingImage.asStateFlow()

    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)

    /** Bytes of the image picked from the gallery for QR decoding, or null when none is selected. */
    val selectedImageBytes: StateFlow<ByteArray?> = _selectedImageBytes.asStateFlow()

    override fun handleAction(action: ScanQrAction) {
        when (action) {
            is ScanQrAction.OnImageQrScanned -> {
                _isProcessingImage.value = false
                if (action.data != null) {
                    sendEvent(ScanQrEvent.OnScanSuccess)
                    onScanned(action.data)
                } else {
                    _selectedImageBytes.value = null
                    sendEvent(ScanQrEvent.OnNoQrFound)
                }
            }
        }
    }

    /** Flips the torch on or off. */
    fun toggleTorch() {
        _isTorchEnabled.update { !it }
    }

    /** Sets the torch state explicitly. */
    fun setTorchEnabled(enabled: Boolean) {
        _isTorchEnabled.value = enabled
    }

    /** Shows the QR help dialog. */
    fun showHelpDialog() {
        _showHelpDialog.value = true
    }

    /** Hides the QR help dialog. */
    fun hideHelpDialog() {
        _showHelpDialog.value = false
    }

    /** Marks whether an imported image is being processed. */
    fun setProcessingImage(processing: Boolean) {
        _isProcessingImage.value = processing
    }

    /** Stores the bytes of the image picked for QR decoding. */
    fun setSelectedImageBytes(bytes: ByteArray?) {
        _selectedImageBytes.value = bytes
    }

    /** Clears the picked image and resets the processing flag. */
    fun clearSelectedImage() {
        _selectedImageBytes.value = null
        _isProcessingImage.value = false
    }

    /** Reports the result of decoding a QR from an imported image; [data] is null when no QR was found. */
    fun onImageQrScanned(data: String?) {
        trySendAction(ScanQrAction.OnImageQrScanned(data))
    }

    /**
     * Decodes a scanned QR string, routes it to the intra-bank, inter-bank or add-beneficiary flow
     * and returns whether it was handled.
     */
    fun onScanned(data: String): Boolean {
        Logger.d { "========== QR SCAN START ==========" }
        Logger.d { "QR Raw Data length: ${data.length}" }
        Logger.d { "QR Raw Data (first 100 chars): ${data.take(100)}" }
        Logger.d { "QR Raw Data (last 50 chars): ${data.takeLast(50)}" }

        return try {
            val qrCodeData = MpayQrCodeProcessor.decodeMpayString(data)

            Logger.d { "QR Decoded SUCCESS - fspId: ${qrCodeData.fspId}, type: ${qrCodeData.type}, clientId: ${qrCodeData.clientId}, accountId: ${qrCodeData.accountId}" }

            // Use smart routing based on FSP ID comparison
            val routeResult = qrTransferRouter.routeQrScan(qrCodeData)
            Logger.d { "QR Route result: $routeResult" }

            // Navigate based on routing result (don't emit OnScanSuccess before navigation
            // to avoid StateFlow conflation issues)
            when (routeResult) {
                is QrRouteResult.IntraBank -> {
                    Logger.d { "QR Route -> Intra-bank transfer, emitting event..." }
                    sendEvent(ScanQrEvent.OnNavigateToIntraBankTransfer(routeResult.qrData))
                    Logger.d { "QR Route -> Event emitted: OnNavigateToIntraBankTransfer" }
                }

                is QrRouteResult.InterBank -> {
                    Logger.d { "QR Route -> Inter-bank transfer to ${routeResult.accountExternalId}" }
                    sendEvent(
                        ScanQrEvent.OnNavigateToInterbankTransfer(
                            accountExternalId = routeResult.accountExternalId,
                            recipientName = routeResult.recipientName ?: "",
                            amount = routeResult.amount ?: "",
                        ),
                    )
                    Logger.d { "QR Route -> Event emitted: OnNavigateToInterbankTransfer" }
                }

                is QrRouteResult.Error -> {
                    Logger.w { "QR Route -> Error: ${routeResult.message}" }
                    sendEvent(ScanQrEvent.ShowToast(routeResult.message))
                }
            }

            Logger.d { "========== QR SCAN SUCCESS ==========" }
            true
        } catch (e: Exception) {
            Logger.e(e) { "QR decode FAILED with exception: ${e.message}" }
            Logger.e { "Exception type: ${e::class.simpleName}" }
            Logger.e { "Stack trace: ${e.stackTraceToString().take(500)}" }
            getQrCodeResult(data)
        }
    }

    private fun getQrCodeResult(data: String): Boolean {
        val trimmedData = data.trim()
        Logger.d { "QR scanned (fallback parsing): $data" }

        if (!trimmedData.startsWith("{") || !trimmedData.endsWith("}")) {
            sendEvent(ScanQrEvent.ShowToast("Scan a Valid QR Code"))
            return false
        }

        return try {
            val beneficiary = parseBeneficiaryFromJson(trimmedData)
            if (beneficiary != null) {
                val beneficiaryString = Json.encodeToString<Beneficiary>(beneficiary)
                sendEvent(ScanQrEvent.OnScanSuccess)
                sendEvent(ScanQrEvent.OnNavigateToAddBeneficiary(beneficiaryString))
                true
            } else {
                sendEvent(ScanQrEvent.ShowToast("Scan a Valid QR Code"))
                false
            }
        } catch (_: Exception) {
            sendEvent(ScanQrEvent.ShowToast("Scan a Valid QR Code"))
            false
        }
    }

    private fun parseBeneficiaryFromJson(jsonString: String): Beneficiary? {
        return try {
            Json.decodeFromString<Beneficiary>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}

/** Empty UI state; the screen has no state beyond the view model's flows. */
data object ScanQrState

/** One-shot events emitted by [ScanQrViewModel]. */
sealed interface ScanQrEvent {
    /**
     * Navigate to the intra-bank transfer flow.
     * @property qrData Decoded QR payload identifying the payee.
     */
    data class OnNavigateToIntraBankTransfer(val qrData: QrCodeData) : ScanQrEvent

    /**
     * Navigate to the inter-bank transfer flow.
     * @property accountExternalId External id of the destination account.
     * @property recipientName Name of the recipient.
     * @property amount Amount encoded in the QR code.
     */
    data class OnNavigateToInterbankTransfer(
        val accountExternalId: String,
        val recipientName: String,
        val amount: String,
    ) : ScanQrEvent

    /**
     * Navigate to the add-beneficiary screen.
     * @property beneficiary Serialized beneficiary taken from the QR code.
     */
    data class OnNavigateToAddBeneficiary(val beneficiary: String) : ScanQrEvent

    /**
     * Show a toast message.
     * @property message Text to display.
     */
    data class ShowToast(val message: String) : ScanQrEvent

    /** The QR code was scanned successfully. */
    data object OnScanSuccess : ScanQrEvent

    /** No QR code was found in the imported image. */
    data object OnNoQrFound : ScanQrEvent
}

/** Actions handled by [ScanQrViewModel]. */
sealed interface ScanQrAction {
    /**
     * An imported image has been decoded.
     * @property data Decoded QR text, or null if no QR code was found.
     */
    data class OnImageQrScanned(val data: String?) : ScanQrAction
}
