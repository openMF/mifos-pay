/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.infra.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kpt.core.base.database.infra.entity.FetchedAtEntity

/**
 * DAO for the framework-owned `framework_fetched_at` table.
 *
 * Backing store for [kpt.core.base.store.FetchedAtRepository]. Wired in
 * `core/data` via `RoomFetchedAtRepository`.
 */
@Dao
interface FetchedAtDao {

    /** When [storeKey] was last fetched, epoch millis, or null if never — the input to every freshness decision. */
    @Query("SELECT lastFetchedMillis FROM framework_fetched_at WHERE storeKey = :storeKey")
    suspend fun read(storeKey: String): Long?

    /** Stamps a store key as fetched now. */
    @Upsert
    suspend fun upsert(entity: FetchedAtEntity)
}
