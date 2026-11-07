# `core-base/common`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_COMMON.md`
> **Measured:** 10 Kotlin files, 0 test files

**Defines annotations:** `@IgnoredOnParcel`, `@Parcelize`, `@TypeParceler`

## Principal types

`DispatcherManager`, `DispatcherManagerImpl`, `IgnoredOnParcel`, `Parcel`, `Parcelable`, `Parceler`, `Parcelize`, `TypeParceler`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/common sha=b653a78d80ff4c798605036bc461dbe456574d5f -->
## API reference

_Generated from `core-base/common` at tree `b653a78d80ff` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/ImageExtension.kt`

```kotlin
fun ByteArray.toBase64(): String
```
Extension function to convert ByteArray to Base64 string

```kotlin
fun ByteArray.toBase64DataUri(mimeType: String = "application/octet-stream"): String
```
Extension function to convert ByteArray to Base64 string with data URI prefix

```kotlin
fun String.fromBase64(): ByteArray
```
Extension function to convert Base64 string to ByteArray

```kotlin
fun String.fromBase64OrNull(): ByteArray?
```
Extension function to safely convert Base64 string to ByteArray

```kotlin
fun String.fromBase64DataUri(): ByteArray
```
Extension function to convert Base64 data URI to ByteArray Handles data URIs in format: "data:mime/type;base64,actualBase64Data"

```kotlin
fun String.fromBase64DataUriOrNull(): ByteArray?
```
Extension function to safely convert Base64 data URI to ByteArray

```kotlin
fun String.extractMimeTypeFromDataUri(): String?
```
Extension function to extract MIME type from Base64 data URI

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/Parcelize.kt`

```kotlin
expect annotation class Parcelize()
```
Marks a class as parcelable so it can cross an Android process/configuration boundary.

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/Parcelize.kt:19</code></summary>

```kotlin
import kotlinx.parcelize.TypeParceler

actual typealias Parcelize = Parcelize

actual typealias Parcelable = Parcelable

actual typealias IgnoredOnParcel = IgnoredOnParcel
```

</details>

```kotlin
expect interface Parcelable
```
The platform's parcelable contract — `android.os.Parcelable` on Android, an empty marker elsewhere. Implement it on a shared model together with `Parcelize`; do not hand-write the read/write pair.

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/Parcelize.kt:21</code></summary>

```kotlin
actual typealias Parcelize = Parcelize

actual typealias Parcelable = Parcelable

actual typealias IgnoredOnParcel = IgnoredOnParcel

actual typealias Parceler<P> = Parceler<P>
```

</details>

```kotlin
expect annotation class IgnoredOnParcel()
```
Excludes one property from parcelling, for values that are derived or cannot cross a process boundary (a lambda, a coroutine scope, a cached bitmap). The property must have a default, since it is reconstructed rather than restored.

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/Parcelize.kt:23</code></summary>

```kotlin
actual typealias Parcelable = Parcelable

actual typealias IgnoredOnParcel = IgnoredOnParcel

actual typealias Parceler<P> = Parceler<P>

actual typealias TypeParceler<T, P> = TypeParceler<T, P>
```

</details>

```kotlin
expect interface Parceler<P>
```
Custom parcelling for a type the platform cannot serialise on its own — a value class, a third-party type, anything needing a narrower wire form than its fields. Pair it with `TypeParceler` at the use site.

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/Parcelize.kt:25</code></summary>

```kotlin
actual typealias IgnoredOnParcel = IgnoredOnParcel

actual typealias Parceler<P> = Parceler<P>

actual typealias TypeParceler<T, P> = TypeParceler<T, P>

actual typealias Parcel = Parcel
```

</details>

- `fun create(parcel: Parcel): P` — Reconstructs a `P` from `parcel`. Must read fields in exactly the order `write` produced them.
- `fun P.write(parcel: Parcel, flags: Int)` — Writes this `P` into `parcel`. The read side depends on this order — see `create`.

```kotlin
expect annotation class TypeParceler<T, P : Parceler<in T>>()
```
Binds a `Parceler` to type `T` for one property or file, so a shared model can carry a type the platform does not know how to parcel.

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/Parcelize.kt:27</code></summary>

```kotlin
actual typealias Parceler<P> = Parceler<P>

actual typealias TypeParceler<T, P> = TypeParceler<T, P>

actual typealias Parcel = Parcel
```

</details>

```kotlin
expect class Parcel
```
The platform write buffer a `Parceler` reads from and writes to. Strictly POSITIONAL — there are no field names on the wire, so reads must mirror the write order exactly.

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/Parcelize.kt:29</code></summary>

```kotlin
actual typealias TypeParceler<T, P> = TypeParceler<T, P>

actual typealias Parcel = Parcel
```

</details>

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/di/CommonModule.kt`

```kotlin
val CommonModule = module
```
Koin module for `core-base/common` — currently the platform `dispatcherManagerModule` binding. Include it once from the app's module graph; every other core module assumes a `DispatcherManager` is already resolvable.

<details><summary>Used in the template — <code>core/data/src/androidMain/kotlin/kpt/core/data/di/PlatformDependentDataModule.android.kt:25</code></summary>

```kotlin
 */
actual val platformModule: Module = module {
    includes(CommonModule)

    singleOf(::TimeZoneMonitorImpl) bind TimeZoneMonitor::class
}
```

</details>

```kotlin
expect val dispatcherManagerModule: Module
```
The per-platform `DispatcherManager` binding, supplied by each target's `actual`. Separate from `CommonModule` because the dispatcher set is the one part of this module that cannot be expressed in common code.

### `core-base/common/src/commonMain/kotlin/kpt/core/base/common/manager/DispatcherManager.kt`

```kotlin
interface DispatcherManager
```
Injectable access to the app's coroutine dispatchers.

<details><summary>Used in the template — <code>feature/fast-mpay/src/commonTest/kotlin/kpt/feature/fastmpay/TestFakes.kt:277</code></summary>

```kotlin
    plainSettings = MapSettings(),
    secureSettings = MapSettings(),
    dispatcher = object : DispatcherManager {
        override val default = Dispatchers.Unconfined
        override val main = Dispatchers.Main
        override val io = Dispatchers.Unconfined
        override val unconfined = Dispatchers.Unconfined
```

</details>

- `val default: CoroutineDispatcher` — The default `CoroutineDispatcher` for the app.
- `val main: MainCoroutineDispatcher` — The `MainCoroutineDispatcher` for the app.
- `val io: CoroutineDispatcher` — The IO `CoroutineDispatcher` for the app.
- `val unconfined: CoroutineDispatcher` — The unconfined `CoroutineDispatcher` for the app.
- `val appScope: CoroutineScope` — Application-lifetime scope for work that must outlive any screen — cancelled only at process end.

---

_7 type(s), 16 function(s)/property(ies); 23 carry KDoc at source; 0 authored example(s); 8 live call site(s)._
<!-- api-docs:end -->
