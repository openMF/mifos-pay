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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun UpiTransactionHistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpiTransactionHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            UpiTransactionHistoryEvent.NavigateBack -> {
                onBackClick.invoke()
            }
        }
    }

    MifosScaffold(
        modifier = modifier,
        containerColor = KptTheme.colorScheme.background,
        topBar = {
            UpiTransactionHistoryTopBar(
                searchQuery = state.searchQuery,
                onSearchQueryChange = { query ->
                    viewModel.trySendAction(UpiTransactionHistoryAction.SearchQueryChanged(query))
                },
                onSearch = {
                    viewModel.trySendAction(UpiTransactionHistoryAction.SearchPerformed)
                },
                onBackClick = {
                    viewModel.trySendAction(UpiTransactionHistoryAction.BackPressed)
                },
                onClearSearch = {
                    viewModel.trySendAction(UpiTransactionHistoryAction.ClearSearch)
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Loading transactions...",
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onSurface,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = KptTheme.spacing.lg)
                    .padding(top = KptTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                items(state.groupedTransactions) { monthGroup ->
                    MonthTransactionGroup(
                        monthGroup = monthGroup,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpiTransactionHistoryTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackClick: () -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = KptTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = MifosIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = KptTheme.colorScheme.onSurface,
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.Center)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Enter) {
                            onSearch()
                            true
                        } else {
                            false
                        }
                    },
                placeholder = {
                    Text(
                        text = "Search transactions...",
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KptTheme.colorScheme.primary,
                    unfocusedBorderColor = KptTheme.colorScheme.outline,
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = onClearSearch,
                        ) {
                            Icon(
                                imageVector = MifosIcons.Close,
                                contentDescription = "Clear search",
                                tint = KptTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun MonthTransactionGroup(
    monthGroup: MonthTransactionGroup,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = monthGroup.monthYear,
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
            )

            Text(
                text = CurrencyFormatter.format(
                    balance = monthGroup.totalAmount,
                    currencyCode = "INR",
                    maximumFractionDigits = 2,
                ),
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            monthGroup.transactions.forEach { transaction ->
                TransactionHistoryCard(
                    transaction = transaction,
                )
            }
        }
    }
}

@Composable
private fun TransactionHistoryCard(
    transaction: UpiTransaction,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = KptTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (transaction.profileImageUrl != null) {
                    Text(
                        text = transaction.payeeName.take(1).uppercase(),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onPrimaryContainer,
                    )
                } else {
                    Text(
                        text = transaction.payeeName.take(1).uppercase(),
                        style = KptTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = KptTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = transaction.payeeName.uppercase(),
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = transaction.formattedDate,
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = CurrencyFormatter.format(
                    balance = transaction.amount,
                    currencyCode = "INR",
                    maximumFractionDigits = 2,
                ),
                style = KptTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
            )
        }
    }
}

data class UpiTransaction(
    val id: String,
    val payeeName: String,
    val amount: Double,
    val date: LocalDate,
    val profileImageUrl: String?,
) {
    val formattedDate: String
        get() = "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
}

data class MonthTransactionGroup(
    val monthYear: String,
    val totalAmount: Double,
    val transactions: List<UpiTransaction>,
)
