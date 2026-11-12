/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.submit.draft

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult

/**
 * One form's draft slot — what a ViewModel holds so drafts cost it a few lines.
 *
 * [S] is the form's own snapshot type (what [resume] gives back), [P] the payload its submit sends.
 * The form calls [keep] when a submit does not land, [clear] when it does, and renders [offered]
 * as the Resume / Discard banner (`FormDraftBanner` in core-base/ui).
 */
class FormDraftSlot<S, P>(
    private val drafts: FormDraftRepository,
    private val scope: CoroutineScope,
    /** The form's key — also the outbox `formKey`. */
    val form: String,
    private val ownerId: Long?,
    private val stateSerializer: KSerializer<S>,
    private val payloadSerializer: KSerializer<P>,
    private val ownerKind: String? = null,
) {
    private val slot = formDraftSlot(ownerId, ownerKind)

    /** Hidden once resumed or discarded this visit — the row itself stays until the submit lands. */
    private val handled = MutableStateFlow(false)

    /** The draft to offer on this form, or null. */
    val offered: StateFlow<SavedFormDraft?> =
        combine(drafts.observe(form, slot), handled) { draft, done -> draft.takeUnless { done } }
            .stateIn(scope, SharingStarted.Eagerly, null)

    /** The offered draft's snapshot, and the banner goes. Null when there is nothing to resume. */
    fun resume(): S? {
        val draft = offered.value ?: return null
        handled.value = true
        return runCatching { FormDraftJson.decode(stateSerializer, draft.draft.state) }.getOrNull()
    }

    /** Hide the banner and delete the draft. */
    fun discard() {
        handled.value = true
        scope.launch { drafts.delete(form, slot) }
    }

    /**
     * Keep this submit. [failure] null = never reached the server (replayed on reconnect);
     * otherwise the server's reason, kept FAILED and not replayed.
     */
    fun keep(title: String, state: S, payload: P, failure: String?) {
        handled.value = true
        scope.launch {
            drafts.save(
                slot,
                FormDraft(
                    form = form,
                    title = title,
                    ownerId = ownerId,
                    ownerKind = ownerKind,
                    payload = FormDraftJson.encode(payloadSerializer, payload),
                    state = FormDraftJson.encode(stateSerializer, state),
                ),
                failure,
            )
        }
    }

    /** The submit landed: nothing left to keep. */
    fun clear() {
        scope.launch { drafts.delete(form, slot) }
    }
}

/**
 * True when the write never reached the server — the case a draft is kept for automatically.
 *
 * Both shapes of it: the gateway refused to send (`Blocked(OFFLINE)`), or it sent and the request
 * failed on the network (a [categorize] Network failure — the monitor had not yet seen the
 * connection drop; device, 2026-10-26: airplane mode mid-form produced the second, and the create
 * showed a generic error instead of being kept).
 */
val MutationResult<*>.isOffline: Boolean
    get() = (this is MutationResult.Blocked && reason == BlockReason.OFFLINE) ||
        (this is MutationResult.Failed && categorize(cause) == ErrorCategory.Network)

/**
 * A network failure as the `Blocked(OFFLINE)` it really is, so a form's existing offline handling
 * (keep the draft, say "no connection") covers both shapes of [isOffline]. Anything else unchanged.
 */
fun <T> MutationResult<T>.offlineWhenUnsent(): MutationResult<T> =
    if (this is MutationResult.Failed && isOffline) MutationResult.Blocked(BlockReason.OFFLINE) else this

/**
 * Why a write did not land, for a FAILED draft. [reasonOf] extracts the server's own message from
 * a failure's cause (apps parse their backend's error body); the cause's message is the fallback.
 */
fun MutationResult<*>.failureReason(reasonOf: (Throwable) -> String? = { null }): String = when (this) {
    is MutationResult.Failed -> reasonOf(cause) ?: cause.message ?: "Not accepted by the server."
    is MutationResult.Blocked -> reason.name
    is MutationResult.Conflicted -> "Conflicted"
    is MutationResult.Applied -> ""
}
