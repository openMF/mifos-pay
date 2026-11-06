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
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosCard
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.model.interbank.InterBankPartyInfoResponse
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosProgressIndicator
import mifos_pay.feature.transfer_interbank.generated.resources.Res
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_bank
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_enter_phone_number
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_enter_phone_to_search
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_found_recipients
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_no_recipients_found
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_no_results
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_search_recipient
import mifos_pay.feature.transfer_interbank.generated.resources.feature_send_interbank_to_account_interbank
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
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

    MifosScaffold(
        modifier = modifier,
        topBar = {
            MifosTopBar(
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
            MifosTextField(
                label = stringResource(Res.string.feature_send_interbank_enter_phone_number),
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            MifosButton(
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
                    MifosProgressIndicator()
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
    MifosCard(
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
                    icon = MifosIcons.Person,
                    backgroundColor = KptTheme.colorScheme.secondaryContainer,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Preview
@Composable
fun SearchRecipientScreenPreview() {
    MifosTheme {
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

@Preview
@Composable
fun SearchRecipientScreenEmptyPreview() {
    MifosTheme {
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

@Preview
@Composable
fun SearchRecipientScreenNoResultsPreview() {
    MifosTheme {
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

@Preview
@Composable
fun SearchRecipientScreenLoadingPreview() {
    MifosTheme {
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
