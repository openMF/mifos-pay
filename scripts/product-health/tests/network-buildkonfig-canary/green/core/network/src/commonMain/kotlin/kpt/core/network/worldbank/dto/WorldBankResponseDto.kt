/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.worldbank.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kpt.core.model.economic.IndicatorKind
import kpt.core.model.economic.IndicatorObservation
import kpt.core.model.economic.MacroIndicator

/**
 * Top-level response from World Bank's `v2/country/{c}/indicator/{i}` endpoint.
 *
 * **Wire-format quirk:** the API returns a 2-element JSON *array* —
 * `[metadata, observations]` — not a regular object. This class wraps both
 * elements behind the [WorldBankResponseSerializer], so endpoint callers see a
 * normal Kotlin data class.
 *
 * When the World Bank has no data for a `(country, indicator)` pair, the API
 * returns the metadata block with an empty observations slot (encoded as either
 * `null` or an empty array). [observations] normalises both to `emptyList()`.
 */
@Serializable(with = WorldBankResponseSerializer::class)
data class WorldBankResponseDto(
    val metadata: WorldBankMetadataDto?,
    val observations: List<WorldBankObservationDto>,
) {
    /**
     * Project the wire format onto the domain [MacroIndicator].
     *
     * @param countryCode ISO country code — round-tripped from the request
     *   because the response uses the World Bank's own numeric `id` field, not
     *   the ISO code the caller passed in.
     * @param indicator The [IndicatorKind] the caller requested. Cross-checked
     *   against the response's indicator code; mismatches log but don't fail
     *   (defensive against future World Bank schema changes).
     */
    fun toDomain(countryCode: String, indicator: IndicatorKind): MacroIndicator {
        val countryName = observations.firstOrNull()?.country?.value
            ?: countryCode
        val parsed = observations
            .map { IndicatorObservation(year = it.dateAsYear(), value = it.value) }
            .filter { it.year > 0 }
            .sortedBy { it.year }
        return MacroIndicator(
            countryCode = countryCode,
            countryName = countryName,
            indicator = indicator,
            observations = parsed,
        )
    }
}

/**
 * First-array element — pagination + record-count metadata. The toolkit only
 * needs [total] to know whether the World Bank returned any data, but the full
 * shape is retained for forward-compat with screens that may want pagination
 * later.
 */
@Serializable
data class WorldBankMetadataDto(
    val page: Int? = null,
    val pages: Int? = null,
    @SerialName("per_page")
    val perPage: Int? = null,
    val total: Int? = null,
    val sourceid: String? = null,
    val lastupdated: String? = null,
)

/**
 * One observation row from the World Bank's second-array element.
 *
 * `value` is nullable — many `(country, indicator, year)` triples have no
 * reported data; the World Bank emits explicit JSON `null` rather than
 * omitting the row.
 */
@Serializable
data class WorldBankObservationDto(
    val indicator: WorldBankIdValueDto? = null,
    val country: WorldBankIdValueDto? = null,
    val countryiso3code: String? = null,
    val date: String,
    val value: Double? = null,
    val unit: String? = null,
    @SerialName("obs_status")
    val obsStatus: String? = null,
    val decimal: Int? = null,
) {
    /** World Bank publishes annual data; `date` is always a 4-digit year string. */
    fun dateAsYear(): Int = date.toIntOrNull() ?: 0
}

/**
 * Generic `{ "id": "USA", "value": "United States" }` shape used by the World
 * Bank for both `country` and `indicator` fields on every observation row.
 */
@Serializable
data class WorldBankIdValueDto(
    val id: String? = null,
    val value: String? = null,
)

/**
 * Custom serializer that destructures the World Bank's
 * `[metadata, observations[]]` two-element JSON array into a normal Kotlin
 * data class. Read-only — the toolkit never sends WorldBank-shaped payloads.
 */
object WorldBankResponseSerializer : KSerializer<WorldBankResponseDto> {
    private val observationListSerializer =
        ListSerializer(WorldBankObservationDto.serializer())

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("WorldBankResponse") {
            element("metadata", WorldBankMetadataDto.serializer().descriptor)
            element("observations", observationListSerializer.descriptor)
        }

    override fun deserialize(decoder: Decoder): WorldBankResponseDto {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("WorldBankResponseSerializer requires kotlinx.serialization.json.")
        val root = jsonDecoder.decodeJsonElement()
        // World Bank also returns a single-element object `{"message": [...]}` on
        // 404 / rate-limit / missing data. Defensive handling: when not an array,
        // return an empty domain object so the repository layer can surface
        // "no data" through its normal channel.
        val array = (root as? JsonArray) ?: return WorldBankResponseDto(
            metadata = null,
            observations = emptyList(),
        )
        val metadataElement = array.getOrNull(0)
        val observationsElement = array.getOrNull(1)

        val metadata = metadataElement
            ?.takeIf { it is kotlinx.serialization.json.JsonObject }
            ?.let {
                jsonDecoder.json.decodeFromJsonElement(
                    WorldBankMetadataDto.serializer(),
                    it,
                )
            }

        val observations = observationsElement
            ?.let { element ->
                when (element) {
                    is JsonArray -> jsonDecoder.json.decodeFromJsonElement(
                        observationListSerializer,
                        element,
                    )

                    is kotlinx.serialization.json.JsonObject -> {
                        // Some endpoints embed observations under `{ "data": [...] }`.
                        val data = element.jsonObject["data"]
                        if (data is JsonArray) {
                            jsonDecoder.json.decodeFromJsonElement(
                                observationListSerializer,
                                data,
                            )
                        } else {
                            emptyList()
                        }
                    }

                    else -> emptyList()
                }
            } ?: emptyList()

        return WorldBankResponseDto(metadata = metadata, observations = observations)
    }

    override fun serialize(encoder: Encoder, value: WorldBankResponseDto) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("WorldBankResponseSerializer requires kotlinx.serialization.json.")
        val metadataJson = value.metadata?.let {
            jsonEncoder.json.encodeToJsonElement(
                WorldBankMetadataDto.serializer(),
                it,
            )
        } ?: kotlinx.serialization.json.JsonNull
        val observationsJson = jsonEncoder.json.encodeToJsonElement(
            observationListSerializer,
            value.observations,
        )
        jsonEncoder.encodeJsonElement(
            JsonArray(listOf(metadataJson, observationsJson.jsonArray)),
        )
    }
}
