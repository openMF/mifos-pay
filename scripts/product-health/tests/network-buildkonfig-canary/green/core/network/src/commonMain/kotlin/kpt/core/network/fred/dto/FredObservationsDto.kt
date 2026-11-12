/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.fred.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.economic.InterestRateSeries
import kpt.core.model.economic.RateObservation

/**
 * Wire-format response from FRED's `fred/series/observations` endpoint.
 *
 * FRED publishes daily observations; missing values are encoded as the string
 * `"."` (an explicit no-data marker, not absence of the field). The [toDomain]
 * mapper strips those rows.
 */
@Serializable
data class FredObservationsDto(
    @SerialName("realtime_start") val realtimeStart: String? = null,
    @SerialName("realtime_end") val realtimeEnd: String? = null,
    @SerialName("observation_start") val observationStart: String? = null,
    @SerialName("observation_end") val observationEnd: String? = null,
    val units: String? = null,
    @SerialName("output_type") val outputType: Int? = null,
    @SerialName("file_type") val fileType: String? = null,
    @SerialName("order_by") val orderBy: String? = null,
    @SerialName("sort_order") val sortOrder: String? = null,
    val count: Int? = null,
    val offset: Int? = null,
    val limit: Int? = null,
    val observations: List<FredObservationDto> = emptyList(),
) {
    /**
     * Project the wire format onto the domain [InterestRateSeries].
     *
     * @param seriesId FRED series identifier — round-tripped from the original
     *   request because FRED's response body does not echo it.
     * @param name Human-readable label supplied by the caller (FRED's
     *   `series/observations` endpoint returns no display name; use
     *   `series` endpoint or hardcode in the repository layer).
     * @param unit Unit of measure (`"%"`, `"USD"`, …). Caller-supplied for the
     *   same reason as [name].
     */
    fun toDomain(seriesId: String, name: String, unit: String): InterestRateSeries {
        val parsed = observations.mapNotNull { it.toDomainOrNull() }
        return InterestRateSeries(
            seriesId = seriesId,
            name = name,
            current = parsed.lastOrNull()?.value ?: 0.0,
            unit = unit,
            observations = parsed,
        )
    }
}

/**
 * Single observation row from FRED. `value` arrives as a string because FRED uses
 * `"."` to mark missing data on otherwise-daily series.
 */
@Serializable
data class FredObservationDto(
    @SerialName("realtime_start") val realtimeStart: String? = null,
    @SerialName("realtime_end") val realtimeEnd: String? = null,
    val date: String,
    val value: String,
) {
    /**
     * Convert to domain, returning `null` when:
     * - `value` is FRED's no-data marker `"."`
     * - `value` cannot be parsed as a `Double`
     * - `date` cannot be parsed as `LocalDate`
     *
     * Callers should treat `null` as "drop this row" — never as "value = 0".
     */
    fun toDomainOrNull(): RateObservation? {
        val numeric = if (value == NO_DATA_MARKER) null else value.toDoubleOrNull()
        val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
        return if (numeric != null && parsedDate != null) {
            RateObservation(date = parsedDate, value = numeric)
        } else {
            null
        }
    }

    companion object {
        /** FRED's explicit "missing value" marker — surfaces in daily-cadence series. */
        const val NO_DATA_MARKER: String = "."
    }
}
