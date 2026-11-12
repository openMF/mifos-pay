# `cmp-ios/scripts/`

> Part of [`cmp-ios/`](../cmp-ios.md)  
> **Measured:** 3 tracked files — 3× `.sh`

3 tracked files.

<!-- tree-scaffold:end -->
## Significance

The two Xcode Run-Script phases that make the shared Compose code reachable from Swift:
`embed-xcframework.sh` embeds and signs the KMP XCFramework, and `copy-compose-resources.sh` bundles the
Compose resources.

They run inside Xcode's build environment rather than Gradle's, which is why they are shell scripts in
the iOS module rather than Gradle tasks — and why they must be flavor-aware: Xcode passes the
configuration, and the wrong framework variant embedded in a release build is a defect that only
surfaces on a device.
