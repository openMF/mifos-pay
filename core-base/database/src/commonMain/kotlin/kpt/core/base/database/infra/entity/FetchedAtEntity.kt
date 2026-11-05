/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.infra.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Persistent record of the last successful network fetch for a Store, keyed by
 * the framework's [kpt.core.base.store.FetchedAtRepository] convention
 * (`<feature>:<storeName>[:<key>]`).
 *
 * Backs the framework's [kpt.core.base.store.FetchedAtRepository] interface
 * via [kpt.core.data.repositoryImpl.RoomFetchedAtRepository]. The framework
 * uses this to render real "Updated 5m ago" timestamps in DataFreshnessIndicator
 * across ViewModel destruction, navigation, and app process restart.
 *
 * One row per Store. Tiny by design — eviction is not required.
 */
@Entity(tableName = "framework_fetched_at")
data class FetchedAtEntity(
    /**
     * The store key this timestamp belongs to — the same string the Store uses, so freshness is addressed exactly like
     * data.
     */
    @PrimaryKey val storeKey: String,
    /**
     * When the key was last fetched successfully, epoch millis. Every TTL and freshness band is measured from this, so
     * a failed fetch must NOT update it.
     */
    val lastFetchedMillis: Long,
)
