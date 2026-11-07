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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.AvatarBox
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
// TODO replace dummy data after backend APIs are finalized
/** Screen for paying any recipient by UPI ID, phone number or QR scan. */
@Composable
fun PayAnyoneScreen(
    onBackClick: () -> Unit,
    onContactPickerClick: () -> Unit,
    onContactSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: PayAnyoneViewModel = koinViewModel()
    val state by viewModel.stateFlow.collectAsState()

    var currentPlaceholderIndex by remember { mutableStateOf(0) }

    val placeholderMessages = listOf(
        "Enter UPI ID or number",
        "Enter phone number or name",
    )

    val currentPlaceholder = placeholderMessages[currentPlaceholderIndex]

    val keyboardType = if (state.isKeyboardNumeric) {
        KeyboardType.Number
    } else {
        KeyboardType.Text
    }

    val keyboardToggleText = if (state.isKeyboardNumeric) "ABC" else "123"

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentPlaceholderIndex = (currentPlaceholderIndex + 1) % placeholderMessages.size
        }
    }

    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                MifosTopBar(
                    topBarTitle = "Pay Anyone",
                    backPress = onBackClick,
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = KptTheme.spacing.lg),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.height(KptTheme.spacing.md))

                    Text(
                        text = "Pay any UPI app using name, number or UPI ID",
                        style = KptTheme.typography.titleSmall,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

                    MifosOutlinedTextField(
                        value = state.inputValue,
                        onValueChange = {
                            viewModel.trySendAction(PayAnyoneAction.InputValueChanged(it))
                        },
                        label = "",
                        placeholder = {
                            AnimatedContent(
                                targetState = currentPlaceholder,
                                transitionSpec = {
                                    slideInVertically(
                                        animationSpec = tween(500),
                                        initialOffsetY = { fullHeight -> fullHeight },
                                    ) + fadeIn(animationSpec = tween(500)) togetherWith
                                        slideOutVertically(
                                            animationSpec = tween(500),
                                            targetOffsetY = { fullHeight -> -fullHeight },
                                        ) + fadeOut(animationSpec = tween(500))
                                },
                            ) { placeholder ->
                                Text(
                                    text = placeholder,
                                    style = KptTheme.typography.bodyMedium,
                                    color = KptTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = ImeAction.Search,
                        ),
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AnimatedContent(
                                    targetState = state.showClearIcon,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(200)) togetherWith
                                            fadeOut(animationSpec = tween(200))
                                    },
                                ) { showClear ->
                                    if (showClear) {
                                        IconButton(
                                            onClick = {
                                                viewModel.trySendAction(PayAnyoneAction.ClearInput)
                                            },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = KptTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = MifosIcons.Close,
                                                contentDescription = "Clear input",
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    } else {
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    viewModel.trySendAction(PayAnyoneAction.ToggleKeyboardType)
                                                },
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = KptTheme.colorScheme.primary,
                                                ),
                                            ) {
                                                Text(
                                                    text = keyboardToggleText,
                                                    style = KptTheme.typography.labelMedium,
                                                    color = KptTheme.colorScheme.primary,
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(KptTheme.spacing.xs))

                                            IconButton(
                                                onClick = onContactPickerClick,
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = KptTheme.colorScheme.primary,
                                                ),
                                            ) {
                                                Icon(
                                                    imageVector = MifosIcons.Contact,
                                                    contentDescription = "Select from contacts",
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

                if (state.showUpiHandleSuggestions) {
                    UpiHandleSuggestions(
                        onHandleSelected = { handle ->
                            viewModel.trySendAction(PayAnyoneAction.UpiHandleSelected(handle))
                        },
                    )
                    Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
                }

                if (state.isSearching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = KptTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(KptTheme.spacing.sm))
                        Text(
                            text = "Searching...",
                            style = KptTheme.typography.bodyMedium,
                            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                    Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
                }

                if (state.showPartialNumberNote) {
                    PartialNumberNote()
                    Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
                }

                if (state.showContactNotFoundLoading) {
                    ContactNotFoundLoading()
                    Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
                }

                if (state.showContactNotFoundMessage) {
                    ContactNotFoundMessage()
                    Spacer(modifier = Modifier.height(KptTheme.spacing.lg))
                }

                if (state.inputValue.isNotEmpty()) {
                    SearchResultsContent(
                        searchResults = state.searchResults,
                        onContactSelected = { contact ->
                            viewModel.trySendAction(PayAnyoneAction.ContactSelected(contact))
                            onContactSelected(contact.phoneNumber)
                        },
                    )
                } else {
                    DefaultContent(
                        recentContacts = state.recentContacts,
                        allContacts = state.allContacts,
                        onContactSelected = { contact ->
                            viewModel.trySendAction(PayAnyoneAction.ContactSelected(contact))
                            onContactSelected(contact.phoneNumber)
                        },
                    )
                }
            }
        }
    }

    // Contact Not Found Dialog
    ContactNotFoundDialog(
        showDialog = state.showContactNotFoundDialog,
        selectedOption = state.selectedSharingOption,
        onOptionSelected = { option ->
            viewModel.trySendAction(PayAnyoneAction.SharingOptionSelected(option))
        },
        onNotNowClick = {
            viewModel.trySendAction(PayAnyoneAction.NotNowSharingOption)
        },
        onSetAsDefaultClick = {
            viewModel.trySendAction(PayAnyoneAction.SetAsDefaultSharingOption)
        },
        onDismiss = {
            viewModel.trySendAction(PayAnyoneAction.HideContactNotFoundDialog)
        },
    )
}

@Composable
private fun SearchResultsContent(
    searchResults: ContactSearchResult,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
    ) {
        if (searchResults.people.isNotEmpty()) {
            item {
                SearchSection(
                    title = "People",
                    contacts = searchResults.people,
                    onContactSelected = onContactSelected,
                )
            }
        }

        if (searchResults.businesses.isNotEmpty()) {
            item {
                SearchSection(
                    title = "Businesses",
                    contacts = searchResults.businesses,
                    onContactSelected = onContactSelected,
                )
            }
        }

        if (searchResults.others.isNotEmpty()) {
            item {
                SearchSection(
                    title = "Others",
                    contacts = searchResults.others,
                    onContactSelected = onContactSelected,
                )
            }
        }

        if (searchResults.people.isEmpty() && searchResults.businesses.isEmpty() && searchResults.others.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(KptTheme.spacing.xl))
                    Text(
                        text = "No results found",
                        style = KptTheme.typography.bodyLarge,
                        color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    Text(
                        text = "Try searching with a different term",
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSection(
    title: String,
    contacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = KptTheme.typography.titleMedium,
            color = KptTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            contacts.forEach { contact ->
                SearchContactCard(
                    contact = contact,
                    onClick = { onContactSelected(contact) },
                )
            }
        }
    }
}

@Composable
private fun SearchContactCard(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBox(
                name = contact.name,
                size = 48,
                backgroundColor = if (contact.type == ContactType.BUSINESS) {
                    KptTheme.colorScheme.secondaryContainer
                } else {
                    KptTheme.colorScheme.primaryContainer
                },
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = contact.name,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = contact.phoneNumber,
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (contact.upiId != null) {
                    Text(
                        text = contact.upiId,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultContent(
    recentContacts: List<Contact>,
    allContacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        if (recentContacts.isNotEmpty()) {
            item {
                RecentContactsSection(
                    recentContacts = recentContacts,
                    onContactSelected = onContactSelected,
                )
            }
        }
// TODO Map device contact phone numbers to UPI IDs after backend specifications are finalized
        if (allContacts.isNotEmpty()) {
            item {
                Text(
                    text = "All People on UPI",
                    style = KptTheme.typography.titleMedium,
                    color = KptTheme.colorScheme.onSurface,
                )
            }

            item {
                Spacer(modifier = Modifier.height(KptTheme.spacing.sm))
            }

            items(
                items = allContacts,
                key = { contact -> "all_${contact.id}_${contact.phoneNumber}" },
            ) { contact ->
                AllPeopleContactItem(
                    contact = contact,
                    onClick = {
                        onContactSelected(contact)
                    },
                )
            }
        }
    }
}

@Composable
private fun RecentContactsSection(
    recentContacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Recent",
            style = KptTheme.typography.titleMedium,
            color = KptTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            recentContacts.forEach { contact ->
                RecentContactCard(
                    contact = contact,
                    onClick = { onContactSelected(contact) },
                )
            }
        }
    }
}

@Composable
private fun RecentContactCard(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBox(
                name = contact.name,
                size = 48,
                backgroundColor = KptTheme.colorScheme.primaryContainer,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = contact.name,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (contact.upiId != null) {
                    Text(
                        text = contact.upiId,
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun AllPeopleContactItem(
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
                Column {
                    Text(
                        text = contact.phoneNumber,
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    if (contact.upiId != null) {
                        Text(
                            text = contact.upiId,
                            style = KptTheme.typography.bodySmall,
                            color = KptTheme.colorScheme.primary,
                        )
                    }
                }
            },
            leadingContent = {
                AvatarBox(
                    name = contact.name,
                    backgroundColor = KptTheme.colorScheme.primaryContainer,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = KptTheme.colorScheme.surface,
            ),
        )
    }
}

@Composable
private fun UpiHandleSuggestions(
    onHandleSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Popular UPI handles",
            style = KptTheme.typography.titleSmall,
            color = KptTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = KptTheme.spacing.xs),
        ) {
            items(PayAnyoneViewModel.UPI_HANDLES) { handle ->
                UpiHandleChip(
                    handle = handle,
                    onClick = { onHandleSelected(handle) },
                )
            }
        }
    }
}

@Composable
private fun UpiHandleChip(
    handle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Text(
            text = handle,
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(
                horizontal = KptTheme.spacing.md,
                vertical = KptTheme.spacing.sm,
            ),
        )
    }
}

@Composable
private fun PartialNumberNote(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Info,
                contentDescription = "Information",
                modifier = Modifier.size(16.dp),
                tint = KptTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

            Text(
                text = "To find more people on any UPI app, enter full number",
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ContactNotFoundMessage(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Info,
                contentDescription = "Information",
                modifier = Modifier.size(16.dp),
                tint = KptTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

            Text(
                text = "Could not find user. Redirecting you to invite them on Google Play",
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
        }
    }
}

/** Preview of [PayAnyoneScreen]. */
@Preview
@Composable
fun PayAnyoneScreenPreview() {
    PayAnyoneScreen(
        onBackClick = {},
        onContactPickerClick = {},
        onContactSelected = {},
    )
}
