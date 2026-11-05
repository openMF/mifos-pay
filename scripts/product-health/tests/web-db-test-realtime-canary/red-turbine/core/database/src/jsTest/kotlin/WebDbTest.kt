package sample

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import kotlin.test.Test
import kotlin.test.assertEquals

class WebDbTest {
    private lateinit var database: AppDatabase

    @Test
    fun rapidWrites() = runTest {
        val dao = database.billReminderDao
        dao.count().test {
            assertEquals(0, awaitItem())
            dao.upsert(bill("R1"))
            dao.upsert(bill("R2"))
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
