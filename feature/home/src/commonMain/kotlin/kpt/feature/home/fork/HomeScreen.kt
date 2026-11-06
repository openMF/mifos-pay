/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.fork

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.scrollbar.DraggableScrollbar
import kpt.core.designsystem.component.scrollbar.rememberDraggableScroller
import kpt.core.designsystem.component.scrollbar.scrollbarState
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.model.account.Account
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.Status
import kpt.core.model.savingsaccount.Transaction
import kpt.core.model.savingsaccount.TransactionType
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ErrorScreenContent
import kpt.core.ui.MifosDivider
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.MifosProgressIndicatorMini
import kpt.core.ui.MifosSmallChip
import kpt.core.ui.TransactionFilterBottomSheet
import kpt.core.ui.TransactionItem
import kpt.core.ui.utils.EventsEffect
import kpt.feature.home.HomeAction
import kpt.feature.home.HomeEvent
import kpt.feature.home.HomeState
import kpt.feature.home.HomeViewModel
import kpt.feature.home.ViewState
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.arrow_backward
import kpt.feature.home.generated.resources.coin_image
import kpt.feature.home.generated.resources.feature_home_account_number
import kpt.feature.home.generated.resources.feature_home_account_type
import kpt.feature.home.generated.resources.feature_home_arrow_up
import kpt.feature.home.generated.resources.feature_home_autopay
import kpt.feature.home.generated.resources.feature_home_coin_image
import kpt.feature.home.generated.resources.feature_home_desc
import kpt.feature.home.generated.resources.feature_home_mark_default
import kpt.feature.home.generated.resources.feature_home_no_account
import kpt.feature.home.generated.resources.feature_home_pocket_desc
import kpt.feature.home.generated.resources.feature_home_pocket_title
import kpt.feature.home.generated.resources.feature_home_request
import kpt.feature.home.generated.resources.feature_home_request_money
import kpt.feature.home.generated.resources.feature_home_send
import kpt.feature.home.generated.resources.feature_home_send_money
import kpt.feature.home.generated.resources.feature_home_view_more
import kpt.feature.home.generated.resources.feature_home_wallet_balance
import kpt.feature.home.generated.resources.home_no_transactions_found
import kpt.feature.home.generated.resources.home_transaction_history
import kpt.feature.home.generated.resources.start_sending_your_money_tax_free
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/*
 * Feature Enhancement
 * Show all saving accounts as stacked card
 * Show transaction history of selected account
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onNavigateBack: () -> Unit,
    onRequest: (String) -> Unit,
    onPay: () -> Unit,
    onAutoPay: () -> Unit,
    navigateToPocketDashboard: () -> Unit,
    navigateToTransactionDetail: (Long, Long) -> Unit,
    navigateToAccountDetail: (Long) -> Unit,
    navigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.getAccounts()
    }

    val snackbarState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val homeUIState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullToRefreshState()

    EventsEffect(viewModel) { event ->
        when (event) {
            is HomeEvent.NavigateBack -> onNavigateBack()
            is HomeEvent.NavigateToRequestScreen -> onRequest(event.vpa)
            is HomeEvent.NavigateToSendScreen -> onPay()
            is HomeEvent.NavigateToAutoPayScreen -> onAutoPay.invoke()
            is HomeEvent.NavigateToPocketDashboard -> navigateToPocketDashboard.invoke()
            is HomeEvent.NavigateToClientDetailScreen -> {}
            is HomeEvent.NavigateToTransactionDetail -> {
                navigateToTransactionDetail(event.accountId, event.transactionId)
            }

            is HomeEvent.NavigateToTransactionScreen -> navigateToHistory()
            is HomeEvent.ShowToast -> {
                scope.launch {
                    snackbarState.showSnackbar(getString(event.message))
                }
            }

            is HomeEvent.NavigateToAccountDetail -> {
                navigateToAccountDetail(event.accountId)
            }
        }
    }

    HomeScreenDialog(
        dialogState = homeUIState.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(HomeAction.OnDismissDialog) }
        },
    )

    HomeScreenContent(
        viewState = homeUIState.viewState,
        defaultAccountId = homeUIState.defaultAccountId,
        snackbarHostState = snackbarState,
        isRefreshing = homeUIState.isRefreshing,
        pullRefreshState = pullRefreshState,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        uiState = homeUIState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeState,
    viewState: ViewState,
    defaultAccountId: Long?,
    snackbarHostState: SnackbarHostState,
    isRefreshing: Boolean = false,
    pullRefreshState: PullToRefreshState,
    modifier: Modifier = Modifier,
    onAction: (HomeAction) -> Unit,
) {
    MifosScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onAction(HomeAction.OnPullToRefresh) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            when (viewState) {
                is ViewState.Loading -> MifosProgressIndicator()

                is ViewState.Content -> {
                    HomeScreenContent(
                        transactions = uiState.transactions,
                        accounts = uiState.accounts,
                        defaultAccountId = defaultAccountId,
                        onAction = onAction,
                        modifier = Modifier,
                        showBottomSheet = uiState.showBottomSheet,
                        transactionType = uiState.transactionType,
                        selectedTransactionType = uiState.currentSelectedTransactionType,
                        currentSelectedAccount = uiState.currentSelectedAccount,
                        selectedAccount = uiState.selectedAccount,
                        transactionLoading = uiState.transactionsLoading,
                    )
                }

                is ViewState.Error -> {
                    ErrorScreenContent(
                        subTitle = viewState.message,
                        onClickRetry = {
                            onAction(HomeAction.OnRetryClicked)
                        },
                    )
                }

                ViewState.NoAccounts -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_home_no_account),
                        subTitle = "",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun HomeScreenContent(
    transactionLoading: Boolean,
    showBottomSheet: Boolean,
    currentSelectedAccount: Account?,
    selectedAccount: Account?,
    accounts: List<Account>,
    transactions: List<Transaction>?,
    defaultAccountId: Long?,
    selectedTransactionType: TransactionType,
    transactionType: TransactionType,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = calculateWindowSizeClass()
    val state = rememberLazyListState()

    val scrollbarState = state.scrollbarState(itemsAvailable = state.layoutInfo.totalItemsCount)
    val showScrollBar = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    Box(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
            state = state,
            contentPadding = PaddingValues(),
        ) {
            item {
                AccountList(
                    accounts = accounts,
                    defaultAccountId = defaultAccountId,
                    onClick = {
                        onAction(HomeAction.AccountDetailsClicked(it))
                    },
                    onMarkAsDefault = { accId, accNo ->
                        onAction(HomeAction.MarkAsDefault(accId, accNo))
                    },
                    onPageChanged = {
                        onAction(HomeAction.OnSelectedAccountChanged(accounts[it]))
                    },
                )
            }

            item {
                PayRequestScreen(
                    modifier = Modifier.padding(
                        vertical = KptTheme.spacing.md,
                        horizontal = KptTheme.spacing.md,
                    ),
                    onRequest = {
                        onAction(HomeAction.RequestClicked)
                    },
                    onSend = {
                        onAction(HomeAction.SendClicked)
                    },
                    onAutoPay = {
                        onAction(HomeAction.AutoPayClicked)
                    },
                    onPocketClick = {
                        onAction(HomeAction.PocketDashboardClicked)
                    },
                )
            }

            item {
                MifosSendMoneyFreeCard()
            }

            item {
                HomeTransactionHistoryCard(
                    modifier = Modifier.padding(
                        vertical = KptTheme.spacing.md,
                        horizontal = KptTheme.spacing.md,
                    ),
                    transactions = transactions,
                    selectedAccount = selectedAccount?.number ?: "",
                    onAction = onAction,
                    selectedTransactionType = transactionType,
                    transactionsLoading = transactionLoading,
                )
            }
        }

        if (showScrollBar) {
            state.DraggableScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = KptTheme.spacing.xs)
                    .align(Alignment.CenterEnd),
                state = scrollbarState,
                orientation = Orientation.Vertical,
                onThumbMoved = state.rememberDraggableScroller(
                    itemsAvailable = state.layoutInfo.totalItemsCount,
                ),
            )
        }

        if (showBottomSheet) {
            TransactionFilterBottomSheet(
                selectedAccount = currentSelectedAccount,
                accounts = accounts,
                selectedTransactionType = selectedTransactionType,
                onAccountSelected = {
                    onAction(HomeAction.OnFilterAccountSelected(it))
                },
                onTransactionTypeSelected = {
                    onAction(HomeAction.OnFilterTransactionTypeSelected(it))
                },
                onClearFilters = {
                    onAction(HomeAction.ClearFilters)
                },
                onApplyFilters = {
                    onAction(HomeAction.OnApplyFilterClick)
                },
                onDismiss = {
                    onAction(HomeAction.DismissBottomSheet)
                },
            )
        }
    }
}

@Composable
private fun AccountList(
    accounts: List<Account>,
    defaultAccountId: Long?,
    modifier: Modifier = Modifier,
    onMarkAsDefault: (Long, String) -> Unit,
    onClick: (Long) -> Unit,
    onPageChanged: (Int) -> Unit,
) {
    val pagerState = rememberPagerState { accounts.size }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                onPageChanged(page)
            }
    }

    HorizontalPager(
        state = pagerState,
        pageSpacing = KptTheme.spacing.xs,
        modifier = modifier,
        contentPadding = PaddingValues(start = KptTheme.spacing.md, end = KptTheme.spacing.md),
    ) {
        AccountCard(
            account = accounts[it],
            defaultAccountId = defaultAccountId,
            onMarkAsDefault = onMarkAsDefault,
            onClick = onClick,
        )
    }
}

@Composable
private fun AccountCard(
    account: Account,
    defaultAccountId: Long?,
    onMarkAsDefault: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (Long) -> Unit,
    gradientStartColor: Color = KptTheme.colorScheme.primary,
    gradientEndColor: Color = KptTheme.colorScheme.secondary,
) {
    val brush = remember {
        Brush.linearGradient(
            colors = listOf(gradientStartColor, gradientEndColor),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = brush,
                shape = KptTheme.shapes.large,
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onClick(account.id)
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.feature_home_account_type),
                        fontWeight = FontWeight(300),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.surface,
                    )

                    Text(
                        text = account.name,
                        fontWeight = FontWeight(400),
                        color = KptTheme.colorScheme.surface,
                    )
                }

                AnimatedContent(
                    targetState = account.id == defaultAccountId,
                ) {
                    if (it) {
                        MifosSmallChip(
                            label = "Default",
                            containerColor = KptTheme.colorScheme.primary,
                        )
                    } else {
                        CardDropdownBox(
                            onClickDefault = {
                                onMarkAsDefault(account.id, account.number)
                            },
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.number,
                    fontWeight = FontWeight.Bold,
                    color = KptTheme.colorScheme.surface,
                    style = KptTheme.typography.headlineMedium,
                    letterSpacing = 0.50.sp,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.feature_home_wallet_balance),
                        fontWeight = FontWeight(300),
                        style = KptTheme.typography.bodySmall,
                        color = KptTheme.colorScheme.surface,
                    )

                    val accountBalance = "${account.currency.code} ${account.currency.displaySymbol}${CurrencyFormatter.format(
                        balance = account.balance,
                        maximumFractionDigits = 2,
                    )}"

                    Text(
                        text = accountBalance,
                        color = KptTheme.colorScheme.surface,
                        style = KptTheme.typography.headlineLarge,
                    )
                }

                Icon(
                    modifier = Modifier
                        .graphicsLayer(rotationZ = 90f)
                        .padding(KptTheme.spacing.xs),
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = stringResource(Res.string.feature_home_arrow_up),
                    tint = KptTheme.colorScheme.surface,
                )
            }
        }
    }
}

@Composable
fun CardDropdownBox(
    onClickDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDropdown by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = {
                showDropdown = !showDropdown
            },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = KptTheme.colorScheme.surface,
            ),
        ) {
            Icon(
                imageVector = MifosIcons.MoreVert,
                contentDescription = stringResource(Res.string.feature_home_view_more),
            )
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.feature_home_mark_default)) },
                onClick = {
                    onClickDefault()
                    showDropdown = false
                },
            )
        }
    }
}

@Composable
private fun PayRequestScreen(
    onRequest: () -> Unit,
    onSend: () -> Unit,
    onAutoPay: () -> Unit,
    onPocketClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PaymentButton(
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                text = stringResource(Res.string.feature_home_request),
                onClick = onRequest,
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .size(26.dp),
                        imageVector = vectorResource(
                            Res.drawable.arrow_backward,
                        ),
                        contentDescription = stringResource(Res.string.feature_home_request_money),
                    )
                },
            )

            Spacer(modifier = Modifier.width(20.dp))

            PaymentButton(
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                text = stringResource(Res.string.feature_home_send),
                onClick = onSend,
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer(rotationZ = 180f),
                        imageVector = vectorResource(Res.drawable.arrow_backward),
                        contentDescription = stringResource(Res.string.feature_home_send_money),
                    )
                },
            )
        }

        PaymentButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            text = stringResource(Res.string.feature_home_autopay),
            onClick = onAutoPay,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(26.dp),
                    imageVector = MifosIcons.Schedule,
                    contentDescription = stringResource(Res.string.feature_home_autopay),
                )
            },
        )

        PocketNavigationCard(onClick = onPocketClick)
    }
}

@Composable
private fun PocketNavigationCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        shape = KptTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = MifosIcons.Pocket,
                contentDescription = null,
                tint = KptTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp)
                    .size(40.dp),
            )
            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(Res.string.feature_home_pocket_title),
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight(500),
                    color = KptTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(Res.string.feature_home_pocket_desc),
                    style = KptTheme.typography.bodySmall,
                    fontWeight = FontWeight(300),
                )
            }
        }
    }
}

@Composable
@Preview
private fun MifosSendMoneyFreeCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(horizontal = KptTheme.spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        start = KptTheme.spacing.lg,
                        end = KptTheme.spacing.md,
                        top = KptTheme.spacing.lg,
                        bottom = KptTheme.spacing.lg,
                    )
                    .weight(7.5f),
            ) {
                Text(
                    text = stringResource(Res.string.start_sending_your_money_tax_free),
                    color = KptTheme.colorScheme.primary,
                    fontWeight = FontWeight(500),
                    style = KptTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(Res.string.feature_home_desc),
                    style = KptTheme.typography.bodySmall,
                    fontWeight = FontWeight(300),
                )
            }

            Image(
                modifier = Modifier.weight(2.5f),
                contentScale = ContentScale.Fit,
                painter = painterResource(Res.drawable.coin_image),
                contentDescription = stringResource(Res.string.feature_home_coin_image),
            )
        }
    }
}

@Composable
private fun PaymentButton(
    text: String,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = KptTheme.colorScheme.surface,
            contentColor = KptTheme.colorScheme.onSurface,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            leadingIcon()
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontWeight = FontWeight(400),
            )
        }
    }
}

@Composable
private fun HomeScreenDialog(
    dialogState: HomeState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is HomeState.DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is HomeState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}

@Composable
private fun HomeTransactionHistoryCard(
    transactionsLoading: Boolean,
    selectedTransactionType: TransactionType,
    selectedAccount: String,
    transactions: List<Transaction>?,
    modifier: Modifier = Modifier,
    onAction: (HomeAction) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Row {
                        Text(
                            text = stringResource(Res.string.home_transaction_history),
                            style = KptTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Spacer(Modifier.width(KptTheme.spacing.xs))
                        Icon(
                            imageVector = MifosIcons.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).clickable {
                                onAction(HomeAction.OnClickSeeAllTransactions)
                            },
                        )
                    }
                    Text(
                        text = stringResource(Res.string.feature_home_account_number, selectedAccount),
                        style = KptTheme.typography.bodySmall,
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
                        onClick = {
                            onAction(
                                HomeAction.ShowBottomSheet,
                            )
                        },
                        modifier = Modifier.matchParentSize(),
                    ) {
                        Icon(
                            imageVector = MifosIcons.Filter,
                            contentDescription = null,
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
            }

            transactions?.forEachIndexed { i, transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { accountId, transactionId ->
                        onAction(HomeAction.TransactionClicked(accountId, transactionId))
                    },
                    showLeadingIcon = false,
                )

                if (i != transactions.size - 1) {
                    MifosDivider(
                        modifier = Modifier.padding(horizontal = KptTheme.spacing.sm),
                    )
                }
            }

            if (transactionsLoading) {
                MifosProgressIndicatorMini()
            } else {
                if (transactions != null && transactions.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.home_no_transactions_found),
                        style = KptTheme.typography.bodyMedium,
                        modifier = Modifier.padding(KptTheme.spacing.md),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenContentPreview() {
    val accounts = listOf(
        Account(
            name = "Account 1",
            number = "123456789",
            balance = 1000.0,
            id = 1L,
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
                nameCode = "USD",
                displayLabel = "US Dollar ($)",
            ),
            status = Status(
                id = 300,
                code = "status.active",
                value = "Active",
                submittedAndPendingApproval = false,
                approved = false,
                rejected = false,
                withdrawnByApplicant = false,
                active = true,
                closed = false,
                prematureClosed = false,
                transferInProgress = false,
                transferOnHold = false,
                matured = false,
            ),
        ),
        Account(
            name = "Account 1",
            number = "123456789",
            balance = 1000.0,
            id = 1L,
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
                nameCode = "USD",
                displayLabel = "US Dollar ($)",
            ),
            status = Status(
                id = 300,
                code = "status.active",
                value = "Active",
                submittedAndPendingApproval = false,
                approved = false,
                rejected = false,
                withdrawnByApplicant = false,
                active = true,
                closed = false,
                prematureClosed = false,
                transferInProgress = false,
                transferOnHold = false,
                matured = false,
            ),
        ),
    )
    val transactions = listOf(
        Transaction(
            accountId = 1L,
            amount = 100.0,
            date = "2023-01-01",
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
                nameCode = "USD",
                displayLabel = "US Dollar ($)",
            ),
            transactionType = TransactionType.CREDIT,
            transactionId = 101L,
            accountNo = "123456789",
            transferId = null,
            originalTransactionId = 101L,
            paymentDetailId = null,
            reversed = true,
        ),
        Transaction(
            accountId = 2L,
            amount = 100.0,
            date = "2023-01-01",
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
                nameCode = "USD",
                displayLabel = "US Dollar ($)",
            ),
            transactionType = TransactionType.DEBIT,
            transactionId = 101L,
            accountNo = "123456789",
            transferId = null,
            originalTransactionId = 101L,
            paymentDetailId = null,
            reversed = false,
        ),
        Transaction(
            accountId = 3L,
            amount = 100.0,
            date = "2023-01-01",
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
                nameCode = "USD",
                displayLabel = "US Dollar ($)",
            ),
            transactionType = TransactionType.CREDIT,
            transactionId = 101L,
            accountNo = "123456789",
            transferId = null,
            originalTransactionId = 101L,
            paymentDetailId = null,
            reversed = true,
        ),
        Transaction(
            accountId = 4L,
            amount = 100.0,
            date = "2023-01-01",
            currency = Currency(
                code = "USD",
                name = "US Dollar",
                decimalPlaces = 2,
                displaySymbol = "$",
                nameCode = "USD",
                displayLabel = "US Dollar ($)",
            ),
            transactionType = TransactionType.DEBIT,
            transactionId = 101L,
            accountNo = "123456789",
            transferId = null,
            originalTransactionId = 101L,
            paymentDetailId = null,
            reversed = false,
        ),
    )
    MaterialTheme {
        HomeScreenContent(
            defaultAccountId = 1L,
            onAction = {},
            transactions = transactions,
            accounts = accounts,
            showBottomSheet = false,
            selectedTransactionType = TransactionType.OTHER,
            currentSelectedAccount = null,
            transactionType = TransactionType.CREDIT,
            selectedAccount = null,
            transactionLoading = false,
        )
    }
}
