/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.wallet

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Freshness windows for mifos-pay's wallet stores. Moved out of the hand-written AppStoreRegistry at the
 * 90ff6da6 template sync — store-ksp now generates AppStoreRegistry from @StoreProvider, and a generated
 * object cannot carry fork constants.
 */
object WalletStoreTtl {
    /**
     * `history` LEDGER TTL — 2 minutes. Financial-read cadence: transactions
     * settle in seconds server-side, so a 2-minute band feels responsive
     * without hammering the API on rapid tab-switches. Consumed by
     * `store.asScreenStream(ttl = History TTL)` at the CACHE_FIRST_SWR band gate.
     */
    val HISTORY: Duration = 2.minutes

    /**
     * `accountDetail` SINGLE-ROW-PER-KEY TTL — 2 minutes. Same cadence as
     * [HISTORY] because the detail record derives from the same
     * `getSavingsWithAssociations` endpoint; keeping the two TTLs in lockstep
     * means a user pulling account-detail and history in the same session
     * sees a single coherent freshness band.
     */
    val ACCOUNT_DETAIL: Duration = 2.minutes

    /**
     * `beneficiary` LEDGER TTL — 15 minutes. Beneficiary lists change on
     * explicit user CRUD, not automatic server events, so a longer window
     * suppresses redundant refresh traffic on rapid transfer-flow navigation.
     * User-triggered writes (create/update/delete) still trigger a manual
     * refresh through the VM's `RefreshList` action.
     */
    val BENEFICIARY: Duration = 15.minutes

    /**
     * `savedCards` LEDGER TTL — 15 minutes. Same reasoning as [BENEFICIARY] —
     * saved-card lists change on explicit user CRUD, not server-side events,
     * so a longer band is appropriate.
     */
    val SAVED_CARDS: Duration = 15.minutes

    /**
     * `invoice` LEDGER TTL — 5 minutes. Invoices sit between transaction
     * cadence (fast — HISTORY 2m) and CRUD-only lists (slow — BENEFICIARY 15m).
     * A merchant may create an invoice at any point on the backend; a
     * moderate refresh cadence keeps the client list current without
     * hammering the datatable endpoint.
     */
    val INVOICE: Duration = 5.minutes

    /**
     * `notification` LEDGER TTL — 5 minutes. Notifications trickle in from
     * server events at unpredictable cadences; a moderate revalidation band
     * balances read freshness against endpoint load on a fast-tap
     * notification-bell open.
     */
    val NOTIFICATION: Duration = 5.minutes

    /**
     * `clientDetail` SINGLE-ROW-PER-KEY TTL — 15 minutes. Client profile
     * (`Client` payload — name/email/mobile/office/status/timeline) rarely
     * changes; a long band keeps the profile screen open-instantly and only
     * revalidates on explicit refresh or the 15-minute stale edge.
     */
    val CLIENT_DETAIL: Duration = 15.minutes

    /**
     * `pocket` LEDGER TTL — 5 minutes. Pocket balances shift on any
     * downstream loan/savings/share balance change (deposit, withdrawal,
     * market-price recalculation for shares). A 5-minute band matches
     * account-balance freshness expectations while cushioning the N+2
     * compound fetch (basic + client-accounts + N share market-price
     * round-trips) the fetcher incurs.
     */
    val POCKET: Duration = 5.minutes

    /**
     * `standingInstruction` LEDGER TTL — 10 minutes. Standing instructions
     * change on explicit user CRUD (create/update/delete) rather than
     * automatic server events, so a longer band than HISTORY is fine;
     * shorter than BENEFICIARY because instructions have a scheduled
     * transfer side-effect and a stale-list impression can be materially
     * misleading (a deleted instruction still showing while a scheduled
     * transfer no longer fires).
     */
    val STANDING_INSTRUCTION: Duration = 10.minutes

    /**
     * `offices` LEDGER TTL — 60 minutes. Fineract office data is
     * effectively static organizational reference data (the office tree
     * evolves at admin-review cadence, not user-session cadence). A long
     * band is appropriate; the FastMpayProcessor's lookup pattern is
     * `find { it.id == qrData.officeId }`, and the alternative to a stale
     * cache hit is the DEFAULT_OFFICE_NAME fallback — either way the QR
     * flow keeps moving.
     */
    val OFFICES: Duration = 60.minutes

    /**
     * `selfAccounts` SINGLE-ROW-PER-KEY-family LEDGER TTL — 2 minutes.
     * Same cadence as [HISTORY] because a user's own accounts share the
     * same balance-freshness expectations as their transaction feed; a
     * short revalidation window keeps the QR-generation picker showing
     * up-to-date balances after any transaction posts.
     */
    val SELF_ACCOUNTS: Duration = 2.minutes

    /**
     * `linkableAccounts` LEDGER TTL — 2 minutes. Same cadence as
     * [SELF_ACCOUNTS] because the linkable-accounts derivation source is
     * the SAME `clientsApi.getClientAccounts` endpoint (plus a per-SHARE
     * market-price round-trip and a snapshot-filter against the
     * `wallet_pockets` LEDGER). Keeping the two TTLs in lockstep means a
     * user opening the link-accounts sheet immediately after a balance
     * change on the dashboard sees matching balance figures.
     */
    val LINKABLE_ACCOUNTS: Duration = 2.minutes

    /**
     * `transferDetail` SINGLE-ROW-PER-KEY TTL — 2 minutes. Same cadence as
     * [ACCOUNT_DETAIL] because the transfer-detail record derives from the
     * same financial-read family as the account detail it drills into;
     * keeping the two TTLs in lockstep means a user opening a transaction
     * detail immediately after viewing the account sees a coherent freshness
     * band.
     */
    val TRANSFER_DETAIL: Duration = 2.minutes
}
