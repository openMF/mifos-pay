/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

/**
 * Money tone — how a monetary amount should be colored regardless of the raw value's sign.
 *
 * Most call sites should use [MoneyTone.AutoFromSign] and pass the raw `amount` parameter;
 * [MoneyText] picks moneyPositive / moneyNegative / moneyNeutral from the finance palette
 * based on the sign.
 *
 * Use [MoneyTone.Positive] / [MoneyTone.Negative] / [MoneyTone.Neutral] to force a specific
 * tone (e.g. payment confirmation always green even though the user "paid" / amount is
 * conceptually negative).
 *
 * Use [MoneyTone.Inherit] when you want the surrounding `LocalContentColor` (e.g. inside a
 * gradient hero where black text would clash).
 */
enum class MoneyTone {
    /**
     * Pick positive or negative from the value's own sign. The default, so a caller
     * cannot colour a loss green by mistake.
     */
    AutoFromSign,

    /** Force the gain colour, e.g. for a total the sign does not describe. */
    Positive,

    /** Force the loss colour. */
    Negative,

    /** The muted figure colour — a value with no gain/loss meaning. */
    Neutral,

    /** Take the surrounding text colour, leaving the figure unstyled. */
    Inherit,
}
