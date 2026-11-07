/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.payments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kpt.core.ui.MifosScrollableTabRow
import kpt.core.ui.utility.TabContent
import org.jetbrains.compose.ui.tooling.preview.Preview
/** Route-level composable of the payments screen, hosting the given [tabContents]. */
@Composable
fun PaymentsRoute(
    tabContents: List<TabContent>,
    modifier: Modifier = Modifier,
) {
    PaymentScreenContent(
        tabContents = tabContents,
        modifier = modifier,
    )
}

@Composable
private fun PaymentScreenContent(
    tabContents: List<TabContent>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { tabContents.size })

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(
        pagerState.currentPage,
    ) {
        keyboardController?.hide()
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        MifosScrollableTabRow(
            tabContents = tabContents,
            pagerState = pagerState,
        )
    }
}

// TODO History, SI, Invoices are not using self api
//  https://venus.mifos.community/fineract-provider/api/v1/standinginstructions?clientId=2
//  https://venus.mifos.community/fineract-provider/api/v1/datatables/invoice/2
//
/** Tabs available on the payments screen. */
enum class PaymentsScreenContents {
    /** Send money tab. */
    SEND,

    /** Request money tab. */
    REQUEST,

    /** Transaction history tab. */
    HISTORY,

    /** Standing instructions tab. */
    SI,

    /** Invoices tab. */
    INVOICES,

    /** Autopay tab. */
    AUTOPAY,
}

@Preview
@Composable
private fun PaymentsScreenPreview() {
    PaymentScreenContent(
        tabContents = emptyList(),
    )
}
