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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kpt.core.base.ui.generated.resources.Res
import kpt.core.base.ui.generated.resources.draft_discard
import kpt.core.base.ui.generated.resources.draft_failed
import kpt.core.base.ui.generated.resources.draft_pending
import kpt.core.base.ui.generated.resources.draft_resume
import kpt.core.base.ui.generated.resources.draft_save
import kpt.core.base.ui.generated.resources.draft_saved
import kpt.core.base.ui.generated.resources.draft_saved_offline
import org.jetbrains.compose.resources.stringResource

/**
 * "You have a saved draft" with Resume / Discard — shown when a create form opens on a slot that
 * holds one. Unlike `DraftResumeBanner` (an outbox entry, PENDING only) it also says WHY the draft
 * exists: [failureReason] null = it is waiting to send, non-null = the server refused it, with the
 * server's reason.
 */
@Composable
fun FormDraftBanner(
    failureReason: String?,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val failed = failureReason != null
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp).testTag(FormDraftTestTags.BANNER),
        colors = CardDefaults.cardColors(
            containerColor = if (failed) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(if (failed) Res.string.draft_failed else Res.string.draft_pending),
                style = MaterialTheme.typography.titleSmall,
            )
            failureReason?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End),
            ) {
                TextButton(onClick = onDiscard, modifier = Modifier.testTag(FormDraftTestTags.DISCARD)) {
                    Text(stringResource(Res.string.draft_discard))
                }
                Button(onClick = onResume, modifier = Modifier.testTag(FormDraftTestTags.RESUME)) {
                    Text(stringResource(Res.string.draft_resume))
                }
            }
        }
    }
}

/**
 * Under a create that did not land: what went wrong, the server's own reason, and then either the
 * note that it was kept as a draft ([draftSaved]) or a "Save as draft" button. Offline creates are
 * kept automatically; a refused one is kept only when the officer asks.
 */
@Composable
fun FormDraftFailure(
    message: String,
    serverReason: String?,
    draftSaved: Boolean,
    offline: Boolean,
    onSaveDraft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        serverReason?.takeIf { it.isNotBlank() && it != message }?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        if (draftSaved) {
            Text(
                text = stringResource(if (offline) Res.string.draft_saved_offline else Res.string.draft_saved),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(FormDraftTestTags.SAVED),
            )
        } else {
            TextButton(onClick = onSaveDraft, modifier = Modifier.testTag(FormDraftTestTags.SAVE)) {
                Text(stringResource(Res.string.draft_save))
            }
        }
    }
}

/** Test tags for [FormDraftBanner] and [FormDraftFailure]. */
object FormDraftTestTags {
    /** The "Save draft" button under a failed create. */
    const val SAVE = "form-draft:save"

    /** The "Draft saved" confirmation that replaces it. */
    const val SAVED = "form-draft:saved"

    /** The Resume / Discard banner. */
    const val BANNER = "form-draft:banner"

    /** The banner's Resume button. */
    const val RESUME = "form-draft:resume"

    /** The banner's Discard button. */
    const val DISCARD = "form-draft:discard"
}
