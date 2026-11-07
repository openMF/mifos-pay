/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.utility

import androidx.compose.runtime.Composable

/**
 * A tab together with its page content.
 *
 * @property tabName Tab title.
 * @property content Composable content of the tab page.
 */
data class TabContent(
    val tabName: String,
    val content: @Composable () -> Unit,
)
