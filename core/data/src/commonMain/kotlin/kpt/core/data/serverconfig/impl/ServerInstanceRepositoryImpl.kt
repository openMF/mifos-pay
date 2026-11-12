/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.serverconfig.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.serverconfig.ServerInstanceRepository
import kpt.core.model.instance.ServerInstance
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = ServerInstanceRepository::class)
internal class ServerInstanceRepositoryImpl(
    @FromStore(AppStoreIds.ServerInstances) private val store: Store<Unit, List<ServerInstance>>,
) : ServerInstanceRepository {

    // Cached servers render at once, the remote list refreshes behind them, and with nothing cached the
    // picker shows its loading state until the fetch lands.
    override fun instancesStream(scope: CoroutineScope): ScreenDataStream<List<ServerInstance>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.ServerInstances.KEY,
            scope = scope,
            isEmpty = { it.isEmpty() },
        )
}
