/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.finance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.ui.MifosScrollableTabRow
import kpt.core.ui.utility.TabContent
import org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
internal fun FinanceRoute(
    tabContents: List<TabContent>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { tabContents.size })

    Column(modifier = modifier.fillMaxSize()) {
        MifosScrollableTabRow(
            tabContents = tabContents,
            pagerState = pagerState,
        )
    }
}

// TODO Cards not using self apis until then commented it.
//  https://venus.mifos.community/fineract-provider/api/v1/datatables/saved_cards/2
//  https://venus.mifos.community/fineract-provider/api/v1/datatables/kyc_level1_details/2
/** Tabs shown on the finance screen. */
enum class FinanceScreenContents {
    /** Tab listing the client's accounts. */
    ACCOUNTS,

    /** Tab listing the client's beneficiaries. */
    BENEFICIARIES,
//    CARDS,
//    MERCHANTS,
//    KYC,
}

@Preview
@Composable
private fun FinanceScreenPreview() {
    FinanceRoute(
        tabContents = emptyList(),
    )
}
