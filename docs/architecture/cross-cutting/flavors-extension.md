# Flavor extensions for consumer apps

> Audience: maintainers of downstream apps that inherit build-logic from `kmp-project-template` via `scripts/white-label/sync-dirs.sh` — `mifos-mobile`, `mifos-pay`, `mifos-x-field-officer-app`, `mifos-x-group-banking`, `mifos-x-open-banking`, `reels-downloader-new`, …

## What you get for free (the synced base)

`KMPFlavorsConventionPlugin` ships with every sync and gives you:

| Dimension | Flavors | Default |
|---|---|---|
| `tier` | `demo`, `prod` | `demo` |

| BuildType | Default |
|---|---|
| `debug` | ✅ |
| `staging` | |
| `release` | |

**Variant matrix:** `demoDebug, demoStaging, demoRelease, prodDebug, prodStaging, prodRelease` (6 variants).

The generated `BuildKonfig.kt` (default class name in plugin v1.1.5+; previously `FlavorConfig.kt`) exposes:

- `VARIANT_NAME`, `BUILD_TYPE`
- `IS_DEMO`, `IS_PROD`, `IS_DEBUG`, `IS_STAGING`, `IS_RELEASE`
- `BASE_URL`, `DEMO_USERNAME`, `DEMO_PASSWORD`, `IS_DEMO_BUILD`
- `ENABLE_LOGGING`, `SHOW_DEBUG_OVERLAY`, `LOG_TAG`

> **Branding**: `buildConfigPackage` is read from `gradle/libs.versions.toml` `[versions].appPackage`. Forks change the brand by editing **one line** in `libs.versions.toml` — no build-logic edits.

## Upgrading to plugin v1.1.5

v1.1.5 is a zero-config release. If you previously had any of these in your KMPFlavorsConventionPlugin, **delete them**:

```kotlin
generateBuildConfig.set(false)          // removed — plugin now auto-detects multi-module case
                                        // and only the first claimant generates BuildKonfig
createIntermediateSourceSets.set(false) // removed — Kotlin 2.1+ hierarchy template owns
                                        // webMain/nativeMain wiring; plugin no longer
                                        // injects redundant dependsOn edges
bridgeAgpProductFlavors.set(false)      // removed — bridge is idempotent: if AGP flavors
bridgeAgpBuildTypes.set(false)          //   are already registered (e.g. via your own
                                        //   configureFlavors() in a withPlugin hook),
                                        //   the bridge silently no-ops
buildConfigClassName.set("FlavorConfig") // removed — default is now "BuildKonfig"
```

Also delete this from `gradle.properties` if you added it:

```properties
kotlin.suppressGradlePluginWarnings=UnusedSourceSetsWarning  # removed — plugin now creates
                                                              # per-flavor source sets
                                                              # lazily, so no warnings
```

After cleanup, your convention plugin should look like:

```kotlin
extensions.configure<KmpFlavorExtension> {
    buildConfigPackage.set(libs.findVersion("appPackage").get().requiredVersion)
    enableBuildTypes.set(true)
    flavorDimensions { ... }
    flavors { ... }
    buildTypes { ... }
}
```

### What the plugin now does automatically (v1.1.5+)

| Concern | v1.1.4 and earlier | v1.1.5+ |
|---|---|---|
| Multi-module `BuildKonfig` codegen | Every module generated → DEX duplicate error | First subproject to apply claims codegen; rest silently skip |
| Web intermediate wiring | Redundant `webMain.dependsOn(commonMain)` warnings | Owned by Kotlin 2.1+ hierarchy template; plugin only registers src dirs |
| Per-flavor source sets (`commonProd`, `iosDemoTest`, …) | Created eagerly → "Unused Kotlin Source Sets" warnings | Lazy: created only for active flavor OR when devs add files under `src/<name>/kotlin/` |
| AGP product-flavor registration | Bridge clashed with manual `configureFlavors()` | Bridge is idempotent — detects existing flavors and no-ops silently |


## How to add your own flavors

Create one file in your consumer-app repo:

```text
build-logic/convention/src/main/kotlin/local/LocalFlavors.kt
```

That `local/` directory is **excluded** from `scripts/white-label/sync-dirs.sh`. Anything inside survives every sync of `build-logic/` from the template.

### Skeleton

```kotlin
package local

import com.mobilebytelabs.kmpflavors.KmpFlavorExtension
import org.gradle.api.Project

object LocalFlavors {
    @JvmStatic
    fun apply(ext: KmpFlavorExtension, project: Project) {
        // your customisations here
    }
}
```

That's it. `KMPFlavorsConventionPlugin` invokes `LocalFlavorsLoader.applyIfPresent(...)` reflectively — if the file is absent, nothing happens.

## Recipes

### Recipe 1 — Add a new dimension (e.g. per-language)

```kotlin
object LocalFlavors {
    @JvmStatic
    fun apply(ext: KmpFlavorExtension, project: Project) {
        ext.flavorDimensions.register("locale") { priority.set(1) }
        ext.flavors.register("en") {
            dimension.set("locale")
            isDefault.set(true)
        }
        ext.flavors.register("hi") {
            dimension.set("locale")
            buildConfigField("String", "DEFAULT_LOCALE", "\"hi-IN\"")
        }
        ext.flavors.register("es") {
            dimension.set("locale")
            buildConfigField("String", "DEFAULT_LOCALE", "\"es-ES\"")
        }
    }
}
```

→ Matrix: `2 (tier) × 3 (locale) × 3 (buildType) = 18 variants`.

### Recipe 2 — Add extra flavors to the existing `tier` dimension

```kotlin
ext.flavors.register("internal") {
    dimension.set("tier")
    applicationIdSuffix.set(".internal")
    buildConfigField("Boolean", "IS_INTERNAL_BUILD", "true")
    buildConfigField("String", "BASE_URL", "\"https://internal.openmf.org\"")
}
ext.flavors.register("beta") {
    dimension.set("tier")
    applicationIdSuffix.set(".beta")
    buildConfigField("String", "BASE_URL", "\"https://beta.openmf.org\"")
}
```

→ Matrix: `4 (tier) × 3 (buildType) = 12 variants` (`demoDebug, demoStaging, …, betaRelease`).

### Recipe 3 — Override a synced base flavor

Need different demo credentials for your consumer? Use `named()`:

```kotlin
ext.flavors.named("demo") {
    buildConfigField("String", "BASE_URL", "\"https://demo.mifos-pay.openmf.org\"")
    buildConfigField("String", "DEMO_USERNAME", "\"sandbox-user\"")
    buildConfigField("String", "DEMO_PASSWORD", "\"sandbox-pass\"")
}
```

### Recipe 4 — Tenant / bank flavors

```kotlin
ext.flavorDimensions.register("bank") { priority.set(1) }
ext.flavors.register("bankA") {
    dimension.set("bank")
    isDefault.set(true)
    applicationIdSuffix.set(".banka")
    buildConfigField("String", "BANK_URL", "\"https://api.banka.com\"")
}
ext.flavors.register("bankB") {
    dimension.set("bank")
    applicationIdSuffix.set(".bankb")
    buildConfigField("String", "BANK_URL", "\"https://api.bankb.com\"")
}
```

→ Matrix: `2 (tier) × 2 (bank) × 3 (buildType) = 12 variants`.

### Recipe 5 — Restrict the variant matrix

If your consumer doesn't need every combination (e.g. demo never targets the banking backend), filter:

```kotlin
ext.variantFilter {
    if ("demo" in flavorNames && "bankA" in flavorNames && buildType == "release") {
        exclude()
    }
}
```

## How it works under the hood

1. `KMPFlavorsConventionPlugin` (synced) registers the base `demo/prod × debug/staging/release` and calls `LocalFlavorsLoader.applyIfPresent(this, target)` as the last statement of the `kmpFlavors {}` block.
2. `LocalFlavorsLoader` (synced) does a reflective `Class.forName("local.LocalFlavors")`. If the class exists, it invokes the static `apply(ext, project)` method. If not, it logs an info line and returns.
3. Your `local/LocalFlavors.kt` (NOT synced) gets a fully-configured `KmpFlavorExtension` and can add/override anything the v1.1.2 DSL supports.

The matrix expansion happens **after** your apply runs — so adding a new dimension automatically combines with the base `tier` and `buildType` axes.

## Build output you should see

When the plugin runs and finds your file:

```text
[KMPFlavors] Applied local.LocalFlavors extensions
[KMP Flavors] Configuring N flavors across M dimensions
[KMP Flavors] Active variant: demoBankADebug
```

When you don't have a local file:

```text
[KMPFlavors] No local.LocalFlavors override — using base demo/prod only.
```

## FAQ

**Q: Why not just edit `KMPFlavorsConventionPlugin.kt`?**
A: That file is synced from `kmp-project-template`. Your edits will be wiped on the next `scripts/white-label/sync-dirs.sh`. The `local/` directory is the only file path inside `build-logic/` that survives sync.

**Q: Can I delete `demo` or `prod` from the synced base?**
A: Don't. They're the shared contract every downstream consumer relies on. If you genuinely don't want them, exclude their variants via `variantFilter { exclude() }` in your local file.

**Q: How do I run a specific variant?**
A: `./gradlew :cmp-shared:assemble -PkmpFlavor=demoBankADebug` (or any variant from `./gradlew :cmp-shared:listFlavors`).

**Q: Where do flavor-specific Kotlin sources go?**
A: Same as base — under `src/<sourceSetName>/kotlin/`. For example, an `internal` tier flavor would have its `actual` declarations under `src/commonInternal/kotlin/`. The plugin's source-set wiring (v1.1.2 F8) makes those reachable from `compileKotlin<Target>` for the active variant.

**Q: I want to share an extension across multiple consumers.**
A: Today, each consumer maintains their own `LocalFlavors.kt` — copy/adapt as needed. If shared infrastructure becomes important, a separate `mbl-build-logic-extensions` repo can hold reusable building blocks and your `LocalFlavors.kt` can compose them.

## Reference files

- `build-logic/convention/src/main/kotlin/KMPFlavorsConventionPlugin.kt` (synced)
- `build-logic/convention/src/main/kotlin/LocalFlavorsLoader.kt` (synced)
- `build-logic/convention/src/main/kotlin/local/LocalFlavors.kt` (your file — NOT synced)
- `scripts/white-label/sync-dirs.sh` — `EXCLUSIONS["build-logic"]="convention/src/main/kotlin/local:dir"`
