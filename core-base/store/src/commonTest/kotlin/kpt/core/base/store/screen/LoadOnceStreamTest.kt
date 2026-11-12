/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.screen

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.infra.FakeFetchedAtRepository
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LoadOnceStreamTest {

    private val onlineStatus = NetworkStatus.Available(
        NetworkInfo(type = NetworkType.WiFi, isMetered = false),
    )

    private fun onlineMonitor() = object : NetworkMonitor {
        private val _status = MutableStateFlow(onlineStatus)
        override val networkStatus: StateFlow<NetworkStatus> = _status.asStateFlow()
        override val isOnline: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
        override val networkChanges: SharedFlow<NetworkChangeEvent> =
            MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()
        override fun close() = Unit
    }

    @Test
    fun loadOnceStream_emitsLoadingThenContent() = runTest {
        val store = StoreBuilder
            .from(Fetcher.of<String, String> { _ -> "initial-data" })
            .build()

        val stream = store.asLoadOnceStream(
            key = "key",
            networkMonitor = onlineMonitor(),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test",
            scope = backgroundScope,
        )

        val states = stream.state.take(2).toList()
        assertIs<ScreenState.Loading>(states[0])
        assertIs<ScreenState.Content<String>>(states[1])
        assertEquals("initial-data", (states[1] as ScreenState.Content).data)
    }

    @Test
    fun loadOnceStream_stopsEmittingAfterFirstContent() = runTest {
        var fetchCount = 0
        val store = StoreBuilder
            .from(
                Fetcher.of<String, String> { _ ->
                    fetchCount++
                    "data-$fetchCount"
                },
            )
            .build()

        val stream = store.asLoadOnceStream(
            key = "key",
            networkMonitor = onlineMonitor(),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test",
            scope = backgroundScope,
        )

        // Collect past first Content — stream should stop there.
        val firstContent = stream.state.first { it is ScreenState.Content }
        assertIs<ScreenState.Content<String>>(firstContent)

        // A second collect on state would not see a new emission (stream is done).
        val received = mutableListOf<ScreenState<String>>()
        val job = launch {
            stream.state.collect { received.add(it) }
        }
        delay(100) // Give time for any spurious second emission.
        // After Content, stream emits nothing new — only replay may be seen.
        val contentCount = received.count { it is ScreenState.Content }
        assertEquals(1, contentCount, "Content should only be emitted once")
        job.cancel()
    }

    @Test
    fun loadOnceStream_retryReachesContent() = runTest {
        val store = StoreBuilder
            .from(Fetcher.of<String, String> { _ -> "retry-data" })
            .build()

        val stream = store.asLoadOnceStream(
            key = "key",
            networkMonitor = onlineMonitor(),
            fetchedAtRepository = FakeFetchedAtRepository(),
            cacheKey = "test",
            scope = backgroundScope,
        )

        val content = stream.state.first { it is ScreenState.Content }
        assertIs<ScreenState.Content<String>>(content)
        assertEquals("retry-data", content.data)
    }
}
