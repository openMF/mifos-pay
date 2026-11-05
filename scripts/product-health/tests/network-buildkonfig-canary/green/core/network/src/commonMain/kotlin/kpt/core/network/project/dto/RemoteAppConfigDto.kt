/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.project.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of a Supabase `app_config` table — the canonical "runtime server config" shape a fork
 * fetches at start-up (feature flags, a minimum supported version, a maintenance banner).
 *
 * Field names mirror Postgres snake_case columns via [SerialName].
 */
@Serializable
data class RemoteAppConfigDto(
    @SerialName("key") val key: String,
    @SerialName("value") val value: String,
    @SerialName("updated_at") val updatedAt: String? = null,
)
