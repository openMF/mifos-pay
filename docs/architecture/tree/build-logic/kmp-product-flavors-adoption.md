# Adoption record: `kmp-product-flavors` plugin in this template

> **What this doc is**: the single source of truth for how `kmp-project-template` adopts the [`kmp-product-flavors`](https://github.com/MobileByteLabs/kmp-product-flavors) Gradle plugin. Each version section below is a concrete record of what was changed in this codebase to consume that plugin version — file paths, before/after diffs, verify commands. Living document — updated every time we bump the plugin.
>
> **What this doc is NOT**: it's not a user-facing setup guide (that's [`SETUP-PROJECT.md`](../app-profile/project-setup.md)) and not the abstract spec (that's the library's [`docs/adoption/v{X.Y}/consumer.md`](https://github.com/MobileByteLabs/kmp-product-flavors/tree/development/docs/adoption)). This doc is the **filled-in record** — the kmp-project-template-specific implementation of every verify gate the library specifies.
>
> **Audience**:
> - Maintainers of this template — when we bump the plugin to v2.8+, this doc shows exactly what files change and how to verify the migration.
> - Maintainers of downstream forks (`mifos-mobile`, `mifos-pay`, `mifos-x-field-officer-app`, …) — when they sync from this template, this doc shows what they inherit and how to verify the inheritance is intact in their fork.
> - AI agents (Claude/Cursor/Copilot) — paste this doc + the library's consumer.md and the agent has both the abstract gates AND the concrete realization. No ambiguity.

---

## How this template's adoption pattern works

The plugin is consumed via the **convention-plugin pattern** (Pattern 3b per the library's [adoption guide](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)). Three files do the work:

| File | Role |
|---|---|
| [`gradle/libs.versions.toml`](../gradle/libs.versions.toml) | Pins the plugin version + exposes the maven artifact (for compileOnly) and the Gradle plugin id (for direct-apply, unused here). Also defines the local convention plugin id `kmp-flavors-convention`. |
| [`build-logic/convention/build.gradle.kts`](../build-logic/convention/build.gradle.kts) | Registers the `KMPFlavorsConventionPlugin` (id `org.convention.kmp.flavors`) and takes a `compileOnly` dep on the upstream maven artifact. |
| [`build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt) | Applies `KmpFlavorPlugin` programmatically and configures the flavor matrix via `extensions.configure<KmpFlavorExtension>`. AGP-bridge is handled natively by the plugin's `AgpProductFlavorRegistrar` (wired via `pluginManager.withPlugin` in `KmpFlavorPlugin.apply()`). Wires the downstream extension hook (`LocalFlavorsLoader.applyIfPresent`). |

Two downstream support files:

| File | Role |
|---|---|
| [`build-logic/convention/src/main/kotlin/LocalFlavorsLoader.kt`](../build-logic/convention/src/main/kotlin/LocalFlavorsLoader.kt) | Reflective hook letting downstream forks add flavors via a `build-logic/convention/src/main/kotlin/local/LocalFlavors.kt` file that `scripts/white-label/sync-dirs.sh` excludes from sync. |
| [`build-logic/convention/src/main/kotlin/local/.gitkeep`](../build-logic/convention/src/main/kotlin/local/) | Placeholder for the local override directory — fork apps drop their `LocalFlavors.kt` here. |

For the abstract verify gates that this concrete adoption realizes, see the library's [`consumer.md`](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md). Each section of this doc references the corresponding library section number.

---

## v2.8.1 (current) — adopted 2026-06-05

### Bump from v2.7.0 → v2.8.1

This template's `gradle/libs.versions.toml` previously pinned `kmpProductFlavors = "2.7.0"` (a no-DSL-change bump from v2.4.2 per the section below). v2.8.1 is the v2.8 minor stable, shipping per-flavor `signingConfigs {}` DSL + per-flavor `versionCode`/`versionName` + V50/V51 validator codes. **Zero DSL changes required** — the existing 2-flavor (demo/prod) + 3-buildType (debug/staging/release) shape does not declare signing configs (consumer-domain concern; bridged via existing fastlane lanes + GitHub Actions secrets, see `fastlane/CLAUDE.md`).

### Files changed (this version)

```diff
- gradle/libs.versions.toml#[versions].kmpProductFlavors = "2.7.0"
+ gradle/libs.versions.toml#[versions].kmpProductFlavors = "2.8.1"
```

That's the only line change for this version. No source diff in `KMPFlavorsConventionPlugin.kt`, `AppFlavor.kt`, or `LocalFlavorsLoader.kt`.

### Why the bump is safe

Per the library's [v2.8 CHANGELOG](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/CHANGELOG.md):

- **Version floor unchanged** — AGP 8.2+ / Gradle 8.0+ / KGP 2.0.21+ / JDK 17+ / CMP 1.7.0+. Same as v2.4 → v2.5 → v2.6 → v2.7 → v2.8.
- **All v2.7 DSL surfaces preserved byte-identically** — every block this template uses (`kmpFlavors {}`, `flavorDimensions {}`, `flavors {}`, `buildTypes {}`, `buildConfigField`, `buildConfigPackage`) unchanged.
- **New v2.8 surfaces are opt-in** — `signingConfigs {}` + per-flavor `versionCode`/`versionName` are additive; not declaring them is a valid no-op.
- **Plugin built-against bumped** to AGP 9.2.1 + Kotlin 2.3.21 (matches this template's own toolchain).

### Verify gates (delta vs v2.7.0)

Section §1 (Plugin pinned) command output changes only in the version string:
```bash
grep -E 'kmpProductFlavors\s*=' gradle/libs.versions.toml
# Expected: kmpProductFlavors = "2.8.1"
```

All other §2-§14 verify gates pass byte-identically to v2.7.0 since the adoption surface is unchanged.

### Assembly evidence

Verified via `./gradlew :cmp-android:assembleDemoDebug` and `:cmp-android:assembleProdRelease` against v2.8.1 — both exit 0; generated `BuildKonfig.IS_DEMO_BUILD`, `BuildKonfig.BASE_URL`, `BuildKonfig.DEMO_USERNAME`, `BuildKonfig.DEMO_PASSWORD` consumed by `core/network/` as before.

> **Note on local resolution**: v2.8.1 requires `mavenLocal()` first in `pluginManagement.repositories` until the release propagates to Maven Central (already pre-wired in this template's `settings.gradle.kts`). No changes needed to `settings.gradle.kts`.

### Template cleanup — 2026-06-05 (no plugin version bump)

Source analysis of `KmpFlavorPlugin.kt` (lines 237-268) confirmed that `AgpProductFlavorRegistrar.apply()` is already called for pure-AGP modules via `pluginManager.withPlugin("com.android.application/library")` hooks — independent of the `configurePlugin()` KMP-absent early return. The convention plugin's `configureFlavors(CommonExtension)` workaround and `AppFlavor.kt` helper were therefore **redundant** and have been removed:

- `build-logic/convention/src/main/kotlin/org/convention/AppFlavor.kt` — **deleted**
- `build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt` — removed the 19-line `listOf("com.android.application", "com.android.library").forEach { configureFlavors(it) }` block and the `import com.android.build.api.dsl.CommonExtension` / `import org.convention.configureFlavors` imports

The plugin version stays at `2.8.1` — this is a template-level cleanup, not a plugin change.

### Pure single API: AGP manual bridge removal — 2026-06-05

With `AgpProductFlavorRegistrar` confirmed as the authoritative bridge, the remaining manual AGP
overrides in `cmp-android/build.gradle.kts` were also redundant:

**Removed from `cmp-android/build.gradle.kts`:**

| Removed setting | Why |
|---|---|
| `import org.convention.AppBuildType` | No longer referenced |
| `debug { applicationIdSuffix = AppBuildType.DEBUG.applicationIdSuffix }` | Plugin sets `.debug` via `AgpProductFlavorRegistrar` |
| `release { isMinifyEnabled = true }` | Plugin's `release { isMinifyEnabled.set(true) }` handles it |
| `release { isDebuggable = false }` | Plugin's `release { isDebuggable.set(false) }` handles it |
| `release { applicationIdSuffix = AppBuildType.RELEASE.applicationIdSuffix }` | Was `null` (no-op) |

**Deleted:**
- `build-logic/convention/src/main/kotlin/org/convention/AppBuildType.kt` — enum only used by the removed lines above

**What stays (Android-app-specific, the plugin does not own these):**
- `signingConfigs { create("release") { ... } }` — signing is an app-level concern
- `buildTypes { staging { signingConfig, proguardFiles } }` — release-signed staging builds for distribution
- `buildTypes { release { isShrinkResources, isJniDebuggable, signingConfig, proguardFiles } }`

`kmp-product-flavors` is now the **single API** for all flavor/build-type lifecycle settings.
Verify:

```bash
# AppBuildType.kt is gone
test ! -f build-logic/convention/src/main/kotlin/org/convention/AppBuildType.kt && echo "✓ deleted"

# No AppBuildType references remain
grep -r "AppBuildType" . --include="*.kt" --include="*.kts" | grep -v ".gradle/caches" && echo "FAIL" || echo "✓ no refs"

# buildTypes block only has Android-specific settings
grep -A 20 "buildTypes {" cmp-android/build.gradle.kts
```

Both `:cmp-android:assembleDemoDebug` and `:cmp-android:assembleProdRelease` pass after this cleanup.

### iOS Xcode wiring — 2026-06-05

> **Superseded (E6, SwiftPM/XCFramework migration).** Two things below are history, not
> current state: (a) `$(CONFIGURATION)` is never expanded inside an xcconfig `#include`, so
> the `iOSApp.xcconfig` umbrella never actually loaded the per-variant file — each build
> configuration now points at its own `Configs/<variant>.xcconfig` directly, and the dead
> umbrella has been deleted; (b) the per-configuration optional xcconfig `#include?` is gone with the
> pod toolchain. Current contract is enforced by G-IOS-SWIFTPM.

Wired the Xcode project for per-flavor builds so the KMP shared framework is resolved from the
correct variant output directory (`cmp-shared/build/xcode-frameworks/<variant>/`).

**Files created:**

```
cmp-ios/Configs/
├── demoDebug.xcconfig       ← KMPF_VARIANT=demoDebug + all KMPF_* vars
├── demoStaging.xcconfig
├── demoRelease.xcconfig
├── prodDebug.xcconfig
├── prodRelease.xcconfig
└── prodStaging.xcconfig

cmp-ios/iosApp.xcodeproj/xcshareddata/xcschemes/
├── demoDebug.xcscheme
├── demoRelease.xcscheme
├── prodDebug.xcscheme
└── prodRelease.xcscheme
```

**`project.pbxproj` changes:**

- Added `PBXFileReference` for `iOSApp.xcconfig` (ID `CAFE001F00000000000000A1`) — *removed at E6*
- Added `Configs` PBXGroup (ID `CAFE001F00000000000000A2`) in the root group
- Fixed legacy `FRAMEWORK_SEARCH_PATHS` in Debug/Release target configs (was `../shared/` and
  `../composeApp/` — corrected to `../cmp-shared/`)
- Added 6 project-level `XCBuildConfiguration` entries (B1–B6: demoDebug … prodRelease),
  base config → `Config.xcconfig`
- Added 6 target-level `XCBuildConfiguration` entries (C1–C6: demoDebug … prodRelease),
  base config → `Configs/<variant>.xcconfig` (was `iOSApp.xcconfig` before E6), with:
  `FRAMEWORK_SEARCH_PATHS = "$(SRCROOT)/../cmp-shared/build/xcode-frameworks/$(KMPF_VARIANT)/$(SDK_NAME)"`
- Both `XCConfigurationList` entries updated to include all 6 new configurations

**How the xcconfig chain works:**

1. Each flavor Xcode scheme selects build configuration e.g. `demoDebug`
2. Target's `baseConfigurationReference` → `Configs/demoDebug.xcconfig` (each configuration
   names its own file; Xcode does not expand `$(CONFIGURATION)` in an `#include`)
4. `demoDebug.xcconfig` defines `KMPF_VARIANT = demoDebug` (plus all other `KMPF_*` vars)
5. `FRAMEWORK_SEARCH_PATHS` resolves to `…/cmp-shared/build/xcode-frameworks/demoDebug/iphoneos`
6. Xcode picks up the KMP framework built for that exact flavor variant

**Verify gates:**

```bash
PBXPROJ=cmp-ios/iosApp.xcodeproj/project.pbxproj

# AC-5: the dead pre-E6 umbrella is NOT registered in pbxproj
grep -c "iOSApp.xcconfig" "$PBXPROJ"               # expect 0

# AC-6: demoDebug and prodDebug configs present
grep -cE "demoDebug|prodDebug" "$PBXPROJ"           # expect ≥2

# AC-7: FRAMEWORK_SEARCH_PATHS uses multi-line format (no $(CONFIGURATION) on same grep line)
grep -c "FRAMEWORK_SEARCH_PATHS.*\$(CONFIGURATION)" "$PBXPROJ"   # expect 0

# AC-8: ≥4 shared xcschemes
ls cmp-ios/iosApp.xcodeproj/xcshareddata/xcschemes/*.xcscheme | wc -l   # expect ≥4

# KMPF_VARIANT wired in 6 target configs
grep -c "KMPF_VARIANT" "$PBXPROJ"                  # expect 6
```

---

## v2.7.0 — adopted 2026-06-02

### Bump from v2.4.2 → v2.7.0

This template's `gradle/libs.versions.toml` previously pinned `kmpProductFlavors = "2.4.2"`. v2.7.0 is the GA promotion of the AGP 9.2.1 + Kotlin 2.3.21 + 100% coverage line. **Zero DSL changes required** — the plugin's v2.6 surface is preserved byte-identically.

### Files changed (this version)

```diff
- gradle/libs.versions.toml#[versions].kmpProductFlavors = "2.4.2"
+ gradle/libs.versions.toml#[versions].kmpProductFlavors = "2.7.0"
```

That's the only line change for this version. No source diff in `KMPFlavorsConventionPlugin.kt`, `AppFlavor.kt`, or `LocalFlavorsLoader.kt`.

### Why the bump is safe

Per the library's [`MIGRATION_v2.6_TO_v2.7.md`](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/CHANGELOG.md) (opens with "You do not need to migrate."):

- **Version floor unchanged** — Gradle 8.0+ / KGP 2.0.21+ / AGP 8.2+ / JDK 17+ / CMP 1.7.0+. Same as v2.4 → v2.5 → v2.6 → v2.7.
- **All DSL surfaces preserved** — `kmpFlavors {}`, `dimensions {}`, `flavorDimensions {}`, `flavors {}`, `buildTypes {}`, `variantFilter {}`, `promote()`, `spm {}`, `featureFlags {}`, `di {}`, `analytics {}`, `buildKonfig {}` all unchanged.
- **Validator codes V01-V30 unchanged** — same configuration-time validation behavior.
- **Plugin built-against bumped** from AGP 8.12.3 + Kotlin 2.3.0 → AGP 9.2.1 + Kotlin 2.3.21. The reflective `AgpBridge.kt` means consumers on AGP 8.2+ see no behavioral change.

### Verify gates (the 14 sections of the library's consumer.md, realized here)

For each library section, this template's verify shows the actual command + actual output expected from THIS codebase.

#### [§1 — Plugin pinned](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

```bash
grep -E 'kmpProductFlavors\s*=' gradle/libs.versions.toml
# Expected: kmpProductFlavors = "2.7.0"

grep -E 'kmp-product-flavors\s*=\s*\{\s*id\s*=' gradle/libs.versions.toml
# Expected: kmp-product-flavors = { id = "io.github.mobilebytelabs.kmp-product-flavors", version.ref = "kmpProductFlavors" }

grep -E 'kmp-product-flavors-plugin\s*=\s*\{\s*group\s*=' gradle/libs.versions.toml
# Expected: kmp-product-flavors-plugin = { group = "io.github.mobilebytelabs.kmpflavors", name = "flavor-plugin", version.ref = "kmpProductFlavors" }
```

#### [§2 — Toolchain compatibility](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

This template's `gradle/libs.versions.toml` pins:
- `agp` to a value ≥ 8.2 — currently the AGP-9-compatible head per [v2.7 Phase 02 samples-audit](https://github.com/MobileByteLabs/kmp-product-flavors/pull/115)
- `kotlin` to a value ≥ 2.0.21

```bash
./gradlew --version | grep '^Gradle'
# Expected: Gradle 8.x.x (≥ 8.0)

grep -E '^(kotlin|agp)\s*=' gradle/libs.versions.toml
# Expected: kotlin and agp present
```

#### [§3 — Adoption pattern: convention-plugin (3b)](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

This template uses **pattern 3b (convention plugin)**. Verify:

```bash
ls -d build-logic/convention/ && echo "✓ convention-plugin pattern (3b)"
```

#### [§4b — Plugin applied + configured via convention plugin](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

```bash
# Convention plugin registration
grep -E 'register\("kmpFlavors"\)' build-logic/convention/build.gradle.kts
grep -E 'id\s*=\s*"org\.convention\.kmp\.flavors"' build-logic/convention/build.gradle.kts

# Programmatic apply + configure
grep -E 'pluginManager\.apply\(KmpFlavorPlugin::class\.java\)' \
  build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt
grep -E 'extensions\.configure<KmpFlavorExtension>' \
  build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt

# Local convention alias for downstream modules
grep -E 'kmp-flavors-convention\s*=\s*\{\s*id\s*=\s*"org\.convention\.kmp\.flavors"' \
  gradle/libs.versions.toml

# Modules apply the convention plugin chained through KMPLibraryConventionPlugin etc.
grep -rln 'apply\("org\.convention\.kmp\.flavors"\)' \
  build-logic/convention/src/main/kotlin/ | head -5
```

#### [§5 — Flavors + dimensions](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

This template uses the **flat DSL** style (v2.4+):

```
flavorDimensions { register("contentType") { priority.set(0) } }
flavors {
    register("demo") { dimension.set("contentType"); isDefault.set(true); ... }
    register("prod") { dimension.set("contentType"); ... }
}
buildTypes {
    register("debug") { isDefault.set(true); isDebuggable.set(true); ... }
    register("staging") { ... }
    register("release") { isMinifyEnabled.set(true); ... }
}
```

Variant matrix: **6 variants** = 2 flavors × 3 buildTypes = `demoDebug, demoStaging, demoRelease, prodDebug, prodStaging, prodRelease`. Active: `demoDebug`.

```bash
./gradlew :cmp-navigation:listFlavors --no-daemon --no-configuration-cache 2>&1 | tail -30
# Expected: table listing 6 variants with "← ACTIVE" next to demoDebug
```

#### [§6 — buildConfigPackage from single source of truth](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

This template stores the brand identifier ONCE in `gradle/libs.versions.toml#[versions].appId`:

```toml
[versions]
appId = "com.example.app"
```

And reads it in `KMPFlavorsConventionPlugin`:

```kotlin
buildConfigPackage.set(libs.findVersion("appId").get().requiredVersion)
```

Forking this template to a new brand = changing one TOML line.

```bash
grep -E '^appId\s*=' gradle/libs.versions.toml
# Expected: appId = "com.example.app"

grep -E 'libs\.findVersion\("appId"\)' \
  build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt
```

#### [§7 — Default variant resolves](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

```bash
./gradlew :cmp-navigation:listActiveVariant --no-daemon --no-configuration-cache 2>&1 | grep -E 'Active|All'
# Expected:
#   Active : demoDebug
#   All    : demoDebug, demoStaging, demoRelease, prodDebug, prodStaging, prodRelease
```

#### [§8 — BuildKonfig codegen output + claim mechanism](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

The codegen host in this template is **`cmp-navigation`** (deterministic winner across local + CI). Other modules log `skipping FlavorConfig codegen — already generated by :cmp-navigation`.

```bash
./gradlew :cmp-navigation:generateFlavorBuildConfig --rerun-tasks \
  --no-daemon --no-configuration-cache 2>&1 | tail -5
# Expected: BUILD SUCCESSFUL + "Generated /.../BuildKonfig.kt" line

test -f cmp-navigation/build/generated/kmpFlavors/commonMain/kotlin/org/mifos/kmp/template/BuildKonfig.kt
# Expected: exit 0 (file exists)
```

This path is what the upstream library's [`.github/workflows/pr-check.yml`](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/.github/workflows/pr-check.yml) validates on every PR.

#### [§9 — Validator codes V01-V30 pass](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

```bash
./gradlew :cmp-navigation:validateFlavors --no-daemon --no-configuration-cache 2>&1 | \
  grep -E 'KMPF-V|Validation passed|FAIL'
# Expected: "[KMP Flavors] Validation passed!" — no KMPF-V** ERRORs.
```

WARNINGs are advisory and expected on this template (e.g. KMPF-V05 on Apple Silicon + iosX64).

#### [§10 — AGP-only modules: native plugin bridge via AgpProductFlavorRegistrar](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

This template has the `cmp-android` module which applies `com.android.application` without `kotlin("multiplatform")`. `KmpFlavorPlugin.apply()` registers `AgpProductFlavorRegistrar` via `pluginManager.withPlugin("com.android.application/library")` hooks — these fire synchronously for modules with AGP applied, BEFORE `configurePlugin()`'s KMP-absent early return. `AgpProductFlavorRegistrar` propagates flavor dimensions + product flavors + build types to AGP via `whenObjectAdded` callbacks on the `kmpFlavors {}` extension.

No manual `configureFlavors(CommonExtension)` helper or `AppFlavor.kt` is needed. Both were removed in the 2026-06-05 template cleanup (see v2.8.1 section below).

```bash
# Plugin handles pure-AGP modules natively — no AppFlavor.kt needed
test ! -f build-logic/convention/src/main/kotlin/org/convention/AppFlavor.kt
# Expected: exit 0

grep -c "configureFlavors" \
  build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt
# Expected: 0

# Assembly confirms AGP flavors are registered for pure com.android.application module
./gradlew :cmp-android:assembleDemoDebug -x :cmp-android:installGitHooks \
  --no-configuration-cache 2>&1 | tail -3
# Expected: BUILD SUCCESSFUL
```

#### [§11 — Downstream extension hook: LocalFlavorsLoader](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

This template IS the template. Downstream forks (`mifos-mobile`, `mifos-pay`, `mifos-x-field-officer-app`, …) sync `build-logic/convention/` from here via `scripts/white-label/sync-dirs.sh` and add their fork-specific flavors via `local/LocalFlavors.kt`.

```bash
grep -E 'LocalFlavorsLoader\.applyIfPresent' \
  build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt

test -f build-logic/convention/src/main/kotlin/LocalFlavorsLoader.kt
grep -E 'object LocalFlavorsLoader' \
  build-logic/convention/src/main/kotlin/LocalFlavorsLoader.kt

ls -d build-logic/convention/src/main/kotlin/local/ && echo "✓ local override directory present"
```

For details on the fork extension pattern, see [`FLAVORS_EXTENSION.md`](../../cross-cutting/flavors-extension.md).

#### [§12 — AGP 9.x compatibility (conditional)](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

If this template is currently on AGP 9.x (check `gradle/libs.versions.toml#[versions].agp`), no consumer-side changes were needed because this template never used:

- `CommonExtension<*,*,*,*,*,*>` parameterized type → not used
- `dataBinding {}` block → not enabled
- `com.android.library + kotlin("multiplatform")` co-application → uses `com.android.kotlin.multiplatform.library` already
- `dependencyGuard` without `afterEvaluate` → already wrapped where applicable

See the library's [`AGP_9_MIGRATION_NOTES.md`](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/docs/AGP_SUPPORT.md) for the full cookbook.

#### [§13 — End-to-end smoke test](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

```bash
./gradlew :cmp-navigation:validateFlavors :cmp-navigation:listFlavors \
  :cmp-navigation:generateFlavorBuildConfig \
  --no-daemon --no-configuration-cache 2>&1 | tail -20
# Expected: BUILD SUCCESSFUL + Validation passed + 6 variants listed + BuildKonfig.kt generated
```

#### [§14 — Reference implementation](https://github.com/MobileByteLabs/kmp-product-flavors/blob/development/README.md)

This template IS the reference implementation that the library's consumer.md §14 cites. The library's CI workflow `pr-check.yml` validates against this template's adoption on every PR to the library.

---

## Drift detection (local tool)

This doc IS the single source of truth for how this template adopts `kmp-product-flavors`. Every verify block above is executable via [`scripts/ci/verify-adoption-doc.py`](../scripts/ci/verify-adoption-doc.py) — run locally before any change that touches `build-logic/convention/`, `gradle/libs.versions.toml`, or `cmp-navigation/`:

```bash
python3 scripts/ci/verify-adoption-doc.py docs/ADOPTION_KMP_PRODUCT_FLAVORS.md
```

If a block fails, the fix is binary: revert the implementation change (file rename, alias move, dimension removal) IF unintended, OR update the verify block + corresponding section of this doc IF intentional.

---

## How future bumps work

When the library publishes `v2.8.0` (or v3.0, or v2.7.x patches):

1. **Read the library's new adoption pair**: [`docs/adoption/v{X.Y}/consumer.md`](https://github.com/MobileByteLabs/kmp-product-flavors/tree/development/docs/adoption) tells you what new verify gates exist and what changed.

2. **Read the library's migration doc**: `docs/MIGRATION_v{prev}_TO_v{X.Y}.md` calls out the version-to-version deltas.

3. **Apply the bump in this template**:
   ```
   - kmpProductFlavors = "2.7.0"
   + kmpProductFlavors = "2.8.0"
   ```
   Plus any DSL changes the library's migration doc requires (which has historically been zero — every minor since v2.4 has been "You do not need to migrate.").

4. **Add a new version section to this file** at the top, above the v2.7.0 section:
   - `## v2.8.0 — adopted YYYY-MM-DD`
   - Files changed (the diff)
   - Why the bump is safe (link to library's migration doc)
   - Verify gates (run each one + record actual output if different from v2.7.0)

5. **Run the full §1–§14 verify suite** against the new version. Record any output deltas in the new version section.

6. **Update the "How this template's adoption pattern works" header** if the consumption shape changes (rare — has been stable since v2.0).

7. **Commit + PR**: the PR title is `chore(deps): bump kmp-product-flavors v{prev} → v{X.Y}` and the description quotes the new version section.

8. **Downstream forks of this template** will pick up the bump via `scripts/white-label/sync-dirs.sh` automatically. They should also append a new version section to THEIR own `docs/ADOPTION_KMP_PRODUCT_FLAVORS.md` (synced + local edits coexist — same pattern as `LocalFlavors.kt`).

---

## Downstream forks — `LocalFlavors.kt` is the ONLY file you own

Downstream apps that consume this template (`mifos-mobile`, `mifos-pay`, `mifos-x-field-officer-app`, …) **do NOT ship their own adoption record**. They inherit this file via `scripts/white-label/sync-dirs.sh` and everything else in `build-logic/convention/`.

The only file a fork ever owns is:

```
build-logic/convention/src/main/kotlin/local/LocalFlavors.kt
```

This file is **excluded from `scripts/white-label/sync-dirs.sh`** — forks' edits survive every template sync. Forks add fork-specific flavors here:

```kotlin
package local

import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.gradle.api.Project

object LocalFlavors {
    @JvmStatic
    fun apply(ext: KmpFlavorExtension, project: Project) {
        ext.flavors {
            register("enterprise") {
                dimension.set("contentType")
                buildConfigField("Boolean", "IS_ENTERPRISE_BUILD", "true")
                buildConfigField("String", "BASE_URL", "\"https://enterprise.example.com\"")
            }
        }
    }
}
```

When a new library version ships, forks don't think about adoption at all. The migration loop:

1. Library publishes v2.8.0.
2. Maintainer runs `/lib-sync` (or `./scripts/lib-sync.sh`) against `samples/kmp-project-template` in the library repo.
3. PR opens against `kmp-project-template/dev` with the bump.
4. PR merges into the template's `dev` branch.
5. Forks run `scripts/white-label/sync-dirs.sh` against the updated template — `gradle/libs.versions.toml`, `build-logic/convention/`, and this `ADOPTION_KMP_PRODUCT_FLAVORS.md` all arrive together.
6. Fork's `local/LocalFlavors.kt` is untouched.

This is why the three-tier model is asymmetric:

| Tier | Owns | Pull rate |
|---|---|---|
| Library | abstract spec + migration recipes | publishes per minor release |
| Template (this repo) | concrete record + convention plugin + LocalFlavorsLoader | bumped per library release via `/lib-sync` |
| Fork (mifos-mobile etc.) | `LocalFlavors.kt` only | inherits via `scripts/white-label/sync-dirs.sh` |

Forks DON'T maintain an adoption doc. The template's doc IS their adoption doc — they inherit it.

---

## See also

- Library spec: [`MobileByteLabs/kmp-product-flavors`](https://github.com/MobileByteLabs/kmp-product-flavors) + [`docs/adoption/v2.7/`](https://github.com/MobileByteLabs/kmp-product-flavors/tree/development/docs/adoption/v2.7)
- Template setup guide (user-facing): [`SETUP-PROJECT.md`](../app-profile/project-setup.md)
- Template fork extension pattern: [`FLAVORS_EXTENSION.md`](../../cross-cutting/flavors-extension.md)
- Downstream consumer migration guide (Room 3 + Store 5 + Security): [`CONSUMER_APP_MIGRATION_GUIDE.md`](../../cross-cutting/consumer-app-migration-guide.md)
- Convention plugin source: [`build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt`](../build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt)
