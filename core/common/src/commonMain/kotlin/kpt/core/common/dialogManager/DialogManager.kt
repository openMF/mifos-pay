/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.dialogManager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.StringResource

/**
 * A singleton for managing dialog state across the app.
 * It works with [DialogMessage] to determine what kind of dialog should be shown.
 *
 * This is typically observed in the UI to display a loading spinner or error message.
 */
object DialogManager {

    private val _dialogMessage = MutableStateFlow<DialogMessage>(DialogMessage.None)

    /**
     * Public read-only dialog message flow.
     * UI layers should collect this to react to dialog state changes.
     */
    val dialogMessage: StateFlow<DialogMessage> = _dialogMessage.asStateFlow()

    /**
     * Dismisses any currently shown dialog.
     *
     * ### Example:
     * ```
     * DialogManager.dismissDialog()
     * ```
     */
    fun dismissDialog() {
        _dialogMessage.value = DialogMessage.None
    }

    /**
     * Shows a loading dialog.
     *
     * ### Example:
     * ```
     * DialogManager.showLoading()
     * ```
     */
    fun showLoading() {
        _dialogMessage.value = DialogMessage.Loading
    }

    /**
     * Shows a plain string message in a dialog.
     *
     * @param message The text to display.
     *
     * ### Example:
     * ```
     * DialogManager.showMessage("Invalid email address.")
     * ```
     */
    fun showMessage(message: String) {
        _dialogMessage.value = DialogMessage.StringMessage(message)
    }

    /**
     * Shows a localized string resource message in a dialog.
     *
     * @param message The resource ID to display.
     *
     * ### Example:
     * ```
     * DialogManager.showMessage(Res.string.feature_auth_error_email_required)
     * ```
     */
    fun showMessage(message: StringResource) {
        _dialogMessage.value = DialogMessage.ResourceMessage(message)
    }

    /**
     * Allows setting any [DialogMessage] manually.
     *
     * @param message The [DialogMessage] to show.
     *
     * ### Example:
     * ```
     * DialogManager.showMessage(DialogMessage.Loading)
     * ```
     */
    fun showMessage(message: DialogMessage) {
        _dialogMessage.value = message
    }
}
