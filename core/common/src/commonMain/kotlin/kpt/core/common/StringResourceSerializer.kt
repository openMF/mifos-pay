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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.compose.resources.StringResource

/**
 * A custom [KSerializer] for [StringResource] that enables serialization of StringResource objects.
 *
 * This serializer extracts the resource key from StringResource during serialization
 * and reconstructs the StringResource during deserialization using a registry.
 *
 * ### Usage:
 * ```kotlin
 * @Serializable
 * data class DialogState(
 *     @Serializable(with = StringResourceSerializer::class)
 *     val message: StringResource
 * )
 * ```
 */
object StringResourceSerializer : KSerializer<StringResource> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "StringResource",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: StringResource) {
        val resourceKey = extractResourceKey(value)
        encoder.encodeString(resourceKey)
    }

    override fun deserialize(decoder: Decoder): StringResource {
        val resourceKey = decoder.decodeString()
        return StringResourceRegistry.getResource(resourceKey)
            ?: throw IllegalArgumentException("StringResource not found for key: $resourceKey")
    }
}

/**
 * Extracts the resource key from a StringResource object.
 *
 * @param stringResource The StringResource to extract the key from
 * @return The resource key as a string
 */
private fun extractResourceKey(stringResource: StringResource): String {
    val stringRepresentation = stringResource.toString()

    val keyPattern = Regex("key=([^,\\s)]+)")
    val match = keyPattern.find(stringRepresentation)

    return if (match != null) {
        match.groupValues[1]
    } else {
        stringRepresentation.substringAfterLast("=").substringBefore(")")
            .ifEmpty { stringRepresentation }
    }
}

/**
 * A registry that maps resource keys to their corresponding StringResource objects.
 *
 * Used during deserialization to reconstruct StringResource objects from their serialized resource keys.
 */
object StringResourceRegistry {
    private val resourceMap = mutableMapOf<String, StringResource>()

    fun register(resourceKey: String, stringResource: StringResource) {
        resourceMap[resourceKey] = stringResource
    }

    fun getResource(resourceKey: String): StringResource? {
        return resourceMap[resourceKey]
    }
}
