/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import androidx.compose.runtime.Composable

/** Source of the device contacts used when choosing a payee. */
interface ContactRepository {
    /** Returns all device contacts that have a valid phone number. */
    suspend fun getContacts(): List<Contact>

    /** Returns contacts whose name or phone number matches [query]. */
    suspend fun searchContacts(query: String): List<Contact>
}

/** Remembers a platform-specific [ContactRepository] for the current composition. */
@Composable
expect fun rememberContactRepository(): ContactRepository
