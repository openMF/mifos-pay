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
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsJsonTest {

    @Serializable
    private data class Session(val name: String = "", val codes: List<String> = emptyList())

    @Test
    fun collectionsWithDefaultsSurviveTheRoundTrip() {
        val settings = MapSettings()
        val value = Session("a", listOf("READ_CLIENT", "READ_LOAN"))

        settings.putJson(Session.serializer(), "s", value)

        assertEquals(value, settings.getJsonOrNull(Session.serializer(), "s"))
    }

    /** The defect this replaces: `encodeValue` writes `s.codes.0`, but its decoder skips the list. */
    @Test
    fun theLibraryLayoutDropsDefaultedCollections() {
        val settings = MapSettings()
        settings.encodeValue(Session.serializer(), "s", Session("a", listOf("READ_CLIENT")))

        assertEquals(Session("a", emptyList()), settings.decodeValueOrNull(Session.serializer(), "s"))
    }

    @Test
    fun aValueWrittenInTheLegacyLayoutIsStillRead() {
        val settings = MapSettings()
        settings.encodeValue(Session.serializer(), "s", Session("legacy"))

        assertEquals(Session("legacy"), settings.getJsonOrNull(Session.serializer(), "s"))
    }

    @Test
    fun anAbsentKeyReadsAsNull() {
        assertNull(MapSettings().getJsonOrNull(Session.serializer(), "s"))
    }

    @Test
    fun syncVersionsSurviveARestart() = runTest {
        val settings = MapSettings()
        SettingsSyncStatePersister(settings).write(ChangeListVersions(mapOf("clients" to 5L)))

        assertEquals(ChangeListVersions(mapOf("clients" to 5L)), SettingsSyncStatePersister(settings).read())
    }
}
