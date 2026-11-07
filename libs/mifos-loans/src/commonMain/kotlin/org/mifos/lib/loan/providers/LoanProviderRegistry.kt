/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package org.mifos.lib.loan.providers

import org.mifos.lib.loan.core.LoanProvider

/**
 * Resolves a [LoanProvider] adapter (Fineract or a third-party bank integration) by the
 * `providerId` chosen on the Select Loan Provider screen (e.g. `"hdfc"`, `"sbi"`, `"icici"`,
 * `"axis"`).
 *
 * There is no real per-bank backend distinction yet, so [org.mifos.lib.loan.di.LoanApplicationModule]
 * currently maps all four ids to the same [DummyLoanProvider] instance in [providersById] — this
 * class only owns the *resolution* mechanism, not the decision of which concrete adapter backs
 * which id. Any entry in [providersById] can be swapped for a distinct adapter later without
 * touching call sites that go through [resolve].
 *
 * @param providersById Concrete [LoanProvider] adapters, keyed by provider id.
 * @param defaultProvider Fallback used when [resolve] is called with an id absent from
 * [providersById] (defensive — the Select Loan Provider screen only ever offers ids that are
 * present in the map).
 */
class LoanProviderRegistry(
    private val providersById: Map<String, LoanProvider>,
    private val defaultProvider: LoanProvider,
) {
    /** Returns the adapter registered for [providerId], or the default provider if none is registered. */
    fun resolve(providerId: String): LoanProvider = providersById[providerId] ?: defaultProvider
}
