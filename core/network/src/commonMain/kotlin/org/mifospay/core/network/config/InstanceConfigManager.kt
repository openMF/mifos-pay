/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifospay.core.network.config

import kotlinx.coroutines.flow.StateFlow
import org.mifos.corebase.network.MultiUrlConfigProvider
import org.mifospay.core.datastore.UserPreferencesRepository
import org.mifospay.core.model.instance.InterbankServer
import org.mifospay.core.model.instance.ServerInstance

class InstanceConfigManager(
    private val userPreferencesRepository: UserPreferencesRepository,
) : MultiUrlConfigProvider {
    companion object {
        private const val MAX_TENANT_ID_LENGTH = 64
        private val VALID_TENANT_ID_REGEX = Regex("^[A-Za-z0-9_-]+$")

        // Default main instance configuration
        private val DEFAULT_MAIN_INSTANCE = ServerInstance(
            endpoint = "mifos-bank-2.mifos.community",
            protocol = "https://",
            path = "/fineract-provider/api/v1/",
            platformTenantId = "mifos-bank-2",
            label = "Default Instance",
            isDefault = true,
            interbankServers = listOf(
                InterbankServer(
                    endpoint = "apis.flexcore.mx",
                    protocol = "https://",
                    path = "/v1.0/vnext2/",
                    label = "Default Interbank",
                    isDefault = true,
                ),
            ),
        )

        // Default interbank instance configuration
        private val DEFAULT_INTERBANK_INSTANCE = InterbankServer(
            endpoint = "apis.flexcore.mx",
            protocol = "https://",
            path = "/v1.0/vnext2/",
            label = "Default Interbank",
            isDefault = true,
        )
    }

    val selectedInstance: StateFlow<ServerInstance?> = userPreferencesRepository.selectedInstance
    val selectedInterbankInstance: StateFlow<InterbankServer?> =
        userPreferencesRepository.selectedInterbankInstance

    fun getCurrentInstance(): ServerInstance {
        return selectedInstance.value ?: DEFAULT_MAIN_INSTANCE
    }

    fun getCurrentInterbankInstance(): InterbankServer {
        return selectedInterbankInstance.value
            ?: getCurrentInstance().getDefaultInterbankServer()
            ?: DEFAULT_INTERBANK_INSTANCE
    }

    fun getEndpoint(): String = getCurrentInstance().endpoint

    fun getProtocol(): String = getCurrentInstance().protocol

    fun getPath(): String = getCurrentInstance().path

    fun getPlatformTenantId(): String = sanitizeTenantId(getCurrentInstance().platformTenantId)

    fun getUrl(): String = getCurrentInstance().fullUrl

    fun getSelfServiceUrl(): String {
        val instance = getCurrentInstance()
        return "${instance.protocol}${instance.endpoint}${instance.path}self/"
    }

    fun getInterbankUrl(): String = getCurrentInterbankInstance().fullUrl

    // MultiUrlConfigProvider implementation
    override fun getBaseUrl(type: MultiUrlConfigProvider.UrlType): String = when (type) {
        MultiUrlConfigProvider.UrlType.MAIN -> getUrl()
        MultiUrlConfigProvider.UrlType.SELF_SERVICE -> getSelfServiceUrl()
        MultiUrlConfigProvider.UrlType.INTERBANK -> getInterbankUrl()
    }

    override fun getLoggableHosts(): List<String> = listOf(
        getEndpoint(),
        getCurrentInterbankInstance().endpoint,
    )

    private fun sanitizeTenantId(rawTenantId: String): String {
        val normalized = rawTenantId
            .trim()
            .replace("\r", "")
            .replace("\n", "")
            .take(MAX_TENANT_ID_LENGTH)

        return if (normalized.isNotEmpty() && VALID_TENANT_ID_REGEX.matches(normalized)) {
            normalized
        } else {
            DEFAULT_MAIN_INSTANCE.platformTenantId
        }
    }
}
