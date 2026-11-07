/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savedcards.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.model.savedcards.SavedCard
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosDivider
import kpt.core.ui.MifosProgressIndicator
import kpt.core.ui.utils.EventsEffect
import kpt.feature.savedcards.components.CreditCard
import kpt.feature.savedcards.generated.resources.Res
import kpt.feature.savedcards.generated.resources.feature_savedcards_error_oops
import kpt.feature.savedcards.generated.resources.feature_savedcards_subtitle
import kpt.feature.savedcards.utils.CardMaskStyle
import kpt.feature.savedcards.utils.CreditCardUtils.maskCreditCardNumber
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun CardDetailScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = koinViewModel(),
) {
    val state by viewModel.cartDetailState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            CardDetailEvent.OnNavigateBack -> navigateBack.invoke()
        }
    }

    CardDetailScreen(
        state = state,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        modifier = modifier,
    )
}

@Composable
internal fun CardDetailScreen(
    state: ViewState,
    modifier: Modifier = Modifier,
    onAction: (CardDetailAction) -> Unit,
) {
    MifosScaffold(
        topBarTitle = "Card Details",
        backPress = {
            onAction(CardDetailAction.NavigateBack)
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is ViewState.Loading -> MifosProgressIndicator()

                is ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_savedcards_error_oops),
                        subTitle = stringResource(Res.string.feature_savedcards_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is ViewState.Content -> {
                    CardDetailScreenContent(
                        state = state,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardDetailScreenContent(
    state: ViewState.Content,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        item {
            var isCardNumberVisible by remember { mutableStateOf(false) }

            val maskStyle by remember(isCardNumberVisible) {
                mutableStateOf(
                    if (isCardNumberVisible) {
                        CardMaskStyle.SHOW_ALL
                    } else {
                        CardMaskStyle.ALL_EXCEPT_LAST_FOUR
                    },
                )
            }

            val formattedCvv by remember(isCardNumberVisible) {
                mutableStateOf(
                    if (isCardNumberVisible) state.savedCard.cvv else state.savedCard.maskedCvv,
                )
            }

            CreditCard(
                cardNumber = state.savedCard.cardNumber,
                fullName = state.savedCard.fullName,
                expiryDate = state.savedCard.formattedExpiryDate,
                cvv = formattedCvv,
                baseColor = state.selectedColor,
                maskStyle = maskStyle,
                onClick = {
                    isCardNumberVisible = !isCardNumberVisible
                },
            )
        }

        item {
            CardDetail(
                savedCard = state.savedCard,
                containerColor = KptTheme.colorScheme.surface,
            )
        }
    }
}

@Composable
private fun CardDetail(
    savedCard: SavedCard,
    modifier: Modifier = Modifier,
    containerColor: Color = KptTheme.colorScheme.surface,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KptTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            CreditCardLabelAndText(
                label = "Card Holder",
                text = savedCard.fullName,
            )
            MifosDivider(color = KptTheme.colorScheme.outlineVariant)
            CreditCardLabelAndText(
                label = "Card Number",
                text = savedCard.cardNumber.maskCreditCardNumber(),
            )
            MifosDivider(color = KptTheme.colorScheme.outlineVariant)
            CreditCardLabelAndText(
                label = "Expiry Date",
                text = savedCard.formattedExpiryDate,
            )
            MifosDivider(color = KptTheme.colorScheme.outlineVariant)
            CreditCardLabelAndText(
                label = "CVV",
                text = savedCard.maskedCvv,
            )
        }
    }
}

@Composable
private fun CreditCardLabelAndText(
    label: String,
    text: String,
    labelColor: Color = KptTheme.colorScheme.primary,
    textColor: Color = KptTheme.colorScheme.onSurface,
) {
    Column(
        modifier = Modifier
            .wrapContentSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label.uppercase(),
            style = KptTheme.typography.labelSmall,
            color = labelColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = TextStyle(
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
            ),
            color = textColor,
        )
    }
}
