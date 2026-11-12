/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.data.annotation

import kotlin.reflect.KClass

/**
 * Marks a repository implementation so its Koin binding is DERIVED, not hand-written.
 *
 * `tools/data-ksp` emits `single<Iface> { Impl(param = get(), …) }` into
 * `di/GeneratedRepositoryBindings`, reading the constructor the same way `@StoreProvider` reads a
 * provider function: every parameter is resolved by TYPE, except a store, which needs the qualifier
 * its [FromStore] carries.
 *
 * ```kotlin
 * @RepositoryBinding(binds = LoanRepository::class)
 * internal class LoanRepositoryImpl(
 *     @FromStore("loans") private val loansStore: Store<Unit, List<Loan>>,
 *     @FromStore("loansMutable") private val loansWriteStore: MutableStore<String, Loan>,
 *     private val loanDao: LoanDao,
 * ) : LoanRepository
 * ```
 *
 * A parameter with a DEFAULT is omitted from the generated call — `clock: Clock = Clock.System` is a
 * seam for tests, not something Koin should resolve, and asking the graph for a `Clock` that nothing
 * binds would fail at construction.
 *
 * Deleting a package deletes its annotations, so a stripped demo simply generates fewer bindings —
 * there is no committed module left holding a `single` for an implementation that no longer exists.
 *
 * @param binds the interface to bind the implementation as.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RepositoryBinding(val binds: KClass<*>)

/**
 * Resolves this parameter from the store registry rather than by bare type.
 *
 * Store5 stores are bound BY QUALIFIER (`AppStoreRegistry.Loans`) because their erased type
 * `Store<*, *>` is shared by every store in the graph — a bare `get()` would resolve an arbitrary
 * one. The [id] is the store's `@StoreProvider(id = …)`, and the generated code maps it to that
 * store's registry member, so the two declarations cannot drift: a typo names a qualifier the
 * registry does not expose, which is a compile error in generated code rather than the wrong store
 * silently injected at runtime.
 *
 * @param id the `@StoreProvider` id of the store to inject.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class FromStore(val id: String)

/**
 * Marks a factory function whose Koin `single` is DERIVED from its signature.
 *
 * The data layer's twin of `@StoreProvider`, and it exists for the same reason: a binding whose
 * VALUE needs a lambda — an outbox serializer, a syncer's `submitBlock`, a bookkeeper's
 * `keySerializer` — cannot be described by an annotation without embedding code in it. So it is not
 * described. The lambda stays in an ordinary function body where it is readable and type-checked,
 * and only the WIRING is derived: return type becomes the bound type, parameters become `get()`.
 *
 * ```kotlin
 * @DataProvider(qualifier = "outbox.loan")
 * fun provideLoanOutbox(dao: DraftDao): SubmitOutbox<Loan> =
 *     RoomSubmitOutbox(dao = dao, serializer = Loan.serializer())
 * ```
 *
 * A NULLABLE parameter resolves with `getOrNull()` — optional collaborators (a crash reporter that
 * a fork may not install) must not fail graph construction.
 *
 * @param qualifier binds under `named(qualifier)` when non-blank. Required when several bindings
 *                  share an erased type: Koin matches `single<SubmitOutbox<*>>` definitions by raw
 *                  class, not full `KType`, so four outboxes without qualifiers collapse to
 *                  whichever was registered last — silently, and only for the losers.
 * @param createdAtStart eager construction. A syncer that is only built when first injected never
 *                       starts watching for reconnects, so the backlog it exists to drain is never
 *                       drained.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class DataProvider(
    val qualifier: String = "",
    val createdAtStart: Boolean = false,
)

/**
 * Resolves this parameter from a NAMED qualifier rather than by bare type.
 *
 * The counterpart to [FromStore] for bindings the data layer itself qualifies — chiefly a
 * `SubmitOutbox<T>`, whose erased type is shared by every outbox in the graph.
 *
 * @param name the [DataProvider.qualifier] of the binding to inject.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class FromQualifier(val name: String)
