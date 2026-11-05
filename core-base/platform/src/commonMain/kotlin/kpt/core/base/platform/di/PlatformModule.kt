/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.di

import com.mobilebytelabs.kmptoolkit.appintents.di.appIntentsModule
import com.mobilebytelabs.kmptoolkit.bubble.di.bubbleModule
import com.mobilebytelabs.kmptoolkit.clipboard.di.clipboardModule
import com.mobilebytelabs.kmptoolkit.pdfgenerator.di.pdfModule
import com.mobilebytelabs.kmptoolkit.toast.di.toastModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kpt.core.base.platform.garbage.GarbageCollectionManager
import kpt.core.base.platform.garbage.GarbageCollectionManagerImpl
import kpt.core.base.platform.intent.IntentManager
import kpt.core.base.platform.intent.IntentManagerImpl
import kpt.core.base.platform.review.AppReviewManager
import kpt.core.base.platform.review.AppReviewManagerImpl
import kpt.core.base.platform.share.ShareManager
import kpt.core.base.platform.share.ShareManagerImpl
import kpt.core.base.platform.update.AppUpdateManager
import kpt.core.base.platform.update.AppUpdateManagerImpl
import kpt.core.base.platform.url.UrlLauncher
import kpt.core.base.platform.url.UrlLauncherImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.mobilebytelabs.kmptoolkit.intentlauncher.IntentManager as ToolkitIntentManager
import com.mobilebytelabs.kmptoolkit.intentlauncher.IntentManagerImpl as ToolkitIntentManagerImpl
import com.mobilebytelabs.kmptoolkit.share.ShareManager as ToolkitShareManager
import com.mobilebytelabs.kmptoolkit.share.ShareManagerImpl as ToolkitShareManagerImpl

/**
 * Qualifier for the toolkit bindings whose simple names collide with the template's own
 * (`ShareManager`, `IntentManager`). Inject them with `koinInject<…>(ToolkitQualifier)`.
 */
val ToolkitQualifier = named("kmptoolkit")

/**
 * Koin module binding every `core-base/platform` capability — toast, share, URL launch, app update,
 * garbage-collection hint — plus the per-target bindings from each `actual`.
 *
 * Include it once from the app graph. Features resolve these interfaces rather than reaching for a
 * platform API directly, which is what keeps a feature module compilable on every target.
 */
val platformModule = module {
    // cmp-toast's own module, included rather than re-declared: it binds ONE ToastHostState and
    // exposes that SAME instance as ToastDispatcher. Two `single { }` declarations would build
    // separate objects and split the queue, so the host would render nothing a ViewModel raised.
    includes(toastModule)

    single<CoroutineDispatcher> { Dispatchers.Unconfined }
    single<GarbageCollectionManager> { GarbageCollectionManagerImpl(get()) }

    // The three platform-capability managers. Bound here as well as provided through the
    // CompositionLocals in LocalManagerProviders, so a ViewModel or repository can inject one
    // without reaching into composition. All three are stateless — the per-target behaviour lives
    // in the toolkit engines they delegate to — so `single` is safe.
    single<UrlLauncher> { UrlLauncherImpl() }
    single<ShareManager> { ShareManagerImpl() }
    single<IntentManager> { IntentManagerImpl() }

    // Bindable as a single since cmp-in-app-update replaced the Play Core impl: the engine
    // resolves the target itself, so there is no Activity to hold and nothing per-platform
    // to construct.
    single<AppUpdateManager> { AppUpdateManagerImpl() }

    // AppReviewManager moved here once cmp-app-review replaced the Play-Core-plus-no-op pair —
    // it no longer takes an Activity, so it is a single like the rest instead of being
    // constructed inside composition.
    //
    // Deliberately NOT `includes(appReviewModule())`, unlike the four below. That function's real
    // job is `AppReview.configure(listing)`, and calling it with the default `StoreListing.None`
    // would RESET a listing a fork had already configured at startup. The template type here wraps
    // the global `AppReview` object directly, so nothing needs the toolkit's binding. A fork that
    // wants the toolkit module should pass its own listing: `includes(appReviewModule(listing))`.
    single<AppReviewManager> { AppReviewManagerImpl() }

    // Capabilities added with the 3.5.28 toolkit bump. Each library ships its OWN Koin module, so
    // they are INCLUDED rather than re-declared here — the toolkit owns what its bindings are, and
    // hand-rolling them is the same duplication-that-drifts problem as wrapping the libraries.
    //
    // Two of these were re-derived by hand in the first draft and got it wrong:
    //   • clipboardModule already binds ClipboardManager AND the same instance as `Clipboard` —
    //     the rich reactive members (history, changes, urlDetections) live only on the final class,
    //     while FakeClipboard implements the interface, so both are needed and must share one
    //     object. Re-declaring that by hand reproduced the logic; including it reuses it.
    //   • bubbleModule binds BubblePermission alongside Bubble. The hand-rolled version bound only
    //     Bubble, silently dropping the runtime permission gate a fork needs to request overlay
    //     access at all.
    //
    // No wrapper interfaces: each library ships a public Fake* in its main artifact
    // (FakeClipboard, FakeAppIntentsManager, FakeBubble, FakePdfManager, FakeAppReviewManager), so
    // substitution in tests never needed a template-owned type, and insulation was already gone
    // once these types appeared in this module's public API.
    // The TOOLKIT's ShareManager and IntentManager, bound under [ToolkitQualifier] instead of
    // via `includes(shareModule)` / `includes(intentLauncherModule)`. Koin keys a binding by the
    // class's simple name on JS and Wasm, and the template has its own `ShareManager` /
    // `ShareManagerImpl` / `IntentManager` — so unqualified, each pair shares one key and the later
    // binding replaces the earlier: Wasm crashes at startup ("Cannot cast IntentManagerImpl"), JS
    // silently hands the template's local the toolkit object. The modules each held one binding,
    // re-declared here as they are (no-arg constructors, the toolkit's per-platform defaults).
    //
    // The template's ShareManager (a wrapper adding MimeType + ImageBitmap encoding) stays the one
    // this module's own local exposes; the toolkit's exists so its LocalShareManager is provided from
    // DI rather than defaulting to a second instance. Both delegate to the same global `Share` engine.
    // On Android the toolkit IntentManager has no IntentLauncher (one is Activity-scoped), so its
    // pickers report unsupported — the library's documented degradation. A fork wanting pickers calls
    // `rememberIntentManagerFromLauncher()` in its Activity and wraps the app in
    // `ProvideIntentManager(manager)`, which overrides the local.
    single<ToolkitShareManager>(ToolkitQualifier) { ToolkitShareManagerImpl() }
    single<ToolkitIntentManager>(ToolkitQualifier) { ToolkitIntentManagerImpl() }
    includes(clipboardModule())
    includes(bubbleModule())
    includes(appIntentsModule)
    includes(pdfModule)
}
