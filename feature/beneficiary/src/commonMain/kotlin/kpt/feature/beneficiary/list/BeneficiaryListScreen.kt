/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.beneficiary.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.model.beneficiary.Beneficiary
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosProgressIndicatorOverlay
import kpt.core.ui.utils.EventsEffect
import kpt.feature.beneficiary.addupdatebeneficiary.BeneficiaryAddEditType
import kpt.feature.beneficiary.deletebeneficiary.DeleteBeneficiaryBottomSheet
import kpt.feature.beneficiary.deletebeneficiary.DeleteBeneficiaryState
import kpt.feature.beneficiary.deletebeneficiary.DeleteBeneficiaryViewModel
import mifos_pay.feature.beneficiary.generated.resources.Res
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_add
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_add_beneficiary
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_add_beneficiary_hint
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_delete_beneficiary
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_edit_beneficiary
import mifos_pay.feature.beneficiary.generated.resources.feature_beneficiary_no_beneficiaries
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun BeneficiaryListScreen(
    onAddOrEditBeneficiary: (BeneficiaryAddEditType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BeneficiaryListViewModel = koinViewModel(),
    deleteViewModel: DeleteBeneficiaryViewModel = koinViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val deleteState by deleteViewModel.deleteState.collectAsStateWithLifecycle()

    // Observe delete success and trigger list refresh
    LaunchedEffect(deleteState.deleteSuccessful) {
        if (deleteState.deleteSuccessful) {
            viewModel.refreshBeneficiaryList()
            deleteViewModel.consumeDeleteSuccess()
        }
    }

    EventsEffect(viewModel) { event ->
        when (event) {
            is BeneficiaryListEvent.OnAddOrEditTPTBeneficiary -> onAddOrEditBeneficiary.invoke(event.type)

            is BeneficiaryListEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }
        }
    }

    // Delete beneficiary bottom sheet - managed by separate ViewModel
    DeleteBeneficiaryBottomSheet(
        dialogState = deleteState.dialogState,
        onConfirmDelete = remember(deleteViewModel) {
            { beneficiaryId -> deleteViewModel.confirmDelete(beneficiaryId) }
        },
        onDismiss = remember(deleteViewModel) {
            { deleteViewModel.dismissDialog() }
        },
    )

    Box(modifier = modifier.fillMaxSize()) {
        BeneficiaryListScreenContent(
            state = listState,
            onRetry = remember(viewModel) {
                { viewModel.retry() }
            },
            onAction = remember(viewModel) {
                { viewModel.trySendAction(it) }
            },
            onDeleteBeneficiary = remember(deleteViewModel) {
                { id, name -> deleteViewModel.showDeleteConfirmation(id, name) }
            },
            snackbarHostState = snackbarHostState,
        )

        // Show overlay progress indicator over the list when deleting
        if (deleteState.dialogState is DeleteBeneficiaryState.DialogState.Deleting) {
            MifosProgressIndicatorOverlay()
        }
    }
}

@Composable
private fun BeneficiaryListScreenContent(
    state: ScreenState<List<Beneficiary>>,
    onRetry: () -> Unit,
    onAction: (BeneficiaryListAction) -> Unit,
    onDeleteBeneficiary: (Long, String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    MifosScaffold(
        modifier = modifier,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            // FAB only when the list is actually rendered (Content) — mirrors the
            // pre-migration `state.hasFab` gate that hid the FAB in Loading/Error.
            AnimatedVisibility(
                visible = state is ScreenState.Content,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onAction(BeneficiaryListAction.AddTPTBeneficiary)
                    },
                    icon = {
                        Icon(
                            imageVector = MifosIcons.Add,
                            contentDescription = stringResource(Res.string.feature_beneficiary_add),
                        )
                    },
                    text = {
                        Text(text = stringResource(Res.string.feature_beneficiary_add_beneficiary))
                    },
                )
            }
        },
    ) { paddingValues ->
        // Template idiom: `ScreenContent` (core-base/ui) owns every render branch —
        // loading / empty / no-network / unauthenticated / error+retry — driven by
        // the stream's pre-decided `ScreenState`. Only the Content list body is
        // authored here; the empty state keeps the feature's existing copy.
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            empty = {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_beneficiary_no_beneficiaries),
                    subTitle = stringResource(Res.string.feature_beneficiary_add_beneficiary_hint),
                    modifier = Modifier.fillMaxSize(),
                )
            },
        ) { beneficiaries, _ ->
            BeneficiariesList(
                beneficiaries = beneficiaries,
                onAction = onAction,
                onDeleteBeneficiary = onDeleteBeneficiary,
            )
        }
    }
}

@Composable
fun BeneficiariesList(
    beneficiaries: List<Beneficiary>,
    onAction: (BeneficiaryListAction) -> Unit,
    onDeleteBeneficiary: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        items(
            items = beneficiaries,
            key = { it.id },
        ) { beneficiary ->
            BeneficiaryItem(
                beneficiary = beneficiary,
                onClickEdit = { onAction(BeneficiaryListAction.EditBeneficiary(it)) },
                onClickDelete = onDeleteBeneficiary,
            )
        }
    }
}

@Composable
fun BeneficiaryItem(
    beneficiary: Beneficiary,
    modifier: Modifier = Modifier,
    onClickEdit: (Beneficiary) -> Unit,
    onClickDelete: (Long, String) -> Unit,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.small,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
            contentColor = KptTheme.colorScheme.onSurface,
        ),
    ) {
        ListItem(
            headlineContent = {
                Text(text = beneficiary.name)
            },
            supportingContent = {
                Text(text = beneficiary.accountNumber)
            },
            leadingContent = {
                AvatarBox(
                    icon = MifosIcons.AccountCircle,
                    backgroundColor = KptTheme.colorScheme.tertiaryContainer,
                    contentColor = KptTheme.colorScheme.tertiary,
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            onClickEdit(beneficiary)
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = KptTheme.colorScheme.surfaceContainerHighest,
                            contentColor = KptTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            imageVector = MifosIcons.Edit2,
                            contentDescription = stringResource(Res.string.feature_beneficiary_edit_beneficiary),
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            onClickDelete(beneficiary.id, beneficiary.name)
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = KptTheme.colorScheme.errorContainer,
                            contentColor = KptTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(
                            imageVector = MifosIcons.OutlinedDelete,
                            contentDescription = stringResource(Res.string.feature_beneficiary_delete_beneficiary),
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Preview
@Composable
fun PreviewBeneficiaryListScreen() {
    MifosTheme {
        BeneficiaryListScreenContent(
            state = ScreenState.Content(sampleBeneficiaryList),
            onRetry = { },
            onAction = { },
            onDeleteBeneficiary = { _, _ -> },
            snackbarHostState = remember { SnackbarHostState() },
            modifier = Modifier,
        )
    }
}

internal val sampleBeneficiaryList: List<Beneficiary> = List(6) {
    Beneficiary(
        id = it.toLong(),
        name = "Beneficiary $it",
        officeName = "Head Office",
        clientName = "Client $it",
        accountType = Beneficiary.AccountType(
            id = 1,
            code = "savings",
            value = "Savings",
        ),
        accountNumber = "00000000$it",
        transferLimit = 1000,
    )
}
