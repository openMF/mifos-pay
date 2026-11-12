/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common

import androidx.lifecycle.SavedStateHandle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Retrieves a serialized object from [SavedStateHandle] by decoding it from a JSON string.
 *
 * @param key The key under which the object is stored.
 * @return The decoded object of type [T], or `null` if the key does not exist or decoding fails.
 *
 * @throws kotlinx.serialization.SerializationException If the stored JSON is malformed or incompatible.
 *
 * Example usage:
 * ```
 * val state: MyState? = savedStateHandle.getSerialized("myKey")
 * ```
 */
inline fun <reified T> SavedStateHandle.getSerialized(key: String): T? {
    return get<String>(key)?.let { Json.decodeFromString(it) }
}

/**
 * Serializes the given object to JSON and stores it in [SavedStateHandle] under the specified key.
 *
 * @param key The key under which to store the serialized object.
 * @param value The object to serialize and store.
 *
 * Example usage:
 * ```
 * savedStateHandle.setSerialized("myKey", MyState(...))
 * ```
 */
inline fun <reified T> SavedStateHandle.setSerialized(key: String, value: T) {
    set(key, Json.encodeToString(value))
}
