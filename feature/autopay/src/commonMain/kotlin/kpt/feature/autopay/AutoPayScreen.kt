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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.rememberForkPullToRefreshState
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.ui.fork.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
/** AutoPay dashboard screen showing active schedules and upcoming payments, with entry points to related screens. */
@Composable
fun AutoPayScreen(
    onNavigateToScheduleManagement: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToScheduleDetails: (String) -> Unit,
    onNavigateToAddBiller: () -> Unit,
    onNavigateToBillerList: () -> Unit,
    onNavigateToAddBill: () -> Unit,
    onNavigateToBillList: () -> Unit,
    onNavigateBack: () -> Unit = {},
    showTopBar: Boolean = true,
    modifier: Modifier = Modifier,
    viewModel: AutoPayViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val pullRefreshState = rememberForkPullToRefreshState(
        isEnabled = true,
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.trySendAction(AutoPayAction.RefreshDashboard) },
    )

    ForkScaffold(
        modifier = modifier,
        topBarTitle = if (showTopBar) "AutoPay Dashboard" else null,
        backPress = onNavigateBack,
        pullToRefreshState = pullRefreshState,
    ) { paddingValues ->
        if (state.isLoading && state.activeSchedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            AutoPayDashboardContent(
                state = state,
                viewModel = viewModel,
                onRefresh = { viewModel.trySendAction(AutoPayAction.RefreshDashboard) },
                onViewScheduleDetails = { scheduleId ->
                    viewModel.trySendAction(AutoPayAction.ViewScheduleDetails(scheduleId))
                },
                onNavigateToScheduleManagement = onNavigateToScheduleManagement,
                onNavigateToPreferences = onNavigateToPreferences,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToScheduleDetails = onNavigateToScheduleDetails,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }

    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayEvent.NavigateToScheduleManagement -> onNavigateToScheduleManagement()
            is AutoPayEvent.NavigateToPreferences -> onNavigateToPreferences()
            is AutoPayEvent.NavigateToHistory -> onNavigateToHistory()
            is AutoPayEvent.NavigateToAddBiller -> onNavigateToAddBiller()
            is AutoPayEvent.NavigateToBillerList -> onNavigateToBillerList()
            is AutoPayEvent.NavigateToAddBill -> onNavigateToAddBill()
            is AutoPayEvent.NavigateToBillList -> onNavigateToBillList()
            is AutoPayEvent.NavigateToScheduleDetails -> onNavigateToScheduleDetails(event.scheduleId)
        }
    }
}

@Composable
private fun AutoPayDashboardContent(
    state: AutoPayState,
    viewModel: AutoPayViewModel,
    onRefresh: () -> Unit,
    onViewScheduleDetails: (String) -> Unit,
    onNavigateToScheduleManagement: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToScheduleDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DashboardHeader(
                totalActiveSchedules = state.totalActiveSchedules,
                totalUpcomingPayments = state.totalUpcomingPayments,
            )
        }

        item {
            QuickActionsSection(
                onAddBill = { viewModel.trySendAction(AutoPayAction.AddNewBill) },
                onManageBills = { viewModel.trySendAction(AutoPayAction.ViewBillList) },
                onAddBiller = { viewModel.trySendAction(AutoPayAction.AddNewBiller) },
                onManageBillers = { viewModel.trySendAction(AutoPayAction.ViewBillerList) },
                onAutoPaySettings = { viewModel.trySendAction(AutoPayAction.ManagePaymentPreferences) },
                onScheduleManagement = onNavigateToScheduleManagement,
                onViewHistory = { viewModel.trySendAction(AutoPayAction.GetPaymentHistory) },
            )
        }

        item {
            Text(
                text = "Active Schedules",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        if (state.activeSchedules.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Active Schedules",
                    description = "You don't have any active AutoPay schedules. Create one to get started!",
                    icon = ForkIcons.Payment,
                )
            }
        } else {
            items(state.activeSchedules) { schedule ->
                ActiveScheduleCard(
                    schedule = schedule,
                    onClick = { onViewScheduleDetails(schedule.id) },
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Upcoming Payments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        if (state.upcomingPayments.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Upcoming Payments",
                    description = "No payments are scheduled for the near future.",
                    icon = ForkIcons.CalenderMonth,
                )
            }
        } else {
            items(state.upcomingPayments) { payment ->
                UpcomingPaymentCard(
                    payment = payment,
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    totalActiveSchedules: Int,
    totalUpcomingPayments: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                DashboardStat(
                    label = "Active Schedules",
                    value = totalActiveSchedules.toString(),
                    icon = ForkIcons.Payment,
                )

                DashboardStat(
                    label = "Upcoming Payments",
                    value = totalUpcomingPayments.toString(),
                    icon = ForkIcons.CalenderMonth,
                )
            }
        }
    }
}

@Composable
private fun DashboardStat(
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
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun QuickActionsSection(
    onAddBill: () -> Unit,
    onManageBills: () -> Unit,
    onAddBiller: () -> Unit,
    onManageBillers: () -> Unit,
    onAutoPaySettings: () -> Unit,
    onScheduleManagement: () -> Unit,
    onViewHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SquareActionButton(
                    text = "Add Bill",
                    icon = ForkIcons.Receipt,
                    onClick = onAddBill,
                    modifier = Modifier.weight(1f),
                )

                SquareActionButton(
                    text = "Manage Bills",
                    icon = ForkIcons.List,
                    onClick = onManageBills,
                    modifier = Modifier.weight(1f),
                )

                SquareActionButton(
                    text = "Add Biller",
                    icon = ForkIcons.PersonAdd,
                    onClick = onAddBiller,
                    modifier = Modifier.weight(1f),
                )

                SquareActionButton(
                    text = "Manage Billers",
                    icon = ForkIcons.Person,
                    onClick = onManageBillers,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SquareActionButton(
                    text = "AutoPay Settings",
                    icon = ForkIcons.Settings,
                    onClick = onAutoPaySettings,
                    modifier = Modifier.weight(1f),
                )

                SquareActionButton(
                    text = "Manage Schedules",
                    icon = ForkIcons.Schedule,
                    onClick = onScheduleManagement,
                    modifier = Modifier.weight(1f),
                )

                SquareActionButton(
                    text = "View History",
                    icon = ForkIcons.History,
                    onClick = onViewHistory,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SquareActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun ActiveScheduleCard(
    schedule: AutoPaySchedule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
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
                            text = schedule.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )

                        Text(
                            text = schedule.recipientName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    StatusChip(status = schedule.status)
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
                            text = CurrencyFormatter.format(schedule.amount, schedule.currency, 2),
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
                            text = schedule.nextPaymentDate,
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
                            text = schedule.frequency,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingPaymentCard(
    payment: UpcomingPayment,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = payment.scheduleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )

                    Text(
                        text = payment.recipientName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                StatusChip(status = payment.status)
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
                        text = CurrencyFormatter.format(payment.amount, payment.currency, 2),
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
                        text = payment.dueDate,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: AutoPayStatus,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor) = when (status) {
        AutoPayStatus.ACTIVE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        AutoPayStatus.PAUSED -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        AutoPayStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        AutoPayStatus.COMPLETED -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
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
private fun StatusChip(
    status: PaymentStatus,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor) = when (status) {
        PaymentStatus.UPCOMING -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        PaymentStatus.PROCESSING -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        PaymentStatus.COMPLETED -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        PaymentStatus.FAILED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        PaymentStatus.CANCELLED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
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
private fun EmptyStateCard(
    title: String,
    description: String,
    icon: ImageVector,
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
                textAlign = TextAlign.Center,
            )
        }
    }
}
