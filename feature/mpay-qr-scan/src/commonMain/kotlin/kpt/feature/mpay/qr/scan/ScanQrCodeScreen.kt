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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.model.utils.QrCodeData
import kpt.feature.mpay.qr.scan.components.QrActionButton
import kpt.feature.mpay.qr.scan.components.QrHelpDialog
import kpt.feature.mpay.qr.scan.components.QrProcessingOverlay
import kpt.feature.mpay.qr.scan.components.QrScanFooter
import kpt.feature.mpay.qr.scan.components.QrScanTopBar
import kpt.feature.mpay.qr.scan.components.QrViewfinder
import kpt.feature.mpay.qr.scan.generated.resources.Res
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_no_qr_found
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_scan_success
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_torch
import kpt.feature.mpay.qr.scan.generated.resources.feature_qr_upload_qr
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun ScanQrCodeScreen(
    navigateBack: () -> Unit,
    navigateToIntraBankTransfer: (QrCodeData) -> Unit,
    navigateToInterbankTransfer: (accountExternalId: String, recipientName: String, amount: String) -> Unit,
    navigateToAddBeneficiaryScreen: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScanQrViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val isTorchEnabled by viewModel.isTorchEnabled.collectAsStateWithLifecycle()
    val showHelpDialog by viewModel.showHelpDialog.collectAsStateWithLifecycle()
    val isProcessingImage by viewModel.isProcessingImage.collectAsStateWithLifecycle()
    val selectedImageBytes by viewModel.selectedImageBytes.collectAsStateWithLifecycle()

    val scanSuccessMessage = stringResource(Res.string.feature_qr_scan_success)
    val noQrFoundMessage = stringResource(Res.string.feature_qr_no_qr_found)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ScanQrEvent.OnNavigateToIntraBankTransfer -> {
                    navigateToIntraBankTransfer.invoke(event.qrData)
                }

                is ScanQrEvent.OnNavigateToInterbankTransfer -> {
                    navigateToInterbankTransfer.invoke(
                        event.accountExternalId,
                        event.recipientName,
                        event.amount,
                    )
                }

                is ScanQrEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is ScanQrEvent.OnNavigateToAddBeneficiary -> {
                    navigateToAddBeneficiaryScreen.invoke(event.beneficiary)
                }

                is ScanQrEvent.OnScanSuccess -> {
                    snackbarHostState.showSnackbar(scanSuccessMessage)
                }

                is ScanQrEvent.OnNoQrFound -> {
                    snackbarHostState.showSnackbar(noQrFoundMessage)
                }
            }
        }
    }

    ScanQrCodeScreenContent(
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        isTorchEnabled = isTorchEnabled,
        showHelpDialog = showHelpDialog,
        isProcessingImage = isProcessingImage,
        selectedImageBytes = selectedImageBytes,
        onScanned = viewModel::onScanned,
        onImageQrScanned = viewModel::onImageQrScanned,
        onSetProcessingImage = viewModel::setProcessingImage,
        onSetSelectedImageBytes = viewModel::setSelectedImageBytes,
        onToggleTorch = viewModel::toggleTorch,
        onShowHelpDialog = viewModel::showHelpDialog,
        onDismissHelpDialog = viewModel::hideHelpDialog,
        navigateBack = navigateBack,
    )
}

/** Stateless content of the scan-QR screen: camera viewfinder, torch and help controls, and gallery image import. */
@Composable
fun ScanQrCodeScreenContent(
    snackbarHostState: SnackbarHostState,
    isTorchEnabled: Boolean,
    showHelpDialog: Boolean,
    isProcessingImage: Boolean,
    selectedImageBytes: ByteArray?,
    onScanned: (String) -> Boolean,
    onImageQrScanned: (String?) -> Unit,
    onSetProcessingImage: (Boolean) -> Unit,
    onSetSelectedImageBytes: (ByteArray?) -> Unit,
    onToggleTorch: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onDismissHelpDialog: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isTorchAvailable by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Check if we're on web desktop - if so, only show file picker UI
    val isWebDesktop = WebPlatform.isWebDesktop

    // FileKit image picker launcher
    val imagePicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
    ) { file ->
        if (file != null) {
            scope.launch {
                onSetProcessingImage(true)
                try {
                    val imageBytes = file.readBytes()
                    onSetSelectedImageBytes(imageBytes)
                    val qrData = decodeQrFromFile(file)
                    onImageQrScanned(qrData)
                } catch (e: Exception) {
                    onImageQrScanned(null)
                } finally {
                    onSetProcessingImage(false)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Camera preview / QR Scanner (full screen)
        // On web desktop, this shows file picker UI; on mobile/native, shows camera
        QrScannerWithPermissions(
            types = listOf(CodeType.QR),
            modifier = Modifier.fillMaxSize(),
            isTorchEnabled = isTorchEnabled,
            onTorchAvailabilityChanged = { available ->
                isTorchAvailable = available
            },
            onScanned = onScanned,
            onUploadQr = { imagePicker.launch() },
        )

        // Only show scanner-specific overlays on non-web-desktop platforms
        if (!isWebDesktop) {
            // Viewfinder overlay with animation
            QrViewfinder(
                modifier = Modifier.fillMaxSize(),
            )

            // Action buttons and footer (bottom aligned)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                ) {
                    QrActionButton(
                        icon = ForkIcons.PhotoLibrary,
                        label = stringResource(Res.string.feature_qr_upload_qr),
                        onClick = { imagePicker.launch() },
                    )

                    QrActionButton(
                        icon = if (isTorchEnabled) ForkIcons.FlashOn else ForkIcons.FlashOff,
                        label = stringResource(Res.string.feature_qr_torch),
                        onClick = onToggleTorch,
                        enabled = isTorchAvailable,
                    )
                }

                QrScanFooter()
            }
        }

        // Top bar - always show for consistent navigation
        QrScanTopBar(
            onCloseClick = navigateBack,
            onHelpClick = onShowHelpDialog,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Processing overlay
        if (isProcessingImage) {
            QrProcessingOverlay(
                imageBytes = selectedImageBytes,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isWebDesktop) 32.dp else 120.dp),
        )

        // Help dialog
        if (showHelpDialog) {
            QrHelpDialog(
                onDismiss = onDismissHelpDialog,
            )
        }
    }
}
