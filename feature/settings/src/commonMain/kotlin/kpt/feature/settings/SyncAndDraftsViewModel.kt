/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kpt.core.base.store.infra.DraftInventory
import kpt.core.base.store.infra.DraftRecord
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.base.ui.viewmodel.BaseViewModel

/**
 * ViewModel for the template-level **Sync & Drafts** screen (Settings).
 *
 * A read-only cross-form window over [DraftInventory.observeAll] — the live feed of every draft
 * the app is holding — grouped into the three buckets the user cares about: **Drafts** (PENDING,
 * waiting to sync), **Syncing** (RETRYING, in-flight), and **Failed** (FAILED, needs attention).
 * Each row exposes the two payload-agnostic actions the cross-form screen can honor — **Discard**
 * (any row) and **Retry** (re-queue a failed row for the next online transition) — plus the manual
 * **Prune expired** action. (Resuming a draft into its original form is form-scoped and lives in
 * that form's draft picker, not here — a generic list can't route to an arbitrary form.)
 *
 * On the same MVI [BaseViewModel] idiom as every other feature.
 */
class SyncAndDraftsViewModel(
    private val draftInventory: DraftInventory,
) : BaseViewModel<SyncAndDraftsUiState, Nothing, SyncAndDraftsAction>(SyncAndDraftsUiState.Loading) {

    init {
        draftInventory.observeAll()
            .onEach { records -> updateState { records.toUiState() } }
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SyncAndDraftsAction) {
        when (action) {
            is SyncAndDraftsAction.Retry -> viewModelScope.launch { draftInventory.retry(action.id) }
            is SyncAndDraftsAction.Discard -> viewModelScope.launch { draftInventory.discard(action.id) }
            SyncAndDraftsAction.PruneExpired -> viewModelScope.launch { draftInventory.pruneExpired() }
        }
    }

    /** Re-queue a failed draft ([id]) for sync — the per-row Retry. */
    fun retry(id: Long) = trySendAction(SyncAndDraftsAction.Retry(id))

    /** Permanently discard a draft ([id]) — the per-row Discard (confirmed in the UI). */
    fun discard(id: Long) = trySendAction(SyncAndDraftsAction.Discard(id))

    /** Manually delete SUBMITTED/FAILED drafts past the retention window — the Prune expired action. */
    fun pruneExpired() = trySendAction(SyncAndDraftsAction.PruneExpired)
}

private fun List<DraftRecord>.toUiState(): SyncAndDraftsUiState.Success = SyncAndDraftsUiState.Success(
    drafts = filter { it.status == SubmitOutboxStatus.PENDING },
    syncing = filter { it.status == SubmitOutboxStatus.RETRYING },
    failed = filter { it.status == SubmitOutboxStatus.FAILED },
)

/** Cross-form draft actions accepted by [SyncAndDraftsViewModel]. */
sealed interface SyncAndDraftsAction {
    /**
     * Re-queue the failed draft [id] for sync (FAILED → PENDING).
     *
     * @property id Id of the failed draft to retry.
     */
    data class Retry(val id: Long) : SyncAndDraftsAction

    /**
     * Permanently delete the draft [id].
     *
     * @property id Id of the draft to delete.
     */
    data class Discard(val id: Long) : SyncAndDraftsAction

    /** Delete SUBMITTED/FAILED drafts older than the retention window. */
    data object PruneExpired : SyncAndDraftsAction
}

/** UI state for the Sync & Drafts screen. */
sealed interface SyncAndDraftsUiState {
    /** Initial state before the first [DraftInventory.observeAll] emission. */
    data object Loading : SyncAndDraftsUiState

    /**
     * The live grouped feed. Terminal SUBMITTED rows are never surfaced (they aren't actionable).
     *
     * @param drafts  PENDING rows — saved locally, waiting to sync.
     * @param syncing RETRYING rows — currently claimed by a syncer, in-flight.
     * @param failed  FAILED rows — the last sync attempt failed; each carries an error message.
     */
    data class Success(
        val drafts: List<DraftRecord>,
        val syncing: List<DraftRecord>,
        val failed: List<DraftRecord>,
    ) : SyncAndDraftsUiState {
        /** True when there is nothing to sync and no drafts held — drives the empty state. */
        val isEmpty: Boolean get() = drafts.isEmpty() && syncing.isEmpty() && failed.isEmpty()
    }
}
