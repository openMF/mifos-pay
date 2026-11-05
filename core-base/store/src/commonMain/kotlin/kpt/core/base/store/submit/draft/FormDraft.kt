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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A form submission that did not reach the server, kept so it can be RESUMED in its form or
 * REPLAYED by an `OfflineSubmitSyncer` — one row type for every form, in `framework_submit_drafts`.
 *
 * It carries two copies of the same input because the two uses need different shapes: [payload] is
 * exactly what the submit sends (decoded at replay with the form's payload serializer), [state] is
 * the form's own snapshot (labels, picker selections, wizard step) so a resumed form looks as the
 * user left it. Both are JSON strings, which is what lets one table hold every form's drafts.
 *
 * @property form the form's key — also the outbox `formKey`.
 * @property title what Sync & Drafts calls this draft (a person's name, a record's title …).
 * @property ownerId the record the form belongs to, if any (the customer an application is for).
 * @property ownerKind what [ownerId] is, for a form that serves several kinds of owner.
 */
@Serializable
data class FormDraft(
    val form: String,
    val title: String,
    val ownerId: Long? = null,
    val ownerKind: String? = null,
    /** Exactly what the submit sends, as JSON; a replay decodes it with the form's payload serializer. */
    val payload: String,
    /** The form's own snapshot as JSON (labels, picker selections, wizard step), for Resume. */
    val state: String,
)

/** The outbox slot: one draft per form per owner — [kind] keeps owner 7 of two kinds apart. */
fun formDraftSlot(ownerId: Long?, kind: String? = null): String = ownerId?.let { "${kind ?: "owner"}:$it" } ?: "new"

/** The one JSON configuration drafts are written and read with. */
object FormDraftJson {
    /** Ignores unknown keys, so a draft saved by an older build still decodes. */
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** [value] as a draft JSON string. */
    fun <T> encode(serializer: KSerializer<T>, value: T): String = json.encodeToString(serializer, value)

    /** A draft JSON string back to [T]. Throws when it no longer matches [serializer]. */
    fun <T> decode(serializer: KSerializer<T>, value: String): T = json.decodeFromString(serializer, value)

    /** A replay decodes the payload with the form's own serializer. */
    fun <T> FormDraft.payloadAs(serializer: KSerializer<T>): T = decode(serializer, payload)
}
