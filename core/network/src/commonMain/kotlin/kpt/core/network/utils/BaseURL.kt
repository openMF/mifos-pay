/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.utils

import kpt.core.network.config.InstanceConfigManager
class BaseURL(
    private val configManager: InstanceConfigManager,
) {
    companion object {
        const val HEADER_TENANT = "Fineract-Platform-TenantId"
        const val HEADER_CONTENT_TYPE = "Content-Type"
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_AUTHORIZATION = "Authorization"

        const val HEADER_CONTENT_TYPE_VALUE = "application/json"
        const val HEADER_ACCEPT_VALUE = "application/json"
    }

    val url: String
        get() = configManager.getUrl()

    val selfServiceUrl: String
        get() = configManager.getSelfServiceUrl()

    val interBankUrl: String
        get() = configManager.getInterbankUrl()

    val fineractPlatformTenantId: String
        get() = configManager.getPlatformTenantId()
}
