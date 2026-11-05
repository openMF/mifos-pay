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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.database.infra.entity.DraftEntity
import kpt.core.base.store.submit.SubmitOutboxStatus
import kotlin.time.Clock

/**
 * Drafts of a set of forms — one slot per form per owner ([formDraftSlot]).
 *
 * A draft is written when a submit does not land, and is either REPLAYED (offline ones, on
 * reconnect) or RESUMED in its form. Rows live in `framework_submit_drafts`, so the Sync & Drafts
 * screen's Retry / Discard / Prune act on them unchanged.
 */
interface FormDraftRepository {

    /** The draft in [slot] of [form] — pending, retrying or failed — or null. */
    fun observe(form: String, slot: String): Flow<SavedFormDraft?>

    /** Every draft of these forms, newest first — what Sync & Drafts lists. */
    fun observeAll(): Flow<List<SavedFormDraft>>

    /**
     * Keep [draft] in its slot, replacing whatever was there.
     *
     * [failure] null = the submit never reached the server (offline): PENDING, and the syncer sends
     * it on reconnect. Non-null = the server refused it: FAILED with that reason and NOT replayed —
     * resending what was just rejected would only fail again. The user resumes and fixes it, or
     * taps Retry in Sync & Drafts.
     */
    suspend fun save(slot: String, draft: FormDraft, failure: String? = null)

    /** Drop the slot's draft — on a successful submit, or Discard. */
    suspend fun delete(form: String, slot: String)
}

/** A stored [FormDraft] with its outbox bookkeeping — what a form offers and Sync & Drafts lists. */
data class SavedFormDraft(
    /** The outbox row id. */
    val id: Long,
    /** The [formDraftSlot] it occupies. */
    val slot: String,
    /** The kept input. */
    val draft: FormDraft,
    /** PENDING (will be replayed), RETRYING or FAILED. */
    val status: SubmitOutboxStatus,
    /** When it was last written, epoch millis. */
    val updatedAtMs: Long,
    /** Why the last attempt failed, or null. */
    val errorMessage: String?,
)

/**
 * Over the DAO rather than `RoomSubmitOutbox`: a form must see its FAILED draft too (the outbox's
 * observers are PENDING-only), and a save must replace the slot whatever its status (the outbox
 * upserts only a PENDING row, so saving over a FAILED one would leave two).
 *
 * @param forms the form keys this repository lists in [observeAll].
 */
class RoomFormDraftRepository(
    private val dao: DraftDao,
    private val forms: List<String>,
) : FormDraftRepository {

    override fun observe(form: String, slot: String): Flow<SavedFormDraft?> =
        dao.observeAllByFormKey(form).map { rows -> rows.firstOrNull { it.uniqueKey == slot }?.toSaved() }

    override fun observeAll(): Flow<List<SavedFormDraft>> =
        if (forms.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(forms.map { dao.observeAllByFormKey(it) }) { perForm ->
                perForm.flatMap { rows -> rows.mapNotNull { it.toSaved() } }.sortedByDescending { it.updatedAtMs }
            }
        }

    override suspend fun save(slot: String, draft: FormDraft, failure: String?) {
        val now = Clock.System.now().toEpochMilliseconds()
        dao.deleteByUniqueKey(draft.form, slot)
        dao.insert(
            DraftEntity(
                formKey = draft.form,
                uniqueKey = slot,
                payloadJson = FormDraftJson.encode(FormDraft.serializer(), draft),
                status = if (failure == null) SubmitOutboxStatus.PENDING.name else SubmitOutboxStatus.FAILED.name,
                createdAtMs = now,
                updatedAtMs = now,
                errorMessage = failure,
            ),
        )
    }

    override suspend fun delete(form: String, slot: String) = dao.deleteByUniqueKey(form, slot)

    /** Null when the row has no slot or its payload no longer decodes. */
    private fun DraftEntity.toSaved(): SavedFormDraft? {
        val draft = runCatching { FormDraftJson.decode(FormDraft.serializer(), payloadJson) }.getOrNull()
        val slot = uniqueKey
        return if (draft == null || slot == null) {
            null
        } else {
            SavedFormDraft(
                id = id,
                slot = slot,
                draft = draft,
                status = runCatching { SubmitOutboxStatus.valueOf(status) }.getOrDefault(SubmitOutboxStatus.PENDING),
                updatedAtMs = updatedAtMs,
                errorMessage = errorMessage,
            )
        }
    }
}
