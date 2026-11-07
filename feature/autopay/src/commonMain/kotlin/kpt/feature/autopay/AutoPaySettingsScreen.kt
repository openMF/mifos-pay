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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import org.koin.compose.viewmodel.koinViewModel
/** Screen for editing AutoPay preferences: global, notification and security settings and rules. */
@Composable
fun AutoPayPreferencesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AutoPayPreferencesViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AutoPayPreferencesEvent.SettingsSaved -> {
                onNavigateBack()
            }
            is AutoPayPreferencesEvent.ShowError -> {
                // Handle error display
            }
        }
    }

    MifosScaffold(
        modifier = modifier,
        topBarTitle = "AutoPay Settings",
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GeneralSettingsSection(
                    settings = state.globalSettings,
                    onToggleAutoPay = { enabled ->
                        viewModel.trySendAction(AutoPayPreferencesAction.ToggleAutoPayEnabled(enabled))
                    },
                )

                NotificationSettingsSection(
                    settings = state.globalSettings.notificationSettings,
                    onSettingsChanged = { notificationSettings ->
                        viewModel.trySendAction(AutoPayPreferencesAction.UpdateNotificationSettings(notificationSettings))
                    },
                )

                SecuritySettingsSection(
                    settings = state.globalSettings.securitySettings,
                    onSettingsChanged = { securitySettings ->
                        viewModel.trySendAction(AutoPayPreferencesAction.UpdateSecuritySettings(securitySettings))
                    },
                )

                AutoPayRulesSection(
                    rules = state.globalSettings.globalAutoPayRules,
                    onRulesChanged = { rules ->
                        viewModel.trySendAction(AutoPayPreferencesAction.UpdateAutoPayRules(rules))
                    },
                )

                if (state.hasUnsavedChanges) {
                    Spacer(modifier = Modifier.height(16.dp))

                    MifosButton(
                        onClick = { viewModel.trySendAction(AutoPayPreferencesAction.SaveSettings) },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Save Settings")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneralSettingsSection(
    settings: kpt.core.model.autopay.AutoPayGlobalSettings,
    onToggleAutoPay: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = "General",
        modifier = modifier,
    ) {
        SettingRow(
            title = "AutoPay Enabled",
            description = "Enable or disable AutoPay functionality globally",
            icon = MifosIcons.Power,
            checked = settings.isAutoPayEnabled,
            onCheckedChange = onToggleAutoPay,
        )
    }
}

@Composable
private fun NotificationSettingsSection(
    settings: kpt.core.model.autopay.NotificationSettings,
    onSettingsChanged: (kpt.core.model.autopay.NotificationSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = "Notifications",
        modifier = modifier,
    ) {
        SettingRow(
            title = "Payment Confirmations",
            description = "Receive notifications when payments are processed",
            icon = MifosIcons.OutlinedNotifications,
            checked = settings.paymentConfirmations,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(paymentConfirmations = enabled))
            },
        )

        SettingRow(
            title = "Failed Payment Alerts",
            description = "Get notified when payments fail",
            icon = MifosIcons.Warning,
            checked = settings.failedPaymentAlerts,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(failedPaymentAlerts = enabled))
            },
        )

        SettingRow(
            title = "Schedule Reminders",
            description = "Receive reminders before scheduled payments",
            icon = MifosIcons.Schedule,
            checked = settings.scheduleReminders,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(scheduleReminders = enabled))
            },
        )

        SettingRow(
            title = "Email Notifications",
            description = "Receive notifications via email",
            icon = MifosIcons.Email,
            checked = settings.emailNotifications,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(emailNotifications = enabled))
            },
        )

        SettingRow(
            title = "Push Notifications",
            description = "Receive push notifications on your device",
            icon = MifosIcons.OutlinedNotifications,
            checked = settings.pushNotifications,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(pushNotifications = enabled))
            },
        )
    }
}

@Composable
private fun SecuritySettingsSection(
    settings: kpt.core.model.autopay.SecuritySettings,
    onSettingsChanged: (kpt.core.model.autopay.SecuritySettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = "Security",
        modifier = modifier,
    ) {
        SettingRow(
            title = "Two-Factor Authentication",
            description = "Require 2FA for AutoPay changes",
            icon = MifosIcons.Security,
            checked = settings.requireTwoFactorAuth,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(requireTwoFactorAuth = enabled))
            },
        )

        SettingRow(
            title = "Large Payment Confirmation",
            description = "Require confirmation for payments above threshold",
            icon = MifosIcons.AttachMoney,
            checked = settings.requireConfirmationForLargePayments,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(requireConfirmationForLargePayments = enabled))
            },
        )

        SettingRow(
            title = "Multiple Payments Per Day",
            description = "Allow multiple AutoPay transactions per day",
            icon = MifosIcons.Repeat,
            checked = settings.allowMultiplePaymentsPerDay,
            onCheckedChange = { enabled ->
                onSettingsChanged(settings.copy(allowMultiplePaymentsPerDay = enabled))
            },
        )
    }
}

@Composable
private fun AutoPayRulesSection(
    rules: kpt.core.model.autopay.AutoPayRules,
    onRulesChanged: (kpt.core.model.autopay.AutoPayRules) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = "AutoPay Rules",
        modifier = modifier,
    ) {
        SettingRow(
            title = "Auto-Approve Payments",
            description = "Automatically approve payments without manual confirmation",
            icon = MifosIcons.CheckCircle,
            checked = rules.autoApprovePayments,
            onCheckedChange = { enabled ->
                onRulesChanged(rules.copy(autoApprovePayments = enabled))
            },
        )

        SettingRow(
            title = "Skip Payments on Holidays",
            description = "Skip AutoPay on bank holidays",
            icon = MifosIcons.CalenderMonth,
            checked = rules.skipPaymentsOnHolidays,
            onCheckedChange = { enabled ->
                onRulesChanged(rules.copy(skipPaymentsOnHolidays = enabled))
            },
        )

        SettingRow(
            title = "Retry Failed Payments",
            description = "Automatically retry failed payment attempts",
            icon = MifosIcons.Refresh,
            checked = rules.retryFailedPayments,
            onCheckedChange = { enabled ->
                onRulesChanged(rules.copy(retryFailedPayments = enabled))
            },
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
