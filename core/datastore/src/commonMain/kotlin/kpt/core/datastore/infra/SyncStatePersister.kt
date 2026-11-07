/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package kpt.core.datastore.infra

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Persistence seam for the [Synchronizer]'s per-feature last-synced version map.
 *
 * Backed by Multiplatform Settings (same store used by [UserPreferencesRepositoryImpl]
 * for plain user prefs) — survives process restart but not data wipe. Sync
 * state is non-secret so the "plain" Settings instance is the right home.
 *
 * The sync/ module's [DataSyncWorker] wires this to its `Synchronizer`
 * implementation: `getChangeListVersions()` reads, `updateChangeListVersions { ... }`
 * writes.
 */
interface SyncStatePersister {
    /** Reads the persisted versions, or an empty map if none. */
    suspend fun read(): ChangeListVersions

    /** Persists [versions]. */
    suspend fun write(versions: ChangeListVersions)
}

private const val SYNC_VERSIONS_KEY = "sync_change_list_versions"

/** [SyncStatePersister] backed by plain multiplatform Settings. */
class SettingsSyncStatePersister(
    private val plainSettings: Settings,
) : SyncStatePersister {

    override suspend fun read(): ChangeListVersions = plainSettings.decodeValueOrNull(
        key = SYNC_VERSIONS_KEY,
        serializer = ChangeListVersions.serializer(),
    ) ?: ChangeListVersions()

    override suspend fun write(versions: ChangeListVersions) {
        plainSettings.encodeValue(
            key = SYNC_VERSIONS_KEY,
            serializer = ChangeListVersions.serializer(),
            value = versions,
        )
    }
}
