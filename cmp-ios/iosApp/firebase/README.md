# Firebase on iOS (fork-owned)

Put your app's `GoogleService-Info.plist` in this folder. It is the iOS counterpart of
`cmp-android/google-services.json`: a public client config, not a secret.

- `cmp-ios/scripts/copy-compose-resources.sh` bundles it into the app at build time.
- `iOSApp.swift` calls `FirebaseApp.configure()` only when the plist is in the bundle, so a fork without
  Firebase on iOS still builds and runs.
- This folder is fork-owned (`customization-surface.yaml`): a template sync never touches it.

After adding or changing the plist, run a clean build. The copy runs in a build phase that an
incremental build can skip.
