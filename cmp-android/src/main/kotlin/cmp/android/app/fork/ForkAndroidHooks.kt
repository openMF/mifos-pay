/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.android.app.fork

import android.app.Application

/**
 * The fork's Android-only startup work — FORK-OWNED (customization-surface.yaml). A template sync
 * seeds this file once and never touches it again.
 *
 * The template's `AndroidApp.onCreate` calls this after Koin is started, so an ads SDK, consent flow
 * or app-open-ad lifecycle is wired here instead of in template-owned `AndroidApp` / `MainActivity`.
 * Per-activity work: `registerActivityLifecycleCallbacks(...)` from here. The template does nothing.
 */
fun Application.onForkCreate() = Unit
