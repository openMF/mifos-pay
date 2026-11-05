/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.ui.screen

import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import kpt.core.base.ui.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Convenience suspend loaders for the bundled default Lottie animations that ship with
 * `core-base/ui`. Consumer apps can opt into them with no setup:
 *
 * ```kotlin
 * ScreenStateEmpty(
 *     visual = ScreenStateVisual.Lottie(spec = DefaultLottieAnimations.empty),
 * )
 * ```
 *
 * Each loader reads a JSON Lottie file from this module's `composeResources/files/screenstate/`
 * directory at runtime. To override per call site, supply a custom suspend lambda pointing
 * at any composeResources file (your app's, or a remote URL via `compottie-network`):
 *
 * ```kotlin
 * ScreenStateVisual.Lottie(spec = {
 *     LottieCompositionSpec.JsonString(MyAppRes.readBytes("anim.json").decodeToString())
 * })
 * ```
 *
 * Asset provenance: see `core-base/ui/src/commonMain/composeResources/files/screenstate/README.md`.
 */
object DefaultLottieAnimations {
    /** Animation for the empty state. */
    val empty: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/empty.json") }

    /** Animation for the error state. */
    val error: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/error.json") }

    /** Animation for the no-network state. */
    val noNetwork: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/no_network.json") }

    /** Animation for the loading state. */
    val loading: suspend () -> LottieCompositionSpec = { loadJson("files/screenstate/loading.json") }
}

@OptIn(ExperimentalResourceApi::class)
private suspend fun loadJson(path: String): LottieCompositionSpec =
    LottieCompositionSpec.JsonString(Res.readBytes(path).decodeToString())
