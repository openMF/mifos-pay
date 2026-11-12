/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.viewmodel

/**
 * MVI actions emitted by mutation-flow screens (form edit, settings save, etc.).
 *
 * Consumed by [BaseMutationViewModel]'s inherited action channel — UI calls
 * `viewModel.trySendAction(MutationAction.Submit(payload))` (or the convenience method
 * `onSubmit(payload)`) and the channel serializes processing through `handleAction`.
 *
 * @param T Domain payload type submitted to the server / repository.
 */
sealed interface MutationAction<out T> {

    /**
     * User confirmed the form — submit [payload] via the subclass's
     * [BaseMutationViewModel.performSubmit] implementation. While a submission is in flight,
     * additional Submit actions queued through the channel are processed sequentially.
     *
     * @property payload the value to submit. Must be serializable when the handler is draft-backed, since it has to
     *   survive process death.
     */
    data class Submit<T>(val payload: T) : MutationAction<T>

    /** Retry the last failed submission with the previously-submitted payload. */
    data object Retry : MutationAction<Nothing>

    /** Dismiss the result overlay and return the submit state to `SubmitState.Idle`. */
    data object Dismiss : MutationAction<Nothing>

    // ── Draft-resolution actions (the three-case resume flow) ────────────────────────────────
    // Emitted by draft-backed forms from DraftResolutionPrompt / DraftPickerList when the user
    // re-opens a form that already has a saved draft. No-ops for in-session (non-draft) forms.

    /** Resume the saved draft — the screen pre-fills from [MutationUiState.resumableDraft]; dismisses the prompt. */
    data object ResumeDraft : MutationAction<Nothing>

    /** Permanently delete the saved draft, then edit blank. */
    data object DiscardSavedDraft : MutationAction<Nothing>

    /** Keep the saved draft (recoverable in Settings → Sync & Drafts) but start a blank new entry. */
    data object StartFreshDraft : MutationAction<Nothing>
}
