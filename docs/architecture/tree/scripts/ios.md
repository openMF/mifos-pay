# `scripts/ios/`

> Part of [`scripts/`](../scripts.md)  
> **Measured:** 5 tracked files — 5× `.sh`

### Scripts

| Script | Purpose |
|---|---|
| `check_ios_version.sh` | ============================================================================== |
| `setup_apn_key.sh` | ============================================================================== |
| `setup_ios_complete.sh` | ============================================================================== |
| `verify_apn_setup.sh` | ============================================================================== |
| `verify_ios_deployment.sh` | ============================================================================== |

<!-- tree-scaffold:end -->
## Significance

iOS setup and verification. `setup_ios_complete.sh` is the wizard (Team ID, App Store Connect API key,
Match SSH key); `verify_ios_deployment.sh` is 70+ checks across prerequisites, Match, signing, Firebase
and security.

A verifier is only useful if it fails for the right reasons: six of its checks used to assert that
`scripts/deploy/deploy_*.sh` exist, long after those wrappers were deliberately removed in favour of
Fastlane lanes — so it reported red on a correct repository, which trains a reader to ignore red output.
They now verify the path deployment actually takes.
