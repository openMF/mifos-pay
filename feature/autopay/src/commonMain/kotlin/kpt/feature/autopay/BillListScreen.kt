/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.autopay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.rememberMifosPullToRefreshState
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.autopay.Bill
import kpt.core.model.autopay.BillStatus
import kpt.core.ui.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime
/** Screen listing the user's bills with add, edit and delete actions. */
@Composable
fun BillListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddBill: () -> Unit,
    onNavigateToEditBill: (String) -> Unit,
    onNavigateToAutopay: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BillListViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberMifosPullToRefreshState(
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(BillListAction.RefreshBills) },
    )

    Box(modifier = modifier.fillMaxSize()) {
        MifosScaffold(
            topBarTitle = "Bill List",
            backPress = onNavigateBack,
            pullToRefreshState = pullRefreshState,
        ) { paddingValues ->
            if (state.isLoading && state.bills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
                ) {
                    if (state.bills.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No Bills Found",
                                description = "You don't have any bills yet. Add your first bill to get started!",
                                icon = MifosIcons.Receipt,
                                onAddBill = onNavigateToAddBill,
                            )
                        }
                    } else {
                        items(state.bills) { bill ->
                            BillCard(
                                bill = bill,
                                onEdit = { onNavigateToEditBill(bill.id ?: "") },
                                onDelete = { viewModel.trySendAction(BillListAction.DeleteBill(bill.id ?: "")) },
                            )
                        }
                    }
                }
            }
        }

        MifosButton(
            text = { Text("Back to AutoPay") },
            onClick = onNavigateToAutopay,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
        )
    }

    EventsEffect(viewModel) { event ->
        when (event) {
            is BillListEvent.NavigateToAddBill -> onNavigateToAddBill()
            is BillListEvent.NavigateToEditBill -> onNavigateToEditBill(event.billId)
            is BillListEvent.BillDeleted -> {
                // Bill deleted successfully, no action needed
            }
        }
    }
}

@Composable
private fun BillCard(
    bill: Bill,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = bill.billerName ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (bill.autoPayEnabled) {
                        AutoPayChip()
                    }
                    BillStatusChip(status = bill.status)
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                    ) {
                        Icon(
                            imageVector = MifosIcons.MoreVert,
                            contentDescription = "More options",
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(
                                    imageVector = MifosIcons.Edit,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    imageVector = MifosIcons.Delete,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = CurrencyFormatter.format(bill.amount, bill.currency, 2),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Column {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatDate(bill.dueDate),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Column {
                    Text(
                        text = "Frequency",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = bill.recurrencePattern.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun BillStatusChip(
    status: BillStatus,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor) = when (status) {
        BillStatus.ACTIVE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        BillStatus.PAUSED -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        BillStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        BillStatus.COMPLETED -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun AutoPayChip(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MifosIcons.Payment,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSecondary,
            )
            Text(
                text = "AutoPay",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onAddBill: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddBill,
            ) {
                Icon(
                    imageVector = MifosIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Bill")
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatDate(timestamp: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.monthNumber}/${localDateTime.dayOfMonth}/${localDateTime.year}"
    } catch (e: Exception) {
        "Invalid Date"
    }
}
