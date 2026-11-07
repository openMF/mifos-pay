/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.deletebeneficiary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.beneficiary.generated.resources.Res
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_cancel
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_delete
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_delete_beneficiary
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_delete_confirmation_message
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_delete_failed
import kpt.feature.beneficiary.generated.resources.feature_beneficiary_ok
import org.jetbrains.compose.resources.stringResource
/**
 * Bottom sheet composable for delete beneficiary flow.
 *
 * Shows different states:
 * - Confirmation: Asks user to confirm deletion
 * - Deleting: Shows overlay progress indicator
 * - Error: Shows error message from server
 */
@Composable
fun DeleteBeneficiaryBottomSheet(
    dialogState: DeleteBeneficiaryState.DialogState?,
    onConfirmDelete: (beneficiaryId: Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (dialogState) {
        is DeleteBeneficiaryState.DialogState.Confirmation -> {
            MifosBottomSheet(
                onDismiss = onDismiss,
            ) {
                DeleteConfirmationContent(
                    beneficiaryName = dialogState.beneficiaryName,
                    onConfirm = { onConfirmDelete(dialogState.beneficiaryId) },
                    onCancel = onDismiss,
                )
            }
        }

        is DeleteBeneficiaryState.DialogState.Deleting -> {
            // Overlay is handled separately in the parent screen
            Unit
        }

        is DeleteBeneficiaryState.DialogState.Error -> {
            MifosBottomSheet(
                onDismiss = onDismiss,
            ) {
                DeleteErrorContent(
                    message = dialogState.message,
                    onDismiss = onDismiss,
                )
            }
        }

        null -> Unit
    }
}

@Composable
private fun DeleteConfirmationContent(
    beneficiaryName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(KptTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MifosIcons.OutlinedDelete,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = KptTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(Res.string.feature_beneficiary_delete_beneficiary),
            style = KptTheme.typography.headlineSmall,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(
                Res.string.feature_beneficiary_delete_confirmation_message,
                beneficiaryName,
            ),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = KptTheme.colorScheme.onSurface,
                ),
            ) {
                Text(text = stringResource(Res.string.feature_beneficiary_cancel))
            }

            MifosButton(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.error,
                    contentColor = KptTheme.colorScheme.onError,
                ),
            ) {
                Text(text = stringResource(Res.string.feature_beneficiary_delete))
            }
        }
    }
}

@Composable
private fun DeleteErrorContent(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(KptTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MifosIcons.Warning,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = KptTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(Res.string.feature_beneficiary_delete_failed),
            style = KptTheme.typography.headlineSmall,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        MifosButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(Res.string.feature_beneficiary_ok))
        }
    }
}
