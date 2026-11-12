/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.utils

import kpt.core.network.main.config.InstanceConfigManager
/** Exposes the base URLs and tenant id of the active server instance from [InstanceConfigManager]. */
class BaseURL(
    private val configManager: InstanceConfigManager,
) {
    /** Header names and values shared by the HTTP interceptors. */
    companion object {
        /** Name of the Fineract tenant id header. */
        const val HEADER_TENANT = "Fineract-Platform-TenantId"

        /** Name of the Content-Type header. */
        const val HEADER_CONTENT_TYPE = "Content-Type"

        /** Name of the Accept header. */
        const val HEADER_ACCEPT = "Accept"

        /** Name of the Authorization header. */
        const val HEADER_AUTHORIZATION = "Authorization"

        /** Value of the Content-Type header. */
        const val HEADER_CONTENT_TYPE_VALUE = "application/json"

        /** Value of the Accept header. */
        const val HEADER_ACCEPT_VALUE = "application/json"
    }

    /** Fineract API base URL of the current instance. */
    val url: String
        get() = configManager.getUrl()

    /** Self-service API base URL of the current instance. */
    val selfServiceUrl: String
        get() = configManager.getSelfServiceUrl()

    /** Inter-bank API base URL of the current interbank server. */
    val interBankUrl: String
        get() = configManager.getInterbankUrl()

    /** Fineract platform tenant id of the current instance. */
    val fineractPlatformTenantId: String
        get() = configManager.getPlatformTenantId()
}
