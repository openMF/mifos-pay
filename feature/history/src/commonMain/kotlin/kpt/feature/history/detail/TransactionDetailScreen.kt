/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.utils.EventsEffect
import kpt.feature.history.components.TransactionDetail
import kpt.feature.history.generated.resources.Res
import kpt.feature.history.generated.resources.feature_history_error
import kpt.feature.history.generated.resources.feature_history_error_oops
import kpt.feature.history.generated.resources.feature_history_share
import kpt.feature.history.generated.resources.feature_history_transaction_details
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onShareTransaction: (transferId: Long) -> Unit = {},
    viewModel: TransactionDetailViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            TransactionDetailEvent.OnNavigateBack -> onNavigateBack.invoke()
            TransactionDetailEvent.OnShareTransaction -> {
                // Opens the dedicated Receipt card (shareable summary) for this transfer —
                // reads off the same TransferDetail Store5 vertical this screen already uses.
                viewModel.transferId?.let(onShareTransaction)
            }
        }
    }

    TransactionDetailScreenContent(
        state = state.viewState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { action -> viewModel.trySendAction(action) }
        },
    )
}

@Composable
internal fun TransactionDetailScreenContent(
    state: TransactionDetailState.ViewState,
    modifier: Modifier = Modifier,
    onAction: (TransactionDetailAction) -> Unit,
) {
    MifosScaffold(
        backPress = { onAction(TransactionDetailAction.NavigateBack) },
        topBarTitle = stringResource(Res.string.feature_history_transaction_details),
        actions = {
            IconButton(
                onClick = {
                    onAction(TransactionDetailAction.ShareTransaction)
                },
            ) {
                Icon(
                    imageVector = MifosIcons.OutlinedShare,
                    contentDescription = stringResource(Res.string.feature_history_share),
                )
            }
        },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is TransactionDetailState.ViewState.Loading -> MifosProgressIndicator()

                is TransactionDetailState.ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_history_error_oops),
                        subTitle = stringResource(Res.string.feature_history_error),
                        modifier = Modifier.align(Alignment.Center),
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is TransactionDetailState.ViewState.Content -> {
                    TransactionDetailScreenContent(
                        state = state,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

@Composable
internal fun TransactionDetailScreenContent(
    state: TransactionDetailState.ViewState.Content,
    modifier: Modifier = Modifier,
) {
    TransactionDetail(
        detail = state.transaction,
        modifier = modifier,
    )
}
