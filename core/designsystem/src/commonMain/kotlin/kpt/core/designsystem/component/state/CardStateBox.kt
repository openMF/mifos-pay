/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.store.screen.ScreenState

/**
 * State-aware Card wrapper. Renders the right component-scale UI for each [ScreenState]
 * variant *inside* the Card bounds — no full-screen overlay. Use for individual cards on
 * a dashboard, where each card resolves its own data independently.
 *
 *  - [ScreenState.Loading] → [CardLoadingSkeleton]
 *  - [ScreenState.Empty] → centered "No data" text
 *  - [ScreenState.Error] → [InlineErrorPill] with the error message
 *  - [ScreenState.NoNetwork] → [InlineErrorPill] with "Offline"
 *  - [ScreenState.Unauthenticated] → [InlineErrorPill] with "Sign in required"
 *  - [ScreenState.Content] → user-supplied [content] lambda receives the payload
 */
@Composable
fun <T> CardStateBox(
    state: ScreenState<T>,
    modifier: Modifier = Modifier,
    emptyText: String = "No data",
    offlineText: String = "Offline",
    unauthText: String = "Sign in required",
    content: @Composable (T) -> Unit,
) {
    when (state) {
        is ScreenState.Loading -> CardLoadingSkeleton(modifier = modifier)
        is ScreenState.Empty -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        is ScreenState.Error -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                InlineErrorPill(message = state.error.message ?: "Something went wrong")
            }
        }
        is ScreenState.NoNetwork -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                InlineErrorPill(message = offlineText)
            }
        }
        is ScreenState.Unauthenticated -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                InlineErrorPill(message = unauthText)
            }
        }
        is ScreenState.Content -> content(state.data)
    }
}
