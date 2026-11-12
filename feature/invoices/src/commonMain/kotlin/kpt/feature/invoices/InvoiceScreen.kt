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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.model.datatables.invoice.Invoice
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.invoices.generated.resources.Res
import kpt.feature.invoices.generated.resources.feature_invoices_error_no_invoices_found
import kpt.feature.invoices.generated.resources.feature_invoices_error_oops
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
/** Screen listing the client's invoices; selecting one calls [navigateToInvoiceDetailScreen] with its id. */
@Composable
fun InvoiceScreen(
    navigateToInvoiceDetailScreen: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoicesViewModel = koinViewModel(),
) {
    val invoiceUiState by viewModel.invoiceUiState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is InvoiceEvent.NavigateToInvoiceDetail -> {
                navigateToInvoiceDetailScreen(event.invoiceId)
            }
        }
    }

    InvoiceScreen(
        state = invoiceUiState,
        onRetry = viewModel::retry,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        modifier = modifier,
    )
}

@Composable
private fun InvoiceScreen(
    state: ScreenState<List<Invoice>>,
    onRetry: () -> Unit,
    onAction: (InvoiceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
    ) { paddingValues ->
        // Template idiom: `ScreenContent` (core-base/ui) owns every render branch —
        // loading / empty / no-network / unauthenticated / error+retry — driven by
        // the stream's pre-decided `ScreenState`. Only the per-item Content body is
        // authored here; the empty state keeps the feature's existing copy.
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            empty = {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_invoices_error_oops),
                    subTitle = stringResource(Res.string.feature_invoices_error_no_invoices_found),
                )
            },
        ) { invoices, _ ->
            InvoicesList(
                invoiceList = invoices,
                onClickInvoice = {
                    onAction(InvoiceAction.InvoiceClicked(it))
                },
            )
        }
    }
}

@Composable
private fun InvoicesList(
    invoiceList: List<Invoice>,
    onClickInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        items(
            items = invoiceList,
            key = { it.invoiceId },
        ) { invoice ->
            InvoiceItem(
                invoice = invoice,
                onClick = onClickInvoice,
            )
        }
    }
}
