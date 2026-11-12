/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.serverconfig.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * One server the user can pick — the local source of truth for the `serverInstances` store, filled from
 * Supabase `app_server_config` (this app's rows). Public configuration, not the user's data: it is kept on
 * sign-out so the login screen can offer it offline.
 */
@DbEntity
@Entity(tableName = "server_instances")
data class ServerInstanceEntity(
    /** `endpoint/tenant` — the pair the remote table keys on. */
    @PrimaryKey
    val id: String,
    /** The display name. */
    val label: String,
    /** `http://` or `https://`. */
    val protocol: String,
    /** The server's host name. */
    val endpoint: String,
    /** The Fineract API path on the server. */
    val path: String,
    /** The Fineract tenant identifier. */
    val platformTenantId: String,
    /** Whether this is the server used when the user has chosen none. */
    val isDefault: Boolean,
    /** Serialized inter-bank servers (`[{endpoint, protocol, path, label, isDefault}]`) — only read whole. */
    val interbankServers: String,
    /** The remote `sort_order` position, so the picker keeps the table's order. */
    val position: Int,
    /** When this cached copy was fetched, in epoch milliseconds. */
    val fetchedAtMs: Long,
)
