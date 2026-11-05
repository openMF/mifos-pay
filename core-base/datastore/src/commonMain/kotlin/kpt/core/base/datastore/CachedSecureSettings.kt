/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.datastore

import com.russhwolf.settings.Settings

/**
 * A [Settings] view over an already-decrypted in-memory map.
 *
 * Platform-agnostic on purpose: it holds no browser, file, or crypto types, which is what lets
 * [SecureStoreCore] and this class be tested on every target rather than only where a browser is.
 *
 * Every value is held as a String; the typed accessors parse on read. That keeps the persisted form
 * a plain `Map<String, String>`, which serializes to JSON for a single encrypt-and-store.
 *
 * [onMutate] fires after any change so the owner can schedule a debounced flush.
 */
internal class CachedSecureSettings(
    private val cache: MutableMap<String, String>,
    private val onMutate: () -> Unit,
) : Settings {

    override val keys: Set<String> get() = cache.keys.toSet()
    override val size: Int get() = cache.size

    override fun clear() {
        cache.clear()
        onMutate()
    }

    override fun remove(key: String) {
        if (cache.remove(key) != null) onMutate()
    }

    override fun hasKey(key: String): Boolean = cache.containsKey(key)

    private fun put(key: String, value: String) {
        cache[key] = value
        onMutate()
    }

    override fun putInt(key: String, value: Int) = put(key, value.toString())
    override fun getInt(key: String, defaultValue: Int): Int = getIntOrNull(key) ?: defaultValue
    override fun getIntOrNull(key: String): Int? = cache[key]?.toIntOrNull()

    override fun putLong(key: String, value: Long) = put(key, value.toString())
    override fun getLong(key: String, defaultValue: Long): Long = getLongOrNull(key) ?: defaultValue
    override fun getLongOrNull(key: String): Long? = cache[key]?.toLongOrNull()

    override fun putString(key: String, value: String) = put(key, value)
    override fun getString(key: String, defaultValue: String): String = cache[key] ?: defaultValue
    override fun getStringOrNull(key: String): String? = cache[key]

    override fun putFloat(key: String, value: Float) = put(key, value.toString())
    override fun getFloat(key: String, defaultValue: Float): Float = getFloatOrNull(key) ?: defaultValue
    override fun getFloatOrNull(key: String): Float? = cache[key]?.toFloatOrNull()

    override fun putDouble(key: String, value: Double) = put(key, value.toString())
    override fun getDouble(key: String, defaultValue: Double): Double = getDoubleOrNull(key) ?: defaultValue
    override fun getDoubleOrNull(key: String): Double? = cache[key]?.toDoubleOrNull()

    override fun putBoolean(key: String, value: Boolean) = put(key, value.toString())
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        getBooleanOrNull(key) ?: defaultValue
    override fun getBooleanOrNull(key: String): Boolean? = cache[key]?.toBooleanStrictOrNull()
}
