/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.serverconfig

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.instance.ServerInstance

/** The servers the user can pick, read through `core/data` like every other value a screen shows. */
interface ServerInstanceRepository {

    /** Supabase `app_server_config` (this app's rows) through the `serverInstances` store, cached locally. */
    fun instancesStream(scope: CoroutineScope): ScreenDataStream<List<ServerInstance>>
}
