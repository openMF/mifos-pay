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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

/**
 * A custom [KSerializer] for date fields that can be represented in JSON
 * either as a string (`"YYYY-MM-DD"`) or an array of integers (`[YYYY, MM, DD]`).
 *
 * This serializer ensures consistent deserialization to a `String` in the
 * format `"YYYY-MM-DD"`, regardless of the JSON input format.
 *
 * ### Supported Formats:
 * - `"2025-07-02"`
 * - `[2025, 7, 2]` → will be converted to `"2025-07-02"`
 *
 * ### Example Usage:
 * ```
 * @Serializable
 * data class Transfer(
 *     @Serializable(with = DateAsStringSerializer::class)
 *     val transferDate: String
 * )
 * ```
 */
object DateAsStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "DateAsString", kind = PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        val input = decoder as? JsonDecoder ?: error("Only JSON supported")
        val element = input.decodeJsonElement()
        return when (element) {
            is JsonArray -> {
                val parts = element.map { it.jsonPrimitive.int }
                formatDateFromParts(parts)
            }

            is JsonPrimitive -> element.content

            else -> error("Unexpected Date format: $element")
        }
    }
}

/**
 * A custom [KSerializer] for serializing and deserializing an `ImmutableList<String>` using kotlinx.serialization.
 *
 * This serializer bridges the gap between Kotlin's `ImmutableList` from the [kotlinx.collections.immutable] package
 * and standard JSON array representations. Internally, it uses a [ListSerializer] to handle the
 * (de)serialization as a regular `List<String>`, then converts to or from an `ImmutableList`.
 *
 * ### Example Usage:
 * ```
 * @Serializable
 * data class UserPreferences(
 *     @Serializable(with = ImmutableListSerializer::class)
 *     val favoriteTags: ImmutableList<String>
 * )
 * ```
 *
 * ### Supported Format:
 * - JSON Array: `["tag1", "tag2", "tag3"]`
 *
 * ### Notes:
 * - During serialization, the `ImmutableList` is converted to a regular list before encoding.
 * - During deserialization, the resulting list is converted to an `ImmutableList` using `.toPersistentList()`.
 */
object ImmutableListSerializer : KSerializer<ImmutableList<String>> {
    override val descriptor = ListSerializer(String.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: ImmutableList<String>) {
        ListSerializer(String.serializer()).serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ImmutableList<String> {
        return ListSerializer(String.serializer()).deserialize(decoder).toPersistentList()
    }
}

/**
 * Formats a date represented as a list of integers into a string of the format `"YYYY-MM-DD"`.
 *
 * @param parts A list of exactly three integers representing `[year, month, day]`.
 * @return A zero-padded string in the format `"YYYY-MM-DD"`.
 * @throws IllegalArgumentException if the list does not contain exactly 3 elements.
 */
private fun formatDateFromParts(parts: List<Int>): String {
    require(parts.size == 3) { "Date parts must contain exactly 3 elements: [year, month, day]" }
    val year = parts[0]
    val month = parts[1].toString().padStart(2, '0')
    val day = parts[2].toString().padStart(2, '0')
    return "$year-$month-$day"
}
