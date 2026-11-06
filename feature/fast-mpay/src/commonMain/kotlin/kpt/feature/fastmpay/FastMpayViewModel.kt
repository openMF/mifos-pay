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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.data.util.MpayQrCodeProcessor
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.fastmpay.model.QrProcessResult
import kpt.feature.fastmpay.navigation.QR_DATA_ARG
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel for fast MPay QR code processing.
 *
 * Receives encoded QR data from navigation, decodes it,
 * processes it using [FastMpayProcessor], and emits the navigation result.
 *
 * The decode/process pass is kicked off from `init` through the MVI [FastMpayAction]
 * channel. The [resultFlow]/[errorFlow]/[isLoading] surfaces stay plain [MutableStateFlow]
 * state the screen collects and reacts to (clearing them after consumption).
 */
class FastMpayViewModel(
    savedStateHandle: SavedStateHandle,
    private val processor: FastMpayProcessor,
) : BaseViewModel<FastMpayState, FastMpayEvent, FastMpayAction>(
    initialState = FastMpayState,
) {

    private val _resultFlow = MutableStateFlow<QrProcessResult?>(null)
    val resultFlow = _resultFlow.asStateFlow()

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow = _errorFlow.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        val encodedQrData = savedStateHandle.get<String>(QR_DATA_ARG)
        trySendAction(FastMpayAction.ProcessQrData(encodedQrData))
    }

    override fun handleAction(action: FastMpayAction) {
        when (action) {
            is FastMpayAction.ProcessQrData -> processQrData(action.encodedQrData)
        }
    }

    private fun processQrData(encodedQrData: String?) {
        viewModelScope.launch {
            if (encodedQrData == null) {
                _errorFlow.update { "QR data is missing" }
                _isLoading.update { false }
                return@launch
            }

            try {
                val qrData = MpayQrCodeProcessor.decodeMpayString(encodedQrData)
                // Phase-5 Batch-3: processor now consumes the store-backed
                // `getBeneficiaryListScreen(clientId, scope)` (batch-1 reuse)
                // and `getOfficesScreen(scope)` (batch-3 new), so it needs the
                // caller's CoroutineScope for Store5 refresh-trigger wiring.
                val result = processor.processQrCode(qrData, viewModelScope)
                _resultFlow.update { result }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorFlow.update { e.message ?: "Failed to process QR code" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    /**
     * Clear the result to reset state.
     */
    fun clearResult() {
        _resultFlow.update { null }
    }

    /**
     * Clear the error to reset state.
     */
    fun clearError() {
        _errorFlow.update { null }
    }
}

data object FastMpayState

sealed interface FastMpayEvent

sealed interface FastMpayAction {
    data class ProcessQrData(val encodedQrData: String?) : FastMpayAction
}
