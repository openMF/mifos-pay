/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.finance

/**
 * Solid colored dot used as the leading accent on a list row (bill reminder, loan due,
 * task). Pairs cheaply with any list-item layout to encode urgency at a glance without
 * stealing focus from the row's text content.
 */
@Composable
fun UrgencyDot(urgency: Urgency, modifier: Modifier = Modifier, size: Dp = 10.dp) {
    val color = resolveUrgencyColor(urgency)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun resolveUrgencyColor(urgency: Urgency): Color {
    val f = MaterialTheme.finance
    return when (urgency) {
        Urgency.Overdue -> f.urgencyOverdue
        Urgency.Today -> f.urgencyToday
        Urgency.Upcoming -> f.urgencyUpcoming
        Urgency.Distant -> f.urgencyDistant
    }
}
