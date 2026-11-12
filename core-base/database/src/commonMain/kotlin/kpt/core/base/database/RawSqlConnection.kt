/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database

import androidx.room3.PooledConnection
import androidx.room3.RoomDatabase
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLITE_DATA_BLOB
import androidx.sqlite.SQLITE_DATA_FLOAT
import androidx.sqlite.SQLITE_DATA_INTEGER
import androidx.sqlite.SQLITE_DATA_NULL
import androidx.sqlite.SQLiteStatement

/**
 * Raw SQL against a Room database from common code, for statements a DAO cannot declare:
 * `ATTACH` / `DETACH`, `PRAGMA`, or SQL over a schema Room does not know (an attached file).
 *
 * Obtained from [withRawConnection]. Arguments bind as text, in order, to `?` placeholders.
 */
class RawSqlConnection internal constructor(private val connection: PooledConnection) {

    /** Runs [sql] to completion, ignoring any rows it returns. */
    suspend fun execute(sql: String, vararg args: String) {
        connection.usePrepared(sql) { statement: SQLiteStatement ->
            statement.bindAll(args)
            while (statement.stepRow()) Unit
        }
    }

    /**
     * Every row [sql] returns, each as a column-name map. SQLite integers arrive as [Long], reals as
     * [Double], text as [String], blobs as [ByteArray] and NULL as null.
     */
    suspend fun query(sql: String, vararg args: String): List<Map<String, Any?>> =
        connection.usePrepared(sql) { statement: SQLiteStatement ->
            statement.bindAll(args)
            val names = statement.getColumnNames()
            buildList {
                while (statement.stepRow()) {
                    add(names.indices.associate { i -> names[i] to statement.valueAt(i) })
                }
            }
        }

    private fun SQLiteStatement.bindAll(args: Array<out String>) {
        args.forEachIndexed { i, arg -> bindText(i + 1, arg) }
    }

    private fun SQLiteStatement.valueAt(i: Int): Any? = when (getColumnType(i)) {
        SQLITE_DATA_NULL -> null
        SQLITE_DATA_INTEGER -> getLong(i)
        SQLITE_DATA_FLOAT -> getDouble(i)
        SQLITE_DATA_BLOB -> getBlob(i)
        else -> getText(i)
    }
}

/**
 * Runs [block] on the database's writer connection, outside any transaction, so `ATTACH`, `DETACH`
 * and `PRAGMA` are allowed. The connection is held for the whole block: state such as an attached
 * schema carries across its statements, and the block must undo that state before returning.
 */
suspend fun <R> RoomDatabase.withRawConnection(block: suspend RawSqlConnection.() -> R): R =
    useWriterConnection { connection -> RawSqlConnection(connection).block() }

/**
 * Advances [this] by one row, returning false when no row remains. Expected per target because
 * `androidx.sqlite` declares `step()` separately for its synchronous and its web (asynchronous)
 * drivers, with no common declaration.
 */
internal expect suspend fun SQLiteStatement.stepRow(): Boolean
