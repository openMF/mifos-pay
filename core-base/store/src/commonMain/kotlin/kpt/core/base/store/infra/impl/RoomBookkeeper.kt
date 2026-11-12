/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.infra.impl

import kpt.core.base.database.infra.dao.BookkeeperDao
import kpt.core.base.database.infra.entity.BookkeeperEntity
import org.mobilenativefoundation.store.store5.Bookkeeper

/**
 * Room-backed [Bookkeeper] that persists sync-failure tracking across process restarts.
 *
 * Use this with [org.mobilenativefoundation.store.store5.MutableStore] to enable
 * reliable offline-first writes that retry on connectivity restore.
 *
 * @param Key The store key type.
 * @param dao The Room DAO for bookkeeper persistence.
 * @param keySerializer Converts the key to a stable string representation.
 */
class RoomBookkeeper<Key : Any>(
    private val dao: BookkeeperDao,
    private val keySerializer: (Key) -> String,
) : Bookkeeper<Key> {

    override suspend fun getLastFailedSync(key: Key): Long? = dao.getLastFailedSync(keySerializer(key))

    override suspend fun setLastFailedSync(key: Key, timestamp: Long): Boolean {
        dao.upsert(BookkeeperEntity(key = keySerializer(key), lastFailedSync = timestamp))
        return true
    }

    override suspend fun clear(key: Key): Boolean {
        dao.delete(keySerializer(key))
        return true
    }

    override suspend fun clearAll(): Boolean {
        dao.deleteAll()
        return true
    }
}
