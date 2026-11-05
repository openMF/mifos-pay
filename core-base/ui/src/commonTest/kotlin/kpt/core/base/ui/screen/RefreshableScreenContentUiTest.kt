/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kotlin.test.Test

/**
 * Locks [RefreshableScreenContent]'s slot forwarding.
 *
 * It used to accept only `state` / `onRefresh` / `modifier` / `content` and hand the inner
 * [ScreenContent] nothing else, so a caller's branded [ScreenContent.empty] (or loading /
 * no-network / error) silently rendered the framework default instead. The sibling
 * `PagingScreenContent` — same module, same wrapped renderer — forwarded four of the five, so the
 * omission read as an oversight rather than a design choice.
 *
 * Each test passes a slot and asserts the slot's own content is what appears. A caller that brands
 * a state must not have to give up the pull gesture to do it.
 */
@OptIn(ExperimentalTestApi::class)
class RefreshableScreenContentUiTest {

    /** A trivial content slot, so each test asserts only which STATE slot was rendered. */
    private val content: @Composable (data: String, freshnessSignal: FreshnessSignal) -> Unit =
        { data, _ -> Text(text = data, modifier = Modifier.testTag(CONTENT)) }

    @Test
    fun aCallersEmptySlotIsRendered() = runComposeUiTest {
        setContent {
            RefreshableScreenContent(
                state = ScreenState.Empty,
                onRefresh = {},
                empty = { Text("none yet", modifier = Modifier.testTag(CUSTOM)) },
                content = content,
            )
        }
        onNodeWithTag(CUSTOM).assertExists()
    }

    @Test
    fun aCallersLoadingSlotIsRendered() = runComposeUiTest {
        setContent {
            RefreshableScreenContent(
                state = ScreenState.Loading,
                onRefresh = {},
                loading = { Text("working", modifier = Modifier.testTag(CUSTOM)) },
                content = content,
            )
        }
        onNodeWithTag(CUSTOM).assertExists()
    }

    @Test
    fun aCallersErrorSlotIsRendered() = runComposeUiTest {
        setContent {
            RefreshableScreenContent(
                state = ScreenState.Error(IllegalStateException("boom")),
                onRefresh = {},
                error = { Text("broke", modifier = Modifier.testTag(CUSTOM)) },
                content = content,
            )
        }
        onNodeWithTag(CUSTOM).assertExists()
    }

    @Test
    fun aCallersNoNetworkSlotIsRendered() = runComposeUiTest {
        setContent {
            RefreshableScreenContent(
                state = ScreenState.NoNetwork(isCaptivePortal = false),
                onRefresh = {},
                noNetwork = { Text("offline", modifier = Modifier.testTag(CUSTOM)) },
                content = content,
            )
        }
        onNodeWithTag(CUSTOM).assertExists()
    }

    @Test
    fun contentStillRendersWithNoSlotsSupplied() = runComposeUiTest {
        // The defaulted path, unchanged: every slot keeps its framework default, so existing call
        // sites are unaffected by the new parameters.
        setContent {
            RefreshableScreenContent(
                state = ScreenState.Content(data = "hello"),
                onRefresh = {},
                content = content,
            )
        }
        onNodeWithTag(CONTENT).assertExists()
    }

    private companion object {
        const val CUSTOM = "custom-slot"
        const val CONTENT = "content"
    }
}
