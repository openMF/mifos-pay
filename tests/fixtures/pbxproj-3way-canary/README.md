# pbxproj-3way canary

Real data, not synthetic. Captured 2026-09-10 from `mbs/cappy` ⋈ `openMF/kmp-project-template`:

| file | what it is |
|---|---|
| `base.pbxproj`   | the template at `b97eb73d` — cappy's `.template-version#template_sha` |
| `ours.pbxproj`   | cappy's current project (adds a `CappyWidgets` app-extension target) |
| `theirs.pbxproj` | template HEAD (adds the SwiftPM migration, de-hardcodes `DEVELOPMENT_TEAM`) |

It is the honest case because both sides evolved from the base in ways that a line merge cannot
reconcile — `git merge-file` produces **6 conflict markers** here, and a conflict marker in a
pbxproj makes the project unopenable in Xcode.

## What the merge must achieve

- template SPM migration lands — `XCLocalSwiftPackageReference`, `KotlinMultiplatformLinkedPackage`
- fork content survives — the `CappyWidgets` target, its build phases, `PayCraftStoreKit2.swift`
- template's de-hardcoding of `DEVELOPMENT_TEAM` wins (`L432S2FZP5` → `$(TEAM_ID)`), because the
  fork never changed it since base — signing then resolves through the fork-owned `Config.xcconfig`
- zero conflict markers, zero dangling UUID references, graph re-opens through the `xcodeproj` gem

## Why a dangling reference is the interesting failure

`Configs/iOSApp.xcconfig` exists in base and ours, and the template deleted it. The object is
correctly dropped, and the `PBXGroup.children` entry naming it must be dropped WITH it. An earlier
version pruned only the object, leaving a dangling reference the gem discards on load with a
warning — a merge that reports success while silently losing what the reference pointed at. That is
what `PM-4` pins.

Run: `bash scripts/product-health/checks/pbxproj-merge.sh` (via `product-health.sh`).

## plist union inputs (PM-6)

| file | what it is |
|---|---|
| `entitlements.ours` / `.theirs` | cappy's app-group entitlement vs the template's keychain-access-groups |
| `infoplist.ours` / `.theirs`    | cappy's `CFBundleURLTypes` OAuth redirect vs the template's base keys |

Both are merged with `--base -`. That is the measured truth for `iosApp.entitlements`, which is
**add/add** — absent at cappy's base `b97eb73d`, added independently on both sides — where a line
merge conflicts and the output fails `plutil -lint`.

`Info.plist` does have a real ancestor and line-merges cleanly today; it is exercised here for the
same flat-dict shape, and running it base-less is the stricter test (no ancestor to lean on).
