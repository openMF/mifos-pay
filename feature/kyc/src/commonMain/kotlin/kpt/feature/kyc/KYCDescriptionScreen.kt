/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.kyc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fork.icon.ForkIcons
import kpt.core.ui.AvatarBox
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.ForkProgressIndicatorOverlay
import kpt.core.ui.fork.utils.EventsEffect
import kpt.feature.kyc.generated.resources.Res
import kpt.feature.kyc.generated.resources.feature_kyc_check
import kpt.feature.kyc.generated.resources.feature_kyc_complete_kyc
import kpt.feature.kyc.generated.resources.feature_kyc_error_oops
import kpt.feature.kyc.generated.resources.feature_kyc_unexpected_error_subtitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
/** Screen describing the KYC levels; each level calls its matching click callback. */
@Composable
fun KYCScreen(
    onLevel1Clicked: () -> Unit,
    onLevel2Clicked: () -> Unit,
    onLevel3Clicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KYCDescriptionViewModel = koinViewModel(),
) {
    val state by viewModel.kycState.collectAsStateWithLifecycle()

    EventsEffect(viewModel) { event ->
        when (event) {
            KycEvent.OnLevel1Clicked -> onLevel1Clicked.invoke()
            KycEvent.OnLevel2Clicked -> onLevel2Clicked.invoke()
            KycEvent.OnLevel3Clicked -> onLevel3Clicked.invoke()
        }
    }

    KYCDescriptionScreen(
        kUiState = state,
        onAction = remember(viewModel) {
            { viewModel.trySendAction(it) }
        },
        modifier = modifier,
    )
}

@Composable
private fun KYCDescriptionScreen(
    kUiState: KYCDescriptionUiState,
    onAction: (KycAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (kUiState) {
            KYCDescriptionUiState.Loading -> ForkProgressIndicatorOverlay()

            is KYCDescriptionUiState.Error -> {
                EmptyContentScreen(
                    title = stringResource(Res.string.feature_kyc_error_oops),
                    subTitle = stringResource(Res.string.feature_kyc_unexpected_error_subtitle),
                    modifier = Modifier,
                    iconTint = KptTheme.colorScheme.error,
                )
            }

            is KYCDescriptionUiState.Content -> {
                KYCDescriptionScreen(
                    currentLevel = kUiState.currentLevel,
                    onLevel1Clicked = { onAction(KycAction.Level1Clicked) },
                    onLevel2Clicked = { onAction(KycAction.Level2Clicked) },
                    onLevel3Clicked = { onAction(KycAction.Level3Clicked) },
                )
            }
        }
    }
}

@Composable
private fun KYCDescriptionScreen(
    currentLevel: KycLevel?,
    onLevel1Clicked: () -> Unit,
    onLevel2Clicked: () -> Unit,
    onLevel3Clicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(KptTheme.spacing.md)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.feature_kyc_complete_kyc),
            modifier = Modifier.padding(horizontal = KptTheme.spacing.lg),
            textAlign = TextAlign.Center,
            style = KptTheme.typography.titleMedium,
        )

        KycLevel.entries.forEach { kycLevel ->
            KYCLevelCard(
                title = kycLevel.title,
                icon = kycLevel.icon,
                completed = (currentLevel?.level ?: -1) >= kycLevel.level,
                enabled = (currentLevel?.level ?: 0) >= kycLevel.level - 1,
                onClick = {
                    when (kycLevel.level) {
                        1 -> onLevel1Clicked.invoke()
                        2 -> onLevel2Clicked.invoke()
                        3 -> onLevel3Clicked.invoke()
                    }
                },
            )
        }
    }
}

@Composable
private fun KYCLevelCard(
    title: String,
    icon: ImageVector,
    completed: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        OutlinedCard(
            enabled = enabled,
            onClick = onClick,
            shape = KptTheme.shapes.extraSmall,
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.Transparent,
                disabledContainerColor = KptTheme.colorScheme.surfaceContainerHighest,
            ),
            border = CardDefaults.outlinedCardBorder(enabled = true),
            modifier = Modifier.weight(2.5f, false),
        ) {
            ListItem(
                headlineContent = {
                    Text(text = title)
                },
                leadingContent = {
                    AvatarBox(
                        icon = icon,
                        backgroundColor = KptTheme.colorScheme.secondaryContainer,
                    )
                },
                trailingContent = {
                    if (completed) {
                        Icon(
                            imageVector = ForkIcons.OutlinedDoneAll,
                            contentDescription = stringResource(Res.string.feature_kyc_check),
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
