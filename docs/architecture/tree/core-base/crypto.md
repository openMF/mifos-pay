# `core-base/crypto`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_CRYPTO.md`
> **Measured:** 22 Kotlin files, 1 test files

## Principal types

`FieldEncryptor`, `SecureKeyProvider`, `SecureRandom`, `WebSecureCrypto`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/crypto sha=44d6fe516bcc1b7c4046512a62486a024afca1a2 -->
## API reference

_Generated from `core-base/crypto` at tree `44d6fe516bcc` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/crypto/src/commonMain/kotlin/kpt/core/base/crypto/FieldEncryptor.kt`

```kotlin
expect class FieldEncryptor
```
Platform-specific AES-256-GCM field encryption for sensitive data. Encrypts individual fields BEFORE they are stored in Room or Settings.

<details><summary>Used in the template — <code>core-base/crypto/src/androidMain/kotlin/kpt/core/base/crypto/FieldEncryptor.kt:22</code></summary>

```kotlin

/** Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
actual class FieldEncryptor(private val keyProvider: SecureKeyProvider) {

    /** `encrypt` on this target. Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
    actual fun encrypt(plaintext: String): String {
        val encrypted = encrypt(plaintext.encodeToByteArray())
```

</details>

### `core-base/crypto/src/commonMain/kotlin/kpt/core/base/crypto/SecureKeyProvider.kt`

```kotlin
expect class SecureKeyProvider
```
Platform-specific secure key storage and retrieval.

<details><summary>Used in the template — <code>core-base/crypto/src/androidMain/kotlin/kpt/core/base/crypto/FieldEncryptor.kt:22</code></summary>

```kotlin

/** Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
actual class FieldEncryptor(private val keyProvider: SecureKeyProvider) {

    /** `encrypt` on this target. Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
    actual fun encrypt(plaintext: String): String {
        val encrypted = encrypt(plaintext.encodeToByteArray())
```

</details>

### `core-base/crypto/src/commonMain/kotlin/kpt/core/base/crypto/SecureRandom.kt`

```kotlin
expect class SecureRandom
```
Platform-specific cryptographically secure random number generator.

<details><summary>Used in the template — <code>core-base/crypto/src/androidMain/kotlin/kpt/core/base/crypto/SecureRandom.kt:13</code></summary>

```kotlin

/** Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
actual class SecureRandom {
    private val random = java.security.SecureRandom()

    /** `nextBytes` on this target. Android — AES/GCM via `javax.crypto`, keyed from the AndroidKeyStore. */
    actual fun nextBytes(size: Int): ByteArray {
```

</details>

---

_3 type(s), 0 function(s)/property(ies); 3 carry KDoc at source; 0 authored example(s); 3 live call site(s)._
<!-- api-docs:end -->
