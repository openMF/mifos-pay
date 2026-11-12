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
 * Tracks failed sync timestamps for [org.mobilenativefoundation.store.store5.MutableStore]
 * write-back operations. Used by [kpt.core.data.store.RoomBookkeeper] to persist
 * bookkeeper state across process restarts.
 */
@Entity(tableName = "store_bookkeeper")
data class BookkeeperEntity(
    /** The store key whose write failed. */
    @PrimaryKey val key: String,
    /** When that write last failed, epoch millis. */
    val lastFailedSync: Long,
)
