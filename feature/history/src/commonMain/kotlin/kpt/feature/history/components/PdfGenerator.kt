/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("ktlint:standard:filename", "MatchingDeclarationName")

package kpt.feature.history.components

import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.strong
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import kpt.core.common.CurrencyFormatter
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kotlin.time.ExperimentalTime

object HistoryHtmlTemplate {

    @OptIn(ExperimentalTime::class)
    fun generate(
        accountNumber: String,
        transactionType: String,
        transactions: List<Transaction>,
    ): String {
        val document = createHTML().html {
            head {
                style {
                    unsafe {
                        +"""
                            body {
                                font-family: sans-serif;
                                color: #222;
                                font-size: 14px;
                            }

                            h1 {
                                text-align: center;
                                margin-bottom: 4px;
                            }

                            .header {
                                margin-bottom: 24px;
                            }

                            .header p {
                                margin: 4px 0;
                            }

                            table {
                                width: 100%;
                                border-collapse: collapse;
                            }

                            th {
                                background: #f5f5f5;
                                border: 1px solid #cccccc;
                                padding: 10px;
                                text-align: left;
                            }

                            td {
                                border: 1px solid #cccccc;
                                padding: 8px;
                            }

                            .credit {
                                color: green;
                                font-weight: bold;
                            }

                            .debit {
                                color: red;
                                font-weight: bold;
                            }

                            .footer {
                                margin-top: 24px;
                                text-align: right;
                                font-size: 12px;
                                color: gray;
                            }

                            /* PAGE_CONFIG_PLACEHOLDER */
                        """.trimIndent()
                    }
                }
            }

            body {
                h1 {
                    +"Transaction History"
                }

                div("header") {
                    p {
                        strong { +"Account Number : " }
                        +accountNumber
                    }

                    p {
                        strong { +"Transaction Type : " }
                        +transactionType
                    }

                    p {
                        strong { +"Transactions : " }
                        +"${transactions.size}"
                    }
                }

                table {
                    thead {
                        tr {
                            th { +"Date" }

                            th { +"Description" }

                            th { +"Type" }

                            th {
                                style = "text-align:right"
                                +"Amount"
                            }
                        }
                    }

                    tbody {
                        transactions.forEach { transaction ->

                            tr {
                                td {
                                    +transaction.date
                                }

                                td {
                                    +(
                                        transaction.description.ifBlank {
                                            "N/A"
                                        }
                                        )
                                }

                                td {
                                    +transaction.transactionType.name
                                }

                                td {
                                    style = "text-align:right"

                                    span(
                                        classes = if (transaction.transactionType == TransactionType.CREDIT) {
                                            "credit"
                                        } else {
                                            "debit"
                                        },
                                    ) {
                                        +CurrencyFormatter.format(
                                            balance = transaction.amount,
                                            currencyCode = transaction.currency.code,
                                            maximumFractionDigits = 2,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                div("footer") {
                    val instant = kotlin.time.Clock.System.now()

                    +"Generated on $instant"
                }
            }
        }

        return "<!DOCTYPE html>\n$document"
    }
}
