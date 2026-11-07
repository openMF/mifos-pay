/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.auth.socialSignup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.data.util.Constants.WALLET_ACCOUNT_SAVINGS_PRODUCT_ID
import kpt.core.designsystem.component.MifosGradientBackground
import kpt.core.designsystem.component.MifosOutlinedButton
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.MifosTopAppBar
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.feature.auth.generated.resources.Res
import kpt.feature.auth.generated.resources.feature_auth_create_an_account
import kpt.feature.auth.generated.resources.feature_auth_or
import kpt.feature.auth.generated.resources.feature_auth_sign_up_as_customer
import kpt.feature.auth.generated.resources.feature_auth_sign_up_as_merchant
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun SignupMethodScreen(
    modifier: Modifier = Modifier,
    navigateToSignupScreen: (savingsProductId: Int) -> Unit = {},
    onDismissSignUp: () -> Unit = {},
) {
    SignupMethodScreenContent(
        modifier = modifier,
        onDismissSignUp = onDismissSignUp,
        navigateToSignupScreen = navigateToSignupScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupMethodScreenContent(
    modifier: Modifier = Modifier,
    onDismissSignUp: () -> Unit = {},
    navigateToSignupScreen: (savingsProductId: Int) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    MifosScaffold(
        modifier = modifier,
        topBar = {
            MifosTopAppBar(
                title = "Choose Account Type",
                navigationIcon = MifosIcons.Back,
                onNavigationIconClick = onDismissSignUp,
                navigationIconContentDescription = "Back",
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        MifosGradientBackground {
            SignupMethodScreenContent(
                modifier = Modifier.padding(it),
                onSignUpAsMerchant = {
                    navigateToSignupScreen(WALLET_ACCOUNT_SAVINGS_PRODUCT_ID)
                },
                onSignupAsCustomer = {
                    navigateToSignupScreen(WALLET_ACCOUNT_SAVINGS_PRODUCT_ID)
                },
            )
        }
    }
}

@Composable
private fun SignupMethodScreenContent(
    onSignUpAsMerchant: () -> Unit,
    onSignupAsCustomer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
            text = stringResource(Res.string.feature_auth_create_an_account),
        )

        MifosOutlinedButton(
            modifier = Modifier.padding(top = KptTheme.spacing.xl),
            onClick = onSignUpAsMerchant,
            border = BorderStroke(
                1.dp,
                KptTheme.colorScheme.primary,
            ),
            shape = KptTheme.shapes.extraSmall,
        ) {
            Text(
                text = stringResource(Res.string.feature_auth_sign_up_as_merchant).uppercase(),
                style = KptTheme.typography.labelMedium,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = KptTheme.spacing.lg, end = KptTheme.spacing.sm)
                    .weight(.4f),
                thickness = 1.dp,
            )
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .weight(.1f),
                text = stringResource(Res.string.feature_auth_or),
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(start = KptTheme.spacing.sm, end = KptTheme.spacing.lg)
                    .weight(.4f),
                thickness = 1.dp,
            )
        }

        MifosOutlinedButton(
            modifier = Modifier.padding(horizontal = KptTheme.spacing.lg),
            onClick = onSignupAsCustomer,
            border = BorderStroke(
                1.dp,
                KptTheme.colorScheme.primary,
            ),
            shape = KptTheme.shapes.extraSmall,
        ) {
            Text(
                text = stringResource(Res.string.feature_auth_sign_up_as_customer).uppercase(),
                style = KptTheme.typography.labelMedium,
            )
        }
    }
}

@Preview
@Composable
private fun SignupMethodContentScreenPreview() {
    MifosTheme {
        SignupMethodScreenContent(
            onSignUpAsMerchant = {},
            onSignupAsCustomer = {},
        )
    }
}
