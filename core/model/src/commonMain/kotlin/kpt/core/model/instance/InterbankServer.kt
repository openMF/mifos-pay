/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.instance

import kotlinx.serialization.Serializable

/**
 * Endpoint of an interbank (Mojaloop) server used for cross-bank transfers.
 *
 * @property endpoint Host name or address.
 * @property protocol URL scheme prefix.
 * @property path Base path.
 * @property label Display label.
 * @property isDefault Whether this is the default interbank server.
 */
@Serializable
data class InterbankServer(
    val endpoint: String,
    val protocol: String,
    val path: String,
    val label: String,
    val isDefault: Boolean = false,
) {
    /**
     * Complete URL built from protocol, endpoint and path.
     */
    val fullUrl: String
        get() = "$protocol$endpoint$path"
}
