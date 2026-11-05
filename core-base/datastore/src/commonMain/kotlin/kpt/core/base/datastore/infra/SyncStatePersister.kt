/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package kpt.core.base.datastore.infra

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
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
    /** Reads the stored versions; an empty map means nothing has synced yet. */
    suspend fun read(): ChangeListVersions

    /** Persists the versions after a successful sync. */
    suspend fun write(versions: ChangeListVersions)
}

private const val SYNC_VERSIONS_KEY = "sync_change_list_versions"

/**
 * [SyncStatePersister] backed by multiplatform-settings — the per-feature last-synced version map,
 * serialised under a single key.
 *
 * Survives process restart but not a data wipe, which is the correct durability: losing it makes the
 * next sync re-read from the beginning rather than silently skipping a change list.
 */
class SettingsSyncStatePersister(
    private val plainSettings: Settings,
) : SyncStatePersister {

    // JSON, not `encodeValue`: its decoder drops the defaulted `versions` map (see [putJson]).
    override suspend fun read(): ChangeListVersions =
        plainSettings.getJsonOrNull(ChangeListVersions.serializer(), SYNC_VERSIONS_KEY) ?: ChangeListVersions()

    override suspend fun write(versions: ChangeListVersions) {
        plainSettings.putJson(ChangeListVersions.serializer(), SYNC_VERSIONS_KEY, versions)
    }
}
