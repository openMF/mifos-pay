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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.AvatarBox
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Composable that requests contacts permission on platforms that need it. */
@Composable
expect fun ContactPermissionHandler()

/** Screen that lists and searches device contacts so the user can pick a payment recipient. */
@Composable
fun ContactsPickerScreen(
    onBackClick: () -> Unit,
    onContactSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val contactPermissionState = rememberContactPermissionState()
    val contactRepository = rememberContactRepository()

    LaunchedEffect(contactPermissionState.status) {
        when (contactPermissionState.status) {
            ContactPermissionStatus.Granted -> {
                isLoading = true
                try {
                    contacts = contactRepository.getContacts()
                } catch (e: Exception) {
                    contacts = emptyList()
                } finally {
                    isLoading = false
                }
            }
            ContactPermissionStatus.Denied -> {
                isLoading = false
            }
        }
    }

    LaunchedEffect(searchQuery) {
        if (contactPermissionState.status == ContactPermissionStatus.Granted && searchQuery.isNotEmpty()) {
            try {
                contacts = contactRepository.searchContacts(searchQuery)
            } catch (e: Exception) {
            }
        } else if (contactPermissionState.status == ContactPermissionStatus.Granted && searchQuery.isEmpty()) {
            try {
                contacts = contactRepository.getContacts()
            } catch (e: Exception) {
            }
        }
    }

    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                MifosTopBar(
                    topBarTitle = "Select Contact",
                    backPress = onBackClick,
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = KptTheme.spacing.lg),
            ) {
                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                MifosOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "",
                    placeholder = {
                        Text(
                            text = "Search contacts...",
                            style = KptTheme.typography.bodyMedium,
                            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = MifosIcons.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(20.dp),
                            tint = KptTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search,
                    ),
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                Text(
                    text = "All Contacts",
                    style = KptTheme.typography.labelLarge,
                    color = KptTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Left,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Loading contacts...",
                            style = KptTheme.typography.bodyMedium,
                            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                } else if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "No mobile contacts found" else "No mobile contacts match your search",
                            style = KptTheme.typography.bodyMedium,
                            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                    ) {
                        items(
                            items = contacts,
                            key = { contact -> "${contact.id}_${contact.phoneNumber}" },
                        ) { contact ->
                            ContactItem(
                                contact = contact,
                                onClick = {
                                    println("ContactsPickerScreen: Contact clicked - ${contact.phoneNumber}")
                                    onContactSelected(contact.phoneNumber)
                                },
                            )

                            if (contact != contacts.last()) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = KptTheme.spacing.xs),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    ContactPermissionHandler()
}

@Composable
private fun ContactItem(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = KptTheme.shapes.small,
        colors = CardDefaults.outlinedCardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = contact.name,
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurface,
                )
            },
            supportingContent = {
                Text(
                    text = contact.phoneNumber,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            },
            leadingContent = {
                AvatarBox(
                    icon = MifosIcons.Person,
                    backgroundColor = KptTheme.colorScheme.primaryContainer,
                    contentColor = KptTheme.colorScheme.onPrimaryContainer,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = KptTheme.colorScheme.surface,
            ),
        )
    }
}

/** Preview of [ContactsPickerScreen]. */
@Preview
@Composable
fun ContactsPickerScreenPreview() {
    ContactsPickerScreen(
        onBackClick = {},
        onContactSelected = {},
    )
}
