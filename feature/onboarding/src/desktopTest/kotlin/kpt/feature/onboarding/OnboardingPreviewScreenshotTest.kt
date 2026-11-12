/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.onboarding

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import kotlin.test.assertTrue

/**
 * Device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY). Auto-discovers every commonMain
 * `@Preview` in this feature via `CommonComposablePreviewScanner`, renders each off `desktopTest`
 * (JVM — no emulator, no Robolectric) and captures via roborazzi-compose-desktop. Drives
 * `recordRoborazziDesktop` / `verifyRoborazziDesktop`.
 *
 * One golden per onboarding page and per privacy-consent state, so a layout regression
 * on any of them shows in the diff rather than on a device.
 *
 * Mirrors the template's `SettingsPreviewScreenshotTest`; the harness itself comes from
 * `CMPFeatureConventionPlugin`, so there is no per-feature wiring.
 */
@OptIn(ExperimentalTestApi::class)
class OnboardingPreviewScreenshotTest {

    @Test
    fun captureAllPreviews() {
        val previews = CommonComposablePreviewScanner()
            .scanPackageTrees("kpt.feature.onboarding")
            .getPreviews()

        // A scan that finds NOTHING must FAIL, not pass. `previews.forEach { }` over an empty list
        // captures nothing and reports success — which is exactly what happened here when the
        // previews were declared `private` (the scanner only sees internal/public). The golden
        // directory stayed empty and the build went green, so the render tier was silently off.
        assertTrue(
            previews.isNotEmpty(),
            "no @Preview found — the render tier is silently disabled. Previews must be `internal` " +
                "or public; `private` ones are invisible to CommonComposablePreviewScanner.",
        )

        // Names must be unique BEFORE anything is written. Two previews resolving to one file would
        // otherwise have the second silently overwrite the first: the golden count stays plausible,
        // `verifyRoborazziDesktop` stays green (it re-renders the same winner), and one preview is
        // simply no longer covered. Fail loudly and make the author rename.
        val names = previews.map { it.methodName }
        val duplicates = names.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue(
            duplicates.isEmpty(),
            "Preview function names must be unique across this feature — goldens are keyed by name. " +
                "Duplicated: $duplicates",
        )

        previews.forEach { preview ->
            runDesktopComposeUiTest {
                setContent { preview() }
                // Golden under src/desktopTest/resources (committed) so CI `verifyRoborazziDesktop`
                // has a baseline to compare a fresh render against — not a throwaway build/ dir.
                //
                // Keyed by the preview FUNCTION NAME, not its scan index: a rename renames one
                // file and a new preview adds one file, instead of shifting every later golden.
                // A preview that opens a dialog has a SECOND root (the dialog's window), so `onRoot()`
                // would fail. Capture each root: the screen as the plain name, every dialog beside it.
                val roots = onAllNodes(isRoot())
                val count = roots.fetchSemanticsNodes().size
                for (i in 0 until count) {
                    val suffix = if (i == 0) "" else "-$i"
                    roots[i].captureRoboImage(
                        "src/desktopTest/resources/screenshots/onboarding/${preview.methodName}$suffix.png",
                    )
                }
            }
        }
    }
}
