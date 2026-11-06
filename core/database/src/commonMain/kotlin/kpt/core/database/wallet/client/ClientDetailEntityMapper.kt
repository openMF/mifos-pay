/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.wallet.client

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kpt.core.model.client.Client
import kpt.core.model.client.ClientStatus
import kpt.core.model.client.ClientTimeline
/**
 * Entity ↔ domain mapper for the `wallet_client_details` table.
 *
 * Three nested `@Serializable` payloads (`ClientTimeline`, two `ClientStatus`
 * blocks — `status` and `legalForm`) plus two `List<Long>` date payloads
 * (`activationDate`, `dateOfBirth`) are persisted as JSON string columns.
 * See [ClientDetailEntity]'s KDoc for the rationale (single-record whole-payload
 * read model, no per-field query need).
 *
 * The [Json] instance is `ignoreUnknownKeys = true` so an evolving server DTO
 * doesn't crash a cached row on read-back.
 */
private val json: Json = Json { ignoreUnknownKeys = true }

/** Convert a persisted row back into the domain [Client] the app renders. */
fun ClientDetailEntity.toDomain(): Client = Client(
    id = id,
    accountNo = accountNo,
    externalId = externalId,
    active = active,
    activationDate = json.decodeFromString(
        ListSerializer(Long.serializer()),
        activationDateJson,
    ),
    firstname = firstname,
    lastname = lastname,
    displayName = displayName,
    mobileNo = mobileNo,
    emailAddress = emailAddress,
    dateOfBirth = json.decodeFromString(
        ListSerializer(Long.serializer()),
        dateOfBirthJson,
    ),
    isStaff = isStaff,
    officeId = officeId,
    officeName = officeName,
    savingsProductName = savingsProductName,
    timeline = json.decodeFromString(ClientTimeline.serializer(), timelineJson),
    status = json.decodeFromString(ClientStatus.serializer(), statusJson),
    legalForm = json.decodeFromString(ClientStatus.serializer(), legalFormJson),
)

/**
 * Convert a fetched domain [Client] into a persistable row.
 *
 * @param fetchedAtEpochMs local cache-write timestamp — the store's writer
 *   passes `Clock.System.now().toEpochMilliseconds()` here.
 */
fun Client.toEntity(fetchedAtEpochMs: Long): ClientDetailEntity = ClientDetailEntity(
    id = id,
    accountNo = accountNo,
    externalId = externalId,
    active = active,
    firstname = firstname,
    lastname = lastname,
    displayName = displayName,
    mobileNo = mobileNo,
    emailAddress = emailAddress,
    isStaff = isStaff,
    officeId = officeId,
    officeName = officeName,
    savingsProductName = savingsProductName,
    activationDateJson = json.encodeToString(
        ListSerializer(Long.serializer()),
        activationDate,
    ),
    dateOfBirthJson = json.encodeToString(
        ListSerializer(Long.serializer()),
        dateOfBirth,
    ),
    timelineJson = json.encodeToString(ClientTimeline.serializer(), timeline),
    statusJson = json.encodeToString(ClientStatus.serializer(), status),
    legalFormJson = json.encodeToString(ClientStatus.serializer(), legalForm),
    fetchedAtEpochMs = fetchedAtEpochMs,
)
