/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.fork.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.fork.ForkTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Dialog that shows an error-style title and message from a [BasicDialogState] with a single dismiss action. */
@Composable
fun ForkBasicDialog(
    visibilityState: BasicDialogState,
    onDismissRequest: () -> Unit,
): Unit = when (visibilityState) {
    BasicDialogState.Hidden -> Unit
    is BasicDialogState.Shown -> {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                ForkTextButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("AcceptAlertButton"),
                )
            },
            title = visibilityState.title.let {
                {
                    Text(
                        text = it,
                        style = KptTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("AlertTitleText"),
                    )
                }
            },
            text = {
                Text(
                    text = visibilityState.message,
                    style = KptTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("AlertContentText"),
                )
            },
            containerColor = KptTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.semantics {
                testTag = "AlertPopup"
            },
        )
    }
}

/** Dialog that shows a title and message from a [BasicDialogState] with confirm and dismiss actions. */
@Composable
fun ForkBasicDialog(
    visibilityState: BasicDialogState,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
): Unit = when (visibilityState) {
    BasicDialogState.Hidden -> Unit
    is BasicDialogState.Shown -> {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                ForkTextButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onConfirm,
                    modifier = Modifier.testTag("AcceptAlertButton"),
                )
            },
            dismissButton = {
                ForkTextButton(
                    content = {
                        Text(text = "Cancel")
                    },
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("DismissAlertButton"),
                )
            },
            title = visibilityState.title.let {
                {
                    Text(
                        text = it,
                        style = KptTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("AlertTitleText"),
                    )
                }
            },
            text = {
                Text(
                    text = visibilityState.message,
                    style = KptTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("AlertContentText"),
                )
            },
            containerColor = KptTheme.colorScheme.surface,
            modifier = Modifier.semantics {
                testTag = "AlertPopup"
            },
        )
    }
}

@Preview
@Composable
private fun MifosBasicDialog_preview() {
    ForkTheme {
        ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = "An error has occurred.",
                message = "Username or password is incorrect. Try again.",
            ),
            onDismissRequest = {},
        )
    }
}

/**
 * Models display of a [ForkBasicDialog].
 */
sealed class BasicDialogState {

    /** No dialog is displayed. */
    data object Hidden : BasicDialogState()

    /**
     * A dialog is displayed with the given text.
     *
     * @property message Body text of the dialog.
     * @property title Dialog title; defaults to an error heading.
     */
    data class Shown(
        val message: String,
        val title: String = "An Error Occurred!",
    ) : BasicDialogState()
}
