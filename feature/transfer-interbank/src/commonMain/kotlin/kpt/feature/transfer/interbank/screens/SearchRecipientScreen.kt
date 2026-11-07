/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.transfer.interbank.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkButton
import kpt.core.designsystem.fork.component.ForkCard
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.ForkTextField
import kpt.core.designsystem.fork.component.ForkTopBar
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.designsystem.theme.fork.ForkTheme
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkProgressIndicator
import kpt.feature.transfer.interbank.generated.resources.Res
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_bank
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_enter_phone_number
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_enter_phone_to_search
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_found_recipients
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_no_recipients_found
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_no_results
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_search_recipient
import kpt.feature.transfer.interbank.generated.resources.feature_send_interbank_to_account_interbank
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Step for searching a recipient by phone number and choosing from the results. */
@Composable
fun SearchRecipientScreen(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    recipients: List<InterBankPartyInfoResponse>,
    onRecipientSelected: (InterBankPartyInfoResponse) -> Unit,
    onSearchClick: (String) -> Unit,
    isSearching: Boolean = false,
    searchError: String? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ForkScaffold(
        modifier = modifier,
        topBar = {
            ForkTopBar(
                topBarTitle = stringResource(Res.string.feature_send_interbank_search_recipient),
                backPress = onBackClick,
            )
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            ForkTextField(
                label = stringResource(Res.string.feature_send_interbank_enter_phone_number),
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            ForkButton(
                onClick = {
                    keyboardController?.hide()
                    onSearchClick(searchQuery)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = searchQuery.length >= 10 && !isSearching,
            ) {
                Text(stringResource(Res.string.feature_send_interbank_search_recipient))
            }

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = KptTheme.spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    ForkProgressIndicator()
                }
            } else if (searchError != null) {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_send_interbank_no_results),
                    subTitle = searchError,
                )
            } else if (recipients.isEmpty() && searchQuery.isNotEmpty()) {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_send_interbank_no_results),
                    subTitle = stringResource(Res.string.feature_send_interbank_no_recipients_found),
                )
            } else if (recipients.isNotEmpty()) {
                Text(
                    text = stringResource(
                        Res.string.feature_send_interbank_found_recipients,
                        recipients.size,
                    ),
                    style = KptTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    items(recipients) { recipient ->
                        RecipientSelectionCard(
                            recipient = recipient,
                            onClick = { onRecipientSelected(recipient) },
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = KptTheme.spacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.feature_send_interbank_enter_phone_to_search),
                        style = KptTheme.typography.bodyMedium,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipientSelectionCard(
    recipient: InterBankPartyInfoResponse,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.xs)
            .clickable(onClick = onClick),
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.cardColors(KptTheme.colorScheme.surface),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = "${recipient.firstName} ${recipient.lastName}",
                    fontWeight = FontWeight.SemiBold,
                )
            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            Res.string.feature_send_interbank_to_account_interbank,
                            recipient.partyId,
                        ),
                        style = KptTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(
                            Res.string.feature_send_interbank_bank,
                            recipient.destinationFspId,
                        ),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            leadingContent = {
                AvatarBox(
                    icon = ForkIcons.Person,
                    backgroundColor = KptTheme.colorScheme.secondaryContainer,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

/** Compose preview of [SearchRecipientScreen] with results. */
@Preview
@Composable
fun SearchRecipientScreenPreview() {
    ForkTheme {
        val mockRecipients = listOf(
            InterBankPartyInfoResponse(
                sourceFspId = "mifos-bank-1",
                destinationFspId = "blackbank-test",
                requestId = "req-001",
                partyId = "9880000020",
                currencyCode = "MXN",
                firstName = "Pedro",
                lastName = "Barreto",
                systemMessage = "Success",
                executionStatus = true,
                partyIdType = "MSISDN",
            ),
            InterBankPartyInfoResponse(
                sourceFspId = "mifos-bank-1",
                destinationFspId = "blackbank-test",
                requestId = "req-002",
                partyId = "9880000021",
                currencyCode = "MXN",
                firstName = "Maria",
                lastName = "Garcia",
                systemMessage = "Success",
                executionStatus = true,
                partyIdType = "MSISDN",
            ),
        )

        SearchRecipientScreen(
            searchQuery = "988",
            onSearchQueryChanged = {},
            recipients = mockRecipients,
            onRecipientSelected = {},
            onSearchClick = {},
            onBackClick = {},
        )
    }
}

/** Compose preview of [SearchRecipientScreen] with no query. */
@Preview
@Composable
fun SearchRecipientScreenEmptyPreview() {
    ForkTheme {
        SearchRecipientScreen(
            searchQuery = "",
            onSearchQueryChanged = {},
            recipients = emptyList(),
            onRecipientSelected = {},
            onSearchClick = {},
            onBackClick = {},
        )
    }
}

/** Compose preview of [SearchRecipientScreen] with no matching recipients. */
@Preview
@Composable
fun SearchRecipientScreenNoResultsPreview() {
    ForkTheme {
        SearchRecipientScreen(
            searchQuery = "999999",
            onSearchQueryChanged = {},
            recipients = emptyList(),
            onRecipientSelected = {},
            onSearchClick = {},
            onBackClick = {},
        )
    }
}

/** Compose preview of [SearchRecipientScreen] while searching. */
@Preview
@Composable
fun SearchRecipientScreenLoadingPreview() {
    ForkTheme {
        SearchRecipientScreen(
            searchQuery = "9388006020",
            onSearchQueryChanged = {},
            recipients = emptyList(),
            onRecipientSelected = {},
            onSearchClick = {},
            isSearching = true,
            onBackClick = {},
        )
    }
}
