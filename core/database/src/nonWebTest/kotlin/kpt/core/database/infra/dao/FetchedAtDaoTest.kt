/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.infra.dao

import kotlinx.coroutines.test.runTest
import kpt.core.base.database.infra.dao.FetchedAtDao
import kpt.core.base.database.infra.entity.FetchedAtEntity
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Verifies the framework-owned `framework_fetched_at` table backing
 * [kpt.core.base.store.FetchedAtRepository] via [FetchedAtDao].
 *
 * Locks the contract for warm-reopen of paginated and single-key streams: write
 * a timestamp on every successful network fetch, read it back on subsequent
 * stream construction so the staleness banner shows real "Updated 5m ago".
 */
class FetchedAtDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FetchedAtDao

    @BeforeTest
    fun setup() {
        // Build through `testPlatformModule`, NOT a hand-rolled Room builder. The driver is
        // per-platform — BundledSQLiteDriver on JVM/native, the web-worker driver on js/wasmJs —
        // so naming one here pins the test to that platform. That is exactly why every DAO test
        // in this module lived in `desktopTest` and no other target ever exercised the schema.
        startKoin { modules(testPlatformModule) }
        database = KoinPlatform.getKoin().get()
        dao = database.fetchedAtDao
    }

    @AfterTest
    fun teardown() {
        // stopKoin() must run even when setup failed, or the FIRST failure leaks a started Koin
        // and every later test dies with KoinApplicationAlreadyStartedException — one real fault
        // reported as four, with three of them meaningless.
        runCatching { database.close() }
        stopKoin()
    }

    @Test
    fun readUnknownKeyReturnsNull() = runTest {
        assertNull(dao.read("never-written"))
    }

    @Test
    fun upsertThenReadReturnsTimestamp() = runTest {
        val now = 1_762_000_000_000L
        dao.upsert(FetchedAtEntity(storeKey = "crypto:coinMarkets", lastFetchedMillis = now))
        assertEquals(now, dao.read("crypto:coinMarkets"))
    }

    @Test
    fun upsertOverwritesPreviousValue() = runTest {
        dao.upsert(FetchedAtEntity(storeKey = "k", lastFetchedMillis = 100L))
        dao.upsert(FetchedAtEntity(storeKey = "k", lastFetchedMillis = 200L))
        assertEquals(200L, dao.read("k"))
    }

    @Test
    fun differentKeysIsolated() = runTest {
        dao.upsert(FetchedAtEntity(storeKey = "alpha", lastFetchedMillis = 111L))
        dao.upsert(FetchedAtEntity(storeKey = "beta", lastFetchedMillis = 222L))
        assertEquals(111L, dao.read("alpha"))
        assertEquals(222L, dao.read("beta"))
    }
}
