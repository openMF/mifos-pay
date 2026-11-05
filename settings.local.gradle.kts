/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// settings.local.gradle.kts — the FORK-OWNED module-include seam (white-label, B1/T11).
//
// `settings.gradle.kts` (template-owned, synced) applies this file via `apply(from = ...)` if it exists,
// so a fork adds/removes its own Gradle module includes HERE without editing the template settings file —
// a template sync full-copies `settings.gradle.kts` while these includes survive. This is the
// settings-graph twin of `feature-deps.gradle.kts` (the dependency seam) and the `cmp.navigation.registry`
// runtime seams.
//
// Mifos Pay's feature-module includes moved here from `settings.gradle.kts` at the 90ff6da6 template
// sync (RULE-KMP-FORK-MODULE-SEAM-001: that file is template-owned and re-synced wholesale). Any NEW
// module a future contributor adds should go here instead, so it survives the next template sync.
//
// Example — add a consumer feature module:
//     include(":feature:my-feature")

// Fork module includes go below this line.
include(":feature:accounts")
include(":feature:auth")
include(":feature:autopay")
include(":feature:beneficiary")
include(":feature:editpassword")
include(":feature:faq")
include(":feature:fast-mpay")
include(":feature:finance")
include(":feature:history")
include(":feature:invoices")
include(":feature:kyc")
include(":feature:merchants")
include(":feature:mpay-qr-scan")
include(":feature:mpay-qr")
include(":feature:notification")
include(":feature:passcode")
include(":feature:payments")
include(":feature:pocket")
include(":feature:receipt")
include(":feature:savedcards")
include(":feature:send-money")
include(":feature:standing-instruction")
include(":feature:transfer-interbank")
include(":feature:transfer-intrabank")
include(":feature:upi-setup")
include(":libs:mifos-loans")
include(":libs")
