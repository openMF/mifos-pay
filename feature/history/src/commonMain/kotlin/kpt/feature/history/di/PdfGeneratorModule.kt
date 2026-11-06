/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.history.di

import org.koin.core.module.Module

/**
 * Provides a platform-appropriate `PdfGenerator` singleton (e.g. bound to an
 * Android `Context` via `createPdfGenerator(context)` on Android).
 */
expect val HistoryPdfModule: Module
