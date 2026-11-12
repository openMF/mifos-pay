/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fork.component.BasicDialogState
import kpt.core.designsystem.fork.component.ForkBasicDialog
import kpt.core.designsystem.fork.component.ForkLoadingDialog
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.component.LoadingDialogState
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.model.savedcards.SavedCard
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.savedcards.createOrUpdate.CardAddEditType
import kpt.feature.savedcards.utils.CreditCardUtils.detectCardType
import kpt.feature.savedcards.utils.CreditCardUtils.maskCreditCardNumber
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
/**
 * Known Issue, On deleting card, state isn't updating automatically
 * whereas on adding or updating, card state is updating properly,
 * This issue will be fixed soon.
 */
@Composable
fun CardsScreen(
    modifier: Modifier = Modifier,
    navigateToAddEdit: (CardAddEditType) -> Unit,
    navigateToViewDetail: (Long) -> Unit,
    viewModel: CardsScreenViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val cartState by viewModel.cardState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is CardEvent.OnNavigateToAddEdit -> navigateToAddEdit.invoke(event.type)

            is CardEvent.OnNavigateToCardDetails -> navigateToViewDetail.invoke(event.cardId)

            is CardEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }
        }
    }

    SavedCardDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(CardAction.DismissDialog) }
        },
    )

    CardsScreen(
        modifier = modifier,
        state = cartState,
        snackbarHostState = snackbarHostState,
        onRetry = viewModel::retry,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun SavedCardDialogs(
    dialogState: CardState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is CardState.DialogState.DeleteCard -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(dialogState.title),
                message = stringResource(dialogState.message),
            ),
            onConfirm = dialogState.onConfirm,
            onDismissRequest = onDismissRequest,
        )

        is CardState.DialogState.Error -> ForkBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is CardState.DialogState.Loading -> ForkLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}

@Composable
internal fun CardsScreen(
    state: ScreenState<List<SavedCard>>,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CardAction) -> Unit,
) {
    ForkScaffold(
        snackbarHostState = snackbarHostState,
        floatingActionButtonPosition = FabPosition.EndOverlay,
        modifier = modifier,
        floatingActionButton = {
            AnimatedVisibility(
                // FAB shows only when a card list is on screen — matches the
                // retired `ViewState.Content.hasFab`. The Empty state carries its
                // own inline "Add New Card" CTA below.
                visible = state is ScreenState.Content,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingActionButton(
                    onClick = {
                        onAction(CardAction.AddNewCard)
                    },
                ) {
                    Icon(imageVector = ForkIcons.Add, "Add")
                }
            }
        },
    ) { paddingValues ->
        // Template idiom: `ScreenContent` (core-base/ui) owns every render branch —
        // loading / empty / no-network / unauthenticated / error+retry — driven by
        // the stream's pre-decided `ScreenState`. Only the Empty CTA + the per-item
        // Content body are authored here; the empty copy keeps the feature's text.
        ScreenContent(
            state = state,
            onRetry = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            empty = {
                EmptyContentScreen(
                    title = "No Saved Cards",
                    subTitle = "No saved cards found, click the button below to add a new card",
                    btnText = "Add New Card",
                    btnIcon = ForkIcons.Add,
                    onClick = {
                        onAction(CardAction.AddNewCard)
                    },
                )
            },
        ) { cards, _ ->
            CardsScreenContent(
                cards = cards,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun CardsScreenContent(
    cards: List<SavedCard>,
    modifier: Modifier = Modifier,
    onAction: (CardAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        items(
            items = cards,
            key = { it.id },
        ) { savedCard ->
            SavedCardItem(
                savedCard = savedCard,
                onClick = {
                    onAction(CardAction.ViewCardDetails(it))
                },
                onClickEdit = {
                    onAction(CardAction.EditCardDetails(it))
                },
                onClickDelete = {
                    onAction(CardAction.DeleteCardClicked(it))
                },
            )
        }
    }
}

@Composable
private fun SavedCardItem(
    savedCard: SavedCard,
    modifier: Modifier = Modifier,
    onClick: (cardId: Long) -> Unit,
    onClickEdit: (cardId: Long) -> Unit,
    onClickDelete: (cardId: Long) -> Unit,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = KptTheme.shapes.small,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
        ),
        onClick = {
            onClick(savedCard.id)
        },
    ) {
        ListItem(
            headlineContent = {
                Text(text = savedCard.fullName)
            },
            supportingContent = {
                Text(text = savedCard.cardNumber.maskCreditCardNumber())
            },
            leadingContent = {
                val cardImage = savedCard.cardNumber.detectCardType().cardImage

                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        imageVector = vectorResource(cardImage),
                        contentDescription = "Card Image",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            onClickEdit(savedCard.id)
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = KptTheme.colorScheme.surfaceContainerHighest,
                            contentColor = KptTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            imageVector = ForkIcons.Edit2,
                            contentDescription = "Edit Card",
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            onClickDelete(savedCard.id)
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = KptTheme.colorScheme.errorContainer,
                            contentColor = KptTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(
                            imageVector = ForkIcons.OutlinedDelete,
                            contentDescription = "Delete Card",
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
