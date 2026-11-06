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

import org.jetbrains.compose.resources.StringResource

/**
 * Represents the possible types of dialog messages shown in the application.
 * Used in combination with [DialogManager] to control dialog visibility and content.
 */
sealed interface DialogMessage {

    /**
     * Represents the absence of a dialog message.
     * Used to indicate that no dialog should be displayed.
     */
    data object None : DialogMessage

    /**
     * Represents a loading dialog.
     * Used to indicate an ongoing operation to the user.
     */
    data object Loading : DialogMessage

    /**
     * Represents a dialog that shows a plain string message.
     *
     * @property message The message to be displayed in the dialog.
     */
    class StringMessage(val message: String) : DialogMessage

    /**
     * Represents a dialog that shows a localized string resource message.
     *
     * @property message A [StringResource] ID for localization.
     */
    class ResourceMessage(val message: StringResource) : DialogMessage

    companion object {

        /**
         * Converts a [Throwable] into a [DialogMessage].
         * Uses [StringMessage] if the exception has a non-blank message,
         * otherwise falls back to a generic error.
         *
         * @return [StringMessage] based on content.
         *
         * ### Example:
         * ```
         * try {
         *     doSomethingRisky()
         * } catch (e: Exception) {
         *     DialogManager.showMessage(e.toDialogMessage())
         * }
         * ```
         */
        fun Throwable.toDialogMessage(): DialogMessage {
            return StringMessage(message ?: "Unknown error occurred.")
        }
    }
}
