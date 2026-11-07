/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.serverconfig.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.serverconfig.entity.ServerInstanceEntity

/** Room queries for server instance rows. */
@DbDao
@Dao
interface ServerInstanceDao {

    /** Observes the `server_instances` rows, ordered by `position`. */
    @Query("SELECT * FROM server_instances ORDER BY position")
    fun observeAll(): Flow<List<ServerInstanceEntity>>

    /** Saves `instances`, replacing rows with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(instances: List<ServerInstanceEntity>)

    /** Deletes every `server_instances` row. */
    @Query("DELETE FROM server_instances")
    suspend fun deleteAll()

    /** The fetched list REPLACES the cached one: a server removed remotely leaves the picker. */
    @Transaction
    suspend fun replaceAll(instances: List<ServerInstanceEntity>) {
        deleteAll()
        insertAll(instances)
    }
}
