# Known deployment issues

Five defects in the deployment path, each with the workaround that actually works. They live here
because every one of them is a deployment concern, and because `CLAUDE.md` and three docs pages had been
linking a `docs/analysis/BUGS_AND_ISSUES.md` that **does not exist in this repository** — a promise made
from the project's own Quick Links and never kept. The content was inline in `CLAUDE.md`; this is its
home.

Verify each against the code before trusting it. A known-issues page that is not re-checked becomes a
list of things that used to be true, which is worse than no page — so each entry names the file to look at.

---

## 🔴 Critical

### 1. Firebase `tester_groups` is accepted and ignored

The reusable Actions accept a `tester_groups` input and pass it through, but the Fastlane lane never
reads it — so a build uploads to the DEFAULT tester group regardless of what the workflow declared. It
fails silently: the upload succeeds, the wrong testers are notified.

**Workaround:** set `ENV['FIREBASE_GROUPS']` in the workflow environment rather than relying on the input.

**Look at:** `deployment/android/firebase/lane.rb`, `deployment/ios/firebase/lane.rb`, and the
`tester_groups` input in the publish actions (`.github/CLAUDE.md` documents both sides).

### 2. Signing parameter naming is inconsistent

The same value travels under three conventions — `snake_case` in a lane, `camelCase` in an action input,
`UPPERCASE` as a GitHub secret. Nothing enforces the mapping, so a renamed secret surfaces as an empty
string and a signing failure a long way from the rename.

**Look at:** `deployment/_shared/config.rb` (the resolver) against the `secrets:` blocks in
`.github/workflows/*.yml`.

---

## 🟡 Medium

### 3. The keystore filename is hardcoded

`upload_keystore.keystore` appears in several places rather than resolving through
`secrets/LAYOUT.yaml` like every other secret. A fork that names its keystore anything else has to find
each occurrence.

**Look at:** `scripts/white-label/keystore.sh` and the Android publish lanes.

### 4. Version generation can fail silently

The version step runs under `set +e`, so a failure in `./gradlew versionFile` is swallowed and the build
continues with a fallback version. The artifact is produced, just not with the version anyone expected.

**Look at:** the `versionFile` handling in the Android build action, and `scripts/ios/check_ios_version.sh`
for the sanitisation that depends on it.

### 5. Production promotion does not verify a beta exists

`promote_to_production` moves a beta build to production without first checking that one is there. With
an empty beta track it fails inside the Play API rather than up front with a useful message.

**Look at:** `promote_to_production` in the Android lanes.

---

## Why these are listed rather than fixed

Four of the five are in the **reusable workflows and actions** (`openMF/mifos-x-actionhub`), not in this
template — a fork cannot fix them here, only work around them. Issues 3 and 4 are fixable in-tree and are
good first contributions; 1, 2 and 5 need a change upstream, which is why the workaround matters more
than the diagnosis.
