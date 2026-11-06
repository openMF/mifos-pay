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

import com.mobilebytelabs.kmptoolkit.pdfgenerator.ExperimentalPdfGeneratorApi
import com.mobilebytelabs.kmptoolkit.pdfgenerator.PdfGenerator
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(ExperimentalPdfGeneratorApi::class)
actual val HistoryPdfModule: Module
    get() = module {
        single<PdfGenerator> { PdfGenerator() }
    }
