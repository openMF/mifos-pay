/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.infra

import kotlinx.serialization.Serializable

/**
 * Per-feature last-synced version map. Persisted across runs via
 * [SyncStatePersister] so a `Synchronizer` (in `core/data`) can resume from
 * where the prior sync left off — used by `changeListSync` (Int version
 * → delta `since=N` cursor) and `snapshotSync` (Long epoch-seconds → freshness
 * timestamp).
 *
 * Long-typed values so snapshotSync can store `Clock.System.now().epochSeconds`
 * without overflow.
 *
 * Lives in `core/datastore` (not `core/data`) so [SyncStatePersister] can
 * reference it without creating a `core/datastore → core/data` cycle.
 * `core/data`'s `Synchronizer.kt` imports it via the existing
 * `core/data → core/datastore` dep.
 *
 * @property versions Last-synced version (or epoch-seconds timestamp) per feature name.
 */
@Serializable
data class ChangeListVersions(val versions: Map<String, Long> = emptyMap()) {
    /** Returns a copy with [name] mapped to [version]. */
    fun set(name: String, version: Long): ChangeListVersions = copy(versions = versions + (name to version))
}
