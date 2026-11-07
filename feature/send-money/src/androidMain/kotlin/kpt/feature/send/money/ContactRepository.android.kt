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

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Android actual: remembers an [AndroidContactRepository] bound to the current context. */
@Composable
actual fun rememberContactRepository(): ContactRepository {
    val context = LocalContext.current
    return remember { AndroidContactRepository(context.contentResolver) }
}

/**
 * Android [ContactRepository] that reads device contacts through the ContactsContract provider.
 * @property contentResolver Resolver used to query the contacts provider.
 */
class AndroidContactRepository(
    private val contentResolver: ContentResolver,
) : ContactRepository {

    override suspend fun getContacts(): List<Contact> {
        val rawContacts = queryContacts(null)
        return PhoneNumberUtils.filterAndFormatContacts(rawContacts)
    }

    override suspend fun searchContacts(query: String): List<Contact> {
        val normalizedQuery = PhoneNumberUtils.normalizeSearchQuery(query)
        // Search by name (contains) and phone number (starts with normalized query)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$query%", "$normalizedQuery%")
        val rawContacts = queryContacts(selection, selectionArgs)
        return PhoneNumberUtils.filterAndFormatContacts(rawContacts)
    }

    private fun queryContacts(selection: String? = null, selectionArgs: Array<String>? = null): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val number = cursor.getString(numberColumn) ?: ""

                val contact = Contact(
                    id = id,
                    name = name,
                    phoneNumber = number,
                    upiId = null,
                )

                if (!contacts.any { it.name == name && it.phoneNumber == number }) {
                    contacts.add(contact)
                }
            }
        }

        return contacts
    }
}
