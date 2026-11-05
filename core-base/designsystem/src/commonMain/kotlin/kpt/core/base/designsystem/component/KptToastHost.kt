/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.mobilebytelabs.kmptoolkit.toast.DefaultToast
import com.mobilebytelabs.kmptoolkit.toast.ToastData
import com.mobilebytelabs.kmptoolkit.toast.ToastHost
import com.mobilebytelabs.kmptoolkit.toast.ToastHostState

/**
 * The app's transient-message host. Place once near the root of the UI.
 *
 * Replaces `KptSnackbarHost`, which wrapped Material3's `SnackbarHost`. Two reasons it went:
 *
 *  1. **Showing a message required composition.** `showKptSnackbar` was an extension on
 *     `SnackbarHostState`, so a ViewModel could not raise one without holding Compose state. The
 *     `ToastDispatcher` behind this host is an ordinary interface bound in `platformModule`, so a
 *     ViewModel injects it and `FakeToastDispatcher` makes the message assertable in a unit test.
 *  2. **`SnackbarConfiguration.onActionClick` was never wired.** `showKptSnackbar` passed
 *     `message`, `actionLabel`, `duration` and `withDismissAction` to `showSnackbar` and dropped
 *     the callback, so an action button rendered and then did nothing when tapped. Nothing in the
 *     template called it, so the defect never surfaced.
 *
 * Obtain [hostState] from Koin (`koinInject<ToastHostState>()`) so the instance rendering here is
 * the same one injected as `ToastDispatcher` elsewhere — `toastModule` binds one object under both
 * types precisely so the queue cannot split.
 */
@Composable
fun KptToastHost(
    hostState: ToastHostState,
    modifier: Modifier = Modifier,
    toast: @Composable (ToastData) -> Unit = { DefaultToast(it) },
) {
    ToastHost(
        hostState = hostState,
        modifier = modifier.testTag("KptToastHost"),
        toast = toast,
    )
}
