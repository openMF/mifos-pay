/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.annotation

/**
 * Marks a Room `@Entity` as a table of THIS app's database.
 *
 * `tools/database-ksp` collects every annotated class and emits the `@Database(entities = [...])`
 * literal, so a table is declared exactly once — on the entity itself. There is no YAML row to add
 * and no `AppDatabase` region to regenerate.
 *
 * ```kotlin
 * @DbEntity
 * @Entity(tableName = "banking_loans")
 * data class LoanEntity(...)
 * ```
 *
 * Room needs ONE compile-time `entities = [...]` array inside the `@Database` class, which is why
 * `AppDatabase` is GENERATED WHOLE rather than assembled from a separate bindings object the way
 * `GeneratedStoreBindings` is: an annotation argument cannot be read from a `val`.
 *
 * Deleting a package deletes its annotations, so a stripped demo simply generates a smaller
 * database — nothing to reset, and no way to leave a dangling reference to a table that is gone.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DbEntity

/**
 * Marks a Room `@Dao` as an accessor of THIS app's database.
 *
 * Emits BOTH the `abstract val` on the generated `AppDatabase` AND the Koin binding in
 * `GeneratedDaoBindings`. One annotation produces both because a DAO that exists on the database
 * but is never bound is invisible to injection, and a binding for an accessor that does not exist
 * does not compile — they cannot disagree when one declaration produces both.
 *
 * ```kotlin
 * @DbDao(accessor = "loanDao")
 * @Dao
 * interface LoanDao { ... }
 * ```
 *
 * @param accessor Property name on `AppDatabase`. Defaults to the interface name with a lowercase
 *                 first letter (`LoanDao` -> `loanDao`); set it only when that reads wrong.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DbDao(val accessor: String = "")

/**
 * Marks a class holding Room `@TypeConverter` functions.
 *
 * Collected into the generated `@ColumnTypeConverters(...)` annotation on `AppDatabase`. Same
 * declare-once contract as [DbEntity]: the converter class is the only place it is named.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DbConverters
