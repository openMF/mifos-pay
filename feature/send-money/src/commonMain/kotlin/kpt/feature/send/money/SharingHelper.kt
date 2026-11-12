/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

/**
 * Common interface for sharing invite messages across platforms
 */
interface SharingHelper {
    /**
     * Shares the invite message via the selected sharing option
     *
     * @param sharingOption The selected sharing method (SMS or WhatsApp)
     * @param phoneNumber The target phone number
     * @param message The invite message to share
     */
    fun shareInviteMessage(
        sharingOption: SharingOption,
        phoneNumber: String,
        message: String,
    )
}

/**
 * Default implementation that does nothing (for platforms without sharing support)
 */
object DefaultSharingHelper : SharingHelper {
    override fun shareInviteMessage(
        sharingOption: SharingOption,
        phoneNumber: String,
        message: String,
    ) {
        println("Sharing not supported on this platform: $sharingOption to $phoneNumber")
    }
}

/**
 * Factory function to get the appropriate sharing helper for the current platform
 */
expect fun getSharingHelper(): SharingHelper
