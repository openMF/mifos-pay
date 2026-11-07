/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network

import kpt.core.network.main.services.ClientService
import kpt.core.network.main.services.PocketService
import kpt.core.network.main.services.ShareAccountService
/** Provides the network services needed to load a client's pocket accounts. */
interface PocketDataManager {
    /** Clients API. */
    val clientsApi: ClientService

    /** Pocket accounts API. */
    val pocketApi: PocketService

    /** Share accounts API. */
    val shareAccountApi: ShareAccountService
}
