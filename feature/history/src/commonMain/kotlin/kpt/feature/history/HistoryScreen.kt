/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.TransactionFilterBottomSheet
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.history.components.TransactionList
import kpt.feature.history.generated.resources.Res
import kpt.feature.history.generated.resources.feature_history_empty
import kpt.feature.history.generated.resources.feature_history_empty_filter
import kpt.feature.history.generated.resources.feature_history_error_oops
import kpt.feature.history.generated.resources.feature_history_filter_content_desc
import kpt.feature.history.generated.resources.feature_history_header_account
import kpt.feature.history.generated.resources.feature_history_header_all
import kpt.feature.history.generated.resources.feature_history_header_credit
import kpt.feature.history.generated.resources.feature_history_header_debit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
/** Transaction history screen with type and account filters and PDF export; [viewTransferDetail] opens a transfer. */
@Composable
fun HistoryScreen(
    viewTransferDetail: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    EventsEffect(viewModel) { event ->
        when (event) {
            is HistoryEvent.OnTransactionDetail -> {
                state.selectedAccount?.id?.let { accountId ->
                    viewTransferDetail.invoke(accountId, event.transferId)
                }
            }

            is HistoryEvent.PdfExportError -> {
                scope.launch { snackbarHostState.showSnackbar(event.message) }
            }
        }
    }

    HistoryScreenContent(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = remember(viewModel) {
            { action -> viewModel.trySendAction(action) }
        },
    )
}

@Composable
internal fun HistoryScreenContent(
    state: HistoryState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (HistoryAction) -> Unit,
) {
    ForkScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHostState = snackbarHostState,
    ) { paddingValues ->
        when (state.viewState) {
            is HistoryState.ViewState.Loading -> ForkProgressIndicator()

            is HistoryState.ViewState.Error -> {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_history_error_oops),
                    subTitle = stringResource(state.viewState.message),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    iconTint = KptTheme.colorScheme.error,
                )
            }

            is HistoryState.ViewState.Empty -> {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_history_error_oops),
                    subTitle = stringResource(Res.string.feature_history_empty),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }

            is HistoryState.ViewState.Content -> {
                Column(
                    Modifier.fillMaxWidth().padding(paddingValues),
                ) {
                    HistoryScreenHeader(
                        accountNo = state.selectedAccount?.number ?: "",
                        selectedTransactionType = state.selectedTransactionType,
                        isExportingPdf = state.isExportingPdf,
                        onFilterClick = {
                            onAction(HistoryAction.OnFilterClick)
                        },
                        onExportClick = {
                            onAction(HistoryAction.ExportPdf)
                        },
                    )
                    if (state.viewState.list.isEmpty()) {
                        EmptyContentScreen(
                            title = stringResource(Res.string.feature_history_error_oops),
                            subTitle = stringResource(Res.string.feature_history_empty_filter),
                            modifier = Modifier
                                .fillMaxSize(),
                        )
                    } else {
                        TransactionList(
                            transactions = state.viewState.list,
                            onAction = onAction,
                            modifier = modifier,
                        )
                    }
                }
                if (state.showFilter) {
                    TransactionFilterBottomSheet(
                        selectedAccount = state.selectedAccount,
                        accounts = state.accounts,
                        selectedTransactionType = state.selectedTransactionType,
                        onAccountSelected = {
                            onAction(HistoryAction.SetSelectedAccount(it))
                        },
                        onTransactionTypeSelected = {
                            onAction(HistoryAction.SetTransactionType(it))
                        },
                        onClearFilters = {
                            onAction(HistoryAction.ClearFilters)
                        },
                        onApplyFilters = {
                            onAction(HistoryAction.OnApplyFilterClick)
                        },
                        onDismiss = {
                            onAction(HistoryAction.OnFilterClick)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryScreenHeader(
    accountNo: String,
    selectedTransactionType: TransactionType,
    isExportingPdf: Boolean,
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(Res.string.feature_history_header_account, accountNo),
                style = KptTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(KptTheme.spacing.xs))
            Text(
                text = when (selectedTransactionType) {
                    TransactionType.OTHER -> stringResource(Res.string.feature_history_header_all)
                    TransactionType.DEBIT -> stringResource(Res.string.feature_history_header_debit)
                    TransactionType.CREDIT -> stringResource(Res.string.feature_history_header_credit)
                },
                style = KptTheme.typography.bodyMedium,
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .border(
                    width = 1.dp,
                    color = KptTheme.colorScheme.outline,
                    shape = KptTheme.shapes.medium,
                )
                .clip(KptTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.matchParentSize(),
            ) {
                Icon(
                    imageVector = ForkIcons.Filter,
                    contentDescription = stringResource(Res.string.feature_history_filter_content_desc),
                    tint = KptTheme.colorScheme.onSurface,
                )
            }

            if (selectedTransactionType != TransactionType.OTHER) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(KptTheme.colorScheme.error),
                )
            }
        }

        IconButton(
            onClick = onExportClick,
            enabled = !isExportingPdf,
        ) {
            if (isExportingPdf) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = ForkIcons.Download,
                    contentDescription = "Export PDF",
                )
            }
        }
    }
}
