/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pocket.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.freshness.FreshnessIndicator
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosAlertDialog
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTabPager
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.model.enums.AccountType
import kpt.core.model.pocket.LinkableAccount
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.utils.EventsEffect
import kpt.feature.pocket.generated.resources.Res
import kpt.feature.pocket.generated.resources.feature_pocket_action_cancel
import kpt.feature.pocket.generated.resources.feature_pocket_action_link
import kpt.feature.pocket.generated.resources.feature_pocket_action_ok
import kpt.feature.pocket.generated.resources.feature_pocket_action_remove
import kpt.feature.pocket.generated.resources.feature_pocket_close
import kpt.feature.pocket.generated.resources.feature_pocket_delink_account_message
import kpt.feature.pocket.generated.resources.feature_pocket_dialog_error_title
import kpt.feature.pocket.generated.resources.feature_pocket_empty_action
import kpt.feature.pocket.generated.resources.feature_pocket_empty_title
import kpt.feature.pocket.generated.resources.feature_pocket_link_accounts_title
import kpt.feature.pocket.generated.resources.feature_pocket_link_more_accounts
import kpt.feature.pocket.generated.resources.feature_pocket_link_selected
import kpt.feature.pocket.generated.resources.feature_pocket_linked_accounts
import kpt.feature.pocket.generated.resources.feature_pocket_manage_title
import kpt.feature.pocket.generated.resources.feature_pocket_no_available_accounts
import kpt.feature.pocket.generated.resources.feature_pocket_no_linked_accounts
import kpt.feature.pocket.generated.resources.feature_pocket_remove_account_detail
import kpt.feature.pocket.generated.resources.feature_pocket_remove_account_title
import kpt.feature.pocket.generated.resources.feature_pocket_search_accounts_hint
import kpt.feature.pocket.generated.resources.feature_pocket_unknown_account
import kpt.feature.pocket.generated.resources.feature_pocket_you_are_removing
import kpt.feature.pocket.viewmodels.ManagePocketAccount
import kpt.feature.pocket.viewmodels.ManagePocketAction
import kpt.feature.pocket.viewmodels.ManagePocketDialogState
import kpt.feature.pocket.viewmodels.ManagePocketEvent
import kpt.feature.pocket.viewmodels.ManagePocketState
import kpt.feature.pocket.viewmodels.ManagePocketViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/**
 * Screen for managing (linking and delinking) pockets.
 *
 * **Architecture Decisions:**
 * - **Empty States for Available Accounts**: When `availableUiState` is `ScreenState.Error` or
 *   `NoNetwork`, the UI explicitly renders a retry-able failure state. Failing to explicitly
 *   display a network error would falsely tell the user "You have no more accounts to link" when in
 *   reality their internet just dropped.
 *
 * **Component Structure:**
 * - `SelectablePocketAccountCard`: A reusable, interactive card that receives `isSelected` state
 *   and toggles its background color/checkbox. It intentionally holds no business logic.
 * - `LinkAccountsSheet`: A Modal Bottom Sheet containing a `ScreenContent` block. It completely
 *   isolates the "Available to Link" flow from the "Currently Linked" main screen, preventing
 *   visual overlap and scroll conflicts.
 */
@Composable
internal fun ManagePocketScreen(
    navigateBack: () -> Unit,
    viewModel: ManagePocketViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val mappedLinkedAccounts by viewModel.mappedLinkedAccounts.collectAsStateWithLifecycle()
    val linkedFreshness by viewModel.linkedFreshness.collectAsStateWithLifecycle()
    val availableUiState by viewModel.availableUiState.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            ManagePocketEvent.NavigateBack -> navigateBack.invoke()
        }
    }

    ManagePocketContent(
        linkedUiState = mappedLinkedAccounts,
        linkedFreshness = linkedFreshness,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )

    ManagePocketDialogs(
        state = state,
        availableUiState = availableUiState,
        searchResults = searchResults,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
internal fun ManagePocketContent(
    linkedUiState: ScreenState<List<ManagePocketAccount>>,
    linkedFreshness: FreshnessSignal,
    onAction: (ManagePocketAction) -> Unit,
) {
    MifosScaffold(
        backPress = { onAction(ManagePocketAction.NavigateBack) },
        topBarTitle = stringResource(Res.string.feature_pocket_manage_title),
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ScreenContent(
                state = linkedUiState,
                onRetry = { onAction(ManagePocketAction.Retry) },
                empty = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(KptTheme.spacing.md),
                    ) {
                        LinkMoreAccountsCard(
                            onLinkClick = { onAction(ManagePocketAction.OpenLinkAccounts) },
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = KptTheme.spacing.xl),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_pocket_no_linked_accounts),
                                style = KptTheme.typography.bodyMedium,
                                color = KptTheme.colorScheme.secondary,
                            )
                        }
                    }
                },
            ) { mappedLinkedAccounts, _ ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .let {
                            if (mappedLinkedAccounts.isNotEmpty()) {
                                it.verticalScroll(rememberScrollState())
                            } else {
                                it
                            }
                        }
                        .padding(KptTheme.spacing.md),
                ) {
                    LinkMoreAccountsCard(
                        onLinkClick = { onAction(ManagePocketAction.OpenLinkAccounts) },
                    )

                    if (mappedLinkedAccounts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = KptTheme.spacing.xl),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_pocket_no_linked_accounts),
                                style = KptTheme.typography.bodyMedium,
                                color = KptTheme.colorScheme.secondary,
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_pocket_linked_accounts),
                                style = KptTheme.typography.titleSmall,
                                color = KptTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(KptTheme.spacing.xs))
                            FreshnessIndicator(
                                signal = linkedFreshness,
                                onRefresh = { onAction(ManagePocketAction.Retry) },
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                        ) {
                            mappedLinkedAccounts.forEach { account ->
                                val displayName = account.name
                                    ?: stringResource(Res.string.feature_pocket_unknown_account)
                                LinkedPocketAccountCard(
                                    account = account,
                                    onRemoveClick = {
                                        onAction(
                                            ManagePocketAction.OpenDelinkConfirmation(
                                                accountId = account.mappingId,
                                                accountName = displayName,
                                                accountNumber = account.accountNumber,
                                            ),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManagePocketDialogs(
    state: ManagePocketState,
    availableUiState: ScreenState<List<LinkableAccount>>,
    searchResults: List<LinkableAccount>,
    onAction: (ManagePocketAction) -> Unit,
) {
    when (val dialogState = state.dialogState) {
        ManagePocketDialogState.LinkAccounts -> {
            MifosBottomSheet(
                onDismiss = { onAction(ManagePocketAction.DismissDialog) },
            ) {
                LinkAccountsSheet(
                    state = state,
                    availableUiState = availableUiState,
                    searchResults = searchResults,
                    onAction = onAction,
                )
            }
        }

        is ManagePocketDialogState.DelinkConfirmation -> {
            MifosBottomSheet(
                onDismiss = { onAction(ManagePocketAction.DismissDialog) },
            ) {
                RemoveLinkedAccountSheet(
                    accountName = dialogState.accountName,
                    accountNumber = dialogState.accountNumber,
                    onCancelClick = { onAction(ManagePocketAction.DismissDialog) },
                    onRemoveClick = {
                        onAction(ManagePocketAction.DelinkAccount(dialogState.accountId))
                    },
                )
            }
        }

        is ManagePocketDialogState.Error -> {
            MifosAlertDialog(
                title = stringResource(Res.string.feature_pocket_dialog_error_title),
                message = stringResource(dialogState.message),
                icon = MifosIcons.Error,
                confirmText = stringResource(Res.string.feature_pocket_action_ok),
                dismissText = stringResource(Res.string.feature_pocket_action_cancel),
                onConfirm = { onAction(ManagePocketAction.DismissDialog) },
                onDismiss = { onAction(ManagePocketAction.DismissDialog) },
            )
        }

        ManagePocketDialogState.Loading -> {
            MifosLoadingDialog(visibilityState = LoadingDialogState.Shown)
        }

        null -> Unit
    }
}

@Composable
private fun LinkMoreAccountsCard(
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onLinkClick,
        shape = KptTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            contentColor = KptTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = KptTheme.elevation.level0),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_pocket_link_more_accounts),
                modifier = Modifier.weight(1f),
                style = KptTheme.typography.titleMedium,
                color = KptTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            MifosButton(
                onClick = onLinkClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.primary,
                    contentColor = KptTheme.colorScheme.onPrimary,
                ),
                content = {
                    Text(
                        text = stringResource(Res.string.feature_pocket_action_link),
                        style = KptTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun LinkedPocketAccountCard(
    account: ManagePocketAccount,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PocketAccountRow(
        account = account,
        modifier = modifier,
        trailingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = KptTheme.colorScheme.errorContainer.copy(alpha = 0.62f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onRemoveClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = MifosIcons.Delete,
                        contentDescription = stringResource(Res.string.feature_pocket_action_remove),
                        tint = KptTheme.colorScheme.error,
                        modifier = Modifier.size(KptTheme.spacing.lg),
                    )
                }
            }
        },
    )
}

@Composable
private fun PocketAccountRow(
    account: ManagePocketAccount,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PocketAccountIcon(icon = account.accountType.toIcon())

        Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.accountNumber,
                style = KptTheme.typography.titleSmall,
                color = KptTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.name ?: stringResource(Res.string.feature_pocket_unknown_account),
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

        trailingContent()
    }
}

@Composable
internal fun LinkAccountsSheet(
    state: ManagePocketState,
    availableUiState: ScreenState<List<LinkableAccount>>,
    searchResults: List<LinkableAccount>,
    onAction: (ManagePocketAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(AccountType.SAVINGS, AccountType.LOAN, AccountType.SHARE)
    val selectedTabIndex = tabs.indexOf(state.selectedTab).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { tabs.size },
    )
    var searchValue by remember {
        mutableStateOf(TextFieldValue(state.searchQuery))
    }

    LaunchedEffect(state.searchQuery) {
        if (state.searchQuery != searchValue.text) {
            searchValue = searchValue.copy(text = state.searchQuery)
        }
    }

    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 520.dp, max = 720.dp)
            .padding(horizontal = KptTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.feature_pocket_link_accounts_title),
            style = KptTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = KptTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = KptTheme.spacing.md),
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.md))

        ManagePocketSearchTextField(
            value = searchValue,
            onValueChange = {
                searchValue = it
                onAction(ManagePocketAction.SearchQueryChanged(it.text))
            },
            onSearchDismiss = {
                searchValue = TextFieldValue("")
                onAction(ManagePocketAction.SearchQueryChanged(""))
            },
            hint = stringResource(
                Res.string.feature_pocket_search_accounts_hint,
                tabs[selectedTabIndex].name.lowercase(),
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

        val allAccounts = (availableUiState as? ScreenState.Content)?.data ?: emptyList()
        val isLoading = availableUiState is ScreenState.Loading ||
            (availableUiState as? ScreenState.Content)?.freshnessSignal?.isRefreshing == true

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            MifosTabPager(
                modifier = Modifier.fillMaxSize(),
                pagerState = pagerState,
                currentPage = selectedTabIndex,
                tabs = tabs.map { it.name },
                setCurrentPage = { page ->
                    onAction(ManagePocketAction.TabSelected(tabs[page]))
                },
            ) { page ->
                if (isLoading && allAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        MifosProgressIndicator()
                    }
                } else {
                    val accounts = allAccounts.filter { it.accountType == tabs[page] }

                    if (availableUiState is ScreenState.Error || availableUiState is ScreenState.NoNetwork) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_pocket_empty_title),
                                style = KptTheme.typography.bodyMedium,
                                color = KptTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.height(KptTheme.spacing.sm))
                            MifosButton(
                                onClick = { onAction(ManagePocketAction.RetryAvailable) },
                            ) {
                                Text(stringResource(Res.string.feature_pocket_empty_action))
                            }
                        }
                    } else if (accounts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(Res.string.feature_pocket_no_available_accounts),
                                style = KptTheme.typography.bodyMedium,
                                color = KptTheme.colorScheme.secondary,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                        ) {
                            items(
                                items = accounts,
                                key = { "${it.accountId}_${it.accountType.name}" },
                            ) { account ->
                                SelectablePocketAccountCard(
                                    account = account,
                                    selected = "${account.accountId}_${account.accountType.name}" in state.selectedAccountIdentifiers,
                                    onSelectedChange = { selected ->
                                        onAction(
                                            ManagePocketAction.AccountSelectionChanged(
                                                accountId = account.accountId,
                                                accountType = account.accountType,
                                                selected = selected,
                                            ),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (state.searchQuery.isNotBlank() && allAccounts.isNotEmpty()) {
                LinkAccountsSearchResults(
                    searchResults = searchResults,
                    state = state,
                    onAction = onAction,
                )
            }
        }

        HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)

        LinkSelectedAccountsButton(
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun LinkAccountsSearchResults(
    searchResults: List<LinkableAccount>,
    state: ManagePocketState,
    onAction: (ManagePocketAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KptTheme.colorScheme.surface.copy(alpha = 0.95f)),
    ) {
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.feature_pocket_no_available_accounts),
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.secondary,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                item { Spacer(modifier = Modifier.height(KptTheme.spacing.xs)) }
                items(
                    items = searchResults,
                    key = { "${it.accountId}_${it.accountType.name}" },
                ) { account ->
                    SelectablePocketAccountCard(
                        account = account,
                        selected = "${account.accountId}_${account.accountType.name}" in state.selectedAccountIdentifiers,
                        onSelectedChange = { selected ->
                            onAction(
                                ManagePocketAction.AccountSelectionChanged(
                                    accountId = account.accountId,
                                    accountType = account.accountType,
                                    selected = selected,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkSelectedAccountsButton(
    state: ManagePocketState,
    onAction: (ManagePocketAction) -> Unit,
) {
    MifosButton(
        onClick = {
            onAction(ManagePocketAction.LinkSelectedAccounts)
        },
        enabled = state.selectedAccountIdentifiers.isNotEmpty(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KptTheme.spacing.md),
        content = {
            Text(
                text = stringResource(
                    Res.string.feature_pocket_link_selected,
                    state.selectedAccountIdentifiers.size,
                ),
                style = KptTheme.typography.labelLarge,
            )
        },
    )
}

@Composable
private fun SelectablePocketAccountCard(
    account: LinkableAccount,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val unknownAccount = stringResource(Res.string.feature_pocket_unknown_account)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectedChange(!selected) }
            .padding(vertical = KptTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = onSelectedChange,
        )

        Spacer(modifier = Modifier.width(KptTheme.spacing.xs))

        PocketAccountIcon(icon = account.accountType.toIcon())

        Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.accountNumber ?: "",
                style = KptTheme.typography.titleSmall,
                color = KptTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.productName ?: unknownAccount,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PocketAccountIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = KptTheme.colorScheme.onBackground.copy(alpha = 0.3f),
        modifier = modifier
            .background(
                color = KptTheme.colorScheme.background.copy(alpha = 0.5f),
                shape = CircleShape,
            )
            .padding(KptTheme.spacing.sm),
    )
}

private fun AccountType.toIcon(): ImageVector =
    when (this) {
        AccountType.SAVINGS -> MifosIcons.PersonAccounts
        AccountType.LOAN -> MifosIcons.CoinMultiple
        AccountType.SHARE -> MifosIcons.CoinMultiple
    }

@Composable
private fun ManagePocketSearchTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSearchDismiss: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    TextField(
        modifier = modifier,
        value = value,
        leadingIcon = {
            Icon(
                imageVector = MifosIcons.Search,
                contentDescription = null,
            )
        },
        placeholder = {
            Text(
                text = hint,
                style = KptTheme.typography.bodyLarge,
            )
        },
        onValueChange = onValueChange,
        textStyle = KptTheme.typography.bodyLarge,
        trailingIcon = {
            AnimatedVisibility(visible = value.text.isNotEmpty()) {
                IconButton(onClick = onSearchDismiss) {
                    Icon(
                        imageVector = MifosIcons.Close,
                        contentDescription = stringResource(Res.string.feature_pocket_close),
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors().copy(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.LightGray,
            unfocusedIndicatorColor = Color.LightGray,
            focusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
            unfocusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            },
        ),
        singleLine = true,
    )
}

@Composable
internal fun RemoveLinkedAccountSheet(
    accountName: String,
    accountNumber: String,
    onCancelClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            Icon(
                imageVector = MifosIcons.Warning,
                contentDescription = null,
                tint = KptTheme.colorScheme.error,
            )
            Text(
                text = stringResource(Res.string.feature_pocket_remove_account_title),
                style = KptTheme.typography.headlineSmall,
                color = KptTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = {},
            enabled = false,
            shape = KptTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = KptTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                contentColor = KptTheme.colorScheme.onSurface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
            ) {
                Text(
                    text = stringResource(Res.string.feature_pocket_you_are_removing),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.secondary,
                )
                Text(
                    text = accountName,
                    style = KptTheme.typography.titleMedium,
                    color = KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        Res.string.feature_pocket_remove_account_detail,
                        accountNumber,
                    ),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.secondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

        Text(
            text = stringResource(Res.string.feature_pocket_delink_account_message),
            style = KptTheme.typography.bodyLarge,
            color = KptTheme.colorScheme.secondary,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MifosButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.surface,
                    contentColor = KptTheme.colorScheme.primary,
                ),
                content = {
                    Text(
                        text = stringResource(Res.string.feature_pocket_action_cancel),
                        style = KptTheme.typography.labelLarge,
                    )
                },
            )

            MifosButton(
                onClick = onRemoveClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.error,
                    contentColor = KptTheme.colorScheme.onError,
                ),
                content = {
                    Text(
                        text = stringResource(Res.string.feature_pocket_action_remove),
                        style = KptTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun ManagePocketContentPreview() {
    MifosTheme {
        ManagePocketContent(
            linkedUiState = ScreenState.Content(emptyList()),
            linkedFreshness = FreshnessSignal.initial(),
            onAction = {},
        )
    }
}

internal data class ManagePocketAccount(
    val accountId: Long,
    val mappingId: Long,
    val name: String,
    val accountNumber: String,
    val accountType: AccountType,
)

@Preview
@Composable
private fun LinkAccountsSheetContentPreview() {
    MifosTheme {
        LinkAccountsSheet(
            state = ManagePocketState(),
            availableUiState = ScreenState.Content(emptyList()),
            searchResults = emptyList(),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun RemoveLinkedAccountSheetContentPreview() {
    MifosTheme {
        RemoveLinkedAccountSheet(
            accountName = "Emergency Fund",
            accountNumber = "1004859238",
            onCancelClick = {},
            onRemoveClick = {},
        )
    }
}
