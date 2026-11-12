/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

/**
 * WasmJs factory for creating Room 3 database instances.
 *
 * Workers are bundled locally via webpack (no CDN dependency) using the
 * `sqlite-wasm-worker` npm package declared in core-base/database/build.gradle.kts.
 * webpack resolves `new URL("sqlite-wasm-worker/worker.js", import.meta.url)` at
 * build time and emits the worker + its WASM dependency as a separate bundle.
 *
 * The worker handles OPFS persistent storage when crossOriginIsolated=true
 * (localhost with COOP/COEP headers) and falls back to in-memory when OPFS is
 * unavailable (e.g. GitHub Pages without the required response headers).
 */
@PublishedApi
@OptIn(ExperimentalWasmJsInterop::class)
internal fun createSQLiteWasmWorker(): Worker =
    js("""new Worker(new URL("sqlite-wasm-worker/worker.js", import.meta.url))""")

/**
 * Alternative driver backed by sql.js (in-memory only, broader browser compatibility).
 * Switch by calling `createSqlJsWorker()` in `createDatabase` / `createInMemoryDatabase`.
 */
@PublishedApi
@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("unused")
internal fun createSqlJsWorker(): Worker =
    js("""new Worker(new URL("sql-js-worker/worker.js", import.meta.url))""")

/** Web (WasmJS) implementation of `AppDatabaseFactory`. */
class AppDatabaseFactory {

    /** Opens (or creates) the app database for this target, at the platform's own storage location. */
    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
    ): RoomDatabase.Builder<T> {
        return Room.databaseBuilder<T>(name = databaseName)
            .setDriver(WebWorkerSQLiteDriver(createSQLiteWasmWorker()))
    }

    /** An in-memory database for tests — discarded with the process, so each test starts clean. */
    inline fun <reified T : RoomDatabase> createInMemoryDatabase(): RoomDatabase.Builder<T> {
        return Room.inMemoryDatabaseBuilder<T>()
            .setDriver(WebWorkerSQLiteDriver(createSQLiteWasmWorker()))
    }
}
