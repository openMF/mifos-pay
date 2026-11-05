/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.submit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.component.KptButton
import kpt.core.base.designsystem.component.KptTextButton
import kpt.core.base.store.submit.DraftResumeState
import kpt.core.base.ui.generated.resources.Res
import kpt.core.base.ui.generated.resources.draft_discard
import kpt.core.base.ui.generated.resources.draft_resume
import kpt.core.base.ui.generated.resources.draft_unsaved
import org.jetbrains.compose.resources.stringResource

/**
 * Banner that surfaces a saved draft when [state] is [DraftResumeState.HasDraft].
 *
 * Renders nothing when [state] is [DraftResumeState.None].
 *
 * Place above the form in a Column, or inside the [MutationScreenContent] draft-aware overload.
 *
 * ```kotlin
 * DraftResumeBanner(
 *     state = draftState,
 *     onResume  = viewModel::onResumeDraft,
 *     onDiscard = viewModel::onDiscardDraft,
 * )
 * ```
 */
@Composable
fun DraftResumeBanner(
    state: DraftResumeState<*>,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state !is DraftResumeState.HasDraft) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.draft_unsaved),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                KptTextButton(onClick = onDiscard) {
                    Text(stringResource(Res.string.draft_discard))
                }
                KptButton(onClick = onResume) {
                    Text(stringResource(Res.string.draft_resume))
                }
            }
        }
    }
}
