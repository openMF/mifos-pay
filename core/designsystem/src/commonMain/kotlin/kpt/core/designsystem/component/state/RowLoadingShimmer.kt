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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.KptShimmerLoadingBox

/**
 * Single-row shimmer placeholder. Use inside LazyColumn `items()` while paged data loads,
 * or as a one-off row placeholder for a single record waiting on a network response.
 *
 * Layout: circle avatar + 2 stacked text bars (title + subtitle).
 */
@Composable
fun RowLoadingShimmer(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KptShimmerLoadingBox(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            KptShimmerLoadingBox(
                modifier = Modifier.fillMaxWidth().height(16.dp),
                shape = RoundedCornerShape(4.dp),
            )
            KptShimmerLoadingBox(
                modifier = Modifier.fillMaxWidth(0.6f).height(14.dp),
                shape = RoundedCornerShape(4.dp),
            )
        }
    }
}
