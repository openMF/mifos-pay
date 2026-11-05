# `core-base/firebase`

> **Layer:** core-base — framework-shared; generators CONSUME, never write
> **Corpus surface:** `CORE_BASE_FIREBASE.md`
> **Measured:** 1 Kotlin files, 0 test files

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core-base/firebase sha=c4ce4a43f188a9296733aa6d3f3ce2b367051355 -->
## API reference

_Generated from `core-base/firebase` at tree `c4ce4a43f188` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **framework-shared and read-only to generators** (D9). Everything below is
something a feature CALLS; re-declaring one of these in `core/**` is the duplicate-the-
framework defect. A change here is a TEMPLATE change and flows upstream as a draft PR
(RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001), never a local fix.

### `core-base/firebase/src/commonMain/kotlin/kpt/core/base/firebase/di/FirebaseModule.kt`

```kotlin
val firebaseModule: Module = module
```
Base Firebase integration (the "main implementation", template-owned in core-base/firebase).

---

_0 type(s), 1 function(s)/property(ies); 1 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
