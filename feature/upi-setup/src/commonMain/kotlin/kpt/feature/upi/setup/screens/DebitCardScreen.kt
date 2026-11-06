/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.upi.setup.screens

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.ui.MifosProgressIndicatorOverlay
import kpt.core.ui.VerifyStepHeader
import kpt.feature.upi.setup.viewmodel.DebitCardUiState
import kpt.feature.upi.setup.viewmodel.DebitCardViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
@Composable
internal fun DebitCardScreen(
    onDebitCardVerified: () -> Unit,
    onDebitCardVerificationFailed: (String) -> Unit,
    modifier: Modifier = Modifier,
    verificationStatus: Boolean = false,
    isContentVisible: Boolean = true,
    viewModel: DebitCardViewModel = koinViewModel(),
) {
    val debitCardUiState by viewModel.debitCardUiState.collectAsStateWithLifecycle()

    DebitCardScreenWithHeaderAndContent(
        debitCardUiState = debitCardUiState,
        onDebitCardVerified = onDebitCardVerified,
        onDebitCardVerificationFailed = onDebitCardVerificationFailed,
        onDone = viewModel::verifyDebitCard,
        verificationStatus = verificationStatus,
        isContentVisible = isContentVisible,
        modifier = modifier,
    )
}

@Composable
@VisibleForTesting
internal fun DebitCardScreenWithHeaderAndContent(
    debitCardUiState: DebitCardUiState,
    onDebitCardVerified: () -> Unit,
    onDebitCardVerificationFailed: (String) -> Unit,
    onDone: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
    verificationStatus: Boolean = false,
    isContentVisible: Boolean = true,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.lg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = KptTheme.elevation.level1,
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                top = KptTheme.spacing.md,
                bottom = KptTheme.spacing.md,
                start = KptTheme.spacing.sm,
                end = KptTheme.spacing.sm,
            ),
        ) {
            VerifyStepHeader("Debit Card Details ", verificationStatus)
            if (isContentVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = KptTheme.spacing.md),
                ) {
                    // handle debit card ui state
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        DebitCardScreenContent(
                            onDone = onDone,
                        )

                        when (debitCardUiState) {
                            is DebitCardUiState.Initials -> {
                            }

                            is DebitCardUiState.Verifying -> MifosProgressIndicatorOverlay()

                            is DebitCardUiState.Verified -> {
                                onDebitCardVerified()
                            }

                            is DebitCardUiState.VerificationFailed -> {
                                onDebitCardVerificationFailed((debitCardUiState).errorMessage)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DebitCardScreenInitialsPreview() {
    MifosTheme {
        DebitCardScreenWithHeaderAndContent(
            debitCardUiState = DebitCardUiState.Initials,
            onDebitCardVerified = {},
            onDebitCardVerificationFailed = {},
            onDone = { _, _, _ -> },
        )
    }
}

@Preview
@Composable
private fun DebitCardScreenLoadingPreview() {
    MifosTheme {
        DebitCardScreenWithHeaderAndContent(
            debitCardUiState = DebitCardUiState.Verifying,
            onDebitCardVerified = {},
            onDebitCardVerificationFailed = {},
            onDone = { _, _, _ -> },
        )
    }
}

@Preview
@Composable
private fun DebitCardScreenVerificationFailedPreview() {
    MifosTheme {
        DebitCardScreenWithHeaderAndContent(
            debitCardUiState = DebitCardUiState.VerificationFailed("Error Message"),
            onDebitCardVerified = {},
            onDebitCardVerificationFailed = {},
            onDone = { _, _, _ -> },
        )
    }
}
