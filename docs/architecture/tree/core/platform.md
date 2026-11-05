# `core/platform`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_PLATFORM.md`
> **Measured:** 2 Kotlin files, 1 test files

## Principal types

`AppReviewConfig`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/platform sha=8767e1ab609f37d1bbb958872e239c52a90db5de -->
## API reference

_Generated from `core/platform` at tree `8767e1ab609f` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/platform/src/commonMain/kotlin/kpt/core/platform/config/AppReviewConfig.kt`

```kotlin
object AppReviewConfig
```
Store identity + prompt policy for the in-app review flow.

<details><summary>Used in the template — <code>core/platform/src/commonTest/kotlin/kpt/core/platform/config/AppReviewConfigTest.kt:30</code></summary>

```kotlin
        // One launch short. Everything else maximally satisfied, so only MIN_LAUNCHES can decide.
        assertFalse(
            AppReviewConfig.shouldPromptForReview(
                launchCount = AppReviewConfig.MIN_LAUNCHES - 1,
                daysSinceInstall = Int.MAX_VALUE,
                daysSinceLastPrompt = null,
            ),
```

</details>

- `const val PLAY_STORE_PACKAGE: String = "org.mifospay"` — Play Store package — `identity.app_id`.
- `const val APP_STORE_ID: String = ""` — App Store numeric id — `platforms/apple/apple.yaml#apple.app_store_id`.
- `const val MICROSOFT_STORE_PRODUCT_ID: String = ""` — Microsoft Store product id — `platforms/windows/windows.yaml#windows.store_id`.
- `const val WEB_URL: String = ""` — Open-web fallback for targets with no store — `org.marketing_url`.
- `const val ENABLED: Boolean = true` — `in_app_review.enabled` — false disables the custom prompt entirely.
- `const val MIN_LAUNCHES: Int = 5` — `in_app_review.min_launches`.
- `const val MIN_DAYS_SINCE_INSTALL: Int = 3` — `in_app_review.min_days_since_install`.
- `const val COOLDOWN_DAYS: Int = 90` — `in_app_review.cooldown_days`.
- `fun shouldPromptForReview(` — Whether an AUTOMATIC review prompt is due, given counters the caller keeps. The counters are parameters rather than state owned here, so this stays a pure function of the generated thresholds.
- `val storeListing: StoreListing = StoreListing(` — The listing to hand `AppReview.configure(...)`. Built from whichever ids are non-blank.

---

_1 type(s), 10 function(s)/property(ies); 11 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->
