/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.di

import org.koin.dsl.module

/**
 * The backbone home module — [kpt.feature.home.HomeScreen] carries zero demo imports and takes
 * no ViewModel of its own (the `homeBody` seam is supplied by `cmp-navigation`'s
 * `ForkBackboneRegistry.homeBody`, whose own definitions live wherever that body's real
 * implementation is installed). Empty on purpose.
 */
val HomeModule = module {}
