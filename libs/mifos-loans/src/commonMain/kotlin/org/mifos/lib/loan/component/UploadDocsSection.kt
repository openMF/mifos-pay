/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosCard
import kpt.core.designsystem.icon.MifosIcons
import mifos_pay.libs.mifos_loans.generated.resources.Res
import mifos_pay.libs.mifos_loans.generated.resources.feature_upload_docs_bank_account
import mifos_pay.libs.mifos_loans.generated.resources.feature_upload_docs_collateral
import mifos_pay.libs.mifos_loans.generated.resources.feature_upload_docs_signature
import org.jetbrains.compose.resources.stringResource
import org.mifos.lib.loan.ui.uploadDocs.UploadDocsAction
import org.mifos.lib.loan.ui.uploadDocs.UploadDocsState
/**
 * Renders the document upload section, switching between upload prompts and file summaries based on the current state.
 *
 * @param state The current state containing file data for bank statements, property documents, and signatures.
 * @param onAction Callback to handle user interactions like uploading, removing, or re-selecting documents.
 */
@Composable
internal fun UploadDocumentsSection(
    state: UploadDocsState,
    onAction: (UploadDocsAction) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        if (state.bankStatementFile != null) {
            UploadedFileCard(
                icon = MifosIcons.Receipt,
                label = stringResource(Res.string.feature_upload_docs_bank_account),
                fileName = state.bankStatementFileName ?: "",
                fileSize = state.bankStatementSize ?: "",
                onRemoveClick = { onAction(UploadDocsAction.RemoveDocument(DocumentType.BANK_STATEMENT)) },
                onSelectNewClick = { onAction(UploadDocsAction.SelectNewDocument(DocumentType.BANK_STATEMENT)) },
            )
        } else {
            UploadPromptCard(
                icon = Icons.Filled.Image,
                text = stringResource(Res.string.feature_upload_docs_bank_account),
                onClick = {
                    onAction(UploadDocsAction.UploadDocument(DocumentType.BANK_STATEMENT))
                },
            )
        }

        if (state.propertyDocumentsFile != null) {
            UploadedFileCard(
                icon = MifosIcons.DataInfo,
                label = stringResource(Res.string.feature_upload_docs_collateral),
                fileName = state.propertyDocumentFileName ?: "",
                fileSize = state.propertyDocumentsSize ?: "",
                onRemoveClick = { onAction(UploadDocsAction.RemoveDocument(DocumentType.PROPERTY_DOCUMENT)) },
                onSelectNewClick = { onAction(UploadDocsAction.SelectNewDocument(DocumentType.PROPERTY_DOCUMENT)) },
            )
        } else {
            UploadPromptCard(
                icon = Icons.Filled.Image,
                text = stringResource(Res.string.feature_upload_docs_collateral),
                onClick = {
                    onAction(UploadDocsAction.UploadDocument(DocumentType.PROPERTY_DOCUMENT))
                },
            )
        }

        if (state.signatureDocumentFile != null) {
            UploadedFileCard(
                icon = MifosIcons.Edit,
                label = stringResource(Res.string.feature_upload_docs_signature),
                fileName = state.signatureFileName ?: "",
                fileSize = state.signatureSize ?: "",
                onRemoveClick = { onAction(UploadDocsAction.RemoveDocument(DocumentType.SIGNATURE)) },
                onSelectNewClick = { onAction(UploadDocsAction.ShowSignatureUploadSheet) },
            )
        } else {
            UploadPromptCard(
                icon = Icons.Filled.Image,
                text = stringResource(Res.string.feature_upload_docs_signature),
                onClick = {
                    onAction(UploadDocsAction.ShowSignatureUploadSheet)
                },
            )
        }
    }
}

/**
 * A tappable card prompting the user to upload a document, shown when no file has been selected yet.
 */
@Composable
private fun UploadPromptCard(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosCard(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.medium,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KptTheme.colorScheme.primary,
            )
            Text(
                text = text,
                style = KptTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * A summary card for a document that has already been selected, showing its name, size, and
 * actions to remove or replace it.
 */
@Composable
private fun UploadedFileCard(
    icon: ImageVector,
    label: String,
    fileName: String,
    fileSize: String,
    onRemoveClick: () -> Unit,
    onSelectNewClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosCard(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.medium,
        onClick = onSelectNewClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KptTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = KptTheme.typography.labelMedium,
                )
                Text(
                    text = fileName,
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                if (fileSize.isNotBlank()) {
                    Text(
                        text = fileSize,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = MifosIcons.Delete,
                    contentDescription = null,
                )
            }
        }
    }
}
