/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.createOrUpdate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.BasicDialogState
import kpt.core.designsystem.component.LoadingDialogState
import kpt.core.designsystem.component.MifosBasicDialog
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.component.MifosLoadingDialog
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTextField
import kpt.core.ui.MifosDivider
import kpt.core.ui.MifosPasswordField
import kpt.core.ui.utils.EventsEffect
import kpt.feature.savedcards.components.CreditCard
import kpt.feature.savedcards.components.getCardNumberTransformation
import kpt.feature.savedcards.generated.resources.Res
import kpt.feature.savedcards.generated.resources.feature_savedcards_card_number
import kpt.feature.savedcards.generated.resources.feature_savedcards_expiry_date
import kpt.feature.savedcards.generated.resources.feature_savedcards_first_name
import kpt.feature.savedcards.generated.resources.feature_savedcards_last_name
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun AddEditCardScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditCardViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            is AECardEvent.OnNavigateBack -> navigateBack.invoke()
            is AECardEvent.ShowToast -> {
                scope.launch {
                    snackbarHostState.showSnackbar(message = getString(event.message))
                }
            }
        }
    }

    AddEditCardDialogs(
        dialogState = state.dialogState,
        onDismissRequest = remember(viewModel) {
            { viewModel.trySendAction(AECardAction.DismissDialog) }
        },
    )

    AddEditCardScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onAction = viewModel::trySendAction,
    )
}

@Composable
private fun AddEditCardDialogs(
    dialogState: AECardState.DialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is AECardState.DialogState.Error -> MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = dialogState.message,
            ),
            onDismissRequest = onDismissRequest,
        )

        is AECardState.DialogState.Loading -> MifosLoadingDialog(
            visibilityState = LoadingDialogState.Shown,
        )

        null -> Unit
    }
}

@Composable
private fun AddEditCardScreenContent(
    state: AECardState,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onAction: (AECardAction) -> Unit,
) {
    MifosScaffold(
        topBarTitle = state.title,
        backPress = { onAction(AECardAction.NavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        AddEditCardScreenContent(
            state = state,
            onAction = onAction,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun AddEditCardScreenContent(
    state: AECardState,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    onAction: (AECardAction) -> Unit,
) {
    val keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next,
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        item {
            CreditCard(
                card = state,
                baseColor = state.selectedColor,
            )
        }

        item {
            MifosDivider()
        }

        item {
            CreditCardColorSelector(
                cardColors = state.backgroundColors,
                selectedColor = state.selectedColor,
                onColorSelected = {
                    onAction(AECardAction.BackgroundColorChanged(it))
                },
            )
        }

        item {
            MifosOutlinedTextField(
                value = state.firstName,
                label = stringResource(Res.string.feature_savedcards_first_name),
                onValueChange = { onAction(AECardAction.FirstNameChanged(it)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text,
                ),
            )
        }

        item {
            MifosTextField(
                value = state.lastName,
                label = stringResource(Res.string.feature_savedcards_last_name),
                onValueChange = { onAction(AECardAction.LastNameChanged(it)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text,
                ),
            )
        }

        item {
            var isFocused by remember { mutableStateOf(false) }

            MifosTextField(
                value = state.cardNumber,
                label = stringResource(Res.string.feature_savedcards_card_number),
                visualTransformation = getCardNumberTransformation(
                    cardNumber = state.cardNumber,
                    isMasked = !isFocused && state.cardNumber.isNotEmpty(),
                ),
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() } &&
                        newValue.length <= state.cardType.maxLength
                    ) {
                        onAction(AECardAction.CardNumberChanged(newValue))
                    }
                },
                keyboardOptions = keyboardOptions,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
            )
        }

        item {
            var showCVV by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
            ) {
                MifosTextField(
                    value = state.expiryDateFormatted,
                    label = stringResource(Res.string.feature_savedcards_expiry_date),
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = keyboardOptions,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                            onAction(AECardAction.ExpiryDateChanged(newValue))
                        }
                    },
                )

                MifosPasswordField(
                    value = state.cvv,
                    label = "CVV",
                    modifier = Modifier.weight(1.5f),
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } &&
                            newValue.length <= state.cardType.cvvLength
                        ) {
                            onAction(AECardAction.CVVChanged(newValue))
                        }
                    },
                    showPassword = showCVV,
                    showPasswordChange = {
                        showCVV = !showCVV
                    },
                    keyboardType = KeyboardType.Number,
                )
            }
        }

        item {
            MifosButton(
                text = {
                    Text(text = state.buttonText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KptTheme.spacing.md),
                onClick = { onAction(AECardAction.SaveCard) },
            )
        }
    }
}

/**
 * Horizontally scrolling row of color swatches for choosing a card color.
 */
@Composable
fun CreditCardColorSelector(
    cardColors: List<Color>,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onColorSelected: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
    ) {
        cardColors.forEach {
            ColorSelector(
                color = it,
                isSelected = it == selectedColor,
                onColorSelected = { onColorSelected(it.value.toString()) },
            )
        }
    }
}

/**
 * A single selectable color swatch, outlined when [isSelected].
 */
@Composable
fun ColorSelector(
    color: Color,
    isSelected: Boolean,
    onColorSelected: () -> Unit,
) {
    val borderColor = if (isSelected) KptTheme.colorScheme.onSurface else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(KptTheme.shapes.extraSmall)
            .border(borderWidth, borderColor)
            .background(color)
            .clickable { onColorSelected() },
    )
}
