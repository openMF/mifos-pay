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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

/** Screen listing bills that have AutoPay, with toggles and edit, delete and add actions. */
@Composable
fun AutoPayScheduleManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddBill: () -> Unit,
    onNavigateToEditBill: (String) -> Unit,
    onNavigateToBillList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayScheduleManagementViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberMifosPullToRefreshState(
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayScheduleManagementAction.RefreshSchedules) },
    )

    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayScheduleManagementEvent.NavigateToAddBill -> onNavigateToAddBill()
            is AutoPayScheduleManagementEvent.NavigateToEditBill -> onNavigateToEditBill(event.billId)
            is AutoPayScheduleManagementEvent.NavigateToBillList -> onNavigateToBillList()
            is AutoPayScheduleManagementEvent.AutoPayToggled -> {
                // AutoPay toggled successfully
            }
        }
    }

    MifosScaffold(
        modifier = modifier,
        topBarTitle = "AutoPay Schedules",
        backPress = onNavigateBack,
        pullToRefreshState = pullRefreshState,
    ) { paddingValues ->
        if (state.isLoading && state.billsWithAutoPay.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
            ) {
                item {
                    ScheduleManagementHeader(
                        totalSchedules = state.billsWithAutoPay.size,
                        activeSchedules = state.billsWithAutoPay.count { it.status == BillStatus.ACTIVE },
                    )
                }

                if (state.billsWithAutoPay.isEmpty()) {
                    item {
                        EmptySchedulesCard(
                            onAddBill = { viewModel.trySendAction(AutoPayScheduleManagementAction.AddNewBill) },
                            onViewAllBills = { viewModel.trySendAction(AutoPayScheduleManagementAction.ViewAllBills) },
                        )
                    }
                } else {
                    items(state.billsWithAutoPay) { bill ->
                        AutoPayScheduleCard(
                            bill = bill,
                            onEdit = { viewModel.trySendAction(AutoPayScheduleManagementAction.EditBill(bill.id ?: "")) },
                            onToggleAutoPay = { enabled ->
                                viewModel.trySendAction(AutoPayScheduleManagementAction.ToggleAutoPay(bill.id ?: "", enabled))
                            },
                            onDelete = { viewModel.trySendAction(AutoPayScheduleManagementAction.DeleteBill(bill.id ?: "")) },
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MifosButton(
                    text = { Text("Add Bill") },
                    onClick = { viewModel.trySendAction(AutoPayScheduleManagementAction.AddNewBill) },
                    modifier = Modifier.weight(1f),
                )

                MifosButton(
                    text = { Text("All Bills") },
                    onClick = { viewModel.trySendAction(AutoPayScheduleManagementAction.ViewAllBills) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ScheduleManagementHeader(
    totalSchedules: Int,
    activeSchedules: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
            ) {
                ScheduleStatItem(
                    label = "Total Schedules",
                    value = totalSchedules.toString(),
                    icon = MifosIcons.List,
                )

                ScheduleStatItem(
                    label = "Active Schedules",
                    value = activeSchedules.toString(),
                    icon = MifosIcons.CheckCircle,
                )
            }
        }
    }
}

@Composable
private fun ScheduleStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun AutoPayScheduleCard(
    bill: Bill,
    onEdit: () -> Unit,
    onToggleAutoPay: (Boolean) -> Unit,
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
                    Switch(
                        checked = bill.autoPayEnabled,
                        onCheckedChange = onToggleAutoPay,
                    )

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
                        text = "Next Payment",
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

            if (bill.autoPayPaymentMethod != null || bill.autoPaySourceAccount != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (bill.autoPayPaymentMethod != null) {
                        Column {
                            Text(
                                text = "Payment Method",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = bill.autoPayPaymentMethod ?: "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    if (bill.autoPaySourceAccount != null) {
                        Column {
                            Text(
                                text = "Source Account",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = bill.autoPaySourceAccount ?: "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySchedulesCard(
    onAddBill: () -> Unit,
    onViewAllBills: () -> Unit,
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
                imageVector = MifosIcons.Payment,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No AutoPay Schedules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create bills with AutoPay enabled to see your schedules here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
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

                Button(
                    onClick = onViewAllBills,
                ) {
                    Icon(
                        imageVector = MifosIcons.List,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View All Bills")
                }
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
