/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.main.config

import kotlinx.coroutines.flow.StateFlow
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.instance.InterbankServer
import kpt.core.model.instance.ServerInstance
import org.mifos.corebase.network.MultiUrlConfigProvider
/** Provides the active Fineract server instance and interbank server, and resolves the API base URLs. */
class InstanceConfigManager(
    private val userPreferencesRepository: ForkPreferencesRepository,
) : MultiUrlConfigProvider {
    /** Built-in default instance configuration. */
    companion object {
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

    /** Currently selected server instance, if any. */
    val selectedInstance: StateFlow<ServerInstance?> = userPreferencesRepository.selectedInstance

    /** Currently selected interbank server, if any. */
    val selectedInterbankInstance: StateFlow<InterbankServer?> =
        userPreferencesRepository.selectedInterbankInstance

    /** Returns the selected server instance, or the built-in default when none is selected. */
    fun getCurrentInstance(): ServerInstance {
        return selectedInstance.value ?: DEFAULT_MAIN_INSTANCE
    }

    /** Returns the selected interbank server, falling back to the instance default and then the built-in default. */
    fun getCurrentInterbankInstance(): InterbankServer {
        return selectedInterbankInstance.value
            ?: getCurrentInstance().getDefaultInterbankServer()
            ?: DEFAULT_INTERBANK_INSTANCE
    }

    /** Host of the current instance. */
    fun getEndpoint(): String = getCurrentInstance().endpoint

    /** Protocol (scheme) of the current instance. */
    fun getProtocol(): String = getCurrentInstance().protocol

    /** Path component of the current instance's API URL. */
    fun getPath(): String = getCurrentInstance().path

    /** Fineract platform tenant id of the current instance. */
    fun getPlatformTenantId(): String = getCurrentInstance().platformTenantId

    /** Full base URL of the current instance. */
    fun getUrl(): String = getCurrentInstance().fullUrl

    /** Base URL of the self-service API for the current instance. */
    fun getSelfServiceUrl(): String {
        val instance = getCurrentInstance()
        return "${instance.protocol}${instance.endpoint}${instance.path}self/"
    }

    /** Full base URL of the current interbank server. */
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
}
