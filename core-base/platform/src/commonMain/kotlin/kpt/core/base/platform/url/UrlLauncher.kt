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

/**
 * Opens a URL in whatever the platform considers the right handler.
 *
 * ## Why this is not part of `IntentManager` or `ShareManager`
 * OPENING a URL and SHARING one are different acts with different outcomes: opening hands the user
 * to a browser or a deep-linked app, sharing raises a chooser so they can send it elsewhere. They
 * used to sit on one interface, where `launchUri` was implemented with `Share.url(...)` — so
 * "launch this URI" actually raised a share sheet. Separating the interfaces makes that class of
 * mistake unrepresentable rather than merely fixed once.
 *
 * Every method is synchronous: the platform call returns as soon as the handler is dispatched, and
 * a suspend signature would imply this waits for the user, which it does not.
 *
 * ## Why the methods are `launch*`, not `open*`
 * [UrlLauncherImpl] delegates to `com.mobilebytelabs.kmptoolkit.openurl`'s top-level `openUrl` /
 * `openInBrowser` / `canOpen`. In Kotlin a **member function shadows an imported top-level function
 * of the same name**, so when these methods were named `open`/`openInBrowser`/`canOpen` the
 * implementation `override fun openInBrowser(url) = openInBrowser(url)` resolved to ITSELF —
 * infinite recursion that compiles cleanly, reviews cleanly, and dies at runtime with
 * `java.lang.StackOverflowError`.
 *
 * It shipped. A downstream fork hit it on the first tap of a legal link on its sign-in screen
 * (2026-10-03); `canOpen` carried the same latent defect, unnoticed only because nothing called it.
 * `open` survived by coincidence — the function it delegates to happens to be named `openUrl`.
 *
 * Naming the members `launch*` — a vocabulary the delegate does not use — makes the collision
 * **impossible by construction**, rather than avoided by an import alias plus a comment asking the
 * next reader not to "simplify" it away. The type is a `UrlLauncher`; `launch` is also simply the
 * better verb for it.
 */
interface UrlLauncher {

    /** Launch [url] with the platform's default handler. Returns false if nothing could handle it. */
    fun launch(url: String): Boolean

    /** Launch [url] in a browser specifically, bypassing any app that claims the link. */
    fun launchInBrowser(url: String): Boolean

    /**
     * Show [url] in a browser INSIDE the app — Custom Tabs on Android, SFSafariViewController on iOS, the system
     * browser where the platform has no in-app one. For pages the user reads and returns from: a privacy
     * policy, terms, help.
     */
    fun launchInApp(url: String): Boolean

    /** Whether [url] has a handler — check before offering the action, not after it fails. */
    fun canLaunch(url: String): Boolean
}
