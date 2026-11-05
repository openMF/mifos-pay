/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.push

/**
 * Server-originated push notification that mutates a [org.mobilenativefoundation.store.store5.MutableStore].
 *
 * The three variants map 1:1 to typical CRUD push payloads from a backend over
 * WebSocket / SSE / FCM data-message channels:
 *
 * - [Insert] — a new entity arrived; the local cache should add it.
 * - [Update] — an existing entity was modified server-side; the local cache should reflect
 *   the new value. (Merge vs. replace is determined by [PushMergeStrategy].)
 * - [Delete] — an entity was removed server-side; the local cache should evict it.
 *
 * Consumers wrap their transport (WebSocket frame parser, SSE event handler, FCM data
 * payload decoder) in a `Flow<PushEvent<K, V>>` and feed it to a [PushStoreAdapter].
 *
 * @param K Store key type (e.g. account id, transaction id).
 * @param V Store value type (the domain model already mapped from the wire payload).
 */
sealed interface PushEvent<K, V> {

    /**
     * A new entity arrived from the server.
     *
     * @property key identity of the new record.
     * @property value the record itself, already mapped to the domain model.
     */
    data class Insert<K, V>(val key: K, val value: V) : PushEvent<K, V>

    /**
     * An existing entity was modified server-side.
     *
     * @property key identity of the changed record.
     * @property value its new state, already mapped to the domain model.
     */
    data class Update<K, V>(val key: K, val value: V) : PushEvent<K, V>

    /**
     * An entity was removed server-side.
     *
     * @property key identity of the removed record. There is no value left to carry.
     */
    data class Delete<K, V>(val key: K) : PushEvent<K, V>
}
