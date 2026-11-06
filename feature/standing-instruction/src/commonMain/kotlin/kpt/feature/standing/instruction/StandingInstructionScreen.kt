/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.toRoundedCornerShape
import kpt.core.model.standinginstruction.StandingInstruction
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.RevealDirection
import kpt.core.ui.RevealSwipe
import kpt.core.ui.rememberRevealState
import kpt.core.ui.utils.EventsEffect
import kpt.feature.standing.instruction.components.FrequencyChip
import kpt.feature.standing.instruction.createOrUpdate.SIAddEditType
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
fun StandingInstructionsScreen(
    onAddEditSI: (SIAddEditType) -> Unit,
    onShowSIDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StandingInstructionViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is SIEvent.OnAddEditSI -> onAddEditSI.invoke(event.type)
            is SIEvent.OnNavigateToSIDetails -> onShowSIDetails.invoke(event.siId)
            is SIEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }
        }
    }

    SIDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(SIAction.DismissDialog) }
        },
    )

    StandingInstructionScreen(
        state = viewState,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onRetry = viewModel::retry,
        onAction = viewModel::trySendAction,
    )
}

@Composable
private fun SIDialogs(
    dialogState: SIUiState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is SIUiState.DialogState.DeleteSI -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
            onConfirm = dialogState.onConfirm,
            onDismissRequest = onDismissRequest,
        )

        is SIUiState.DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is SIUiState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}

@Composable
internal fun StandingInstructionScreen(
    state: ScreenState<List<StandingInstruction>>,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onAction: (SIAction) -> Unit,
) {
    MifosScaffold(
        snackbarHostState = snackbarHostState,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(SIAction.AddNewSI)
                },
            ) {
                Icon(imageVector = MifosIcons.Add, "Add")
            }
        },
    ) { padding ->
        // Template idiom: `ScreenContent` (core-base/ui) owns every render branch —
        // loading / empty / no-network / unauthenticated / error+retry — driven by
        // the stream's pre-decided `ScreenState`. Only the per-item Content body is
        // authored here; the empty state keeps the feature's add-SI CTA copy.
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            empty = {
                EmptyContentScreen(
                    title = "No Standing Instructions",
                    subTitle = "No standing instructions found for this client.",
                    btnText = "Add New SI",
                    btnIcon = MifosIcons.Add,
                    onClick = {
                        onAction(SIAction.AddNewSI)
                    },
                )
            },
        ) { instructions, _ ->
            StandingInstructionScreenContent(
                list = instructions,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun StandingInstructionScreenContent(
    list: List<StandingInstruction>,
    modifier: Modifier = Modifier,
    onAction: (SIAction) -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        items(
            items = list,
            key = { item -> item.id ?: item.hashCode() },
        ) { item ->
            SIItem(
                item = item,
                onClick = {
                    onAction(SIAction.ViewSIDetails(it))
                },
                onClickEdit = {
                    onAction(SIAction.EditSIDetails(it))
                },
                onClickDelete = {
                    onAction(SIAction.OnDeleteSI(it))
                },
            )
        }
    }
}

@Composable
private fun SIItem(
    item: StandingInstruction,
    modifier: Modifier = Modifier,
    onClick: (Long) -> Unit,
    onClickEdit: (Long) -> Unit,
    onClickDelete: (Long) -> Unit,
) {
    val state = rememberRevealState(
        maxRevealDp = 105.dp,
        directions = setOf(RevealDirection.EndToStart),
    )
    RevealSwipe(
        modifier = modifier,
        state = state,
        shape = KptTheme.shapes.small,
        backgroundCardStartColor = KptTheme.colorScheme.tertiary,
        backgroundCardEndColor = KptTheme.colorScheme.secondary,
        backgroundStartActionLabel = null,
        backgroundEndActionLabel = "Edit",
        card = { shape, content ->
            Card(
                modifier = Modifier.matchParentSize(),
                colors = CardDefaults.cardColors(
                    contentColor = KptTheme.colorScheme.onSecondary,
                    containerColor = Color.Transparent,
                ),
                shape = shape,
                content = content,
            )
        },
        hiddenContentEnd = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        item.id?.let { onClickEdit(it) }
                    },
                ) {
                    Icon(
                        imageVector = MifosIcons.Edit2,
                        contentDescription = "Edit",
                    )
                }

                IconButton(
                    onClick = { item.id?.let { onClickDelete(it) } },
                ) {
                    Icon(
                        imageVector = MifosIcons.Delete,
                        contentDescription = "Delete",
                    )
                }
            }
        },
        onContentClick = { item.id?.let { onClick(it) } },
    ) { shape ->
        val priorityColor = when (item.priority?.id) {
            1L -> KptTheme.colorScheme.error.copy(
                red = 1f,
                green = 0.27f,
                blue = 0.27f,
            )
            2L -> KptTheme.colorScheme.primaryContainer.copy(
                red = 1f,
                green = 0.53f,
                blue = 0f,
            )
            3L -> KptTheme.colorScheme.primaryContainer.copy(
                red = 1f,
                green = 0.73f,
                blue = 0.2f,
            )

            4L -> KptTheme.colorScheme.secondaryContainer.copy(
                red = 0.6f,
                green = 0.8f,
                blue = 0f,
            )
            else -> KptTheme.colorScheme.outlineVariant.copy(
                red = 0.5f,
                green = 0.5f,
                blue = 0.5f,
            )
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(shape),
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(
                        color = priorityColor,
                        shape = KptTheme.shapes.toRoundedCornerShape(
                            topEnd = KptTheme.spacing.sm,
                            bottomEnd = KptTheme.spacing.sm,
                        ),
                    )
                    .align(Alignment.CenterStart),
            )

            OutlinedCard(
                modifier = modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.Transparent,
                    contentColor = KptTheme.colorScheme.onSurface,
                ),
            ) {
                ListItem(
                    headlineContent = {
                        item.name?.let { it1 -> Text(text = it1) }
                    },
                    supportingContent = {
                        Text(
                            text = buildString {
                                append(item.toClient?.displayName ?: "")
                                item.status?.value?.let {
                                    append(" | ")
                                    append(it)
                                }
                            },
                        )
                    },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FrequencyChip(
                                option = item.recurrenceFrequency,
                                interval = item.recurrenceInterval,
                            )

                            val amount = CurrencyFormatter.format(
                                balance = item.amount,
                                currencyCode = "USD",
                                maximumFractionDigits = null,
                            )

                            Text(
                                text = amount,
                                style = KptTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}
