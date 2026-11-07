/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.notification

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.model.notification.Notification
import kpt.core.ui.EmptyContentScreen
import kpt.feature.notification.generated.resources.Res
import kpt.feature.notification.generated.resources.feature_notification_nothing_to_notify
import kpt.feature.notification.generated.resources.feature_notification_notifications
import kpt.feature.notification.generated.resources.feature_notification_there_is_nothing_to_show
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun NotificationScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewmodel: NotificationViewModel = koinViewModel(),
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    NotificationScreen(
        state = state,
        onRetry = viewmodel::retry,
        modifier = modifier,
        navigateBack = navigateBack,
    )
}

@Composable
@VisibleForTesting
internal fun NotificationScreen(
    state: ScreenState<List<Notification>>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    ForkScaffold(
        topBarTitle = stringResource(Res.string.feature_notification_notifications),
        backPress = navigateBack,
        modifier = modifier,
        containerColor = KptTheme.colorScheme.background,
    ) { padding ->
        // Template idiom: `ScreenContent` (core-base/ui) owns every render branch —
        // loading / empty / no-network / unauthenticated / error+retry — driven by
        // the stream's pre-decided `ScreenState`. Only the per-item Content body is
        // authored here; the empty state keeps the feature's existing copy.
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            empty = {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_notification_nothing_to_notify),
                    subTitle = stringResource(Res.string.feature_notification_there_is_nothing_to_show),
                )
            },
        ) { notifications, _ ->
            NotificationList(notifications = notifications)
        }
    }
}

@Composable
private fun NotificationList(
    notifications: List<Notification>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        items(
            items = notifications,
            key = { it.id },
        ) { notification ->
            NotificationListItem(notification = notification)
        }
    }
}

@Composable
private fun NotificationListItem(
    notification: Notification,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(text = notification.content)
            },
            trailingContent = {
                Text(text = notification.formattedDate)
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Preview
@Composable
private fun NotificationScreenPreview() {
    ForkTheme {
        NotificationScreen(
            state = ScreenState.Content(sampleNotificationList),
            onRetry = {},
            navigateBack = {},
        )
    }
}

internal val sampleNotificationList = List(10) {
    Notification(
        id = it.toLong(),
        objectType = "duo",
        objectId = 9851,
        action = "nisl",
        actorId = 9344,
        content = "non",
        isRead = false,
        isSystemGenerated = false,
        createdAt = "bibendum",
    )
}
