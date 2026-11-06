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
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _showHelpDialog = MutableStateFlow(false)
    val showHelpDialog: StateFlow<Boolean> = _showHelpDialog.asStateFlow()

    private val _isProcessingImage = MutableStateFlow(false)
    val isProcessingImage: StateFlow<Boolean> = _isProcessingImage.asStateFlow()

    private val _selectedImageBytes = MutableStateFlow<ByteArray?>(null)
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

    fun toggleTorch() {
        _isTorchEnabled.update { !it }
    }

    fun setTorchEnabled(enabled: Boolean) {
        _isTorchEnabled.value = enabled
    }

    fun showHelpDialog() {
        _showHelpDialog.value = true
    }

    fun hideHelpDialog() {
        _showHelpDialog.value = false
    }

    fun setProcessingImage(processing: Boolean) {
        _isProcessingImage.value = processing
    }

    fun setSelectedImageBytes(bytes: ByteArray?) {
        _selectedImageBytes.value = bytes
    }

    fun clearSelectedImage() {
        _selectedImageBytes.value = null
        _isProcessingImage.value = false
    }

    fun onImageQrScanned(data: String?) {
        trySendAction(ScanQrAction.OnImageQrScanned(data))
    }

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

data object ScanQrState

sealed interface ScanQrEvent {
    data class OnNavigateToIntraBankTransfer(val qrData: QrCodeData) : ScanQrEvent
    data class OnNavigateToInterbankTransfer(
        val accountExternalId: String,
        val recipientName: String,
        val amount: String,
    ) : ScanQrEvent
    data class OnNavigateToAddBeneficiary(val beneficiary: String) : ScanQrEvent
    data class ShowToast(val message: String) : ScanQrEvent
    data object OnScanSuccess : ScanQrEvent
    data object OnNoQrFound : ScanQrEvent
}

sealed interface ScanQrAction {
    data class OnImageQrScanned(val data: String?) : ScanQrAction
}
