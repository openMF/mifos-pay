/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.annotation

/**
 * Resolves a `@StoreProvider` parameter from a NAMED Koin qualifier rather than by bare type.
 *
 * The store-side twin of data-ksp's `kpt.core.base.data.annotation.FromQualifier` (core/store does not
 * depend on core-base/data): store-ksp emits `get(named("<name>"))` for it. Chiefly for an API served by
 * several access points — `@ApiBinding("main", "selfservice")` binds the first point unqualified and each
 * further point under its id, so a store fetching from the self-service endpoint declares
 * `@FromQualifier("selfservice") api: ClientService`.
 *
 * @param name the qualifier of the binding to inject.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class FromQualifier(val name: String)
