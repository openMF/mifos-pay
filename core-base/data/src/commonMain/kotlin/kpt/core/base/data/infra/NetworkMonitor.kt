/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.data.infra

/**
 * Backward-compatible typealias — existing consumers keep their import.
 * Delegates to cmp-network-monitor's full-featured NetworkMonitor interface.
 */
typealias NetworkMonitor = io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
