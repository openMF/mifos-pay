/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.config

/**
 * THE FORK'S domain-error copy. Neutral on the template — this is yours to fill.
 *
 * Implements the TEMPLATE-owned [ErrorMessageOverrides]; [rememberAppErrorMessageFor] consults it
 * FIRST and falls through to the framework's `ErrorCategory` branches when `message` returns null.
 * That ordering is the point: a fork adds its own exception types here without touching a
 * template-owned file, and still receives every upstream improvement to the framework branches.
 *
 * Override only what you handle:
 *
 * ```kotlin
 * object ForkErrorMapper : ErrorMessageOverrides {
 *     override fun message(error: Throwable): String? = when (error) {
 *         is InsufficientFundsException -> "Not enough balance for this transfer."
 *         is CardDeclinedException -> "That card was declined. Try another payment method."
 *         else -> null
 *     }
 * }
 * ```
 *
 * Return `null` for anything you do not handle; never a generic fallback string, or the framework's
 * categorised copy (network / auth / rate-limit / server) becomes unreachable.
 *
 * ## Why this is an implementation, not a function the template calls by name
 * The interface carries the DEFAULT. When the template adds a new hook it lands on
 * [ErrorMessageOverrides] with a default body, so this object keeps compiling across the sync that
 * introduces it. As a bare top-level function the template's new call site would reference something
 * this fork-owned file does not declare, and the sync that delivered the improvement would break the
 * build. Inheritance is what lets the template keep evolving its half unilaterally.
 *
 * ## Why there is no ProjectCacheKeys counterpart
 * Cache keys took a different shape: they are DECLARED on the store itself with `@CacheKey` and
 * generated into [AppCacheKeys], nested under that store's own object. A fork's own store therefore
 * already gets its keys with no seam needed. A hand-written `ProjectCacheKeys` would be a SECOND
 * place to put the same thing, and a key written there is invisible to the processor's duplicate
 * check — two streams could silently share a key, share a fetched-at stamp, and one screen would
 * stop refetching. Declare the key on its store instead.
 */
object ForkErrorMapper : ErrorMessageOverrides
