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

import kotlinx.coroutines.flow.Flow
import kpt.core.common.ScreenState
import kpt.core.model.instance.InstancesConfig
/** Loads the list of available server instances. */
interface InstanceConfigLoader {
    /** Fetches the server instances configuration once. */
    suspend fun fetchInstancesConfig(): InstancesConfig

    /** Streams the server instances configuration wrapped in a [ScreenState]. */
    fun observeInstancesConfig(): Flow<ScreenState<InstancesConfig>>
}
