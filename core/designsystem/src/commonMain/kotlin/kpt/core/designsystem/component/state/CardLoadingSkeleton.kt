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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.KptShimmerLoadingBox

/**
 * Whole-card shimmer placeholder. Drop in for `AppCard` / `Card` while the underlying data
 * loads. Renders 3 stacked shimmer bars to suggest a typical card layout (title + 2 body
 * lines).
 */
@Composable
fun CardLoadingSkeleton(modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KptShimmerLoadingBox(
                modifier = Modifier.fillMaxWidth().height(20.dp),
                shape = RoundedCornerShape(4.dp),
            )
            KptShimmerLoadingBox(
                modifier = Modifier.fillMaxWidth(0.7f).height(16.dp),
                shape = RoundedCornerShape(4.dp),
            )
            KptShimmerLoadingBox(
                modifier = Modifier.fillMaxWidth(0.5f).height(16.dp),
                shape = RoundedCornerShape(4.dp),
            )
        }
    }
}
