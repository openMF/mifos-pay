/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.mobilebytelabs.kmptoolkit.appintents.compose.LocalAppIntentsManager
import com.mobilebytelabs.kmptoolkit.bubble.Bubble
import com.mobilebytelabs.kmptoolkit.clipboard.ClipboardManager
import com.mobilebytelabs.kmptoolkit.pdfgenerator.PdfManager
import kpt.core.base.platform.context.AppContext
import kpt.core.base.platform.di.ToolkitQualifier
import kpt.core.base.platform.intent.IntentManager
import kpt.core.base.platform.review.AppReviewManager
import kpt.core.base.platform.share.ShareManager
import kpt.core.base.platform.update.AppUpdateManager
import kpt.core.base.platform.url.UrlLauncher
import org.koin.compose.koinInject
import com.mobilebytelabs.kmptoolkit.intentlauncher.IntentManager as ToolkitIntentManager
import com.mobilebytelabs.kmptoolkit.intentlauncher.compose.LocalIntentManager as ToolkitLocalIntentManager
import com.mobilebytelabs.kmptoolkit.share.ShareManager as ToolkitShareManager
import com.mobilebytelabs.kmptoolkit.share.compose.LocalShareManager as ToolkitLocalShareManager

/**
 * Provides every platform manager to composition.
 *
 * This was an `expect fun` with androidMain and nonAndroidMain actuals. The two bodies differed in
 * exactly one line — Android constructed `AppReviewManagerImpl(activity)` because Play Core's
 * review flow needed an `Activity`. `cmp-app-review` removed that requirement, so the bodies became
 * identical and the split had nothing left to express.
 *
 * Every manager is resolved from `platformModule` rather than constructed here: constructing again
 * would hand composition a different instance from the one a ViewModel injects. That matters most
 * for the toast queue, where a second `ToastHostState` would mean the host renders nothing a
 * ViewModel raised.
 *
 * Place this once, near the root of the UI — `cmp-shared`'s `SharedApp` is where the template does
 * it. Everything below can then read any manager with `LocalShareManager.current` and friends.
 *
 * @param context retained for call-site compatibility with forks. Nothing here needs it now that no
 *   manager is Activity-bound; `AppContext` itself remains the expect/actual that backs
 *   [kpt.core.base.platform.context.LocalContext].
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun LocalManagerProvider(
    context: AppContext,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppReviewManager provides koinInject(),
        LocalIntentManager provides koinInject(),
        LocalUrlLauncher provides koinInject(),
        LocalShareManager provides koinInject(),
        LocalAppUpdateManager provides koinInject(),
        LocalClipboardManager provides koinInject(),
        LocalBubbleManager provides koinInject(),
        LocalPdfManager provides koinInject(),
        // The toolkit's own locals, provided from the SAME graph. They default to constructing a
        // separate impl rather than throwing, so leaving them unprovided would make a wrong import
        // silently use a second object instead of failing. cmp-share-compose's composables
        // (ShareButton, ShareSheet) call the global `Share` engine directly and do not need this —
        // it exists so `rememberShareManager()` and `rememberAppIntentsManager()` resolve to the
        // app's managers. Qualified: see ToolkitQualifier in PlatformModule.
        ToolkitLocalShareManager provides koinInject<ToolkitShareManager>(ToolkitQualifier),
        ToolkitLocalIntentManager provides koinInject<ToolkitIntentManager>(ToolkitQualifier),
        LocalAppIntentsManager provides koinInject(),
    ) {
        content()
    }
}

/*
 * Name collision with the toolkit's -compose modules — read before adding an import.
 *
 * cmp-share-compose, cmp-intent-launcher-compose and cmp-app-intents-compose each export their own
 * `LocalShareManager` / `LocalIntentManager` / `LocalAppIntentsManager`, the same simple names used
 * here.
 *
 * None of them throws when unprovided. Each defaults to `staticCompositionLocalOf { SomeImpl() }`,
 * so a wrong import compiles, runs, and quietly uses a SECOND manager — no error to notice. That is
 * why `LocalManagerProvider` above provides all three too, from the same Koin graph: whichever name
 * is imported now resolves to the app's instance.
 *
 * Share and intent-launcher are the real duplicates, because the template's managers are wrappers —
 * `ShareManager` adds MimeType handling and ImageBitmap encoding; `IntentManager` exposes only the
 * SystemIntents-backed operations (openAppSettings, createDocument) that need no Activity. The
 * toolkit's own types carry the full operation sets. Both sides delegate to the same engines, so
 * they cannot disagree; pick the template's for the extras, the toolkit's for the full surface.
 *
 * App-intents had no such difference — both names meant the identical type — so the template's
 * duplicate was removed and the toolkit's is the single one.
 *
 * ## Android + intent pickers
 * The toolkit's `IntentManager` bound in `platformModule` has no `IntentLauncher` on Android,
 * because one is Activity-scoped. That is documented, honest degradation: the pickers report
 * unsupported and `rememberIntentCapabilities()` says so, while openAppSettings/createDocument still
 * work. A fork that wants pickers builds one from its Activity and overrides the local:
 *
 * ```kotlin
 * // inside setContent { } of a ComponentActivity
 * ProvideIntentManager(rememberIntentManagerFromLauncher()) { App() }
 * ```
 */
/**
 * Builds a manager [ProvidableCompositionLocal] that throws, rather than defaulting, when read
 * without a provider above it.
 *
 * **Why throw.** There is no sensible fallback for any of these. A no-op default would let a share,
 * a review prompt or a PDF export silently do nothing — the exact failure mode this module just
 * removed from `AppReviewManager`, whose non-Android implementation was an empty method body. A
 * missing value here always means a wiring mistake, and it should be loud at the first read.
 *
 * **Why a factory.** The nine messages used to be written by hand and had drifted: two named the
 * CompositionLocal (`LocalIntentManager`), three named the type (`AppReviewManager`). Building them
 * from one place makes the wording structural instead of a convention each new local has to
 * remember, and the message can then afford to say how to fix the problem.
 *
 * @param name the local's own identifier — the thing you would grep for after seeing the exception,
 *   which is why the message names it rather than the manager type.
 */
private fun <T> managerCompositionLocal(name: String): ProvidableCompositionLocal<T> =
    compositionLocalOf {
        error(
            "CompositionLocal $name not present. It is provided by LocalManagerProvider, so this " +
                "composable is outside that subtree — wrap it, or in a preview or test supply one " +
                "directly: CompositionLocalProvider($name provides <impl>) { … }",
        )
    }

/** Prompts for an app-store review. Provided by [LocalManagerProvider]. */
val LocalAppReviewManager: ProvidableCompositionLocal<AppReviewManager> =
    managerCompositionLocal("LocalAppReviewManager")

/** Launches platform intents — view, pick, open settings. Provided by [LocalManagerProvider]. */
val LocalIntentManager: ProvidableCompositionLocal<IntentManager> =
    managerCompositionLocal("LocalIntentManager")

/** Opens URLs, email, maps, phone and SMS. Provided by [LocalManagerProvider]. */
val LocalUrlLauncher: ProvidableCompositionLocal<UrlLauncher> =
    managerCompositionLocal("LocalUrlLauncher")

/** Shares text, URLs, files and images to other apps. Provided by [LocalManagerProvider]. */
val LocalShareManager: ProvidableCompositionLocal<ShareManager> =
    managerCompositionLocal("LocalShareManager")

/** Checks for and starts app updates. Provided by [LocalManagerProvider]. */
val LocalAppUpdateManager: ProvidableCompositionLocal<AppUpdateManager> =
    managerCompositionLocal("LocalAppUpdateManager")

/**
 * Reads and writes the system clipboard, plus history, change observation and URL detection.
 *
 * Typed as the toolkit's `ClipboardManager` rather than a template wrapper — see the note in
 * `platformModule`. The same instance is bound under `Clipboard` for code that wants the
 * substitutable interface instead. Provided by [LocalManagerProvider].
 */
val LocalClipboardManager: ProvidableCompositionLocal<ClipboardManager> =
    managerCompositionLocal("LocalClipboardManager")

/** Shows floating bubbles, overlays and heads-up UI. Provided by [LocalManagerProvider]. */
val LocalBubbleManager: ProvidableCompositionLocal<Bubble> =
    managerCompositionLocal("LocalBubbleManager")

/** Generates PDFs — statements, receipts, invoices. Provided by [LocalManagerProvider]. */
val LocalPdfManager: ProvidableCompositionLocal<PdfManager> =
    managerCompositionLocal("LocalPdfManager")
