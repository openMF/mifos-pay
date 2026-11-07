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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
/** Screen showing the details of one AutoPay schedule, with pause, resume, edit and cancel actions. */
@Composable
fun AutoPayScheduleDetailsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayScheduleDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    MifosScaffold(
        modifier = modifier,
        topBarTitle = "Schedule Details",
        backPress = onNavigateBack,
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.schedule == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = MifosIcons.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Schedule Not Found",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The requested AutoPay schedule could not be found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            ScheduleDetailsContent(
                schedule = state.schedule!!,
                modifier = Modifier.padding(paddingValues),
                onPauseResume = {
                    if (state.schedule!!.status == AutoPayStatus.ACTIVE) {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.PauseSchedule)
                    } else {
                        viewModel.trySendAction(AutoPayScheduleDetailsAction.ResumeSchedule)
                    }
                },
                onEdit = { viewModel.trySendAction(AutoPayScheduleDetailsAction.EditSchedule) },
                onCancel = { viewModel.trySendAction(AutoPayScheduleDetailsAction.CancelSchedule) },
            )
        }
    }

    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayScheduleDetailsEvent.NavigateBack -> onNavigateBack()
            is AutoPayScheduleDetailsEvent.SchedulePaused -> { /* TODO: Show success message */ }
            is AutoPayScheduleDetailsEvent.ScheduleResumed -> { /* TODO: Show success message */ }
            is AutoPayScheduleDetailsEvent.ScheduleCancelled -> { /* TODO: Show success message */ }
            is AutoPayScheduleDetailsEvent.NavigateToEdit -> { /* TODO: Navigate to edit screen */ }
        }
    }
}

@Composable
private fun ScheduleDetailsContent(
    schedule: AutoPaySchedule,
    onPauseResume: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScheduleHeaderCard(schedule = schedule)

        ScheduleInfoCard(schedule = schedule)

        PaymentDetailsCard(schedule = schedule)

        ScheduleActionsCard(
            schedule = schedule,
            onPauseResume = onPauseResume,
            onEdit = onEdit,
            onCancel = onCancel,
        )
    }
}

@Composable
private fun ScheduleHeaderCard(
    schedule: AutoPaySchedule,
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
            Text(
                text = schedule.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusChip(status = schedule.status)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = CurrencyFormatter.format(schedule.amount, schedule.currency, 2),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Text(
                text = "per ${schedule.frequency.lowercase()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ScheduleInfoCard(
    schedule: AutoPaySchedule,
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
                text = "Schedule Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                label = "Recipient",
                value = schedule.recipientName,
                icon = MifosIcons.Person,
            )

            InfoRow(
                label = "Account Number",
                value = schedule.accountNumber,
                icon = MifosIcons.Bank,
            )

            InfoRow(
                label = "Frequency",
                value = schedule.frequency,
                icon = MifosIcons.CalenderMonth,
            )

            InfoRow(
                label = "Next Payment",
                value = schedule.nextPaymentDate,
                icon = MifosIcons.CalenderMonth,
            )
        }
    }
}

@Composable
private fun PaymentDetailsCard(
    schedule: AutoPaySchedule,
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
                text = "Payment Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                label = "Amount",
                value = CurrencyFormatter.format(schedule.amount, schedule.currency, 2),
                icon = MifosIcons.AttachMoney,
            )

            InfoRow(
                label = "Status",
                value = schedule.status.name,
                icon = MifosIcons.Info,
            )
        }
    }
}

@Composable
private fun ScheduleActionsCard(
    schedule: AutoPaySchedule,
    onPauseResume: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
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
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionButton(
                    text = if (schedule.status == AutoPayStatus.ACTIVE) "Pause" else "Resume",
                    icon = if (schedule.status == AutoPayStatus.ACTIVE) MifosIcons.FlashOff else MifosIcons.FlashOn,
                    onClick = onPauseResume,
                    modifier = Modifier.weight(1f),
                )

                ActionButton(
                    text = "Edit",
                    icon = MifosIcons.Edit,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionButton(
                text = "Cancel Schedule",
                icon = MifosIcons.Delete,
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(text = text)
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
