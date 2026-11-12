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

/**
 * A recurring payment schedule that automatically pays a recipient from a source account.
 *
 * @property id Unique id of the auto-pay schedule.
 * @property name Display name of the schedule.
 * @property description Free-text description.
 * @property amount Amount paid on each run.
 * @property currency Currency code of the payment.
 * @property frequency Recurrence frequency code.
 * @property frequencyInterval Number of frequency units between runs.
 * @property nextPaymentDate Date of the next scheduled payment.
 * @property status Current lifecycle status of the schedule.
 * @property recipientName Name of the payee.
 * @property recipientAccountNumber Account number of the payee.
 * @property recipientBankCode Bank code of the payee's bank.
 * @property sourceAccountId Id of the account payments are drawn from.
 * @property sourceAccountNumber Account number of the source account.
 * @property sourceAccountType Type of the source account.
 * @property clientId Id of the client that owns the schedule.
 * @property createdDate Date the schedule was created.
 * @property lastModifiedDate Date the schedule was last modified.
 * @property validFrom Date from which the schedule is valid.
 * @property validTill Date until which the schedule is valid.
 * @property maxAmount Upper limit allowed for a single payment.
 * @property minAmount Lower limit allowed for a single payment.
 * @property paymentMethod Payment method used to execute the payment.
 * @property isActive Whether the schedule is currently active.
 */
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

/**
 * Template of the options available when creating an auto-pay schedule.
 *
 * @property id Id of the template.
 * @property name Name of the template.
 * @property description Description of the template.
 * @property frequencyOptions Selectable recurrence frequencies.
 * @property paymentMethods Selectable payment methods.
 * @property currencyOptions Selectable currencies.
 * @property accountTypes Selectable source account types.
 * @property maxAmount Maximum payment amount allowed.
 * @property minAmount Minimum payment amount allowed.
 */
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

/**
 * A selectable recurrence frequency for an auto-pay schedule.
 *
 * @property id Id of the option.
 * @property code Machine-readable code.
 * @property value Human-readable label.
 * @property description Optional longer description.
 */
@Serializable
data class FrequencyOption(
    val id: Long,
    val code: String,
    val value: String,
    val description: String? = null,
)

/**
 * A selectable payment method for an auto-pay schedule.
 *
 * @property id Id of the method.
 * @property code Machine-readable code.
 * @property value Human-readable label.
 * @property description Optional longer description.
 */
@Serializable
data class PaymentMethod(
    val id: Long,
    val code: String,
    val value: String,
    val description: String? = null,
)

/**
 * A selectable currency for an auto-pay schedule.
 *
 * @property code Currency code.
 * @property name Currency name.
 * @property symbol Optional currency symbol.
 */
@Serializable
data class CurrencyOption(
    val code: String,
    val name: String,
    val symbol: String? = null,
)

/**
 * A selectable source account type for an auto-pay schedule.
 *
 * @property id Id of the account type.
 * @property code Machine-readable code.
 * @property value Human-readable label.
 * @property description Optional longer description.
 */
@Serializable
data class AccountType(
    val id: Long,
    val code: String,
    val value: String,
    val description: String? = null,
)

/**
 * Lifecycle status of an auto-pay schedule.
 */
@Serializable
enum class AutoPayStatus {
    /**
     * Schedule is running and will make payments.
     */
    ACTIVE,

    /**
     * Schedule is temporarily suspended.
     */
    PAUSED,

    /**
     * Schedule was cancelled by the user.
     */
    CANCELLED,

    /**
     * Schedule has run to its end date.
     */
    COMPLETED,

    /**
     * Schedule stopped because a payment failed.
     */
    FAILED,

    /**
     * Schedule is awaiting activation or approval.
     */
    PENDING,
}

/**
 * Status of a single auto-pay payment occurrence.
 */
@Serializable
enum class PaymentStatus {
    /**
     * Payment is scheduled but not yet due.
     */
    UPCOMING,

    /**
     * Payment is currently being executed.
     */
    PROCESSING,

    /**
     * Payment is awaiting processing.
     */
    PENDING,

    /**
     * Payment was made successfully.
     */
    COMPLETED,

    /**
     * Payment attempt failed.
     */
    FAILED,

    /**
     * Payment was cancelled.
     */
    CANCELLED,
}

/**
 * User-wide auto-pay preferences that apply across all schedules.
 *
 * @property isAutoPayEnabled Whether auto-pay is enabled globally.
 * @property defaultPaymentMethod Payment method used when none is specified.
 * @property defaultSourceAccount Source account used when none is specified.
 * @property maxPaymentAmount Global cap on a single payment amount.
 * @property notificationSettings Which notifications are sent for auto-pay events.
 * @property securitySettings Security limits applied to auto-pay payments.
 * @property globalAutoPayRules Approval and retry rules applied to auto-pay payments.
 */
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

/**
 * Notification preferences for auto-pay events.
 *
 * @property paymentConfirmations Notify when a payment succeeds.
 * @property failedPaymentAlerts Notify when a payment fails.
 * @property scheduleReminders Send reminders before scheduled payments.
 * @property reminderDaysBefore How many days before a payment the reminder is sent.
 * @property emailNotifications Deliver notifications by email.
 * @property pushNotifications Deliver notifications as push messages.
 * @property smsNotifications Deliver notifications by SMS.
 */
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

/**
 * Security limits applied to auto-pay payments.
 *
 * @property requireTwoFactorAuth Require two-factor authentication for auto-pay changes.
 * @property maxDailyAmount Maximum total amount allowed per day.
 * @property requireConfirmationForLargePayments Ask for confirmation on payments above largePaymentThreshold.
 * @property largePaymentThreshold Amount above which a payment counts as large.
 * @property allowMultiplePaymentsPerDay Whether more than one payment may run per day.
 * @property maxPaymentsPerDay Maximum number of payments allowed per day.
 */
@Serializable
data class SecuritySettings(
    val requireTwoFactorAuth: Boolean = false,
    val maxDailyAmount: Double? = null,
    val requireConfirmationForLargePayments: Boolean = true,
    val largePaymentThreshold: Double = 1000.0,
    val allowMultiplePaymentsPerDay: Boolean = true,
    val maxPaymentsPerDay: Int = 10,
)

/**
 * Approval and retry rules for auto-pay payments.
 *
 * @property autoApprovePayments Approve payments automatically without user action.
 * @property requireManualApprovalAbove Amount above which manual approval is required.
 * @property skipPaymentsOnHolidays Skip payments that fall on holidays.
 * @property retryFailedPayments Retry payments that fail.
 * @property maxRetryAttempts Maximum number of retry attempts.
 * @property retryIntervalHours Hours to wait between retries.
 */
@Serializable
data class AutoPayRules(
    val autoApprovePayments: Boolean = false,
    val requireManualApprovalAbove: Double? = null,
    val skipPaymentsOnHolidays: Boolean = true,
    val retryFailedPayments: Boolean = true,
    val maxRetryAttempts: Int = 3,
    val retryIntervalHours: Int = 24,
)
