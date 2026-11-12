package sample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import kotlin.test.Test
import kotlin.test.assertEquals

class WebDbTest {
    private lateinit var database: AppDatabase

    @Test
    fun rapidWrites() = runTest {
        val dao = database.billReminderDao
        val seen = mutableListOf<Int>()
        val job = launch(Dispatchers.Default) { dao.count().collect { seen += it } }
        withContext(Dispatchers.Default) {
            delay(400)
            dao.upsert(bill("R1"))
            dao.upsert(bill("R2"))
            var waited = 0L
            while (waited < 5_000 && seen.lastOrNull() != 2) { delay(50); waited += 50 }
            assertEquals(2, seen.lastOrNull())
        }
        job.cancel()
    }
}
