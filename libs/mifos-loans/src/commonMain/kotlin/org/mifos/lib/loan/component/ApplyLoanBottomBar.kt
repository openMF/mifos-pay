/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.utils.fork.onClick
import mifos_pay.libs.mifos_loans.generated.resources.Res
import mifos_pay.libs.mifos_loans.generated.resources.feature_apply_loan_title
import mifos_pay.libs.mifos_loans.generated.resources.feature_loan_product_details_terms
import org.jetbrains.compose.resources.stringResource
/**
 * A bottom bar for the loan application screen, featuring a terms agreement
 * checkbox and a submission button.
 *
 * @param checked The current selection state of the terms agreement checkbox.
 * @param isEnabled Controls whether the 'Apply' button is clickable.
 * @param onCheckedChange Callback invoked when the terms checkbox is toggled.
 * @param onApplyClick Action to perform when the 'Apply' button is clicked.
 */
@Composable
fun ApplyLoanBottomBar(
    checked: Boolean,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onApplyClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onClick { onCheckedChange(!checked) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                modifier = Modifier.size(18.dp),
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = KptTheme.colorScheme.primary,
                ),
            )
            Text(
                text = stringResource(Res.string.feature_loan_product_details_terms),
                style = KptTheme.typography.labelMedium,
            )
        }

        MifosButton(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            onClick = onApplyClick,
            enabled = isEnabled,
            shape = KptTheme.shapes.medium,
        ) {
            Text(
                text = stringResource(Res.string.feature_apply_loan_title),
                style = KptTheme.typography.titleMedium,
            )
        }
    }
}
