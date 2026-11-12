/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)

package kpt.core.base.datastore.infra

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

private val settingsJson = Json { ignoreUnknownKeys = true }

/**
 * Store [value] as ONE JSON string at [key].
 *
 * Use this, not multiplatform-settings' `encodeValue`, for any type with a collection property that
 * has a default. That library writes a list as `key.field.size` + `key.field.N`, but its decoder
 * skips an optional property whose bare `key.field` is absent — which a collection never writes —
 * so every defaulted List/Map/Set reads back EMPTY after a restart (measured on 1.3.0: a session's
 * permission list and the sync change-list versions were both lost on every cold start).
 */
fun <T> Settings.putJson(serializer: KSerializer<T>, key: String, value: T) {
    putString(key, settingsJson.encodeToString(serializer, value))
}

/**
 * Read a value written by [putJson]. A value saved earlier in `encodeValue`'s per-field layout lives
 * only under `key.*` sub-keys, never the bare key, so it is read through that decoder instead — with
 * the same lossy collections it always had, until the next write replaces it. Only when such
 * sub-keys exist: for an all-defaults type that decoder answers a MISSING key with a default object.
 */
fun <T> Settings.getJsonOrNull(serializer: KSerializer<T>, key: String): T? =
    getStringOrNull(key)?.let { runCatching { settingsJson.decodeFromString(serializer, it) }.getOrNull() }
        ?: keys.takeIf { all -> all.any { it.startsWith("$key.") } }?.let { decodeValueOrNull(serializer, key) }
