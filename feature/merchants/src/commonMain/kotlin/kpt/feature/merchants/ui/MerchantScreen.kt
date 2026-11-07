/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.merchants.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MfLoadingWheel
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.component.rememberMifosPullToRefreshState
import kpt.core.designsystem.theme.fork.MifosTheme
import kpt.core.model.savingsaccount.Currency
import kpt.core.model.savingsaccount.DepositType
import kpt.core.model.savingsaccount.InterestPeriod
import kpt.core.model.savingsaccount.SavingsWithAssociationsEntity
import kpt.core.model.savingsaccount.Status
import kpt.core.model.savingsaccount.SubStatus
import kpt.core.model.savingsaccount.Summary
import kpt.core.model.savingsaccount.Timeline
import kpt.core.ui.EmptyContentScreen
import kpt.core.ui.MifosSearchBar
import kpt.feature.merchants.MerchantUiState
import kpt.feature.merchants.MerchantViewModel
import kpt.feature.merchants.generated.resources.Res
import kpt.feature.merchants.generated.resources.feature_merchants_empty_no_merchants_subtitle
import kpt.feature.merchants.generated.resources.feature_merchants_empty_no_merchants_title
import kpt.feature.merchants.generated.resources.feature_merchants_error_oops
import kpt.feature.merchants.generated.resources.feature_merchants_loading
import kpt.feature.merchants.generated.resources.feature_merchants_search
import kpt.feature.merchants.generated.resources.feature_merchants_unexpected_error_subtitle
import kpt.feature.merchants.navigation.navigateToMerchantTransferScreen
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
/** Screen listing merchants with search and pull-to-refresh. */
@Composable
fun MerchantScreen(
    modifier: Modifier = Modifier,
    viewModel: MerchantViewModel = koinViewModel(),
) {
    val merchantUiState by viewModel.merchantUiState.collectAsStateWithLifecycle()
    val merchantsListUiState by viewModel.merchantsListUiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    MerchantScreen(
        merchantUiState = merchantUiState,
        merchantListUiState = merchantsListUiState,
        isRefreshing = isRefreshing,
        updateQuery = viewModel::updateSearchQuery,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@Composable
@VisibleForTesting
internal fun MerchantScreen(
    merchantUiState: MerchantUiState,
    merchantListUiState: MerchantUiState,
    isRefreshing: Boolean,
    updateQuery: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullRefreshState = rememberMifosPullToRefreshState(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    )

    MifosScaffold(
        modifier = modifier.fillMaxSize(),
        pullToRefreshState = pullRefreshState,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center,
        ) {
            when (merchantUiState) {
                MerchantUiState.Empty -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_merchants_empty_no_merchants_title),
                        subTitle = stringResource(Res.string.feature_merchants_empty_no_merchants_subtitle),
                        modifier = Modifier,
                    )
                }

                is MerchantUiState.Error -> {
                    EmptyContentScreen(
                        title = stringResource(Res.string.feature_merchants_error_oops),
                        subTitle = stringResource(Res.string.feature_merchants_unexpected_error_subtitle),
                        modifier = Modifier,
                        iconTint = KptTheme.colorScheme.error,
                    )
                }

                MerchantUiState.Loading -> {
                    MfLoadingWheel(
                        contentDesc = stringResource(Res.string.feature_merchants_loading),
                        backgroundColor = KptTheme.colorScheme.surface,
                    )
                }

                is MerchantUiState.ShowMerchants -> {
                    MerchantScreenContent(
                        merchantList = (merchantListUiState as MerchantUiState.ShowMerchants).merchants,
                        updateQuery = updateQuery,
                    )
                }
            }
        }
    }
}

@Composable
private fun MerchantScreenContent(
    merchantList: List<SavingsWithAssociationsEntity>,
    updateQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val query by rememberSaveable { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            SearchBarScreen(
                query = query,
                onQueryChange = { q ->
                    updateQuery(q)
                },
                onSearch = {},
                onClearQuery = { updateQuery("") },
            )
            MerchantList(merchantList = merchantList)
        }
    }
}

@Composable
private fun MerchantList(
    merchantList: List<SavingsWithAssociationsEntity>,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val navController = rememberNavController()

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md),
    ) {
        items(merchantList.size) { index ->
            MerchantsItem(
                savingsWithAssociations = merchantList[index],
                onMerchantClicked = {
                    navController.navigateToMerchantTransferScreen(
                        merchantVPA = merchantList[index].accountNo,
                        merchantName = merchantList[index].clientName,
                        merchantAccountNumber = merchantList[index].accountNo,
                    )
//                    val intent = Intent(context, MerchantTransferActivity::class.java)
//                    intent.putExtra(Constants.MERCHANT_NAME, merchantList[index].clientName)
//                    intent.putExtra(Constants.MERCHANT_VPA, merchantList[index].externalId)
//                    intent.putExtra(Constants.MERCHANT_ACCOUNT_NO, merchantList[index].accountNo)
//                    context.startActivity(intent)
                },
                onMerchantLongPressed = {
                    clipboardManager.setText(AnnotatedString(it ?: ""))
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MifosSearchBar(
        query = query,
        placeHolder = stringResource(Res.string.feature_merchants_search),
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        onClearQuery = onClearQuery,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun MerchantLoadingPreview() {
    MifosTheme {
        MerchantScreen(
            merchantUiState = MerchantUiState.Loading,
            merchantListUiState = MerchantUiState.ShowMerchants(sampleMerchantList),
            isRefreshing = false,
            updateQuery = {},
            onRefresh = {},
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
private fun MerchantListPreview() {
    MifosTheme {
        MerchantScreen(
            merchantUiState = MerchantUiState.ShowMerchants(sampleMerchantList),
            merchantListUiState = MerchantUiState.ShowMerchants(sampleMerchantList),
            isRefreshing = false,
            updateQuery = {},
            onRefresh = {},
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
private fun MerchantErrorPreview() {
    MifosTheme {
        MerchantScreen(
            merchantUiState = MerchantUiState.Error("Error Screen"),
            merchantListUiState = MerchantUiState.ShowMerchants(sampleMerchantList),
            isRefreshing = true,
            updateQuery = {},
            onRefresh = {},
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
private fun MerchantEmptyPreview() {
    MifosTheme {
        MerchantScreen(
            merchantUiState = MerchantUiState.Empty,
            merchantListUiState = MerchantUiState.ShowMerchants(sampleMerchantList),
            isRefreshing = false,
            updateQuery = {},
            onRefresh = {},
            modifier = Modifier,
        )
    }
}

/** Sample merchant list used for previews. */
val sampleMerchantList = List(10) {
    SavingsWithAssociationsEntity(
        id = 1L,
        accountNo = "123456789",
        depositType = DepositType(
            id = 9994,
            code = "iriure",
            value = "liber",
        ),
        clientId = 101,
        clientName = "Alice Bob",
        savingsProductId = 2001,
        savingsProductName = "Premium Savings Account",
        fieldOfficerId = 501,
        status = Status(
            id = 1403,
            code = "ornatus",
            value = "iaculis",
            submittedAndPendingApproval = false,
            approved = false,
            rejected = false,
            withdrawnByApplicant = false,
            active = false,
            closed = false,
            prematureClosed = false,
            transferInProgress = false,
            transferOnHold = false,
            matured = false,
        ),
        timeline = Timeline(
            submittedOnDate = listOf(),
            submittedByUsername = "Lemuel Solomon",
            submittedByFirstname = "Vivian Henson",
            submittedByLastname = "Amalia Booker",
            approvedOnDate = listOf(),
            approvedByUsername = "Helga Randall",
            approvedByFirstname = "Terri Ochoa",
            approvedByLastname = "Sheryl Cain",
            activatedOnDate = listOf(),
            activatedByUsername = "Lela Johnston",
            activatedByFirstname = "Raymundo Foley",
            activatedByLastname = "Deanne Sosa",
        ),
        currency = Currency(
            code = "USD",
            name = "Lessie Lindsey",
            decimalPlaces = 7322,
            inMultiplesOf = 5447,
            displaySymbol = "ut",
            nameCode = "Angelina Walls",
            displayLabel = "iisque",
        ),
        nominalAnnualInterestRate = 3.5,
        withdrawalFeeForTransfers = true,
        allowOverdraft = false,
        enforceMinRequiredBalance = false,
        withHoldTax = true,
        lastActiveTransactionDate = listOf(2024, 3, 24),
        summary = Summary(
            currency = Currency(
                code = "USD",
                name = "Kennith Gray",
                decimalPlaces = 6021,
                inMultiplesOf = 4636,
                displaySymbol = "efficiantur",
                nameCode = "Gerardo Deleon",
                displayLabel = "mollis",
            ),
            totalDeposits = 18.19,
            totalWithdrawals = 20.21,
            totalInterestPosted = 6052.0,
            accountBalance = 22.23,
            totalOverdraftInterestDerived = 2232.0,
            interestNotPosted = 5113.0,
            availableBalance = 24.25,
        ),
        transactions = listOf(),
        subStatus = SubStatus(
            id = 2838,
            code = "nobis",
            value = "mi",
            none = false,
            inactive = false,
            dormant = false,
            escheat = false,
            block = false,
            blockCredit = false,
            blockDebit = false,
        ),
        interestCompoundingPeriodType = InterestPeriod(),
        interestPostingPeriodType = InterestPeriod(),
        interestCalculationType = InterestPeriod(),
        interestCalculationDaysInYearType = InterestPeriod(),
        lienAllowed = false,
        isDormancyTrackingActive = false,
    )
}
