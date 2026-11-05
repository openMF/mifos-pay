/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.sync.infra

import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration

internal actual fun syncNotifierConfiguration(): NotificationPlatformConfiguration =
    NotificationPlatformConfiguration.Android(
        // Framework fallback status-bar icon — a fork should override with its own
        // monochrome notification icon (R.drawable.…) for branded notifications.
        notificationIconResId = android.R.drawable.ic_dialog_info,
        notificationChannelData = NotificationPlatformConfiguration.Android.NotificationChannelData(
            id = "kpt.sync.notifications",
            name = "Sync",
            description = "Background sync + reminder notifications",
        ),
    )
