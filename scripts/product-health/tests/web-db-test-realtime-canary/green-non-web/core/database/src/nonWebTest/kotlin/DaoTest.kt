package sample

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import kotlin.test.Test
import kotlin.test.assertEquals

class DaoTest {
    private lateinit var database: AppDatabase

    @Test
    fun countReflectsInserts() = runTest {
        database.billReminderDao.count().test {
            assertEquals(0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
