/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Confirmation alert dialog with a title, optional text and confirm/dismiss buttons,
 * shown while [showDialogState] is true.
 */
@Composable
fun MifosDialogBox(
    title: String,
    showDialogState: Boolean,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    AnimatedVisibility(showDialogState) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = {
                if (message != null) {
                    Text(text = message)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                    },
                ) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                if (!dismissButtonText.isNullOrBlank()) {
                    TextButton(onClick = onDismiss) {
                        Text(text = dismissButtonText)
                    }
                }
            },
        )
    }
}

/** Basic dialog that hosts arbitrary [content], dismissed through [onDismiss]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MifosCustomDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        content = content,
        modifier = modifier,
    )
}
