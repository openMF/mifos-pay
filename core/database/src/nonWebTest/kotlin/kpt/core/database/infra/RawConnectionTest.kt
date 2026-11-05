/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.infra

import kotlinx.coroutines.test.runTest
import kpt.core.base.database.withRawConnection
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [withRawConnection] runs statements a DAO cannot declare — PRAGMA, ATTACH — on the writer connection,
 * and returns query rows as typed column maps.
 */
class RawConnectionTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        startKoin { modules(testPlatformModule) }
        database = KoinPlatform.getKoin().get()
    }

    @AfterTest
    fun teardown() {
        runCatching { database.close() }
        stopKoin()
    }

    @Test
    fun pragmaRunsOutsideATransaction() = runTest {
        val version = database.withRawConnection {
            execute("PRAGMA user_version = 42")
            query("PRAGMA user_version").single()["user_version"]
        }
        assertEquals(42L, version)
    }

    @Test
    fun queryBindsArgumentsAndTypesColumns() = runTest {
        val row = database.withRawConnection {
            query("SELECT ? AS t, 7 AS i, 1.5 AS r, NULL AS n", "hello").single()
        }
        assertEquals(mapOf("t" to "hello", "i" to 7L, "r" to 1.5, "n" to null), row)
    }
}
