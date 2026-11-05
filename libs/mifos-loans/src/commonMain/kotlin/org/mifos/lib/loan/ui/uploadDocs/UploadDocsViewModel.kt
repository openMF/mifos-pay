/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.ui.uploadDocs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kpt.core.base.common.toBase64DataUri
import mifos_pay.libs.mifos_loans.generated.resources.Res
import mifos_pay.libs.mifos_loans.generated.resources.feature_upload_file_error
import org.jetbrains.compose.resources.StringResource
import org.mifos.lib.loan.component.DocumentType
import org.mifos.lib.loan.component.SignatureUploadType
import org.mifospay.core.ui.utils.BaseViewModel
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * `ViewModel` for the Upload Documents screen.
 *
 * Handles local file selection for the bank statement, property document, and signature
 * required by the loan application, driven purely on-device via FileKit — there is no backend
 * call here (mirrors the source app, where document submission is not yet implemented).
 * Once all three are present, "Next" forwards the application form data carried in
 * [UploadDocsRoute] on to the Confirm Details step.
 *
 * @param savedStateHandle Handle used to read the [UploadDocsRoute] navigation arguments.
 */
@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalEncodingApi::class)
internal class UploadDocsViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<UploadDocsState, UploadDocsEvent, UploadDocsAction>(
    initialState = run {
        val route = savedStateHandle.toRoute<UploadDocsRoute>()
        UploadDocsState(
            clientId = route.clientId,
            productId = route.productId,
            applicantName = route.applicantName,
            loanProductName = route.loanProductName,
            loanPurpose = route.loanPurpose,
            disbursementDate = route.disbursementDate,
            principalAmount = route.principalAmount,
            providerId = route.providerId,
        )
    },
) {
    override fun handleAction(action: UploadDocsAction) {
        when (action) {
            is UploadDocsAction.OnNavigateBack -> {
                sendEvent(UploadDocsEvent.NavigateBack)
            }

            is UploadDocsAction.RemoveDocument -> {
                removeFile(action.type)
            }

            is UploadDocsAction.SelectNewDocument -> handleDocumentUpload(action.type)

            is UploadDocsAction.UploadDocument -> handleDocumentUpload(action.type)

            is UploadDocsAction.ShowSignatureUploadSheet -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = UploadDocumentDialog.ShowSignaturePicker(isSignatureMode = false),
                    )
                }
            }

            is UploadDocsAction.DismissDialog -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
            }

            is UploadDocsAction.UploadSignature -> handleUploadSignature(action)

            is UploadDocsAction.UploadSign -> {
                mutableStateFlow.update {
                    it.copy(
                        dialogState = null,
                        signatureDocumentFile = action.image,
                        signatureFileName = "signature.png",
                        signatureSize = null,
                    )
                }
            }

            is UploadDocsAction.NavigateToNextScreen -> {
                mutableStateFlow.update {
                    it.copy(dialogState = null)
                }
                sendEvent(
                    UploadDocsEvent.NavigateToConfirmDetails(
                        clientId = state.clientId,
                        productId = state.productId,
                        applicantName = state.applicantName,
                        loanProductName = state.loanProductName,
                        loanPurpose = state.loanPurpose,
                        disbursementDate = state.disbursementDate,
                        principalAmount = state.principalAmount,
                        providerId = state.providerId,
                    ),
                )
            }
        }
    }

    /**
     * Handles a user's request to upload a document by delegating to a specific
     * upload function based on the document type.
     *
     * @param type The [DocumentType] to be uploaded.
     */
    private fun handleDocumentUpload(type: DocumentType) {
        when (type) {
            DocumentType.BANK_STATEMENT -> uploadBankStatements()
            DocumentType.PROPERTY_DOCUMENT -> uploadPropertyDocument()
            DocumentType.SIGNATURE -> Unit
        }
    }

    /**
     * Removes a document of a specific type from the state.
     *
     * @param type The [DocumentType] of the document to be removed.
     */
    private fun removeFile(type: DocumentType) {
        when (type) {
            DocumentType.BANK_STATEMENT -> {
                mutableStateFlow.update {
                    it.copy(
                        bankStatementFile = null,
                        bankStatementFileName = null,
                        bankStatementSize = null,
                    )
                }
            }

            DocumentType.PROPERTY_DOCUMENT -> {
                mutableStateFlow.update {
                    it.copy(
                        propertyDocumentsFile = null,
                        propertyDocumentFileName = null,
                        propertyDocumentsSize = null,
                    )
                }
            }

            DocumentType.SIGNATURE -> {
                mutableStateFlow.update {
                    it.copy(
                        signatureDocumentFile = null,
                        signatureFileName = null,
                        signatureSize = null,
                    )
                }
            }
        }
    }

    /**
     * Launches a file picker to select a property document and updates the state
     * with the selected file's data and metadata.
     */
    private fun uploadPropertyDocument() {
        viewModelScope.launch {
            try {
                val image = FileKit.openFilePicker(type = FileKitType.Image)
                image?.let { file ->
                    mutableStateFlow.update {
                        it.copy(
                            propertyDocumentsFile = file.readBytes().toBase64DataUri(),
                            propertyDocumentFileName = file.name,
                            propertyDocumentsSize = formatFileSize(file.size()),
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleErrorMessage(Res.string.feature_upload_file_error)
            }
        }
    }

    /**
     * Launches a file picker to select a bank statement and updates the state
     * with the selected file's data and metadata.
     */
    private fun uploadBankStatements() {
        viewModelScope.launch {
            try {
                val image = FileKit.openFilePicker(type = FileKitType.Image)
                image?.let { file ->
                    mutableStateFlow.update {
                        it.copy(
                            bankStatementFile = file.readBytes().toBase64DataUri(),
                            bankStatementFileName = file.name,
                            bankStatementSize = formatFileSize(file.size()),
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleErrorMessage(Res.string.feature_upload_file_error)
            }
        }
    }

    /**
     * Handles the user's request to upload a signature by delegating to the appropriate
     * function based on the signature upload type.
     *
     * @param action The [UploadDocsAction.UploadSignature] containing the type of upload.
     */
    private fun handleUploadSignature(action: UploadDocsAction.UploadSignature) {
        when (action.type) {
            SignatureUploadType.SIGN -> handleShowSignatureUploadSheet()
            SignatureUploadType.CAPTURE -> uploadSignature()
            SignatureUploadType.GALLERY -> uploadSignature()
        }
    }

    /**
     * Launches a file picker to select an image for the signature and updates the
     * state with the selected file's data.
     */
    private fun uploadSignature() {
        viewModelScope.launch {
            try {
                val image = FileKit.openFilePicker(type = FileKitType.Image)
                image?.let { file ->
                    mutableStateFlow.update {
                        it.copy(
                            dialogState = null,
                            signatureDocumentFile = file.readBytes().toBase64DataUri(),
                            signatureFileName = file.name,
                            signatureSize = formatFileSize(file.size()),
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handleErrorMessage(Res.string.feature_upload_file_error)
            }
        }
    }

    /**
     * Updates the dialog state to show the signature drawing sheet.
     */
    private fun handleShowSignatureUploadSheet() {
        mutableStateFlow.update {
            it.copy(
                dialogState = UploadDocumentDialog.ShowSignaturePicker(isSignatureMode = true),
            )
        }
    }

    /**
     * Updates the state to show an error dialog.
     *
     * @param message The [StringResource] for the error message.
     */
    private fun handleErrorMessage(message: StringResource) {
        mutableStateFlow.update {
            it.copy(
                dialogState = UploadDocumentDialog.Error(message),
            )
        }
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        val sizeInKB = sizeInBytes / BYTES_PER_KB
        val sizeInMb = sizeInKB / BYTES_PER_KB
        return if (sizeInMb >= 1) "$sizeInMb MB" else "$sizeInKB KB"
    }

    private companion object {
        const val BYTES_PER_KB = 1024
    }
}

/**
 * Represents the UI state for the Upload Documents screen.
 *
 * @property clientId The id of the client applying for a loan, forwarded to Confirm Details.
 * @property productId The id of the loan product being applied for, forwarded to Confirm Details.
 * @property applicantName The applicant's display name, forwarded to Confirm Details.
 * @property loanProductName The selected loan product's display name, forwarded to Confirm Details.
 * @property loanPurpose The stated purpose for the loan, forwarded to Confirm Details.
 * @property disbursementDate The scheduled disbursement date, forwarded to Confirm Details.
 * @property principalAmount The requested loan amount, forwarded to Confirm Details.
 * @property providerId The id of the loan provider chosen on the Select Loan Provider screen,
 * forwarded to Confirm Details.
 * @property bankStatementFile The base64-encoded string of the bank statement file, or `null`.
 * @property propertyDocumentsFile The base64-encoded string of the property document file, or `null`.
 * @property signatureDocumentFile The base64-encoded string of the signature file, or `null`.
 * @property bankStatementSize The size of the bank statement file as a formatted string, or `null`.
 * @property propertyDocumentsSize The size of the property document file as a formatted string, or `null`.
 * @property signatureSize The size of the signature file as a formatted string, or `null`.
 * @property bankStatementFileName The name of the bank statement file, or `null`.
 * @property propertyDocumentFileName The name of the property document file, or `null`.
 * @property signatureFileName The name of the signature file, or `null`.
 * @property dialogState The state of any dialog to be shown on the screen.
 */
internal data class UploadDocsState(
    val clientId: Long,
    val productId: Long,
    val applicantName: String,
    val loanProductName: String,
    val loanPurpose: String,
    val disbursementDate: String,
    val principalAmount: String,
    val providerId: String,
    val bankStatementFile: String? = null,
    val propertyDocumentsFile: String? = null,
    val signatureDocumentFile: String? = null,
    val bankStatementSize: String? = null,
    val propertyDocumentsSize: String? = null,
    val signatureSize: String? = null,
    val bankStatementFileName: String? = null,
    val propertyDocumentFileName: String? = null,
    val signatureFileName: String? = null,
    val dialogState: UploadDocumentDialog? = null,
) {
    /**
     * A boolean indicating if the "Next" button should be enabled.
     */
    val isSubmitEnabled: Boolean
        get() = bankStatementFile != null && propertyDocumentsFile != null && signatureDocumentFile != null
}

/**
 * A sealed interface representing the different types of dialogs that can be
 * shown on the Upload Documents screen.
 */
internal sealed interface UploadDocumentDialog {
    /**
     * Represents a modal bottom sheet for signature upload options.
     * @property isSignatureMode A boolean to control the content shown inside the sheet.
     */
    data class ShowSignaturePicker(val isSignatureMode: Boolean) : UploadDocumentDialog

    /**
     * Represents a generic error dialog with a message.
     * @property error The [StringResource] for the error message.
     */
    data class Error(val error: StringResource) : UploadDocumentDialog
}

/**
 * A sealed interface representing one-time events that trigger UI side effects,
 * such as navigation.
 */
internal sealed interface UploadDocsEvent {
    /** Event to navigate back from the screen. */
    data object NavigateBack : UploadDocsEvent

    /**
     * Event to navigate to the Confirm Details step once all required documents have been
     * uploaded, carrying forward the application form data unchanged.
     */
    data class NavigateToConfirmDetails(
        val clientId: Long,
        val productId: Long,
        val applicantName: String,
        val loanProductName: String,
        val loanPurpose: String,
        val disbursementDate: String,
        val principalAmount: String,
        val providerId: String,
    ) : UploadDocsEvent
}

/**
 * A sealed interface representing user actions that the ViewModel needs to handle for the
 * Upload Documents screen.
 */
internal sealed interface UploadDocsAction {
    /** User action to navigate back. */
    data object OnNavigateBack : UploadDocsAction

    /** Action to navigate to the Confirm Details step. */
    data object NavigateToNextScreen : UploadDocsAction

    /** User action to dismiss a dialog. */
    data object DismissDialog : UploadDocsAction

    /** User action to show the signature upload modal bottom sheet. */
    data object ShowSignatureUploadSheet : UploadDocsAction

    /**
     * User action to select a signature based on a specific method.
     * @property type The [SignatureUploadType] for the chosen method.
     */
    data class UploadSignature(val type: SignatureUploadType) : UploadDocsAction

    /**
     * User action to upload a signed image.
     * @property image The base64-encoded string of the signature image.
     */
    data class UploadSign(val image: String) : UploadDocsAction

    /**
     * User action to initiate the upload of a document.
     * @property type The [DocumentType] to be uploaded.
     */
    data class UploadDocument(val type: DocumentType) : UploadDocsAction

    /**
     * User action to remove an uploaded document.
     * @property type The [DocumentType] to be removed.
     */
    data class RemoveDocument(val type: DocumentType) : UploadDocsAction

    /**
     * User action to select a new document for a given type.
     * @property type The [DocumentType] for which a new document is being selected.
     */
    data class SelectNewDocument(val type: DocumentType) : UploadDocsAction
}
