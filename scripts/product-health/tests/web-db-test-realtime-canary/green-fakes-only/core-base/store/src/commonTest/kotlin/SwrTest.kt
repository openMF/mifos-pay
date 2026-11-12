package sample

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SwrTest {
    @Test
    fun reEmits() = runTest {
        val source = MutableStateFlow(0)
        source.test {
            assertEquals(0, awaitItem())
            source.value = 1
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
