/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.faq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.faq.generated.resources.Res
import kpt.feature.faq.generated.resources.feature_faq
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun FaqScreenRoute(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    faqViewModel: FAQViewModel = koinViewModel(),
) {
    val state by faqViewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(faqViewModel) { event ->
        when (event) {
            is FaqEvent.OnNavigateBack -> navigateBack.invoke()
        }
    }

    FaqScreen(
        modifier = modifier,
        state = state,
        onAction = remember(faqViewModel) {
            { faqViewModel.trySendAction(it) }
        },
    )
}

@Composable
private fun FaqScreen(
    state: FaqState,
    onAction: (FaqAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ForkScaffold(
        modifier = modifier,
        topBarTitle = stringResource(Res.string.feature_faq),
        backPress = {
            onAction(FaqAction.NavigateBack)
        },
        containerColor = KptTheme.colorScheme.background,
    ) { contentPadding ->
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            itemsIndexed(
                items = state.faqList,
                key = { _, item -> item.faqId },
            ) { i, faqItem ->
                FaqItemScreen(
                    faq = faqItem,
                    onExpand = { faqId ->
                        onAction(FaqAction.ExpandFaq(faqId))
                    },
                    isExpanded = state.expandedFaq == faqItem.faqId,
                )

                if (i != state.faqList.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KptTheme.spacing.lg),
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqItemScreen(
    faq: FAQ,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onExpand: (Int) -> Unit,
) {
    val density = LocalDensity.current

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onExpand(faq.faqId) },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        shape = RectangleShape,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = KptTheme.spacing.lg,
                vertical = KptTheme.spacing.lg,
            ),
        ) {
            Row {
                Text(
                    text = stringResource(faq.question),
                    fontWeight = FontWeight.W500,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                Icon(
                    imageVector = ForkIcons.KeyboardArrowDown,
                    contentDescription = "drop down",
                    modifier = Modifier
                        .scale(1f, if (isExpanded) -1f else 1f),
                )
            }
            Row {
                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    visible = isExpanded,
                    enter = slideInVertically {
                        with(density) { -40.dp.roundToPx() }
                    } + expandVertically(
                        expandFrom = Alignment.Top,
                    ) + fadeIn(
                        initialAlpha = 0.3f,
                    ),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut(),
                ) {
                    Text(
                        text = stringResource(faq.answer),
                        style = KptTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = KptTheme.spacing.xs)
                            .weight(1f),
                    )
                }

                Spacer(modifier = Modifier.weight(.1f))
            }
        }
    }
}

@Preview
@Composable
private fun FaqScreenPreview() {
    FaqScreen(
        state = FaqState(sampleFaqList),
        onAction = {},
    )
}
