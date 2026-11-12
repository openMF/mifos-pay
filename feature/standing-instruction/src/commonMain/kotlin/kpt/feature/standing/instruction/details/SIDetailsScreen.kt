/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.standing.instruction.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.common.DateHelper
import kpt.core.designsystem.fork.component.ForkScaffold
import kpt.core.model.standinginstruction.StandingInstruction
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkDivider
import kpt.core.ui.ForkProgressIndicator
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.standing.instruction.components.FrequencyChip
import kpt.feature.standing.instruction.components.InstructionTypeChip
import kpt.feature.standing.instruction.components.PriorityChip
import kpt.feature.standing.instruction.generated.resources.Res
import kpt.feature.standing.instruction.generated.resources.feature_standing_instruction_error_oops
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun SIDetailsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SIDetailViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            SIDEvent.OnNavigateBack -> navigateBack.invoke()
        }
    }

    SIDetailsScreen(
        state = state,
        modifier = modifier,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
    )
}

@Composable
internal fun SIDetailsScreen(
    state: SIDetailState,
    modifier: Modifier = Modifier,
    onAction: (SIDAction) -> Unit,
) {
    ForkScaffold(
        topBarTitle = "Instruction Details",
        modifier = modifier,
        backPress = {
            onAction(SIDAction.NavigateBack)
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            when (state.viewState) {
                is ViewState.Loading -> ForkProgressIndicator()

                is ViewState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_standing_instruction_error_oops),
                        subTitle = state.viewState.message,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                is ViewState.Content -> {
                    SIDetailsContent(
                        state = state.viewState,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun SIDetailsContent(
    state: ViewState.Content,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        horizontalAlignment = Alignment.Start,
    ) {
        item {
            SIDetailsCard(
                item = state.data,
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun SIDetailsCard(
    item: StandingInstruction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = KptTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.sm),
            ) {
                RowBlock {
                    Text(
                        text = "Instruction Name",
                    )
                    Text(
                        text = item.name ?: "Not Available",
                        color = colorForMissing(item.name),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                val amount = CurrencyFormatter.format(
                    balance = item.amount,
                    currencyCode = "USD",
                    maximumFractionDigits = 2,
                )

                RowBlock {
                    Text(
                        text = "Instruction Amount",
                    )
                    Text(
                        text = amount,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (item.status?.value?.isEmpty() == false) {
                    RowBlock {
                        Text(
                            text = "Status",
                        )
                        Text(text = item.status?.value ?: "", fontWeight = FontWeight.SemiBold)
                    }
                }

                RowBlock {
                    Text(
                        text = "Transfer Type",
                    )
                    Text(
                        text = item.transferType ?: "Not Available",
                        color = colorForMissing(item.transferType),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (item.priority != null) {
                    RowBlock {
                        Text(
                            text = "Priority",
                        )

                        PriorityChip(
                            priority = item.priority,
                        )
                    }
                }

                RowBlock {
                    Text(
                        text = "Instruction Type",
                    )
                    InstructionTypeChip(
                        type = item.instructionType,
                    )
                }

                RowBlock {
                    Text(
                        text = "Recurrence Frequency",
                    )

                    FrequencyChip(
                        option = item.recurrenceFrequency,
                        interval = item.recurrenceInterval,
                    )
                }

                RowBlock {
                    val validFrom = item.validFrom?.let {
                        DateHelper.getDateAsString(
                            it,
                        )
                    }

                    Text(
                        text = "Valid From",
                    )
                    Text(
                        text = validFrom ?: "Not Available",
                        fontWeight = FontWeight.SemiBold,
                        color = colorForMissing(validFrom),
                    )
                }

                item.recurrenceOnMonthDay?.let {
                    RowBlock(
                        showDivider = it.isNotEmpty(),
                    ) {
                        val validTill = item.validTill?.let { DateHelper.getDateAsString(it) }

                        Text(
                            text = "Valid Till",
                        )
                        Text(
                            text = validTill ?: "Not Available",
                            color = colorForMissing(validTill),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                if (item.recurrenceOnMonthDay?.isNotEmpty() == true) {
                    val recurrenceOnMonthDay =
                        item.recurrenceOnMonthDay?.let { DateHelper.getDateMonthString(it) }

                    RowBlock(false) {
                        Text(
                            text = "Recurrence On Month Day",
                        )

                        Text(
                            text = recurrenceOnMonthDay ?: "Not Available",
                            color = colorForMissing(recurrenceOnMonthDay),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = KptTheme.colorScheme.surface,
                contentColor = KptTheme.colorScheme.onSurface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.sm),
            ) {
                RowBlock {
                    Text(
                        text = "From Office",
                    )
                    Text(
                        text = item.fromOffice?.name ?: "Not Available",
                        color = colorForMissing(item.fromOffice?.name),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                RowBlock {
                    Text(
                        text = "From Client",
                    )
                    Text(
                        text = item.fromClient?.displayName ?: "Not Available",
                        color = colorForMissing(item.fromClient?.displayName),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                RowBlock {
                    Text(
                        text = "From Account Type",
                    )
                    Text(
                        text = item.fromAccountType ?: "Not Available",
                        color = colorForMissing(item.fromAccountType),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                RowBlock(false) {
                    Text(
                        text = "From Account",
                    )
                    Text(
                        text = item.fromAccount?.accountNo ?: "Not Available",
                        color = colorForMissing(item.fromAccount?.accountNo),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = KptTheme.colorScheme.surface,
                contentColor = KptTheme.colorScheme.onSurface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(KptTheme.spacing.sm),
            ) {
                RowBlock {
                    Text(
                        text = "To Office",
                    )
                    Text(
                        text = item.toOffice?.name ?: "Not Available",
                        color = colorForMissing(item.toOffice?.name),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                RowBlock {
                    Text(
                        text = "To Client",
                    )
                    Text(
                        text = item.toClient?.displayName ?: "Not Available",
                        color = colorForMissing(item.toClient?.displayName),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                RowBlock {
                    Text(
                        text = "To Account Type",
                    )
                    Text(
                        text = item.toAccountType ?: "Not Available",
                        color = colorForMissing(item.toAccountType),
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                RowBlock(false) {
                    Text(
                        text = "To Account",
                    )
                    Text(
                        text = item.toAccount?.accountNo ?: "Not Available",
                        color = colorForMissing(item.toAccount?.accountNo),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private inline fun RowBlock(
    showDivider: Boolean = true,
    crossinline content: @Composable (RowScope.() -> Unit),
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }

        if (showDivider) {
            ForkDivider()
        }
    }
}

/** Returns the error color when [value] is null, otherwise the normal text color. */
@Composable
fun colorForMissing(value: String?): Color {
    return if (value == null) {
        KptTheme.colorScheme.error
    } else {
        KptTheme.colorScheme.onBackground
    }
}
