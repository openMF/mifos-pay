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

@Composable
actual fun rememberContactRepository(): ContactRepository {
    return object : ContactRepository {
        override suspend fun getContacts(): List<Contact> {
            return PhoneNumberUtils.filterAndFormatContacts(emptyList())
        }

        override suspend fun searchContacts(query: String): List<Contact> {
            return PhoneNumberUtils.filterAndFormatContacts(emptyList())
        }
    }
}
