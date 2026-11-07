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
 * A Fineract backend the app can connect to, with its tenant and interbank servers.
 *
 * @property endpoint Host name or address.
 * @property protocol URL scheme prefix.
 * @property path Base path.
 * @property platformTenantId Fineract platform tenant id.
 * @property label Display label.
 * @property isDefault Whether this is the default instance.
 * @property interbankServers Interbank servers available for this instance.
 */
@Serializable
data class ServerInstance(
    val endpoint: String,
    val protocol: String,
    val path: String,
    val platformTenantId: String,
    val label: String,
    val isDefault: Boolean = false,
    val interbankServers: List<InterbankServer> = emptyList(),
) {
    /**
     * Complete URL built from protocol, endpoint and path.
     */
    val fullUrl: String
        get() = "$protocol$endpoint$path"

    /**
     * Returns the interbank server flagged as default, else the first one, else null.
     */
    fun getDefaultInterbankServer(): InterbankServer? =
        interbankServers.firstOrNull { it.isDefault } ?: interbankServers.firstOrNull()
}
