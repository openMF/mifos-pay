/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.standinginstruction

import kotlinx.serialization.Serializable
import kpt.core.model.savingsaccount.Currency
/**
 * Template returned by the server with defaults and options for creating a standing instruction.
 *
 * @property fromOffice Office of the paying client.
 * @property fromClient Paying client.
 * @property fromAccountType Type of the paying account.
 * @property fromOfficeOptions Offices selectable as source.
 * @property fromClientOptions Clients selectable as source.
 * @property fromAccountTypeOptions Account types selectable as source.
 * @property fromAccountOptions Accounts selectable as source.
 * @property toOfficeOptions Offices selectable as destination.
 * @property toAccountTypeOptions Account types selectable as destination.
 * @property transferTypeOptions Available transfer types.
 * @property statusOptions Available statuses.
 * @property instructionTypeOptions Available instruction types.
 * @property priorityOptions Available priorities.
 * @property recurrenceTypeOptions Available recurrence types.
 * @property recurrenceFrequencyOptions Available recurrence frequencies.
 */
@Serializable
data class SITemplate(
    val fromOffice: FromOffice? = null,
    val fromClient: FromClient? = null,
    val fromAccountType: String? = null,
    val fromOfficeOptions: List<FromOfficeOption>? = emptyList(),
    val fromClientOptions: List<FromClientOption>? = emptyList(),
    val fromAccountTypeOptions: List<Option>? = emptyList(),
    val fromAccountOptions: List<FromAccountOption>? = emptyList(),
    val toOfficeOptions: List<FromOfficeOption>? = emptyList(),
    val toAccountTypeOptions: List<Option>? = emptyList(),
    val transferTypeOptions: List<Option>? = emptyList(),
    val statusOptions: List<Option>? = emptyList(),
    val instructionTypeOptions: List<Option>? = emptyList(),
    val priorityOptions: List<Option>? = emptyList(),
    val recurrenceTypeOptions: List<Option>? = emptyList(),
    val recurrenceFrequencyOptions: List<Option>? = emptyList(),
) {

    /**
     * A selectable option (id, code and label) in a standing instruction template.
     *
     * @property id Unique identifier.
     * @property code Code identifying the item.
     * @property value Human-readable label.
     */
    @Serializable
    data class Option(
        val id: Long,
        val code: String,
        val value: String,
    )

    /**
     * Office of the paying client in a standing instruction template.
     *
     * @property id Office id.
     * @property name Display name.
     * @property nameDecorated Name decorated with its hierarchy level.
     * @property externalId External identifier.
     * @property openingDate Opening date of the office.
     * @property hierarchy Hierarchy path of the office.
     */
    @Serializable
    data class FromOffice(
        val id: Long,
        val name: String,
        val nameDecorated: String,
        val externalId: String,
        val openingDate: String,
        val hierarchy: String,
    )

    /**
     * Paying client in a standing instruction template.
     *
     * @property id Client id.
     * @property accountNo Account number.
     * @property externalId External identifier.
     * @property status Client status.
     * @property active Active.
     * @property activationDate Date the client was activated.
     * @property firstname First name.
     * @property lastname Last name.
     * @property displayName Display name.
     * @property mobileNo Mobile phone number.
     * @property emailAddress Email address.
     * @property dateOfBirth Date of birth.
     * @property isStaff Whether the client is a staff member.
     * @property officeId Identifier of the office.
     * @property officeName Name of the office.
     * @property timeline Key dates of the client.
     * @property savingsProductName Name of the savings product.
     * @property legalForm Legal form of the client.
     */
    @Serializable
    data class FromClient(
        val id: Long,
        val accountNo: String,
        val externalId: String,
        val status: Option,
        val active: Boolean,
        val activationDate: String,
        val firstname: String,
        val lastname: String,
        val displayName: String,
        val mobileNo: String,
        val emailAddress: String,
        val dateOfBirth: String,
        val isStaff: Boolean,
        val officeId: Long,
        val officeName: String,
        val timeline: Timeline,
        val savingsProductName: String,
        val legalForm: Option,
    )

    /**
     * Key dates and activating user of a client.
     *
     * @property submittedOnDate Date of submission.
     * @property activatedOnDate Date of activation.
     * @property activatedByUsername Username of the user who activated.
     * @property activatedByFirstname First name of the user who activated.
     * @property activatedByLastname Last name of the user who activated.
     */
    @Serializable
    data class Timeline(
        val submittedOnDate: String,
        val activatedOnDate: String,
        val activatedByUsername: String,
        val activatedByFirstname: String,
        val activatedByLastname: String,
    )

    /**
     * An office that can be selected as source or destination of a standing instruction.
     *
     * @property id Unique identifier.
     * @property name Display name.
     * @property nameDecorated Name decorated with its hierarchy level.
     */
    @Serializable
    data class FromOfficeOption(
        val id: Long,
        val name: String,
        val nameDecorated: String,
    )

    /**
     * A client that can be selected as source of a standing instruction.
     *
     * @property id Unique identifier.
     * @property displayName Display name.
     * @property isStaff Whether the client is a staff member.
     * @property officeId Identifier of the office.
     * @property officeName Name of the office.
     */
    @Serializable
    data class FromClientOption(
        val id: Long,
        val displayName: String,
        val isStaff: Boolean,
        val officeId: Long,
        val officeName: String,
    )

    /**
     * A savings account that can be selected as source of a standing instruction.
     *
     * @property id Unique identifier.
     * @property accountNo Account number.
     * @property clientId Identifier of the client.
     * @property clientName Name of the client.
     * @property productId Identifier of the savings product.
     * @property productName Name of the savings product.
     * @property fieldOfficerId Identifier of the assigned field officer.
     * @property currency Currency of the account.
     * @property externalId External identifier.
     */
    @Serializable
    data class FromAccountOption(
        val id: Long,
        val accountNo: String,
        val clientId: Long,
        val clientName: String,
        val productId: Long,
        val productName: String,
        val fieldOfficerId: Long,
        val currency: Currency,
        val externalId: String?,
    )
}
