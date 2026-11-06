/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.autopay

import kotlinx.serialization.Serializable

// TODO: Align data models with final API response schema once confirmed by backend

@Serializable
data class AutoPay(
    val id: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    val frequency: String? = null,
    val frequencyInterval: Int? = null,
    val nextPaymentDate: String? = null,
    val status: AutoPayStatus? = null,
    val recipientName: String? = null,
    val recipientAccountNumber: String? = null,
    val recipientBankCode: String? = null,
    val sourceAccountId: Long? = null,
    val sourceAccountNumber: String? = null,
    val sourceAccountType: String? = null,
    val clientId: Long? = null,
    val createdDate: String? = null,
    val lastModifiedDate: String? = null,
    val validFrom: String? = null,
    val validTill: String? = null,
    val maxAmount: Double? = null,
    val minAmount: Double? = null,
    val paymentMethod: String? = null,
    val isActive: Boolean? = null,
)

@Serializable
data class AutoPayTemplate(
    val id: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val frequencyOptions: List<FrequencyOption>? = emptyList(),
    val paymentMethods: List<PaymentMethod>? = emptyList(),
    val currencyOptions: List<CurrencyOption>? = emptyList(),
    val accountTypes: List<AccountType>? = emptyList(),
    val maxAmount: Double? = null,
    val minAmount: Double? = null,
)

@Serializable
data class FrequencyOption(
    val id: Long,
    val code: String,
    val value: String,
    val description: String? = null,
)

@Serializable
data class PaymentMethod(
    val id: Long,
    val code: String,
    val value: String,
    val description: String? = null,
)

@Serializable
data class CurrencyOption(
    val code: String,
    val name: String,
    val symbol: String? = null,
)

@Serializable
data class AccountType(
    val id: Long,
    val code: String,
    val value: String,
    val description: String? = null,
)

@Serializable
enum class AutoPayStatus {
    ACTIVE,
    PAUSED,
    CANCELLED,
    COMPLETED,
    FAILED,
    PENDING,
}

@Serializable
enum class PaymentStatus {
    UPCOMING,
    PROCESSING,
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
}

@Serializable
data class AutoPayGlobalSettings(
    val isAutoPayEnabled: Boolean = false,
    val defaultPaymentMethod: String? = null,
    val defaultSourceAccount: String? = null,
    val maxPaymentAmount: Double? = null,
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val securitySettings: SecuritySettings = SecuritySettings(),
    val globalAutoPayRules: AutoPayRules = AutoPayRules(),
)

@Serializable
data class NotificationSettings(
    val paymentConfirmations: Boolean = true,
    val failedPaymentAlerts: Boolean = true,
    val scheduleReminders: Boolean = true,
    val reminderDaysBefore: Int = 3,
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val smsNotifications: Boolean = false,
)

@Serializable
data class SecuritySettings(
    val requireTwoFactorAuth: Boolean = false,
    val maxDailyAmount: Double? = null,
    val requireConfirmationForLargePayments: Boolean = true,
    val largePaymentThreshold: Double = 1000.0,
    val allowMultiplePaymentsPerDay: Boolean = true,
    val maxPaymentsPerDay: Int = 10,
)

@Serializable
data class AutoPayRules(
    val autoApprovePayments: Boolean = false,
    val requireManualApprovalAbove: Double? = null,
    val skipPaymentsOnHolidays: Boolean = true,
    val retryFailedPayments: Boolean = true,
    val maxRetryAttempts: Int = 3,
    val retryIntervalHours: Int = 24,
)
