/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.url

import kotlin.test.Test

/**
 * Locks [UrlLauncherImpl] against self-recursion — it must DELEGATE to the kmptoolkit `openurl`
 * functions, never call itself.
 *
 * ## The bug this exists for
 * [UrlLauncher]'s methods were once named `open` / `openInBrowser` / `canOpen`, matching the
 * top-level functions the implementation imports. In Kotlin a **member wins over an imported
 * top-level function**, so `override fun openInBrowser(url) = openInBrowser(url)` resolved to the
 * override and recursed until the stack died:
 * `java.lang.StackOverflowError: stack size 8188KB`, the frame
 * `kpt.core.base.platform.url.UrlLauncherImpl.openInBrowser` repeated to the limit.
 *
 * It compiled, reviewed and shipped. A downstream fork hit it on the first tap of a legal link on
 * its sign-in screen (2026-10-03) — this module's first real consumer. `canOpen` carried the
 * identical defect, unnoticed only because nothing called it; `open` survived by coincidence,
 * because the function it delegates to is named `openUrl`.
 *
 * The fix renamed the interface to a `launch*` vocabulary the delegate does not use, so the
 * shadowing is impossible by construction rather than avoided by an import alias plus a comment.
 * This test is the executable half: it fails if anyone renames the members back into collision.
 *
 * ## Why only [UrlLauncher.canLaunch] is called
 * It is the only one of the three with NO side effect — it asks whether a handler exists rather
 * than launching a browser, which a unit test must not do. It carried the same defect, so it proves
 * the class. The assertion is that the call TERMINATES: a self-recursive implementation can never
 * reach a return value. The returned boolean is deliberately not asserted — whether a handler
 * exists is host-dependent, and pinning it would make this a flaky environment probe rather than a
 * recursion guard.
 */
class UrlLauncherDelegationTest {

    @Test
    fun canLaunchDelegatesInsteadOfRecursingIntoItself() {
        UrlLauncherImpl().canLaunch("https://example.com/terms/")
    }
}
