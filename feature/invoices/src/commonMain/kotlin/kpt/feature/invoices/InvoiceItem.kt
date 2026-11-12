/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.invoices

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.model.datatables.invoice.Invoice
import kpt.core.ui.AvatarBox
import kpt.feature.invoices.generated.resources.Res
import kpt.feature.invoices.generated.resources.ic_check
import kpt.feature.invoices.generated.resources.ic_remove
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun InvoiceItem(
    invoice: Invoice,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        onClick = {
            onClick(invoice.invoiceId)
        },
        shape = KptTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(text = invoice.title)
            },
            supportingContent = {
                Text(text = "${invoice.date} | ${invoice.consumerName}")
            },
            leadingContent = {
                AvatarBox(
                    icon = vectorResource(
                        if (invoice.status == 0L) {
                            Res.drawable.ic_remove
                        } else {
                            Res.drawable.ic_check
                        },
                    ),
                    contentColor = if (invoice.status == 1L) {
                        KptTheme.colorScheme.primary
                    } else {
                        KptTheme.colorScheme.error
                    },
                )
            },
            trailingContent = {
                Text(
                    text = invoice.amount.toString(),
                    fontWeight = FontWeight.SemiBold,
                    style = KptTheme.typography.titleSmall,
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewInvoiceItem() {
    InvoiceItem(
        invoice = Invoice(
            id = 6597,
            clientId = 3986,
            consumerId = "fusce",
            consumerName = "Lara Shepherd",
            amount = 2.3,
            itemsBought = "quaestio",
            status = 8907,
            transactionId = "sem",
            invoiceId = 5209,
            title = "interdum",
            date = "quem",
            createdAt = listOf(),
            updatedAt = listOf(),
        ),
        onClick = {},
    )
}
