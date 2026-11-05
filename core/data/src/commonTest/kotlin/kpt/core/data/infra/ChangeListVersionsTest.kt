/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.infra

import kotlinx.serialization.json.Json
import kpt.core.base.datastore.infra.ChangeListVersions
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `ChangeListVersions` is the persisted sync-state record; it must
 * round-trip through kotlinx.serialization (it is stored as a JSON string by
 * `DataStoreSyncStatePersister`).
 */
class ChangeListVersionsTest {

    @Test
    fun round_trips_through_kotlinx_serialization() {
        val original = ChangeListVersions(
            mapOf("currency-rates" to 1_700_000_000L, "macro-indicators" to 42L),
        )
        val encoded = Json.encodeToString(ChangeListVersions.serializer(), original)
        val decoded = Json.decodeFromString(ChangeListVersions.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun set_stamps_a_version_under_the_given_key() {
        val updated = ChangeListVersions().set("macro-indicators", 7L)
        assertEquals(7L, updated.versions["macro-indicators"])
    }

    @Test
    fun set_is_immutable_and_overwrites_an_existing_key() {
        val base = ChangeListVersions(mapOf("k" to 1L))
        val updated = base.set("k", 2L)
        assertEquals(1L, base.versions["k"], "original must be untouched")
        assertEquals(2L, updated.versions["k"], "copy must carry the new value")
    }
}
