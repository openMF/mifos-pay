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
    NotificationPlatformConfiguration.Ios()
