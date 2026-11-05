# `cmp-ios/`

> **Kind:** Project area  
> **Measured:** 49 tracked files — 7× `.xcconfig`, 7× `.swift`, 7× `.xcscheme`, 4× `.m`, 4× `.h`

## Shape

| Path | Files |
|---|---:|
| `cmp-ios/KotlinMultiplatformLinkedPackage/` | 12 |
| `cmp-ios/iosApp/` | 12 |
| `cmp-ios/iosApp.xcodeproj/` | 11 |
| `cmp-ios/Configs/` | 6 |
| (files at the root) | 4 |
| `cmp-ios/scripts/` | 3 |
| `cmp-ios/Configuration/` | 1 |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `cmp-ios/scripts/` | 3 | [scripts](cmp-ios/scripts.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `cmp-ios/CONSUMPTION.md` | Consuming `cmp-ios` in a fork |
| `cmp-ios/README.md` | cmp-ios |

<!-- tree-scaffold:end -->
## Significance

The Xcode project and the Swift entry point. The shared Compose app arrives as an **XCFramework via
SwiftPM** — `Package.swift` declares the binary target and an Xcode Run-Script phase embeds and signs
it. There is no CocoaPods toolchain, so a contributor needs Xcode and nothing else.

**`Config.xcconfig` is generated** from `app-profile` by `syncForkConfig`; the bundle id and display
name are not authored in Xcode. Editing them in the project file puts the build and the store listing
into disagreement.

**`project.pbxproj` merges, it does not overwrite.** A template sync runs
`scripts/white-label/merge-pbxproj.rb`, which 3-way merges object by object: an upstream addition is
taken, and a genuine overlap is reported as a conflict rather than guessed. A blind copy would discard
every file a fork added to the project.
