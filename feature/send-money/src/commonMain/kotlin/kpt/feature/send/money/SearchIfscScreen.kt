/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosOutlinedButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.designsystem.component.MifosTopBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.utils.EventsEffect
import kpt.feature.send.money.generated.resources.Res
import kpt.feature.send.money.generated.resources.feature_send_money_bank_branch
import kpt.feature.send.money.generated.resources.feature_send_money_bank_name
import kpt.feature.send.money.generated.resources.feature_send_money_cancel
import kpt.feature.send.money.generated.resources.feature_send_money_continue
import kpt.feature.send.money.generated.resources.feature_send_money_ifsc_code
import kpt.feature.send.money.generated.resources.feature_send_money_search_ifsc
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
// TODO replace dummy data with actual data or call API
// TODO fix bank name input box visibility
/** Screen for searching a bank branch by IFSC code, bank, state, district and branch. */
@Composable
fun SearchIfscScreen(
    onBackClick: () -> Unit,
    onIfscSelected: (IfscCode) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchIfscViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val selectedBank = state.selectedBank
    val selectedBranch = state.selectedBranch
    val filteredBranches = state.filteredBranches
    val bankFocusRequester = remember { FocusRequester() }
    val branchFocusRequester = remember { FocusRequester() }

    EventsEffect(viewModel) { event ->
        when (event) {
            SearchIfscEvent.NavigateBack -> {
                onBackClick.invoke()
            }

            is SearchIfscEvent.IfscSelected -> {
                onIfscSelected(event.ifscCode)
                onBackClick.invoke()
            }
        }
    }

    LaunchedEffect(Unit) {
        bankFocusRequester.requestFocus()
    }

    MifosGradientBackground {
        MifosScaffold(
            modifier = modifier,
            topBar = {
                MifosTopBar(
                    topBarTitle = stringResource(Res.string.feature_send_money_search_ifsc),
                    backPress = {
                        viewModel.trySendAction(SearchIfscAction.NavigateBack)
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = KptTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                ) {
                    MifosTextField(
                        value = selectedBank?.name ?: state.bankName,
                        onValueChange = { bankName ->
                            if (selectedBank == null) {
                                viewModel.trySendAction(SearchIfscAction.UpdateBankName(bankName))
                            }
                        },
                        label = stringResource(Res.string.feature_send_money_bank_name),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(bankFocusRequester)
                            .clickable {
                                if (selectedBank != null) {
                                    viewModel.trySendAction(SearchIfscAction.ClearBankSelection)
                                }
                            },
                        enabled = true,
                        readOnly = selectedBank != null,
                        leadingIcon = selectedBank?.let {
                            {
                                Icon(
                                    imageVector = MifosIcons.Bank,
                                    contentDescription = "Bank Logo",
                                    tint = KptTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClickClearIcon = {
                            if (selectedBank != null) {
                                viewModel.trySendAction(SearchIfscAction.ClearBankSelection)
                            } else {
                                viewModel.trySendAction(SearchIfscAction.UpdateBankName(""))
                            }
                        },
                    )

                    if (selectedBank != null) {
                        MifosTextField(
                            value = selectedBranch?.name ?: state.bankBranch,
                            onValueChange = { bankBranch ->
                                if (selectedBranch == null) {
                                    viewModel.trySendAction(
                                        SearchIfscAction.UpdateBankBranch(
                                            bankBranch,
                                        ),
                                    )
                                }
                            },
                            label = stringResource(Res.string.feature_send_money_bank_branch),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(branchFocusRequester)
                                .clickable {
                                    if (selectedBranch != null) {
                                        viewModel.trySendAction(SearchIfscAction.ClearBranchSelection)
                                    }
                                },
                            enabled = true,
                            readOnly = selectedBranch != null,
                            onClickClearIcon = {
                                if (selectedBranch != null) {
                                    viewModel.trySendAction(SearchIfscAction.ClearBranchSelection)
                                } else {
                                    viewModel.trySendAction(SearchIfscAction.UpdateBankBranch(""))
                                }
                            },
                        )

                        if (state.selectedIfscCode != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = KptTheme.spacing.sm),
                            ) {
                                Text(
                                    text = stringResource(Res.string.feature_send_money_ifsc_code),
                                    style = KptTheme.typography.labelMedium,
                                    color = KptTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = state.selectedIfscCode!!,
                                    style = KptTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = KptTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = KptTheme.spacing.xs),
                                )
                            }
                        }
                    }

                    if (selectedBank != null && selectedBranch == null && filteredBranches.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
                            color = KptTheme.colorScheme.outline.copy(alpha = 0.2f),
                        )

                        BranchList(
                            branches = filteredBranches,
                            onBranchSelected = { branch ->
                                viewModel.trySendAction(SearchIfscAction.SelectBranch(branch))
                            },
                        )
                    }
                }

                if (selectedBank == null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
                        color = KptTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )

                    BankList(
                        banks = dummyBanks.filter { bank ->
                            bank.name.contains(state.bankName, ignoreCase = true)
                        },
                        onBankSelected = { bank ->
                            viewModel.trySendAction(SearchIfscAction.SelectBank(bank))
                        },
                    )
                }

                if (state.selectedIfscCode != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KptTheme.spacing.lg)
                            .padding(bottom = KptTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
                    ) {
                        MifosOutlinedButton(
                            text = { Text(stringResource(Res.string.feature_send_money_cancel)) },
                            onClick = {
                                viewModel.trySendAction(SearchIfscAction.NavigateBack)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        MifosButton(
                            text = { Text(stringResource(Res.string.feature_send_money_continue)) },
                            onClick = {
                                val ifscCode = IfscCode(
                                    code = state.selectedIfscCode!!,
                                    bankName = selectedBank?.name ?: "",
                                    branch = selectedBranch?.name ?: "",
                                    address = "",
                                    city = selectedBranch?.state ?: "",
                                    state = selectedBranch?.state ?: "",
                                )
                                viewModel.trySendAction(SearchIfscAction.SelectIfscCode(ifscCode))
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BankList(
    banks: List<DummyBank>,
    onBankSelected: (DummyBank) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = KptTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        items(
            items = banks,
            key = { it.name },
        ) { bank ->
            BankListItem(
                bank = bank,
                onClick = { onBankSelected(bank) },
            )
        }
    }
}

@Composable
private fun BankListItem(
    bank: DummyBank,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = KptTheme.shapes.small,
        color = KptTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = KptTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MifosIcons.Bank,
                    contentDescription = "Bank Logo",
                    tint = KptTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = bank.name,
                style = KptTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BranchList(
    branches: List<DummyBankBranch>,
    onBranchSelected: (DummyBankBranch) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = KptTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        items(
            items = branches,
            key = { "${it.name}_${it.state}" },
        ) { branch ->
            BranchListItem(
                branch = branch,
                onClick = { onBranchSelected(branch) },
            )
        }
    }
}

@Composable
private fun BranchListItem(
    branch: DummyBankBranch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = KptTheme.shapes.small,
        color = KptTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.md),
        ) {
            Text(
                text = branch.name,
                style = KptTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
            )

            Text(
                text = branch.state,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = KptTheme.spacing.xs),
            )
        }
    }
}

private val dummyBanks = listOf(
    DummyBank("State Bank of India"),
    DummyBank("HDFC Bank"),
    DummyBank("ICICI Bank"),
    DummyBank("Punjab National Bank"),
    DummyBank("Bank of Baroda"),
    DummyBank("Canara Bank"),
    DummyBank("Union Bank of India"),
    DummyBank("Axis Bank"),
    DummyBank("Kotak Mahindra Bank"),
    DummyBank("Yes Bank"),
)
